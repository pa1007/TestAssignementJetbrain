package dev.pa1007.app.result.subelem;

import dev.pa1007.app.result.ClassResult;
import dev.pa1007.app.result.ElementResult;
import dev.pa1007.app.result.Prediction;

enum Type {
    MISSED,
    CORRECT,
    INCORRECT
}

public class PredictionElementResult extends ElementResult {


    private Type action;

    private ClassResult oldClass;

    private Prediction prediction;


    private PredictionElementResult(String text, Type action, ClassResult linkedClass) {
        super(text);
    }

    public Prediction getPrediction() {
        return prediction;
    }

    public void setPrediction(Prediction prediction) {
        this.prediction = prediction;
    }

    public Type getAction() {
        return action;
    }

    public ClassResult getOldClass() {
        return oldClass;
    }

    public static PredictionElementResult missed(String text) {
        return new PredictionElementResult(text, Type.MISSED, ClassResult.getInstance("null", "", ""));
    }

    public static PredictionElementResult incorrect(String text, ClassResult result) {
        return new PredictionElementResult(text, Type.INCORRECT, result);
    }

    public static PredictionElementResult correct(String text, ClassResult result) {
        return new PredictionElementResult(text, Type.CORRECT, result);
    }

}