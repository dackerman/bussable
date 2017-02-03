package com.dacklabs.bustracker.application;

import org.immutables.value.Value;

@Value.Immutable(builder = false, intern = true, copy = false)
public abstract class RouteName {
    @Value.Parameter
    public abstract String displayName();
}
