package com.github.pojotester;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.stream.Stream;

import com.github.pojotester.fixtures.ChildType;
import org.junit.Assert;
import org.junit.Test;

public class PJReflectUtilsTest {

    @Test
    public void testGetAllDeclaredFields_withoutSuperclass() {
        // when
        Field[] fields = PJReflectUtils.getAllDeclaredFields(ChildType.class, false);
        // then
        String[] fieldNames = Stream.of(fields).map(Field::getName).toArray(String[]::new);
        Assert.assertArrayEquals(fieldNames, toArray("name", "integerValue", "intValue"));
    }

    @Test
    public void testGetAllDeclaredFields_withSuperclass() {
        // when
        Field[] fields = PJReflectUtils.getAllDeclaredFields(ChildType.class, true);
        // then
        String[] fieldNames = Stream.of(fields).map(Field::getName).toArray(String[]::new);
        Assert.assertArrayEquals(fieldNames, toArray("name", "integerValue", "intValue", "parentName"));
    }

    @Test
    public void testFindMethod() {
        // when
        Method getIntegerValueMethod = PJReflectUtils.findMethod(ChildType.class, "getIntegerValue");
        // then
        Assert.assertNotNull(getIntegerValueMethod);
    }

    @Test
    public void testCheckAccessors() {
        // given
        Method getIntegerValueMethod = PJReflectUtils.findMethod(ChildType.class, "getIntegerValue");

        // then
        Assert.assertTrue(PJReflectUtils.checkAccessors(getIntegerValueMethod, Modifier::isPublic, m -> !Modifier.isNative(m)));
        Assert.assertFalse(PJReflectUtils.checkAccessors(getIntegerValueMethod, Modifier::isPrivate, Modifier::isFinal));
    }

    @Test
    public void testGetAllDeclaredMethods() {
        // when
        Method[] allDeclaredMethods = PJReflectUtils.getAllDeclaredMethods(ChildType.class);

        // then
        String[] methodNames = Stream.of(allDeclaredMethods).map(Method::getName).sorted().toArray(String[]::new);
        Assert.assertArrayEquals(methodNames, toArray("getIntValue", "getIntegerValue", "getName", "setIntValue", "setIntegerValue", "setName"));
    }

    @Test
    public void testInvokeMethod() {
        // given
        ChildType child = new ChildType();
        Method setNameMethod = PJReflectUtils.findMethod(ChildType.class, "setName", String.class);
        Method getNameMethod = PJReflectUtils.findMethod(ChildType.class, "getName");

        // when
        PJReflectUtils.invokeMethod(child, setNameMethod, "name");
        String name = (String) PJReflectUtils.invokeMethod(child, getNameMethod);

        // then
        Assert.assertEquals(name, "name");
    }

    @Test
    public void testGetFieldValue() {
        // given
        ChildType child = new ChildType();
        child.setName("john doe");

        // when
        String name = (String) PJReflectUtils.getFieldValue(child, "name");

        // then
        Assert.assertEquals(name, child.getName());
    }

    // helpers

    private String[] toArray(String... strings) {
        return strings;
    }
}

