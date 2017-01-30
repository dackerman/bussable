package com.dacklabs.bustracker.util;

@FunctionalInterface
public interface Consumer<T> {
    void accept(T value);
}
