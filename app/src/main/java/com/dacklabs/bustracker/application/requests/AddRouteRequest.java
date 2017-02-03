package com.dacklabs.bustracker.application.requests;

import com.dacklabs.bustracker.application.RouteName;

import org.immutables.value.Value;

@Value.Immutable
public abstract class AddRouteRequest implements Message {
    @Value.Parameter
    public abstract RouteName routeNumber();
}
