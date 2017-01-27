package com.dacklabs.bustracker.data;

import org.immutables.value.Value;

@Value.Immutable
public abstract class PathCoordinate {
    @Value.Parameter
    public abstract float lat();

    @Value.Parameter
    public abstract float lon();
}
