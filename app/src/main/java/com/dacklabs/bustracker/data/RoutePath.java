package com.dacklabs.bustracker.data;

import com.google.common.collect.ImmutableList;

import org.immutables.value.Value;

@Value.Immutable
public abstract class RoutePath {
    public abstract ImmutableList<PathCoordinate> coordinates();
}
