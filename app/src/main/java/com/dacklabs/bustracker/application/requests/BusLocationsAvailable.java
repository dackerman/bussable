package com.dacklabs.bustracker.application.requests;

import com.dacklabs.bustracker.data.BusLocation;

import org.immutables.value.Value;

import java.util.Map;

@Value.Immutable
public abstract class BusLocationsAvailable implements Message {
    @Value.Parameter
    public abstract String route();
    @Value.Parameter
    public abstract Map<String, BusLocation> locations();
}
