package com.servet.runner;

import java.io.PrintStream;
import java.util.List;

import com.servet.runner.records.TestResult;
import com.servet.runner.records.TestResult.Status;

/** Sonuçları konsola yazar. Windows cmd'de bozulmaması için emoji yerine ASCII etiketler. */
public class ConsoleReporter {

    private final PrintStream out;

    public ConsoleReporter(PrintStream out) {
        this.out = out;
    }

    public void printClassResults(String className, List<TestResult> results) {
        out.println();
        out.println("===== " + className + " =====");
        for (TestResult r : results) {
            switch (r.status()) {
                case PASSED -> out.printf("  [PASS] %s (%d ms)%n", r.methodName(), r.durationMillis());
                case FAILED -> out.printf("  [FAIL] %s (%d ms)%n         -> %s: %s%n", r.methodName(),
                        r.durationMillis(), r.failure().getClass().getSimpleName(), r.failure().getMessage());
                case SKIPPED -> out.printf("  [SKIP] %s (%s)%n", r.methodName(),
                        r.skipReason().isBlank() ? "devre dışı" : r.skipReason());
            }
        }
    }

    public void printSummary(List<TestResult> all) {
        out.println();
        out.println("--------------------------------------------");
        out.printf("Toplam: %d | Geçti: %d | Başarısız: %d | Atlandı: %d%n",
                all.size(), count(all, Status.PASSED), count(all, Status.FAILED), count(all, Status.SKIPPED));
        out.println(count(all, Status.FAILED) == 0 ? "SONUÇ: BAŞARILI" : "SONUÇ: BAŞARISIZ");
    }

    private static long count(List<TestResult> results, Status status) {
        return results.stream().filter(r -> r.status() == status).count();
    }
}