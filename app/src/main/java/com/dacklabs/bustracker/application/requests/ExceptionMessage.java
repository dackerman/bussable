package com.dacklabs.bustracker.application.requests;

import org.immutables.value.Value;

@Value.Immutable
public abstract class ExceptionMessage implements Message {
    @Value.Parameter
    public abstract Throwable exception();
    @Value.Parameter
    public abstract String message();
}
