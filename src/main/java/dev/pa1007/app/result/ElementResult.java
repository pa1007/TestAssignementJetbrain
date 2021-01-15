package dev.pa1007.app.result;

public class ElementResult extends Result {

    private String text;

    public ElementResult(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}
