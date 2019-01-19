package com.github.pojotester.fixtures;

public class NoDefConstructorType {

    private String value;

    private NoDefConstructorType() {

    }

    public NoDefConstructorType(Object object) {

    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
