package dev.pa1007.app.result;

import dev.pa1007.app.result.subelem.PredictionElementResult;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LineResult {

    private       List<PredictionElementResult> importantElement;
    private final String                        from;
    private       int                           lineNumber;

    public LineResult(String fromLine, int lineNumber) {
        this.from = fromLine;
        this.lineNumber = lineNumber;
        importantElement = new ArrayList<>();
    }

    public List<PredictionElementResult> getImportantElement() {
        return importantElement;
    }

    public String getFrom() {
        return from;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void addImportantElement(PredictionElementResult res) {
        importantElement.add(res);
    }

    public void addAllImportantElement(List<PredictionElementResult> predictionElements) {
        importantElement.addAll(predictionElements);
    }

    @Override
    public String toString() {
        return importantElement.stream().map(PredictionElementResult::toString).collect(Collectors.joining(" "))
               + "\n from (" + lineNumber + ") : "
               + from;
    }
}
