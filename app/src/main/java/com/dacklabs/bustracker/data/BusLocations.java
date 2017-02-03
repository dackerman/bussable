package com.dacklabs.bustracker.data;

import com.dacklabs.bustracker.application.RouteName;

import org.immutables.value.Value;

import java.util.Map;

@Value.Immutable
public abstract class BusLocations {
    public abstract RouteName route();
    public abstract String lastQueryTime();
    public abstract Map<String, BusLocation> locations();
}
