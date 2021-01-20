package dev.pa1007.app;

import dev.pa1007.app.result.ClassResult;
import dev.pa1007.app.result.LineResult;
import dev.pa1007.app.result.ModuleResult;
import dev.pa1007.app.result.subelem.PredictionElementResult;
import dev.pa1007.app.utils.Utils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.io.File;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class HTMLReader {

    public static final String ANSI_RESET  = "\u001B[0m";
    public static final String ANSI_BLACK  = "\u001B[30m";
    public static final String ANSI_RED    = "\u001B[31m";
    public static final String ANSI_GREEN  = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE   = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN   = "\u001B[36m";
    public static final String ANSI_WHITE  = "\u001B[37m";

    public static void main(String... arguments) throws IOException {
        if (arguments.length < 1) {
            System.err.println("Error no arguments provided");
            return;
        }
        Document docu = Jsoup.parse(new File(arguments[0]), "UTF-8", "http://result.com/");

        List<ModuleResult> allModuleRes = Utils.interpretModules(docu);
        Scanner            sc           = new Scanner(System.in);
        printModules(allModuleRes, sc);
    }

    public static void printModules(List<ModuleResult> allModuleRes, Scanner sc) {
        for (int i = 0; i < allModuleRes.size(); i++) {
            ModuleResult     module  = allModuleRes.get(i);
            List<LineResult> results = module.getResults();
            for (int j = 0; j < results.size(); j++) {
                LineResult lineResult = results.get(j);

                StringBuilder sb = new StringBuilder("Module : ");
                sb.append(module.getName()).append("\n");
                sb.append("line ").append(lineResult.getLineNumber()).append(" : \n");
                String line     = "\t" + lineResult.getFrom();
                String usedLine = line;
                sb.append(line);
                String firstPart = sb.toString();

                List<PredictionElementResult> importantElement = lineResult.getImportantElement();
                for (int k = 0; k < importantElement.size(); k++) {
                    sb = new StringBuilder();
                    PredictionElementResult pred = importantElement.get(k);

                    String str    = pred.getOldClass().toString();
                    int    i1     = usedLine.indexOf(str);
                    int    length = str.length();

                    if (usedLine.contains(str)) {
                        StringBuilder replacer = new StringBuilder(usedLine).replace(
                                0,
                                length + i1,
                                String.format("%1$" + (length + i1 - 2) + "s", "")
                        );
                        usedLine = replacer.toString();
                    }

                    String spacing = String.format("%1$" + (i1) + "s", "");
                    sb.append(spacing).append("└");

                    for (int l = 0; l < length - 2; l++) {
                        sb.append("─");
                    }
                    sb.append("┘").append("\n");
                    List<Map.Entry<ClassResult, Double>> sorted = pred.getPrediction().getSortedEntry();
                    if (sorted.isEmpty()) {
                        sorted.add(new AbstractMap.SimpleEntry<>(pred.getOldClass(), 100.0));
                    }
                    for (int l = 0; l < 5; l++) {

                        sb.append(spacing).append(l + 1).append(". ");
                        if (sorted.size() > l) {
                            Map.Entry<ClassResult, Double> entry     = sorted.get(l);
                            String                         className = entry.getKey().getClassName();
                            if (pred.getAction().equals(PredictionElementResult.Type.INCORRECT)) {
                                if (l == 0 && entry.getValue() >= 50.0) {
                                    className = ANSI_GREEN + className;
                                }
                                if (className.equals(pred.getOldClass().getClassName())) {
                                    className = ANSI_RED + className;
                                }
                            }
                            else {
                                if (className.equals(pred.getOldClass().getClassName())) {
                                    className = ANSI_GREEN + className;
                                }
                            }
                            sb.append(className).append(" ").append(entry.getValue()).append("%").append(ANSI_RESET);

                        }
                        sb.append("\n");
                    }
                    System.out.println(firstPart);
                    System.out.println(sb.toString());
                    System.out.println(spacing + "(S)top | (N)ext >");
                    String next = sc.next();
                    if (next.equalsIgnoreCase("S") || next.equalsIgnoreCase("stop")) {
                        return;
                    }
                    else {
                        continue;
                    }

                }
            }


        }
    }

}
