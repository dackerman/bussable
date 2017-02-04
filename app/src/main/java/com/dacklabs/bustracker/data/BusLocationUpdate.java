package com.dacklabs.bustracker.data;

import org.immutables.value.Value;

@Value.Immutable
public abstract class BusLocationUpdate {
    @Value.Parameter public abstract RouteName routeName();
    @Value.Parameter public abstract BusLocation busLocation();
}
