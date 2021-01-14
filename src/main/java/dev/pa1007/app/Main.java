package dev.pa1007.app;

public class Main {


    /**
     * @param arguments The tool should receive a path to the pre-trained model, a path to the parsingFromFile.ts
     *                  (already pre-compiled with tsc) and an input Typescript file as named CLI arguments and print
     *                  the results to stdout.
     */
    public static void main(String[] arguments) {
        if (arguments.length > 2) {

        }
        else {
            System.err.println("Please provide all three required arguments \n" +
                               " Usage : {Pre-trained model path} {parsingFromFile.ts path} {Input Typescript file}");
        }
    }

}
