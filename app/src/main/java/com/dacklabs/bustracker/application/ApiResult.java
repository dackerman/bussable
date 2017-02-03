package com.dacklabs.bustracker.application;

public class ApiResult<T> {
    private final T value;

    public ApiResult(T value) {
        this.value = value;
    }

    public T getResult() {
        return value;
    }
}
