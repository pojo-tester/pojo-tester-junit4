package com.github.pojotester;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
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
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import com.github.pojotester.tester.PJSetterGetterTest;

public class PojoTester<T> {

    private Map<Class<?>, Consumer<PJContext>> defaultTests = new LinkedHashMap<>();
    private Map<String, List<Consumer<PJContext>>> userTests = new LinkedHashMap<>();

    private LinkedList<Class<?>> objectFactoriesOrder = new LinkedList<>();
    private HashMap<Class<?>, Function<Class<?>, ?>> objectFactories = new HashMap<>();
    private Class<T> testType;
    private boolean testSuperclassFields = false;

    private PojoTester(Class<T> clazz) {
        this.testType = clazz;
        defaultTests.put(PJSetterGetterTest.class, new PJSetterGetterTest());

        addObjectFactory(String.class, type -> "string-value");

        addObjectFactory(char.class, type -> 'c');
        addObjectFactory(Character.class, type -> new Character('c'));

        addObjectFactory(boolean.class, type -> true);
        addObjectFactory(Boolean.class, type -> Boolean.TRUE);

        addObjectFactory(int.class, type -> 1);
        addObjectFactory(Integer.class, type -> Integer.parseInt("1"));

        addObjectFactory(short.class, type -> 1);
        addObjectFactory(Short.class, type -> Short.parseShort("1"));

        addObjectFactory(long.class, type -> 1L);
        addObjectFactory(Long.class, type -> Long.parseLong("1"));

        addObjectFactory(float.class, type -> 1.0f);
        addObjectFactory(Float.class, type -> Float.parseFloat("1"));

        addObjectFactory(double.class, type -> 1.0d);
        addObjectFactory(Double.class, type -> Double.parseDouble("1"));

        addObjectFactory(List.class, type -> new ArrayList<>());
        addObjectFactory(Set.class, type -> new HashSet<>());
        addObjectFactory(Map.class, type -> new HashMap<>());
        addObjectFactory(Queue.class, type -> new LinkedList<>());
        addObjectFactory(Collection.class, type -> new ArrayList<>());

        addObjectFactory(BigDecimal.class, type -> new BigDecimal("1.0"));
        addObjectFactory(BigInteger.class, type -> new BigInteger("1.0"));

        addObjectFactory(UUID.class, type -> UUID.randomUUID());

        addObjectFactory(Object.class, PJReflectUtils::newInstance); // must be last
    }

    public static <T> PojoTester forClass(Class<T> clz) {
        return new PojoTester<>(clz);
    }

    public PojoTester addObjectFactory(Class<?> clazz, Function<Class<?>, ?> factory) {
        if (objectFactories.containsKey(clazz)) {
            objectFactories.put(clazz, factory); // override existing factory
        } else { // insert before Object
            int i = objectFactoriesOrder.indexOf(Object.class) - 1;
            if (i < 0) {
                i = 0;
            }
            objectFactoriesOrder.add(i, clazz);
            objectFactories.put(clazz, factory);
        }
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
