package com.servet.runner;

import java.util.Objects;

/** JUnit'in Assertions sınıfının mini versiyonu. Başarısızlık = AssertionError. */
public final class MyAssert {

    private MyAssert() {
    }

    public static void assertEquals(Object expected, Object actual) {
        if (!Objects.equals(expected, actual)) {
            throw new AssertionError("Beklenen: " + expected + ", Gerçek: " + actual);
        }
    }

    public static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    public static void fail(String message) {
        throw new AssertionError(message);
    }
}