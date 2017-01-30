package com.dacklabs.bustracker.data;

import org.immutables.value.Value;

import java.util.Map;

@Value.Immutable
public abstract class BusLocations {
    public abstract String route();
    public abstract String lastQueryTime();
    public abstract Map<String, BusLocation> locations();
}
