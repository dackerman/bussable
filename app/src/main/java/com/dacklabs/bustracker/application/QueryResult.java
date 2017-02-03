package com.dacklabs.bustracker.application;

public class QueryResult<T> {
    public final T result;
    public final String failureMessage;

    private QueryResult(T value, String message) {
        this.result = value;
        this.failureMessage = message;
    }

    public static <A> QueryResult<A> success(A value) {
        return new QueryResult<>(value, null);
    }

    public static <A> QueryResult<A> failure(String failureMessage) {
        return new QueryResult<>(null, failureMessage);
    }

    public boolean succeeded() {
        return result != null;
    }
}
