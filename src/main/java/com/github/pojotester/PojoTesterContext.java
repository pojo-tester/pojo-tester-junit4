package com.github.pojotester;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;

public class PojoTesterContext<T> {

    private final Class<T> type;
    private final Function<Class<?>, ?> objectFactory;
    private final String testedFieldName;

    private final Map<String, Method> methods;
    private final Map<String, Field> fields;

    /**
     * PojoTesterContext
     *
     * @param classType     - (root) class to test
     * @param fieldToTest   - field  to test
     * @param objectFactory - object factory - knows how to create new objects for given type
     * @param methods       - available methods in root class (name : java.lang.reflect.Method)
     * @param fields        - available fields in root class (name : java.lang.reflect.Field)
     */
    public PojoTesterContext(Class<T> classType, String fieldToTest, Function<Class<?>, ?> objectFactory,
                             Map<String, Method> methods, Map<String, Field> fields) {
        this.type = classType;
        this.objectFactory = objectFactory;
        this.testedFieldName = fieldToTest;
        this.methods = methods;
        this.fields = fields;
    }

    public Class<?> getType() {
        return type;
    }

    public T createObject() {
        return type.cast(objectFactory.apply(type));
    }

    public <S> S createObject(Class<S> cls) {
        return cls.cast(objectFactory.apply(cls));
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
