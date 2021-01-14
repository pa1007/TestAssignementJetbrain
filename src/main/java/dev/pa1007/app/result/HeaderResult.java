package dev.pa1007.app.result;

import dev.pa1007.app.result.builder.HeaderResultBuilder;
import org.jsoup.nodes.Element;
import java.util.StringJoiner;

public class HeaderResult extends Result {

    private double libAccNb;
    private String libAccFrac;
    private double ProjAccNb;
    private String ProjAccFrac;
    private String Missing;


    public HeaderResult(double libAccNb, String libAccFrac, double projAccNb, String projAccFrac, String missing) {
        this.libAccNb = libAccNb;
        this.libAccFrac = libAccFrac;
        ProjAccNb = projAccNb;
        ProjAccFrac = projAccFrac;
        Missing = missing;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", HeaderResult.class.getSimpleName() + "[", "]")
                .add("libAccNb=" + libAccNb)
                .add("libAccFrac='" + libAccFrac + "'")
                .add("ProjAccNb=" + ProjAccNb)
                .add("ProjAccFrac='" + ProjAccFrac + "'")
                .add("Missing='" + Missing + "'")
                .toString();
    }

    public static String[] extractFrac(String s) {
        String[] s1    = s.split(" ");
        String[] split = s1[s1.length - 1].split("=");
        return split;
    }

    public static String extractExact(String s) {
        return s.split(": ")[1];
    }

    public static HeaderResult parseHeader(Element h3) {
        //LibAcc: 0.6163=159/258, ProjAcc: 0.2245=11/49, Missing: 886
        HeaderResultBuilder hrb    = new HeaderResultBuilder();
        String[]            split  = h3.text().split(",");
        String[]            libAc  = HeaderResult.extractFrac(split[0]);
        String[]            projAc = HeaderResult.extractFrac(split[1]);
        hrb.setLibAccNb(Double.parseDouble(libAc[0])).setLibAccFrac(libAc[1])
                .setProjAccNb(Double.parseDouble(projAc[0])).setProjAccFrac(projAc[1]).setMissing(HeaderResult.extractExact(
                split[2]));

        return hrb.createHeaderResult();
    }
}
