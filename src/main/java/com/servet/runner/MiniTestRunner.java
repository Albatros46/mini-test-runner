package com.servet.runner;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import com.servet.runner.annotations.MyAfterEach;
import com.servet.runner.annotations.MyBeforeEach;
import com.servet.runner.annotations.MyDisabled;
import com.servet.runner.annotations.MyTest;
import com.servet.runner.records.TestResult;

/**
 * Reflection ile bir sınıftaki @MyTest metotlarını bulup çalıştırır.
 *
 * Her test için: yeni instance oluştur → @MyBeforeEach → test → @MyAfterEach
 */
public class MiniTestRunner {

    public List<TestResult> run(Class<?> testClass) {
        List<Method> beforeEach = findAnnotated(testClass, MyBeforeEach.class);
        List<Method> afterEach = findAnnotated(testClass, MyAfterEach.class);
        List<TestResult> results = new ArrayList<>();

        for (Method test : findAnnotated(testClass, MyTest.class)) {
            MyDisabled disabled = test.getAnnotation(MyDisabled.class);
            if (disabled != null) {
                results.add(TestResult.skipped(testClass.getSimpleName(), test.getName(), disabled.value()));
            } else {
                results.add(runSingleTest(testClass, test, beforeEach, afterEach));
            }
        }
        return results;
    }

    private TestResult runSingleTest(Class<?> testClass, Method test, List<Method> beforeEach,
            List<Method> afterEach) {
        String className = testClass.getSimpleName();
        long start = System.nanoTime();
        Throwable failure;

        if (test.getParameterCount() != 0 || Modifier.isStatic(test.getModifiers())) {
            failure = new IllegalStateException("@MyTest metodu static olmamalı ve parametre almamalı");
        } else {
            try {
                // JUnit gibi: her test için YENİ bir instance → testler birbirini etkilemez
                Object instance = newInstance(testClass);
                failure = runLifecycle(instance, test, beforeEach, afterEach);
            } catch (Throwable t) {
                failure = t;
            }
        }

        long durationMillis = (System.nanoTime() - start) / 1_000_000;
        return failure == null
                ? TestResult.passed(className, test.getName(), durationMillis)
                : TestResult.failed(className, test.getName(), durationMillis, failure);
    }

    /** @return testin hatası; test geçtiyse null */
    private Throwable runLifecycle(Object instance, Method test, List<Method> beforeEach, List<Method> afterEach) {
        Throwable failure;
        try {
            for (Method before : beforeEach) {
                invoke(before, instance);
            }
            failure = runTestBody(instance, test);
        } catch (Throwable t) {
            failure = t; // @MyBeforeEach patladı → test hiç çalışmadı
        }

        // @MyAfterEach her durumda çalışır (temizlik), ama ilk hatayı ezmez
        for (Method after : afterEach) {
            try {
                invoke(after, instance);
            } catch (Throwable t) {
                if (failure == null) {
                    failure = t;
                }
            }
        }
        return failure;
    }

    private Throwable runTestBody(Object instance, Method test) {
        Class<? extends Throwable> expected = test.getAnnotation(MyTest.class).expected();
        try {
            invoke(test, instance);
        } catch (Throwable t) {
            if (expected != MyTest.None.class && expected.isInstance(t)) {
                return null; // beklenen exception geldi → geçti
            }
            return t;
        }
        if (expected != MyTest.None.class) {
            return new AssertionError("Beklenen exception fırlatılmadı: " + expected.getSimpleName());
        }
        return null;
    }

    private static Object newInstance(Class<?> testClass) throws ReflectiveOperationException {
        Constructor<?> constructor = testClass.getDeclaredConstructor();
        constructor.setAccessible(true);
        return constructor.newInstance();
    }

    /** Metodu çağırır; reflection'ın sardığı InvocationTargetException'ı açıp asıl hatayı fırlatır. */
    private static void invoke(Method method, Object instance) throws Throwable {
        method.setAccessible(true);
        try {
            method.invoke(instance);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

    /** getDeclaredMethods() sırası garanti değil → isme göre sıralayarak tekrarlanabilir yap. */
    private static List<Method> findAnnotated(Class<?> testClass, Class<? extends Annotation> annotation) {
        return Arrays.stream(testClass.getDeclaredMethods())
                .filter(m -> m.isAnnotationPresent(annotation))
                .sorted(Comparator.comparing(Method::getName))
                .toList();
    }
}