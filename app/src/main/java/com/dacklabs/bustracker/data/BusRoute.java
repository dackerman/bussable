package com.dacklabs.bustracker.data;

import com.google.common.collect.ImmutableList;

import org.immutables.value.Value;

@Value.Immutable
public abstract class BusRoute {

    @Value.Parameter
    public abstract String routeName();

    @Value.Parameter
    public abstract ImmutableList<RoutePath> paths();
}
