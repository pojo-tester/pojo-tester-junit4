package com.github.pojotester;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

class PJObjectFactory {

    private HashMap<Class<?>, Function<Class<?>, ?>> objectFactories = new HashMap<>();
    private LinkedList<Class<?>> objectFactoriesOrder = new LinkedList<>();

    PJObjectFactory() {
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

    public void addObjectFactory(Class<?> clazz, Function<Class<?>, ?> factory) {
        if (objectFactories.containsKey(clazz)) {
            objectFactories.put(clazz, factory); // override existing factory
        } else { // insert before Object
            int i = objectFactoriesOrder.indexOf(Object.class) - 1;
            objectFactoriesOrder.add(Math.max(i, 0), clazz);
            objectFactories.put(clazz, factory);
        }
    }
}