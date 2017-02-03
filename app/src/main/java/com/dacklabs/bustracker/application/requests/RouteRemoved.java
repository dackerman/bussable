package com.dacklabs.bustracker.application.requests;

import com.dacklabs.bustracker.data.RouteName;

import org.immutables.value.Value;

@Value.Immutable
public abstract class RouteRemoved implements Message {
    @Value.Parameter
    public abstract RouteName routeName();
}
