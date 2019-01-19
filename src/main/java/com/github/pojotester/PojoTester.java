package com.github.pojotester;

public class PojoTester {

    public static PojoTester with(Class<?> clz) {
        return new PojoTester(clz);
    }

    private PojoTester(Class<?> clz) {

    }

    public void test() {

    }
}
