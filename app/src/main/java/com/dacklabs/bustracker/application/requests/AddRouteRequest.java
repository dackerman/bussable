package com.dacklabs.bustracker.application.requests;

import org.immutables.value.Value;

@Value.Immutable
public abstract class AddRouteRequest {

    @Value.Parameter
    public abstract String routeNumber();
}
