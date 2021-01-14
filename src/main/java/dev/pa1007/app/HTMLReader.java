package dev.pa1007.app;

import dev.pa1007.app.result.HeaderResult;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

public class HTMLReader {

    static Pattern p = Pattern.compile("\\d[^,]++");

    public static void main(String[] arguments) throws IOException {

        Document docu = Jsoup.parse(new File("ts-algorithms-test.html"), "UTF-8", "http://result.com/");

        HeaderResult res = HeaderResult.parseHeader(docu.getElementsByTag("h3").get(0));
        System.out.println(res);
    }

}
