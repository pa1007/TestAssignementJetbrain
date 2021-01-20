package dev.pa1007.app;

import ammonite.ops.Path;
import dev.pa1007.app.result.ModuleResult;
import dev.pa1007.app.utils.Utils;
import funcdiff.Statistics;
import lambdanet.RunTrainedModel;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Main {


    /**
     * @param arguments The tool should receive a path to the pre-trained model, a path to the parsingFromFile.ts
     *                  (already pre-compiled with tsc) and an input Typescript file as named CLI arguments and print
     *                  the results to stdout.
     */
    public static void main(String[] arguments) throws IOException {
        if (arguments.length > 0) {
            if (arguments[0].equals("--parseHtml")) {
                if (arguments.length > 1) {
                    HTMLReader.main(arguments[1]);
                }
                else {
                    System.out.println("please provide an html file to read from");
                }
            }
            else {
                if (arguments.length > 2) {
                    var api             = lambdanet.JavaAPI$.MODULE$;
                    var workDir         = api.pwd();
                    var modelDir        = getSourceDir(api, workDir, arguments[0]);
                    var paramPath       = api.joinPath(modelDir, "params.serialized");
                    var sourceDir       = getSourceDir(api, workDir, arguments[2]);
                    var parsingFromFile = getSourceDir(api, workDir, arguments[1]);
                    var outPath         = api.joinPath(workDir, "temp/result/");
                    RunTrainedModel.runTrainedModel(paramPath, sourceDir, outPath, parsingFromFile, 8);
                    Document docu = Jsoup.parse(
                            new File(outPath.toString() + "\\predictions\\ts-algorithms.html"),
                            "UTF-8",
                            "http://result.com/"
                    );
                    List<ModuleResult> allModuleRes = Utils.interpretModules(docu);
                    Scanner            sc           = new Scanner(System.in);
                    System.out.println();
                    System.out.println();
                    System.out.println();
                    System.out.println();
                    System.out.println();
                    System.out.println();
                    System.out.println();
                    while (true) {
                        System.out.println("Select (S)tatistics or (R)esults or Stop");
                        String x = sc.next();
                        while (!x.equalsIgnoreCase("s") && !x.equalsIgnoreCase("r") && !x.equalsIgnoreCase("stop")) {
                            System.out.println("Select (S)tatistics or (R)esults or Stop");
                            x = sc.next();
                        }
                        if (x.equalsIgnoreCase("S")) {
                            System.out.println("Statistics by modules :");
                            for (ModuleResult allModuleRe : allModuleRes) {
                                Map<String, String> result = allModuleRe.getResult();
                                System.out.println("\t " + allModuleRe.getName());
                                result.forEach((key, value) -> System.out.println("\t\t" + key + " = " + value));
                            }
                            System.out.println("Global statistics :");
                            System.out.println(Statistics.returnPrintableString());
                        }
                        else if (x.equalsIgnoreCase("stop")) {
                          return;
                        }
                        else {
                            HTMLReader.printModules(allModuleRes, sc);
                        }

                    }

                }
                else {
                    System.err.println("Please provide all three required arguments \n" +
                                       " Usage : {Pre-trained model path} {parsingFromFile.ts path} {Input Typescript file}");
                }
            }
        }
    }

    private static Path getSourceDir(lambdanet.JavaAPI$ api, Path workDir, String line) {
        return (line.startsWith("/") || Character.isUpperCase(line.charAt(0)) && line.startsWith(":\\", 1)) ?
                api.absPath(line) :
                api.joinPath(workDir, line);
    }
}
