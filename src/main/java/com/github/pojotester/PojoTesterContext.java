package com.github.pojotester;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.function.Supplier;

public class PojoTesterContext<T> {

    private final Class<T> type;
    private final Supplier<T> newInstance;
    private final String testedFieldName;

    private final Map<String, Method> methods;
    private final Map<String, Field> fields;

    public PojoTesterContext(Class<T> classType, Supplier<T> newInstance, String fieldToTest,
                             Map<String, Method> methods, Map<String, Field> fields) {
        this.type = classType;
        this.newInstance = newInstance;
        this.testedFieldName = fieldToTest;
        this.methods = methods;
        this.fields = fields;
    }

    public Class<T> getType() {
        return type;
    }

    public T createObject() {
        return newInstance.get();
    }

    public String getTestedFieldName() {
        return testedFieldName;
    }

    public Map<String, Method> getMethods() {
        return Collections.unmodifiableMap(methods);
    }

    public Map<String, Field> getFields() {
        return Collections.unmodifiableMap(fields);
    }
}
