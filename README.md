# Mini Test Runner — a tiny JUnit built with Reflection & Annotations

![Java](https://img.shields.io/badge/Java-21-orange) ![Reflection](https://img.shields.io/badge/Java-Reflection-blueviolet) ![JUnit5](https://img.shields.io/badge/JUnit-5-green) ![Maven](https://img.shields.io/badge/Maven-3.9-red) ![Tests](https://img.shields.io/badge/tests-8%20passing-brightgreen)

I use JUnit every day, so I built a small version of it to understand how it works inside.
Custom annotations, a reflection-based runner, a console reporter and CI-friendly exit codes. No dependencies.
Part of my 52-week QA Automation roadmap — **Week 3**.

## Writing tests with my own annotations

```java
public class CalculatorTests {

    private Calculator calculator;

    @MyBeforeEach
    void setUp() { calculator = new Calculator(); }

    @MyTest
    void addsTwoNumbers() { assertEquals(5, calculator.add(2, 3)); }

    @MyTest(expected = ArithmeticException.class)
    void divisionByZeroThrows() { calculator.divide(1, 0); }

    @MyTest
    @MyDisabled("Multiplication not implemented yet")
    void multiplies() { fail("should not run"); }
}
```

```
===== CalculatorTests =====
  [PASS] addsNegativeNumbers (5 ms)
  [PASS] addsTwoNumbers (0 ms)
  [PASS] dividesEvenly (0 ms)
  [PASS] divisionByZeroThrows (0 ms)
  [FAIL] intentionallyFailing (4 ms)
         -> AssertionError: Beklenen: 4, Gerçek: 5
  [SKIP] multiplies (Çarpma özelliği henüz yok)
--------------------------------------------
Toplam: 6 | Geçti: 4 | Başarısız: 1 | Atlandı: 1
SONUÇ: BAŞARISIZ
```

## Features

| Annotation | JUnit 5 equivalent |
|---|---|
| `@MyTest` | `@Test` |
| `@MyTest(expected = X.class)` | `assertThrows(X.class, ...)` |
| `@MyBeforeEach` / `@MyAfterEach` | `@BeforeEach` / `@AfterEach` |
| `@MyDisabled("reason")` | `@Disabled("reason")` |

Also included: `MyAssert` (`assertEquals`, `assertTrue`, `fail`), a `ConsoleReporter`, and a `Main` class that exits with `1` on failure, so Jenkins or GitHub Actions can fail the build.

## How it works

For each `@MyTest` method:

```
new instance  →  @MyBeforeEach  →  test  →  @MyAfterEach (always runs)
```

| Design decision | Why |
|---|---|
| `@Retention(RUNTIME)` | Without it, the annotation is not available at runtime and reflection cannot see it |
| New instance per test | Tests cannot share state. JUnit 5 does the same by default (`PER_METHOD`) |
| Unwrap `InvocationTargetException` | `Method.invoke()` wraps the real error. Without unwrapping, every failure would show as `InvocationTargetException` |
| Sort methods by name | `getDeclaredMethods()` has no guaranteed order, so sorting keeps runs repeatable |
| `@MyAfterEach` errors do not override the first failure | The root cause stays visible in the report |
| `@MyBeforeEach` fails → test body is skipped, `@MyAfterEach` still runs | Cleanup always happens |

## Testing the test framework

`MiniTestRunnerTest` checks the runner itself with JUnit 5. It covers result classification, expected exceptions, lifecycle order, a new instance per test, a failing setup, and exception unwrapping.

## Project structure

```
src/main/java/com/servet/
├── runner/
│   ├── annotations/   MyTest, MyBeforeEach, MyAfterEach, MyDisabled
│   ├── records/       TestResult
│   ├── MiniTestRunner, ConsoleReporter, MyAssert, Main
└── demo/              Calculator, CalculatorTests
src/test/java/com/servet/runner/
└── MiniTestRunnerTest
```

## Run

```bash
mvn clean test                                   # JUnit tests of the runner
java -cp target/classes com.servet.runner.Main   # run demo tests with my runner
```

## Next ideas
- [ ] `@MyBeforeAll` / `@MyAfterAll` (static, once per class)
- [ ] Inherit lifecycle methods from a base class (`getSuperclass()`)
- [ ] `@MyTimeout(ms)`
- [ ] Scan a whole package for test classes

## What I learned
- _TODO: 2–3 sentences in your own words_