package com.github.pojotester;

import com.github.pojotester.fixtures.ChildType;
import org.junit.Test;

public class PojoTesterTest {

    @Test
    public void test() {
        PojoTester.forClass(ChildType.class).test();
    }

    @Test
    public void testCreators() {
        PojoTester pojoTester = PojoTester.forClass(Object.class);

        PJObjectFactory objectFactory = new PJObjectFactory();
        objectFactory.createObject(String[].class);
    }

}
