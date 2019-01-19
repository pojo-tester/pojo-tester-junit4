package com.github.pojotester;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * Code partially copied from:
 * org.springframework.util.ReflectionUtils
 */
public final class PJReflectUtils {

    private PJReflectUtils() {

    }

    public static Class<?> getDeclaredFieldType(Class<?> clazz, String fieldName) {
        Objects.requireNonNull(clazz, "Class must not be null");
        Objects.requireNonNull(fieldName, "fieldName must not be null");
        try {
            return clazz.getDeclaredField(fieldName).getType();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public static Field[] getAllDeclaredFields(Class<?> clazz, boolean useSuperclass) {
        Objects.requireNonNull(clazz, "Class must not be null");
        List<Field> fields = new LinkedList<>();
        Class<?> current = clazz;
        do {
            Collections.addAll(fields, current.getDeclaredFields());
            current = current.getSuperclass();
        } while (current != Object.class && useSuperclass);
        return fields.toArray(new Field[0]);
    }

    @SafeVarargs
    public static boolean checkAccessors(Member member, Function<Integer, Boolean>... matchers) {
        Objects.requireNonNull(member, "Member must not be null");
        boolean retVal = true;
        for (Function<Integer, Boolean> matcher : matchers) {
            retVal &= matcher.apply(member.getModifiers());
        }
        return retVal;
    }

    public static Method findMethod(Class<?> clazz, String name, Class<?>... paramTypes) {
        Objects.requireNonNull(clazz, "Class must not be null");
        Objects.requireNonNull(name, "Method name must not be null");
        Class<?> searchType = clazz;
        while (searchType != null) {
            Method[] methods = (searchType.isInterface() ? searchType.getMethods() : getAllDeclaredMethods(searchType));
            for (Method method : methods) {
                if (name.equals(method.getName()) && (paramTypes == null || Arrays.equals(paramTypes, method.getParameterTypes()))) {
                    return method;
                }
            }
            searchType = searchType.getSuperclass();
        }
        return null;
    }

    public static Method[] getAllDeclaredMethods(Class<?> clazz) {
        Objects.requireNonNull(clazz, "Class must not be null");
        Method[] result = clazz.getDeclaredMethods();
        List<Method> defaultMethods = findConcreteMethodsOnInterfaces(clazz);
        if (defaultMethods != null) {
            result = new Method[result.length + defaultMethods.size()];
            System.arraycopy(result, 0, result, 0, result.length);
            int index = result.length;
            for (Method defaultMethod : defaultMethods) {
                result[index] = defaultMethod;
                index++;
            }
        }
        return result;
    }

    public static Object invokeMethod(Object target, Method method, Object... args) {
        Objects.requireNonNull(target, "Target object must not be null");
        Objects.requireNonNull(method, "Method must not be null");
        try {
            return method.invoke(target, args);
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }


    public static Object getFieldValue(Object target, String fieldName) {
        Objects.requireNonNull(target, "Target object must not be null");
        Objects.requireNonNull(fieldName, "fieldName must not be null");
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            if (!checkAccessors(field, Modifier::isPublic)) {
                field.setAccessible(true);
            }
            return field.get(target);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public static <T> T newInstance(Class<T> clazz) {
        Objects.requireNonNull(clazz, "Clazz must not be null");
        try {
            return clazz.newInstance();
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static List<Method> findConcreteMethodsOnInterfaces(Class<?> clazz) {
        List<Method> result = null;
        for (Class<?> ifc : clazz.getInterfaces()) {
            for (Method ifcMethod : ifc.getMethods()) {
                if (!Modifier.isAbstract(ifcMethod.getModifiers())) {
                    if (result == null) {
                        result = new LinkedList<>();
                    }
                    result.add(ifcMethod);
                }
            }
        }
        return result;
    }
}
