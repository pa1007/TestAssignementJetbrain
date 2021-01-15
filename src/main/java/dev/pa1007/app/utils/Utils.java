package dev.pa1007.app.utils;

import dev.pa1007.app.result.ModuleResult;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.util.ArrayList;
import java.util.List;

public class Utils {

    public static List<ModuleResult> interpretModules(Document docu) {
        Elements           divs        = docu.body().getElementsByTag("div");
        List<ModuleResult> resultsList = new ArrayList<>();
        for (Element div : divs) { //Each module
            for (Element span : div.getElementsByTag("code").first().getElementsByAttribute("style")) {
                Element globalSpan = span.getElementsByTag("span").first();
                for (Element ligneSpan : globalSpan.getElementsByTag("span")) {
                    Utils.understandLine(ligneSpan);
                }


            }
            String name = div.getElementsByTag("h2").text().replace("Module: ", "");
            System.out.println(name);
            ModuleResult mr = new ModuleResult(name);

            resultsList.add(mr);
        }


        return resultsList;
    }

    private static void understandLine(Element ligneSpan) {


    }
}
