package com.dacklabs.bustracker.application.requests;

import org.immutables.value.Value;

@Value.Immutable
public abstract class QueryBusRoute implements Message {
    @Value.Parameter public abstract String provider();
    @Value.Parameter public abstract String route();
}
