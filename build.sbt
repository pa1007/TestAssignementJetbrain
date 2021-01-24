name := "lambdaNet"

version := "0.3"

organization in ThisBuild := "mrvplusone.github.io"
scalaVersion in ThisBuild := "2.12.10"

scalacOptions ++= Seq(
  "-feature",
  "-Ypartial-unification", // for using cats
  "-language:higherKinds"
  //  "-deprecation"
)

// to make the classpath right
fork in run := true
connectInput in run := true // for StdIn to work

val runOnMac = System.getProperty("os.name") == "Mac OS X"
val memories = {
  if (new File("configs/memory.txt").exists()) {
    val s = scala.io.Source.fromFile("configs/memory.txt")
    val r = s.getLines().toList.map(_.trim.toInt)
    s.close()
    r
  } else List(5, 7) // default heap and off-heap limit
}
val heapLimit = memories.head
val offHeapLimit = memories(1)
javaOptions ++= Seq(
  s"-Xms2G",
  s"-Xmx${heapLimit}G",
  s"-Dorg.bytedeco.javacpp.maxbytes=${offHeapLimit}G",
  s"-Dorg.bytedeco.javacpp.maxphysicalbytes=${offHeapLimit + heapLimit + 1}G"
)
val nd4jBinary = {
  val useCuda = new File("configs/useCuda.txt").exists()
  if (useCuda) "nd4j-cuda-10.0-platform" else "nd4j-native-platform"
}
libraryDependencies ++= Seq(
  "com.lihaoyi" %% "fastparse" % "2.0.4",
  "org.scalacheck" %% "scalacheck" % "1.14.0",
  "org.scalatest" %% "scalatest" % "3.0.3" % Test,
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.lihaoyi" %% "ammonite-ops" % "1.0.3",
  "org.json4s" %% "json4s-native" % "3.6.3",
  "com.github.daddykotex" %% "courier" % "1.0.0", // for email notifications
  //  "be.botkop" %% "numsca" % "0.1.4",
  // for building numsca
  "org.nd4j" % nd4jBinary % "1.0.0-beta4",
  "org.nd4j" % nd4jBinary % "1.0.0-beta4" % "runtime",
  "org.nd4j" % "nd4j-native" % "1.0.0-beta4" % "runtime" classifier ("windows-x86_64") ,
  "org.nd4j" % "nd4j-native" % "1.0.0-beta4" classifier ("windows-x86_64") ,
  "org.nd4j" % "nd4j-context" % "1.0.0-beta4",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2",
  "org.typelevel" %% "cats-core" % "2.0.0-M3" withSources(),
  "org.typelevel" %% "cats-effect" % "2.0.0-M3" withSources(),
  "com.github.nscala-time" %% "nscala-time" % "2.22.0",
  "com.lihaoyi" %% "upickle" % "0.7.5",
  "com.lihaoyi" %% "scalatags" % "0.7.0",
  "org.jsoup" % "jsoup" % "1.13.1"

)
addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.0")
mainClass in assembly := Some("dev.pa1007.app.Main")
fullClasspath in assembly := (fullClasspath in Runtime).value
assemblyJarName in assembly := "lambdaNetApp.jar"
assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs@_*) => MergeStrategy.discard
  case x => MergeStrategy.first
}