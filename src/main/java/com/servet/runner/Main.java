package com.servet.runner;

import java.util.ArrayList;
import java.util.List;

import com.servet.runner.records.TestResult;
import com.servet.runner.records.TestResult.Status;

/**
 * Kullanım: java com.servet.runner.Main com.paket.TestSinifi1 com.paket.TestSinifi2 ...
 * Argüman verilmezse demo test sınıfını çalıştırır.
 */
public class Main {

    public static void main(String[] args) throws ClassNotFoundException {
        String[] classNames = args.length > 0 ? args : new String[] { "com.servet.demo.CalculatorTests" };

        MiniTestRunner runner = new MiniTestRunner();
        ConsoleReporter reporter = new ConsoleReporter(System.out);
        List<TestResult> allResults = new ArrayList<>();

        for (String className : classNames) {
            // Sınıf adı (String) → Class nesnesi: reflection'ın giriş kapısı
            Class<?> testClass = Class.forName(className);
            List<TestResult> results = runner.run(testClass);
            reporter.printClassResults(testClass.getSimpleName(), results);
            allResults.addAll(results);
        }
        reporter.printSummary(allResults);

        // CI araçları (Jenkins, GitHub Actions) çıkış koduna bakar: 0 = başarılı
        boolean anyFailed = allResults.stream().anyMatch(r -> r.status() == Status.FAILED);
        System.exit(anyFailed ? 1 : 0);
    }
}