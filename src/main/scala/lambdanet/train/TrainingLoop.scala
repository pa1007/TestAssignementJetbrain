package lambdanet.train

import ammonite.ops.{Path, RelPath, pwd}
import botkop.numsca
import botkop.numsca.Tensor
import cats.Monoid
import funcdiff.{SimpleMath => SM, _}
import lambdanet._
import lambdanet.architecture._
import lambdanet.train.TrainingState._
import lambdanet.translation.PredicateGraph.{PNode, PType, ProjNode}
import lambdanet.utils._
import org.nd4j.linalg.api.buffer.DataType

import java.util.Calendar
import java.util.concurrent.ForkJoinPool
import scala.collection.parallel.ForkJoinTaskSupport
import scala.concurrent._
import scala.language.reflectiveCalls
import scala.util.Random

object TrainingLoop {
  val toyMode: Boolean = false
  val useSeqModel = false
  val useDropout: Boolean = true
  val useOracleForIsLib: Boolean = false
  /* Assign more weights to project type to battle label imbalance */
  val maxLibRatio: Real = 3.0
  val projWeight: Real = maxLibRatio
  val gatHead = 1
  val weightDecay: Option[Real] = Some(1e-4)
  val onlyPredictLibType = false

  val debugTime: Boolean = false

  val taskName: String = {
    val flags = Seq(
      "newSim" -> NNArchitecture.compareDecoding,
      "oracle" -> useOracleForIsLib,
      "fix" -> NeuralInference.fixBetweenIteration,
      "decay" -> weightDecay.nonEmpty,
      "toy" -> toyMode
    ).map(flag(_)).mkString

    val ablationFlag = Seq(
      "noContextual" -> NeuralInference.noContextual,
      "noAttention" -> NeuralInference.noAttentional,
      "noLogical" -> NeuralInference.noLogical
    ).map(flag(_, post = true)).mkString

    if (useSeqModel) "seqModel-theirName1-node"
    else
      s"${ablationFlag}newParsing-GAT$gatHead-fc${NNArchitecture.messageLayers}" +
        s"$flags-${TrainingState.iterationNum}"
    //    "testBaseline"
  }

  def flag(nameValue: (String, Boolean), post: Boolean = false): String = {
    val (name, value) = nameValue
    if (value) (if (post) s"$name-" else s"-$name") else ""
  }

  def scaleLearningRate(epoch: Int): Double = {
    val min = 0.3
    val epochToSlowDown = if (toyMode) 100 else 30
    SM
      .linearInterpolate(1.0, min)(epoch.toDouble / epochToSlowDown)
      .max(min)
  }

  def main(args: Array[String]): Unit = {
    //    PrepareRepos.main(args)

    Tensor.floatingDataType = DataType.DOUBLE

    val threadNumber: Int = {
      import ammonite.ops._
      val f = pwd / "configs" / "threads.txt"
      if (exists(f)) {
        read(f).trim.toInt
      } else {
        Runtime.getRuntime.availableProcessors() / 2
      }
    }
    val resultsDir: ammonite.ops.Path = {
      import ammonite.ops._
      val pathText = read(pwd / "configs" / "resultsDir.txt").trim
      val path = util
        .Try {
          pwd / RelPath(pathText)
        }
        .getOrElse(Path(pathText))
      (path / taskName).tap { d =>
        lambdanet.printResult(s"save results to directory: $d")
      }
    }

    val (mName, eService) = ReportFinish.readEmailInfo(taskName)

    config(
      threadNumber,
      resultsDir,
      Some(EmailRelated(mName, eService))
    ).result(pwd / RelPath("scripts/ts/parsingFromFile.js"))
  }

  case class EmailRelated(machineName: String, emailService: EmailService)

  case class config(
                     numOfThreads: Int,
                     resultsDir: ammonite.ops.Path,
                     emailRelated: Option[EmailRelated],
                   ) {
    val fileLogger =
      new FileLogger(resultsDir / "console.txt", printToConsole = true)

    import fileLogger._

    printInfo(s"Task: $taskName")
    printInfo(s"threads = $numOfThreads")
    Timeouts.readFromFile()
    val taskSupport: Option[ForkJoinTaskSupport] =
      if (numOfThreads == 1) None
      else Some(new ForkJoinTaskSupport(pool))
    val parallelCtx: ExecutionContextExecutorService = {
      import ExecutionContext.fromExecutorService
      fromExecutorService(pool)
    }
    private val pool = new ForkJoinPool(numOfThreads)

    def result(parsingFromFile: Path): Unit = {
      val (state, pc, logger) = loadTrainingState(resultsDir, fileLogger)

      val repos = DataSet.loadRepos(toyMode, parsingFromFile)
      val dataSet = DataSet.makeDataSet(
        repos,
        taskSupport,
        useSeqModel,
        toyMode,
        onlyPredictLibType
      )
      makeModel(pc, dataSet)
        .train(maxTrainingEpochs = if (toyMode) 500 else 100, state, logger)

      //      namingHelpfulness(run)

    }

    def makeModel(pc: ParamCollection, dataSet: DataSet) = {
      printResult(s"Message layer: ${NNArchitecture.messageLayers} FC")

      val dimMessage = if (TrainingLoop.useSeqModel) 64 else 32
      val architecture =
        if (useSeqModel)
          SequenceModel.SeqArchitecture(dimMessage, pc)
        else GATArchitecture(gatHead, dimMessage, pc)
      //        else SimpleArchitecture(state.dimMessage, pc)
      printResult(s"NN Architecture: ${architecture.arcName}")

      //      NamingBaseline.test(dataSet)
      //      MostFreqConstructorBaseline.test(dataSet, useByFreq = false)

      buildModel(
        dataSet,
        pc,
        architecture
      )
    }

    def namingHelpfulness(dataSet: DataSet, run: Model): Unit = {
      import cats.implicits._

      def showCount(c: Counted[Int]): String = {
        s"$c, percentage: ${toAccuracy(c)}"
      }

      val (helpSet, notHelpSet) =
        (for {
          datum <- dataSet.trainSet
          predictions = NamingBaseline
            .testOnDatum(datum, useOracle = true, identity)
            .predict(0)
          fwd = run
            .forwardStep(
              datum,
              shouldDownsample = false,
              shouldDropout = false,
              maxBatchSize = Some(600)
            )
            ._2
          Seq(s1, s2) = Seq(fwd.incorrectSet -> false, fwd.correctSet -> true)
            .map {
              case (set, correctness) =>
                set
                  .filterNot(_._2.madeFromLibTypes)
                  .toVector
                  .foldMap {
                    case (n, label, _) =>
                      val (truthPosition, label1) = predictions(ProjNode(n))
                      assert(label1 == label)
                      Counted.fromBool((truthPosition == 0) != correctness)
                  }
                  .tap { stat =>
                    val name = if (correctness) "unhelpful" else "help"
                    printResult(s"$name set: ${showCount(stat)}")
                  }
            }
        } yield (s1, s2)).combineAll

      printResult(s"Overall helpful set: ${showCount(helpSet)}")
      printResult(s"Overall unhelpful set: ${showCount(notHelpSet)}")
    }

    //noinspection TypeAnnotation
    case class buildModel(
                           dataSet: DataSet,
                           pc: ParamCollection,
                           architecture: NNArchitecture,
                         ) {

      import dataSet._

      val rand = new Random(1)
      val model = Model.fromData(dataSet, architecture, rand)

      val maxBatchSize = dataSet
        .signalSizeMedian(maxLibRatio)
        .tap(s => {
          printResult(s"maxBatchSize: $s")
          Statistics.setStat("Max Batch", s"$s")
        })


      printResult(s"Label encoder: ${model.labelEncoder.name}")
      printResult(s"Name encoder: ${model.nameEncoder.name}")
      val random = new util.Random(2)
      val lossModel: LossModel = LossModel.NormalLoss
        .tap(m => printResult(s"loss model: ${m.name}"))
      private val avgAnnotations =
        SM.mean(
          trainSet.map(_.downsampleLibAnnots(maxLibRatio, random).size.toDouble)
        )
      var shouldAnnounce: Boolean = true

      def forward(
                   datum: ProcessedProject,
                   shouldDownsample: Boolean,
                   shouldDropout: Boolean,
                   maxBatchSize: Option[Int]
                 ) =
        limitTimeOpt("train-forward", Timeouts.forwardTimeout)(
          model.forwardStep(
            datum,
            shouldDownsample,
            shouldDownsample,
            maxBatchSize
          )
        )

      private def limitTimeOpt[A](
                                   name: String,
                                   timeLimit: Timeouts.Duration
                                 )(f: => A): Option[A] = {
        try {
          Some(limitTime(timeLimit)(f))
        } catch {
          case _: TimeoutException =>
            val msg = s"$name exceeded time limit $timeLimit."
            printWarning(msg)
            emailRelated.foreach { e =>
              import e._
              emailService.atFirstTime {
                emailService.sendMail(emailService.userEmail)(
                  s"TypingNet: timeout on $machineName during $name",
                  s"Details:\n" + msg
                )
              }
            }

            None
        }
      }

      @throws[TimeoutException]
      private def limitTime[A](timeLimit: Timeouts.Duration)(f: => A): A = {
        val exec = scala.concurrent.ExecutionContext.global
        Await.result(Future(f)(exec), timeLimit)
      }

      def calcGradInfo(stats: Optimizer.OptimizeStats) = {
        def meanSquaredNorm(gs: Iterable[Gradient]) = {
          import cats.implicits._
          import numsca._
          val combined = gs.toVector.map { g =>
            val t = g.toTensor()
            Counted(t.elements.toInt, sum(square(t)))
          }.combineAll
          math.sqrt(combined.value / nonZero(combined.count))
        }

        val grads = meanSquaredNorm(stats.gradients.values)
        val transformed = meanSquaredNorm(stats.transformedGrads.values)
        val deltas = meanSquaredNorm(stats.deltas.values)
        (grads, transformed, deltas)
      }

      private def typeAccString(accs: Map[PType, Counted[Correct]]): String = {
        val (tys, counts) = accs.toVector.sortBy { c =>
          -c._2.count
        }.unzip
        val typeStr = tys
          .map(t => SM.wrapInQuotes(t.showSimple))
          .mkString("{", ",", "}")
        val countStr = counts
          .map(c => s"{${c.count}, ${c.value}}")
          .mkString("{", ",", "}")
        s"{$typeStr,$countStr}"
      }

      case class train(
                        maxTrainingEpochs: Int,
                        trainingState: TrainingState,
                        logger: EventLogger
                      ) {

        import trainingState._

        val saveInterval = if (toyMode) 40 else 6

        def run(): Unit = {
          (trainingState.epoch0 + 1 to maxTrainingEpochs).foreach { epoch =>
            shouldAnnounce = epoch == 1 // only announce in the first epoch for debugging purpose
            announced(s"epoch $epoch") {
              TensorExtension.checkNaN = false // (epoch - 1) % 10 == 0
              handleExceptions(epoch) {
                trainStep(epoch)
                if ((epoch - 1) % 2 == 0)
                  DebugTime.logTime("testSteps") {
                    testStep(epoch, isTestSet = false)
                    testStep(epoch, isTestSet = true)
                  }
                if (epoch == 1 || epoch % saveInterval == 0)
                  DebugTime.logTime("saveTraining") {
                    saveTraining(epoch, s"epoch$epoch")
                  }
              }
            }
          }

          saveTraining(maxTrainingEpochs, "finished")
          emailRelated.foreach { params =>
            import params._
            emailService.sendMail(emailService.userEmail)(
              s"TypingNet: Training finished on $machineName!",
              "Training finished!"
            )
          }

        }

        def logAccuracyDetails(
                                stats: Vector[(ProcessedProject, ForwardResult)],
                                epoch: Int
                              ) = {
          import cats.implicits._
          val str = stats
            .map {
              case (d, f) =>
                val size = d.predGraphOpt.map(_.predicates.size)
                val acc = toAccuracy(
                  f.libCorrect.combine(f.projCorrect)
                )
                val name = d.projectName
                s"""{$size, $acc, "$name"}"""
            }
            .mkString("{", ",", "}")
          logger.logString("accuracy-distr", epoch, str)
        }

        def trainStep(epoch: Int): Unit = {
          DebugTime.logTime("GC") {
            System.gc()
          }

          val startTime = System.nanoTime()
          val oldOrder = random.shuffle(trainSet)
          val (h, t) = oldOrder.splitAt(119)
          val stats = (t ++ h).zipWithIndex.map {
            case (datum, i) =>
              import Console.{BLUE, GREEN}
              announced(
                s"$GREEN[epoch $epoch]$BLUE train on $datum",
                shouldAnnounce
              ) {
                //              println(DebugTime.show)
                checkShouldStop(epoch)
                for {
                  (loss, fwd, _) <- forward(
                    datum,
                    shouldDownsample = true,
                    shouldDropout = useDropout,
                    maxBatchSize = Some(maxBatchSize)
                  ).tap(
                    _.foreach(
                      r =>
                        printResult(
                          s"(progress: ${i + 1}/${trainSet.size}) " + r._2
                        )
                    )
                  )
                  _ = checkShouldStop(epoch)
                } yield {
                  checkShouldStop(epoch)

                  def optimize(loss: CompNode) = {
                    val factor = fwd.loss.count.toDouble / avgAnnotations
                    optimizer.minimize(
                      loss * factor,
                      pc.allParams,
                      backPropInParallel =
                        Some(parallelCtx -> Timeouts.optimizationTimeout),
                      gradientTransform = _.clipNorm(2 * factor),
                      scaleLearningRate = scaleLearningRate(epoch),
                      weightDecay = weightDecay
                    )
                  }

                  val gradInfo = limitTimeOpt(
                    s"optimization: $datum",
                    Timeouts.optimizationTimeout
                  ) {
                    announced("optimization", shouldAnnounce) {
                      val stats = DebugTime.logTime("optimization") {
                        optimize(loss)
                      }
                      calcGradInfo(stats)
                    }
                  }.toVector

                  if (debugTime) {
                    println(DebugTime.show)
                  }
                  (fwd, gradInfo, datum)
                }
              }
          }

          import cats.implicits._
          val (fws, gs, data) = stats.flatMap(_.toVector).unzip3

          import logger._

          fws.combineAll.tap { fwd =>
            import fwd._
            logScalar("loss", epoch, toAccuracyD(loss))
            logScalar("libAcc", epoch, toAccuracy(libCorrect))
            logScalar("projAcc", epoch, toAccuracy(projCorrect))
            logConfusionMatrix("confusionMat", epoch, confusionMatrix.value, 2)
            logAccuracyDetails(data zip fws, epoch)
          }

          val gradInfo = gs.combineAll
          gradInfo.unzip3.tap {
            case (grads, transformed, deltas) =>
              logScalar("gradient", epoch, grads.sum)
              logScalar("clippedGradient", epoch, transformed.sum)
              logScalar("paramDelta", epoch, deltas.sum)
          }

          logScalar("nameSharpness", epoch, {
            architecture.layerFactory
              .getVar('decodingSharpness)(
                Tensor(0.1).reshape(1, 1)
              )
              .value
              .squeeze()
          })

          val timeInSec = (System.nanoTime() - startTime).toDouble / 1e9
          logScalar("iter-time", epoch, timeInSec)

          println(DebugTime.show)
        }

        def testStep(epoch: Int, isTestSet: Boolean): Unit = {
          val dataSetName = if (isTestSet) "test" else "dev"
          val dataSet = if (isTestSet) testSet else devSet
          announced(s"test on $dataSetName set") {
            import cats.implicits._

            val (stat, fse1Acc, libTop5Acc, projTop5Acc) = dataSet.flatMap {
              datum =>
                checkShouldStop(epoch)
                announced(s"test on $datum", shouldAnnounce) {
                  forward(
                    datum,
                    shouldDownsample = !isTestSet,
                    shouldDropout = false,
                    maxBatchSize = Some(maxBatchSize)
                  ).map {
                    case (_, fwd, pred) =>
                      val (fse1, _, _) = datum.fseAcc
                        .countTopNCorrect(
                          1,
                          pred.mapValuesNow(_.distr.map(_._2)),
                          onlyCountInSpaceTypes = true
                        )
                      val Seq(libTop5, projTop5) = Seq(true, false).map {
                        fromLib =>
                          val predictions = pred.map {
                            case (n, distr) =>
                              n -> distr.distr.take(5).map(_._2)
                          }
                          val nodesMap = datum.nodesToPredict.collect {
                            case (n, ty)
                              if predictions.contains(n.n) && ty.madeFromLibTypes == fromLib =>
                              n.n -> ty
                          }
                          QLangAccuracy
                            .countTopNCorrect(
                              5,
                              nodesMap,
                              predictions,
                              _ => 1
                            )
                            ._1
                      }
                      (fwd, fse1, libTop5, projTop5)

                  }.toVector
                }
            }.combineAll

            import logger._
            import stat.{categoricalAcc, confusionMatrix, libCorrect, projCorrect}
            logScalar(s"$dataSetName-loss", epoch, toAccuracyD(stat.loss))
            logScalar(s"$dataSetName-libAcc", epoch, toAccuracy(libCorrect))
            logScalar(s"$dataSetName-libTop5Acc", epoch, toAccuracy(libTop5Acc))
            logScalar(s"$dataSetName-projAcc", epoch, toAccuracy(projCorrect))
            logScalar(
              s"$dataSetName-projTop5Acc",
              epoch,
              toAccuracy(projTop5Acc)
            )
            printResult(
              s"lib targets: ${libCorrect.count}, proj targets: ${projCorrect.count}"
            )
            logConfusionMatrix(
              s"$dataSetName-confusionMat",
              epoch,
              confusionMatrix.value,
              2
            )
            logScalar(s"$dataSetName-fse-top1", epoch, toAccuracy(fse1Acc))
            val libTypeAcc = categoricalAcc.filter(_._1.madeFromLibTypes)
            logString(
              s"$dataSetName-typeAcc",
              epoch,
              typeAccString(libTypeAcc)
            )
            val projTypeAcc = categoricalAcc.filterNot(_._1.madeFromLibTypes)
            logString(
              s"$dataSetName-proj-typeAcc",
              epoch,
              typeAccString(projTypeAcc)
            )
          }
        }

        import ammonite.ops._

        private def saveTraining(
                                  epoch: Int,
                                  dirName: String,
                                  skipTest: Boolean = false
                                ): Unit = {
          announced(s"save training to $dirName") {
            val saveDir = resultsDir / "saved" / dirName
            if (!exists(saveDir)) {
              mkdir(saveDir)
            }
            // do the following tasks in parallel
            val tasks = Vector(
              () => {
                val savePath = saveDir / "state.serialized"
                announced("save training state") {
                  TrainingState(epoch, iterationNum, optimizer)
                    .saveToFile(savePath)
                }
              },
              () => {
                announced("save parameters") {
                  pc.saveToFile(saveDir / "params.serialized")
                }
              },
              () => {
                val currentLogFile = resultsDir / "log.txt"
                if (exists(currentLogFile)) {
                  cp.over(currentLogFile, saveDir / "log.txt")
                }
              },
              () =>
                if (testSet.nonEmpty && !skipTest) {
                  import cats.implicits._

                  var progress = 0
                  val (right, wrong) = testSet.flatMap {
                    datum =>
                      checkShouldStop(epoch)
                      forward(
                        datum,
                        shouldDownsample = false,
                        shouldDropout = false,
                        maxBatchSize = None
                      ).map {
                        case (_, fwd, pred) =>
                          printResult(
                            s"(progress: ${progress.tap(_ => progress += 1)}) fwd.toString"
                          )
                          DebugTime.logTime("printQSource") {
                            QLangDisplay.renderProjectToDirectory(
                              datum.projectName.toString,
                              datum.qModules,
                              pred,
                              datum.predictionSpace.allTypes
                            )(saveDir / "predictions")
                          }
                          (fwd.correctSet, fwd.incorrectSet)
                      }.toVector
                  }.combineAll

                  QLangDisplay.renderPredictionIndexToDir(
                    right,
                    wrong,
                    saveDir,
                    sourcePath = "predictions"
                  )
                }
            )

            tasks.par.foreach(_.apply())

            val dateTime = Calendar.getInstance().getTime
            write.over(saveDir / "time.txt", dateTime.toString)
          }
        }

        @throws[StopException]
        private def checkShouldStop(epoch: Int): Unit = {
          if (TrainingControl(resultsDir).shouldStop(consumeFile = true)) {
            saveTraining(epoch, s"stopped-epoch$epoch")
            throw StopException("Stopped by 'stop.txt'.")
          }
        }

        private def handleExceptions(epoch: Int)(f: => Unit): Unit = {
          try f
          catch {
            case ex: Throwable =>
              val isTimeout = ex.isInstanceOf[TimeoutException]
              val errorName = if (isTimeout) "timeout" else "stopped"
              emailRelated.foreach { p =>
                import p._
                emailService.sendMail(emailService.userEmail)(
                  s"TypingNet: $errorName on $machineName at epoch $epoch",
                  s"Details:\n" + ex.getMessage
                )
              }

              if (isTimeout && Timeouts.restartOnTimeout) {
                printWarning(
                  "Timeout... training restarted (skip one training epoch)..."
                )
              } else {
                if (!ex.isInstanceOf[StopException]) {
                  ex.printStackTrace()
                  saveTraining(epoch, "error-save", skipTest = true)
                }
                throw ex
              }
          }
        }
      }

    }

  }

  case class ForwardResult(
                            loss: Counted[Double],
                            correctSet: Set[(PNode, PType, ProjectPath)],
                            incorrectSet: Set[(PNode, PType, ProjectPath)],
                            confusionMatrix: Counted[ConfusionMatrix],
                            categoricalAcc: Map[PType, Counted[Correct]]
                          ) {
    override def toString: String = {
      s"forward result: {loss: ${toAccuracyD(loss)}, " +
        s"lib acc: ${toAccuracy(libCorrect)} (${libCorrect.count} nodes), " +
        s"proj acc: ${toAccuracy(projCorrect)} (${projCorrect.count} nodes)}"
    }

    def libCorrect: Counted[LibCorrect] = countCorrect(true)

    def projCorrect: Counted[ProjCorrect] = countCorrect(false)

    def isConsistent: Boolean = {
      categoricalAcc.keySet == (correctSet ++ incorrectSet).map(_._2)
    }

    private def countCorrect(isLibType: Boolean) = {
      filterCount(_._2.madeFromLibTypes == isLibType)
    }

    private def filterCount(
                             filter: ((PNode, PType, ProjectPath)) => Boolean
                           ) = {
      val correct = correctSet.count(filter)
      val incorrect = incorrectSet.count(filter)
      Counted(correct + incorrect, correct)
    }
  }

  private implicit val forwardResultMonoid: Monoid[ForwardResult] =
    new Monoid[ForwardResult] {

      import Counted.zero
      import cats.implicits._

      def empty: ForwardResult =
        ForwardResult(zero(0), Set(), Set(), zero(Map()), Map())

      def combine(x: ForwardResult, y: ForwardResult): ForwardResult = {
        val z = ForwardResult.unapply(x).get |+| ForwardResult
          .unapply(y)
          .get
        (ForwardResult.apply _).tupled(z)
      }
    }

}
