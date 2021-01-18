package dev.pa1007.app.result;

import java.util.ArrayList;
import java.util.List;

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


    @Override
    public String toString() {
        return "ModuleResult{" +
               "name='" + name + '\'' +
               ", results=" + results +
               '}';
    }
}
