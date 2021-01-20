package dev.pa1007.app.result;

import dev.pa1007.app.result.subelem.PredictionElementResult;
import java.util.*;

public class ModuleResult {

    private String name;  //algorithms/string/brute-force

    private List<LineResult> results;

    public ModuleResult(String name) {
        this.name = name;
        results = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public List<LineResult> getResults() {
        return results;
    }

    public void addLineResult(LineResult lr) {
        results.add(lr);
    }

    public Map<String, String> getResult() {
        Map<String, String> res = new HashMap<>();
        res.put("Lines with prediction", String.valueOf(results.size()));
        res.put(
                "correct prediction ",
                String.valueOf(results.stream().filter(Objects::nonNull).map((x) -> x.getImportantElement().stream().filter(
                        Objects::nonNull)
                        .filter((t) -> t.getAction().equals(PredictionElementResult.Type.CORRECT))
                        .count()).reduce(0L, Long::sum))
        );
        res.put(
                "incorrect prediction ",
                String.valueOf(results.stream().filter(Objects::nonNull).map((x) -> x.getImportantElement().stream().filter(
                        Objects::nonNull)
                        .filter((t) -> t.getAction().equals(PredictionElementResult.Type.INCORRECT))
                        .count()).reduce(0L, Long::sum))
        );
        return res;
    }


    @Override
    public String toString() {
        return "ModuleResult{" +
               "name='" + name + '\'' +
               ", results=" + results +
               '}';
    }
}
