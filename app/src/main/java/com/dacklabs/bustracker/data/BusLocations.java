package com.dacklabs.bustracker.data;

import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
public abstract class BusLocations {
    public abstract String route();
    public abstract String lastQueryTime();
    public abstract List<BusLocation> locations();
}
