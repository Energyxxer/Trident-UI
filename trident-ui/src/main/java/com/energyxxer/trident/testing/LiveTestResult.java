package com.energyxxer.trident.testing;

public class LiveTestResult {
    public enum ResultType {
        POSITIVE, NEGATIVE
    }

    private final ResultType type;
    private final String message;

    public LiveTestResult(ResultType type) {
        this(type, null);
    }

    public LiveTestResult(ResultType type, String message) {
        this.type = type;
        this.message = message;
    }

    public ResultType getType() {
        return type;
    }

    @Override
    public String toString() {
        return type.toString() + (message != null ? ": " + message : "");
    }
}
