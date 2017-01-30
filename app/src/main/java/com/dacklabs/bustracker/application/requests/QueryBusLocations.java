package com.dacklabs.bustracker.application.requests;

import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
public abstract class QueryBusLocations implements Message {
    @Value.Parameter public abstract String provider();
    @Value.Parameter public abstract String route();
    @Value.Parameter public abstract Optional<String> lastQueryTime();
}
