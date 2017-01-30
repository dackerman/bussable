package com.dacklabs.bustracker.application;

import com.dacklabs.bustracker.application.requests.AddRouteRequest;
import com.dacklabs.bustracker.application.requests.Message;
import com.dacklabs.bustracker.application.requests.ImmutableRouteAdded;
import com.google.common.collect.Sets;

import java.util.HashSet;
import java.util.Set;

public final class RouteList {

    private final Set<String> routes = new HashSet<>();

    public Set<Message> requestRoute(AddRouteRequest request) {
        routes.add(request.routeNumber());
        return Sets.newHashSet(ImmutableRouteAdded.of(request.routeNumber()));
    }
}
