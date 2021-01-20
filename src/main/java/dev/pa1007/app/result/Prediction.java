package dev.pa1007.app.result;

import java.util.*;

/**
 * Class used for storing possible type
 */
public class Prediction {

    Map<ClassResult, Double> preds;

    public Prediction() {
        preds = new HashMap<>();
    }


    public void addSelectedFromError(ClassResult classResul) {
        if (!preds.containsKey(classResul)) {
            preds.put(classResul, 100D);
        }
    }

    public void addPred(ClassResult classResult, String pourcent) {
        preds.put(classResult, Double.valueOf(pourcent.replace(",", ".")));
    }

    public List<Map.Entry<ClassResult, Double>> getSortedEntry() {
        List<Map.Entry<ClassResult, Double>> temp = new ArrayList<>(preds.entrySet());
        temp.sort(Map.Entry.comparingByValue());
        Collections.reverse(temp);
        return temp;
    }

    @Override
    public String toString() {
        return "Prediction{" +
               "preds=" + preds +
               '}';
    }
}
