package com.servet.runner.records;



/** Tek bir test metodunun sonucu. */
public record TestResult(String className, String methodName, Status status, long durationMillis,
        Throwable failure, String skipReason) {

    public enum Status {
        PASSED, FAILED, SKIPPED
    }

    public static TestResult passed(String className, String methodName, long durationMillis) {
        return new TestResult(className, methodName, Status.PASSED, durationMillis, null, null);
    }

    public static TestResult failed(String className, String methodName, long durationMillis, Throwable failure) {
        return new TestResult(className, methodName, Status.FAILED, durationMillis, failure, null);
    }

    public static TestResult skipped(String className, String methodName, String reason) {
        return new TestResult(className, methodName, Status.SKIPPED, 0, null, reason);
    }
}