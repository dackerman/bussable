package com.dacklabs.bustracker.data;

import org.immutables.value.Value;

@Value.Immutable
public abstract class BusLocation {
    public abstract String vehicleId();
    public abstract Direction direction();
    public abstract double latitude();
    public abstract double longitude();
    public abstract double heading();
    public abstract double speedInKph();
}
