package com.dacklabs.bustracker.application.requests;

import com.dacklabs.bustracker.data.BusRoute;

import org.immutables.value.Value;

@Value.Immutable
public abstract class BusRouteUpdated implements Message {
    @Value.Parameter
    public abstract BusRoute route();
}
