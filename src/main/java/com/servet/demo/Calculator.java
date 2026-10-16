package com.servet.demo;

public class Calculator {

    public int add(int a, int b) {
        return a + b;
    }

    public int divide(int a, int b) {
        return a / b; // b == 0 ise ArithmeticException
    }
}