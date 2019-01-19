package com.github.pojotester.tester;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.function.Consumer;

import com.github.pojotester.PJContext;
import com.github.pojotester.PJReflectUtils;
import com.sun.xml.internal.ws.util.StringUtils;

public class PJSetterGetterTest implements Consumer<PJContext> {

    public void accept(PJContext context) {
        String fieldName = StringUtils.capitalize(context.getTestedFieldName());
        Class<?> fieldType = PJReflectUtils.getDeclaredFieldType(context.getType(), context.getTestedFieldName());

        Method setter = PJReflectUtils.findMethod(context.getType(), "set" + fieldName, fieldType);
        if (setter == null || !PJReflectUtils.checkAccessors(setter, Modifier::isPublic)) {
            return;
        }

        Method getter = null;
        for (String prefix : Arrays.asList("get", "is", "has", "can", "should")) {
            getter = PJReflectUtils.findMethod(context.getType(), prefix + fieldName, fieldType);

            if (getter != null) {
                break;
            }
        }

        if (getter == null || !PJReflectUtils.checkAccessors(getter, Modifier::isPublic)) {
            return;
        }

        Object testObject = context.createObject(context.getType());
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
