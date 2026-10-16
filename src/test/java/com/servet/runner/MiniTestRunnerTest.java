package com.servet.runner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.servet.runner.annotations.MyAfterEach;
import com.servet.runner.annotations.MyBeforeEach;
import com.servet.runner.annotations.MyDisabled;
import com.servet.runner.annotations.MyTest;
import com.servet.runner.records.TestResult;
import com.servet.runner.records.TestResult.Status;

/** Test framework'ümüzü... JUnit ile test ediyoruz. */
@DisplayName("MiniTestRunner")
class MiniTestRunnerTest {

    // ---------- Runner'a verilecek örnek (fixture) sınıflar ----------

    static class SampleTests {
        @MyTest
        void passes() {
        }

        @MyTest
        void fails() {
            throw new AssertionError("bilerek başarısız");
        }

        @MyTest
        @MyDisabled("sebep")
        void skipped() {
        }

        @MyTest(expected = IllegalArgumentException.class)
        void throwsExpected() {
            throw new IllegalArgumentException();
        }

        @MyTest(expected = IllegalArgumentException.class)
        void doesNotThrowExpected() {
        }

        @MyTest(expected = IllegalArgumentException.class)
        void throwsWrongException() {
            throw new IllegalStateException("yanlış tür");
        }

        void notATest() {
        }
    }

    static final List<String> EVENTS = new ArrayList<>();
    static final Set<Object> INSTANCES = new HashSet<>();

    static class LifecycleTests {
        @MyBeforeEach
        void before() {
            EVENTS.add("before");
            INSTANCES.add(this);
        }

        @MyAfterEach
        void after() {
            EVENTS.add("after");
        }

        @MyTest
        void first() {
            EVENTS.add("first");
        }

        @MyTest
        void second() {
            EVENTS.add("second");
            throw new AssertionError("hata olsa da after çalışmalı");
        }
    }

    static class FailingBeforeEach {
        @MyBeforeEach
        void before() {
            throw new IllegalStateException("setup patladı");
        }

        @MyAfterEach
        void after() {
            EVENTS.add("after");
        }

        @MyTest
        void test() {
            EVENTS.add("test");
        }
    }

    // ---------- Testler ----------

    private final MiniTestRunner runner = new MiniTestRunner();

    @BeforeEach
    void clearRecorders() {
        EVENTS.clear();
        INSTANCES.clear();
    }

    private Map<String, TestResult> runByName(Class<?> testClass) {
        return runner.run(testClass).stream()
                .collect(Collectors.toMap(TestResult::methodName, Function.identity()));
    }

    @Test
    @DisplayName("Sadece @MyTest metotları bulunur")
    void findsOnlyAnnotatedMethods() {
        assertEquals(6, runner.run(SampleTests.class).size());
    }

    @Test
    @DisplayName("Geçen, başarısız ve atlanan testler doğru sınıflandırılır")
    void classifiesResults() {
        Map<String, TestResult> results = runByName(SampleTests.class);

        assertEquals(Status.PASSED, results.get("passes").status());
        assertEquals(Status.FAILED, results.get("fails").status());
        assertEquals(Status.SKIPPED, results.get("skipped").status());
        assertEquals("sebep", results.get("skipped").skipReason());
    }

    @Test
    @DisplayName("expected: doğru exception → geçer, yoksa veya yanlış türse → başarısız")
    void handlesExpectedExceptions() {
        Map<String, TestResult> results = runByName(SampleTests.class);

        assertEquals(Status.PASSED, results.get("throwsExpected").status());
        assertEquals(Status.FAILED, results.get("doesNotThrowExpected").status());
        assertInstanceOf(AssertionError.class, results.get("doesNotThrowExpected").failure());
        assertInstanceOf(IllegalStateException.class, results.get("throwsWrongException").failure());
    }

    @Test
    @DisplayName("Hata, InvocationTargetException değil asıl exception olarak raporlanır")
    void unwrapsInvocationTargetException() {
        TestResult failed = runByName(SampleTests.class).get("fails");

        assertInstanceOf(AssertionError.class, failed.failure());
        assertEquals("bilerek başarısız", failed.failure().getMessage());
    }

    @Test
    @DisplayName("Sıra: before → test → after; test başarısız olsa da after çalışır")
    void runsLifecycleInOrder() {
        runner.run(LifecycleTests.class);

        assertEquals(List.of("before", "first", "after", "before", "second", "after"), EVENTS);
    }

    @Test
    @DisplayName("Her test için yeni bir instance oluşturulur")
    void createsNewInstancePerTest() {
        runner.run(LifecycleTests.class);

        assertEquals(2, INSTANCES.size());
    }

    @Test
    @DisplayName("@MyBeforeEach patlarsa test çalışmaz ama after yine çalışır")
    void failingBeforeEachSkipsTestBody() {
        TestResult result = runner.run(FailingBeforeEach.class).get(0);

        assertEquals(Status.FAILED, result.status());
        assertEquals("setup patladı", result.failure().getMessage());
        assertEquals(List.of("after"), EVENTS);
    }

    @Test
    @DisplayName("Geçen testte failure alanı boştur")
    void passedTestHasNoFailure() {
        assertNull(runByName(SampleTests.class).get("passes").failure());
    }
}