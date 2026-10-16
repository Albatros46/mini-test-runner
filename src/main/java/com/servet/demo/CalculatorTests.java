package com.servet.demo;

import static com.servet.runner.MyAssert.assertEquals;
import static com.servet.runner.MyAssert.fail;

import com.servet.runner.annotations.MyAfterEach;
import com.servet.runner.annotations.MyBeforeEach;
import com.servet.runner.annotations.MyDisabled;
import com.servet.runner.annotations.MyTest;

/** Kendi annotation'larımızla yazılmış örnek test sınıfı. */
public class CalculatorTests {

    private Calculator calculator;

    @MyBeforeEach
    void setUp() {
        calculator = new Calculator();
    }

    @MyAfterEach
    void tearDown() {
        calculator = null;
    }

    @MyTest
    void addsTwoNumbers() {
        assertEquals(5, calculator.add(2, 3));
    }

    @MyTest
    void addsNegativeNumbers() {
        assertEquals(-5, calculator.add(-2, -3));
    }

    @MyTest
    void dividesEvenly() {
        assertEquals(2, calculator.divide(6, 3));
    }

    @MyTest(expected = ArithmeticException.class)
    void divisionByZeroThrows() {
        calculator.divide(1, 0);
    }

    @MyTest
    @MyDisabled("Çarpma özelliği henüz yok")
    void multiplies() {
        fail("Atlanan test çalışmamalıydı");
    }

    /** Raporun hata çıktısını görmek için bilerek yanlış yazıldı. Düzeltip tekrar çalıştır! */
    @MyTest
    void intentionallyFailing() {
        assertEquals(4, calculator.add(2, 3));
    }
}