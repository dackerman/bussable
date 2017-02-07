package com.dacklabs.bustracker.activity;

import com.google.common.collect.ImmutableList;

import org.immutables.value.Value;

@Value.Immutable
public abstract class AgencyRoutes {
    @Value.Parameter public abstract String provider();
    @Value.Parameter public abstract ImmutableList<RouteInfo> routes();
}
