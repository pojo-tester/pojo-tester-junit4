package com.github.pojotester;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

public class PojoTester<T> {

    private Map<Class<?>, Consumer<PJContext>> defaultTests = new LinkedHashMap<>();
    private Map<String, List<Consumer<PJContext>>> userTests = new LinkedHashMap<>();
    private Map<Class<?>, Function<Class<?>, ?>> objectFactories = new LinkedHashMap<>();
    private Class<T> testType;
    private boolean testSuperclassFields = false;

    private PojoTester(Class<T> clazz) {
        this.testType = clazz;
        objectFactories.put(String.class, type -> "string-value");

        objectFactories.put(char.class, type -> 'c');
        objectFactories.put(Character.class, type -> new Character('c'));

        objectFactories.put(boolean.class, type -> true);
        objectFactories.put(Boolean.class, type -> Boolean.TRUE);

        objectFactories.put(int.class, type -> 1);
        objectFactories.put(Integer.class, type -> Integer.parseInt("1"));

        objectFactories.put(short.class, type -> 1);
        objectFactories.put(Short.class, type -> Short.parseShort("1"));

        objectFactories.put(long.class, type -> 1L);
        objectFactories.put(Long.class, type -> Long.parseLong("1"));

        objectFactories.put(float.class, type -> 1.0f);
        objectFactories.put(Float.class, type -> Float.parseFloat("1"));

        objectFactories.put(double.class, type -> 1.0d);
        objectFactories.put(Double.class, type -> Double.parseDouble("1"));

        objectFactories.put(List.class, type -> new ArrayList<>());
        objectFactories.put(Set.class, type -> new HashSet<>());
        objectFactories.put(Map.class, type -> new HashMap<>());
        objectFactories.put(Queue.class, type -> new LinkedList<>());

        objectFactories.put(Collection.class, type -> new ArrayList<>());

        objectFactories.put(Object.class, PJReflectUtils::newInstance);

    }

    public static <T> PojoTester forClass(Class<T> clz) {
        return new PojoTester<>(clz);
    }

    public PojoTester addObjectFactory(Class<?> clz, Function<Class<?>, ?> factory) {
        objectFactories.put(clz, factory);
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
            PJContext<T> ctx = new PJContext<>(testType, fieldName, this::createObject);
            defaultTests.values().forEach(test -> test.accept(ctx));
            userTests.getOrDefault(fieldName, Collections.emptyList()).forEach(test -> test.accept(ctx));
        }
    }

    public <S> S createObject(Class<S> clz) {
        if (clz.isEnum()) {
            if (clz.getEnumConstants().length == 0) {
                return null;
            } else {
                return clz.getEnumConstants()[0];
            }
        }
        if (clz.isArray()) {
            return clz.cast(Array.newInstance(clz.getTypeParameters().getClass(), 1));
        }
        Function<Class<?>, ?> factory = objectFactories.get(clz);
        if (factory != null) {
            return clz.cast(factory.apply(clz));
        }
        for (Map.Entry<Class<?>, Function<Class<?>, ?>> entry : objectFactories.entrySet()) {
            Class<?> factoryCandidate = entry.getKey();
            factory = entry.getValue();
            if (factoryCandidate.isAssignableFrom(clz)) {
                return clz.cast(factory.apply(clz));
            }
        }
        throw new IllegalArgumentException("Factory not found for type: " + clz);
    }

}
