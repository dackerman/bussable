package com.dacklabs.bustracker.application;

import com.dacklabs.bustracker.data.BusLocations;
import com.dacklabs.bustracker.data.BusRoute;
import com.google.common.collect.ImmutableList;

import org.immutables.value.Value;

@Value.Immutable
public abstract class MapState {
    public abstract ImmutableList<BusLocations> busLocations();
    public abstract ImmutableList<BusRoute> routes();
}
