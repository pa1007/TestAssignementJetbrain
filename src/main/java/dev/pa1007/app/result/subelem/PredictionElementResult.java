package dev.pa1007.app.result.subelem;

import dev.pa1007.app.HTMLReader;
import dev.pa1007.app.result.ClassResult;
import dev.pa1007.app.result.ElementResult;
import dev.pa1007.app.result.Prediction;


public class PredictionElementResult extends ElementResult {

    private Type        action;
    private ClassResult oldClass;
    private Prediction  prediction;

    private PredictionElementResult(String text, Type action, ClassResult linkedClass, Prediction prediction) {
        super(text);
        this.action = action;
        oldClass = linkedClass;
        this.prediction = prediction;
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

    @Override
    public String toString() {
        return "prediction=" + prediction;
    }

    public static PredictionElementResult missed(String text, ClassResult result, Prediction prediction) {
        return new PredictionElementResult(text, Type.MISSED, result, prediction);
    }

    public static PredictionElementResult incorrect(String text, ClassResult result, Prediction prediction) {
        return new PredictionElementResult(text, Type.INCORRECT, result, prediction);
    }

    public static PredictionElementResult correct(String text, ClassResult result, Prediction prediction) {
        return new PredictionElementResult(text, Type.CORRECT, result, prediction);
    }

    public static PredictionElementResult error() {
        return new PredictionElementResult("",Type.ERROR,ClassResult.getInstance("Error","",""),new Prediction());
    }

    public enum Type {
        MISSED,
        CORRECT,
        INCORRECT,
        ERROR;

        public String getColor() {
            return switch (name()) {
                case "CORRECT" -> HTMLReader.ANSI_GREEN;
                case "INCORRECT" -> HTMLReader.ANSI_RED;
                case "MISSED" -> HTMLReader.ANSI_WHITE;
                default -> HTMLReader.ANSI_RESET;
            };
        }
    }
}