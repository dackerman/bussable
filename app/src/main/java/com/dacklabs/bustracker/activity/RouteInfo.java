package com.dacklabs.bustracker.activity;

import com.dacklabs.bustracker.data.RouteName;

import org.immutables.value.Value;

@Value.Immutable
public abstract class RouteInfo {
    @Value.Parameter public abstract RouteName routeName();
    @Value.Parameter public abstract String routeTitle();
}
