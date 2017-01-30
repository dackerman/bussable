package com.dacklabs.bustracker.application.requests;

import com.dacklabs.bustracker.data.BusLocations;

import org.immutables.value.Value;

@Value.Immutable
public abstract class BusLocationsUpdated implements Message {
    @Value.Parameter
    public abstract BusLocations busses();
}
