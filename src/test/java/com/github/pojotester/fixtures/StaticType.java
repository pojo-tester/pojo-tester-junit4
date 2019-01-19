package com.github.pojotester.fixtures;

public class StaticType {

    private static String value;

    public static void setValue(String value) {
        StaticType.value = value;
    }

    public static String getValue() {
        return value;
    }
}
