package com.dacklabs.bustracker.util;

import com.google.common.base.Function;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

public final class Async<T> {

    private final ListenableFuture<T> future;
    private final Function<Throwable, T> fromThrowable;

    public Async(ListenableFuture<T> future, Function<Throwable, T> fromThrowable) {
        this.future = future;
        this.fromThrowable = fromThrowable;
    }

    public void map(Consumer<T> fn) {
        Futures.addCallback(future, new FutureCallback<T>() {
            @Override
            public void onSuccess(T result) {
                fn.accept(result);
            }

            @Override
            public void onFailure(Throwable t) {
                fn.accept(fromThrowable.apply(t));
            }
        });
    }
}
