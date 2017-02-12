package com.dacklabs.bustracker.data;

import org.immutables.value.Value;

@Value.Immutable
public abstract class BusRouteUpdated implements Message {
    @Value.Parameter
    public abstract BusRoute route();
}
