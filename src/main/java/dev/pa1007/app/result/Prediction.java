package dev.pa1007.app.result;

import java.util.HashMap;
import java.util.Map;

/**
 * Class used for storing possible type
 */
public class Prediction {

    Map<ClassResult, Double> preds;

    public Prediction() {
        preds = new HashMap<>();
    }


    public void addPred(ClassResult classResult, String pourcent) {
        preds.put(classResult, Double.valueOf(pourcent.replace("%", "").trim()));
    }


}
