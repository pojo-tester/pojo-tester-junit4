package com.github.pojotester.tester;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.function.Consumer;

import com.github.pojotester.PJContext;
import com.github.pojotester.PJReflectUtils;

public class PJSetterGetterTest implements Consumer<PJContext> {

    public void accept(PJContext context) {
        String fieldName = context.getTestedFieldName();
        String sufix = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
        Class<?> fieldType = PJReflectUtils.getDeclaredFieldType(context.getTestedType(), fieldName);

        Method setter = PJReflectUtils.findMethod(context.getTestedType(), "set" + sufix, fieldType);
        if (setter == null || !PJReflectUtils.checkAccessors(setter, Modifier::isPublic)) {
            return;
        }

        Method getter = null;
        for (String prefix : Arrays.asList("get", "is", "has", "can", "should")) {
            getter = PJReflectUtils.findMethod(context.getTestedType(), prefix + sufix);

            if (getter != null) {
                break;
            }
        }

        if (getter == null || !PJReflectUtils.checkAccessors(getter, Modifier::isPublic)) {
            return;
        }

        Object testObject = context.createObject(context.getTestedType());
        Object fieldObject = context.createObject(fieldType);

        PJReflectUtils.invokeMethod(testObject, setter, fieldObject);

        Object gotObject = PJReflectUtils.invokeMethod(testObject, getter);
        if (gotObject == null) {
            throw new IllegalStateException("null received!");
        }

        if (!testObject.equals(gotObject)) {
            throw new IllegalStateException("objects not equal!");
        }
    }
}
