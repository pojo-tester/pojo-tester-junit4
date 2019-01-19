package com.github.pojotester;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import com.github.pojotester.tester.PJSetterGetterTest;

public class PojoTester<T> {

    private final Map<Class<?>, Consumer<PJContext>> defaultTests = new LinkedHashMap<>();
    private final Map<String, List<Consumer<PJContext>>> userTests = new LinkedHashMap<>();
    private final PJObjectFactory objectFactory;

    private Class<T> testType;
    private boolean testSuperclassFields = false;

    private PojoTester(Class<T> clazz) {
        this.testType = clazz;
        defaultTests.put(PJSetterGetterTest.class, new PJSetterGetterTest());
        objectFactory = new PJObjectFactory();
    }

    public static <T> PojoTester forClass(Class<T> clz) {
        return new PojoTester<>(clz);
    }

    public PojoTester addObjectFactory(Class<?> clazz, Function<Class<?>, ?> factory) {
        objectFactory.addObjectFactory(clazz, factory);
        return this;
    }

    public PojoTester addFieldTest(String fieldName, Consumer<PJContext> test) {
        List<Consumer<PJContext>> testsPerField = userTests.computeIfAbsent(fieldName, e -> new LinkedList<>());
        testsPerField.add(test);
        return this;
    }

    public PojoTester removeDefaultTest(Class<?> testClass) {
        defaultTests.remove(testClass);
        return this;
    }

    public PojoTester includeSuperclass() {
        testSuperclassFields = true;
        return this;
    }

    public void test() {
        Field[] allDeclaredFields = PJReflectUtils.getAllDeclaredFields(testType, testSuperclassFields);
        String[] fieldNames = Stream.of(allDeclaredFields).map(Field::getName).sorted().toArray(String[]::new);
        for (String fieldName : fieldNames) {
            PJContext<T> ctx = new PJContext<>(testType, fieldName, objectFactory::createObject);
            defaultTests.values().forEach(test -> test.accept(ctx));
            userTests.getOrDefault(fieldName, Collections.emptyList()).forEach(test -> test.accept(ctx));
        }
    }

}
