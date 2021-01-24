package dev.pa1007.app.result;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ClassResult {

    private static Map<String, ClassResult> instanceMap = new HashMap<>();

    /**
     * Name (Type : {Number/String/....}
     */
    private String className;
    /**
     * Nb associated (Find in report)
     */
    private String classNumber;
    /**
     * Find in report e.g: [ty]
     */
    private String id;


    public ClassResult(String className, String classNumber, String id) {
        this.className = className;
        this.classNumber = classNumber;
        this.id = id;
    }

    public String getClassName() {
        return className;
    }

    public String getClassNumber() {
        return classNumber;
    }

    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ClassResult that = (ClassResult) o;
        return Objects.equals(className, that.className) && Objects.equals(
                classNumber,
                that.classNumber
        ) && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(className, classNumber, id);
    }

    @Override
    public String toString() {
        if (className.equalsIgnoreCase("Error")) {
            return "[Truth Missing]";
        }
        else {
            return "\uD835\uDCDB[" + id + "]" + classNumber + "{" + className + "}";
        }
    }

    public static ClassResult getInstance(String className, String classNumber, String id) {
        if (instanceMap.containsKey(className)) {
            return instanceMap.get(className);
        }
        else {
            ClassResult classResult = new ClassResult(className, classNumber, id);
            instanceMap.put(className, classResult);
            return classResult;
        }
    }
}
