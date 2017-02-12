package com.dacklabs.bustracker.data;


import org.immutables.value.Value;

@Value.Immutable
public abstract class RouteAdded implements Message {
    @Value.Parameter
    public abstract RouteName routeName();
}
