package dev.pa1007.app;

import java.io.IOException;

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
            }
            else {
                if (arguments.length > 2) {




                }
                else {
                    System.err.println("Please provide all three required arguments \n" +
                                       " Usage : {Pre-trained model path} {parsingFromFile.ts path} {Input Typescript file}");
                }
            }
        }
    }

}
