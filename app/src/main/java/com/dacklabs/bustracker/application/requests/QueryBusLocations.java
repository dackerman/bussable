package com.dacklabs.bustracker.application.requests;

import com.dacklabs.bustracker.application.RouteName;
import com.google.common.base.Optional;

import org.immutables.value.Value;

@Value.Immutable
public abstract class QueryBusLocations implements Message {
    @Value.Parameter public abstract String provider();
    @Value.Parameter public abstract RouteName route();
    @Value.Parameter public abstract Optional<String> lastQueryTime();
}
