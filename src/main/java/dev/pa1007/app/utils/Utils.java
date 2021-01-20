package dev.pa1007.app.utils;

import dev.pa1007.app.result.ClassResult;
import dev.pa1007.app.result.LineResult;
import dev.pa1007.app.result.ModuleResult;
import dev.pa1007.app.result.Prediction;
import dev.pa1007.app.result.subelem.PredictionElementResult;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Utils {

    private static final Pattern MAIN_PATTERN            = Pattern.compile(
            "\\[[a-zA-Z]{2}\\][0-9]*\\{[a-zA-Z]*\\}: [0-9]+[,|.][0-9]{2}\\%");
    private static final String  HEAD_GROUP_MAIN_REGEX   = "\\[([a-zA-Z]{2})\\]([0-9]*)\\{([a-zA-Z]*)\\}";
    private static final Pattern HEAD_GROUP_MAIN_PATTERN = Pattern.compile(HEAD_GROUP_MAIN_REGEX, Pattern.MULTILINE);
    private static final Pattern GROUPED_MAIN_PATTERN    = Pattern.compile(
            HEAD_GROUP_MAIN_REGEX + ": ([0-9]+[,|.][0-9]{2})\\%");


    public static List<ModuleResult> interpretModules(Document docu) {
        Elements           divs        = docu.body().getElementsByTag("div");
        List<ModuleResult> resultsList = new ArrayList<>();
        for (Element div : divs) { //Each module
            String       name = div.getElementsByTag("h2").text().replace("Module: ", "");
            ModuleResult mr   = new ModuleResult(name);
            int          i    = 1;
            for (Element span : div.getElementsByTag("code").first().getElementsByAttribute("style")) {
                for (Element ligneSpan : span.children()) {
                    LineResult                    lr                 = new LineResult(ligneSpan.parent().text(), i);
                    List<PredictionElementResult> predictionElements = Utils.understandLine(ligneSpan);
                    if (predictionElements.isEmpty()) {
                        continue;
                    }
                    lr.addAllImportantElement(predictionElements);
                    mr.addLineResult(lr);
                }
                i++;

            }
            resultsList.add(mr);
        }


        return resultsList;
    }

    public static ClassResult getClassResult(String text) {
        Matcher matcher = HEAD_GROUP_MAIN_PATTERN.matcher(text);
        matcher.find();
        return ClassResult.getInstance(matcher.group(3), matcher.group(2), matcher.group(1));
    }

    /**
     * Recursive use for retrieving last element, by getting this one we can recover the parent
     *
     * @param ligneSpan
     * @return a PredictionElementResult
     */
    private static List<PredictionElementResult> understandLine(Element ligneSpan) {
        List<PredictionElementResult> res = new ArrayList<>();
        for (Element element : ligneSpan.children()) {
            if (element.children().isEmpty()) {
                if (!element.classNames().isEmpty()) {
                    Prediction prediction = Utils.processTitle(element.parent());
                    res.add(switch (element.className().toLowerCase(Locale.ROOT)) {
                        case "correct" -> PredictionElementResult.correct(
                                "",
                                Utils.getClassResult(element.text()),
                                prediction
                        );
                        case "incorrect" -> {
                            String[] s = element.text().split(" ");
                            prediction.addSelectedFromError(Utils.getClassResult(s[1]));
                            yield PredictionElementResult.incorrect(
                                    "",
                                    Utils.getClassResult(s[3]),
                                    prediction
                            );
                        }
                        case "missing" -> PredictionElementResult.missed(
                                "",
                                Utils.getClassResult(element.text()),
                                prediction
                        );
                        default -> null;
                    });


                }
            }
            else {
                res.addAll(understandLine(element));
            }
        }
        return res;
    }

    private static Prediction processTitle(Element parent) {
        String       title1  = parent.attr("title");
        Matcher      title   = GROUPED_MAIN_PATTERN.matcher(title1);
        List<String> collect = title.results().map(MatchResult::group).collect(Collectors.toList());
        Prediction   p       = new Prediction();
        for (String s : collect) {
            Matcher matcher = GROUPED_MAIN_PATTERN.matcher(s);
            while (matcher.find()) {
                p.addPred(
                        ClassResult.getInstance(matcher.group(3), matcher.group(2), matcher.group(1)),
                        matcher.group(4)
                );
            }

        }


        return p;
    }
}
