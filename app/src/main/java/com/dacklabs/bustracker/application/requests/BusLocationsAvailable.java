package com.dacklabs.bustracker.application.requests;

import com.dacklabs.bustracker.data.BusLocations;

import org.immutables.value.Value;

@Value.Immutable
public abstract class BusLocationsAvailable {
    @Value.Parameter
    public abstract BusLocations locations();
}
