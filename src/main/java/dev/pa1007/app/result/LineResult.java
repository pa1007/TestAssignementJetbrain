package dev.pa1007.app.result;

import dev.pa1007.app.result.ElementResult;
import dev.pa1007.app.result.subelem.PredictionElementResult;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LineResult {

    private List<ElementResult>           allElement;
    private List<PredictionElementResult> importantElement;

    public LineResult() {
        allElement = new ArrayList<>();
        importantElement = new ArrayList<>();
    }

    public void addElement(ElementResult res) {
        allElement.add(res);
    }

    public void addImportantElement(PredictionElementResult res) {
        importantElement.add(res);
        addElement(res);
    }

    @Override
    public String toString() {
        //todo : Remake to add easy to read expression
        return allElement.stream().map(ElementResult::toString).collect(Collectors.joining(" "));
    }
}
