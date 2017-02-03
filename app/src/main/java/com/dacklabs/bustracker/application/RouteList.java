package com.dacklabs.bustracker.application;

import android.util.Log;

import com.dacklabs.bustracker.application.requests.AddRouteRequest;
import com.dacklabs.bustracker.application.requests.ImmutableRouteAdded;
import com.dacklabs.bustracker.application.requests.ImmutableRouteRemoved;
import com.dacklabs.bustracker.application.requests.RemoveRouteRequest;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import java.util.Set;

public final class RouteList {

    private final Set<RouteName> routes = Sets.newConcurrentHashSet();
    private final EventBus eventBus;

    public RouteList(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Subscribe
    public void requestRoute(AddRouteRequest request) {
        log("adding route to RouteList " + request.routeNumber().displayName());
        if (routes.add(request.routeNumber())) {
            eventBus.post(ImmutableRouteAdded.of(request.routeNumber()));
        }
    }

    @Subscribe
    public void removeRoute(RemoveRouteRequest request) {
        log("removing route from routeList " + request.routeNumber().displayName());
        if (routes.remove(request.routeNumber())) {
            eventBus.post(ImmutableRouteRemoved.of(request.routeNumber()));
        }
    }

    public void save(Storage storage) {
        log("Saving RouteList to storage");
        storage.write("routelist", Joiner.on(",").join(
                Iterables.transform(routes, RouteName::displayName)));
    }

    public void load(Storage storage) {
        log("Loading RouteList from storage");
        Optional<String> data = storage.read("routelist");
        if (data.isPresent()) {
            for (String name : data.get().split(",")) {
                routes.add(ImmutableRouteName.of(name));
            }
        }
    }

    public Iterable<RouteName> routes() {
        return routes;
    }

    private void log(String message) {
        Log.d("DACK:RouteList", message);
    }
}
