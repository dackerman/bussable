package com.dacklabs.bustracker.application;

import android.util.Log;

import com.dacklabs.bustracker.application.requests.AddRouteRequest;
import com.dacklabs.bustracker.application.requests.ImmutableRouteAdded;
import com.dacklabs.bustracker.application.requests.ImmutableRouteRemoved;
import com.dacklabs.bustracker.application.requests.RemoveRouteRequest;
import com.dacklabs.bustracker.data.ImmutableRouteName;
import com.dacklabs.bustracker.data.RouteName;
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
        log("adding route to RouteList " + request.routeName().displayName());
        if (routes.add(request.routeName())) {
            eventBus.post(ImmutableRouteAdded.of(request.routeName()));
        }
    }

    @Subscribe
    public void removeRoute(RemoveRouteRequest request) {
        log("removing route from routeList " + request.routeName().displayName());
        if (routes.remove(request.routeName())) {
            eventBus.post(ImmutableRouteRemoved.of(request.routeName()));
        }
    }

    public void save(Storage storage) {
        String serialized = serialize();
        log("Saving RouteList to storage: " + serialized);
        storage.write("routelist", serialized);
    }

    private String serialize() {
        return Joiner.on(",").join(
                Iterables.transform(routes, RouteName::displayName));
    }

    public void load(Storage storage) {
        log("Loading RouteList from storage");
        Optional<String> data = storage.read("routelist");
        if (data.isPresent()) {
            for (String name : data.get().split(",")) {
                routes.add(ImmutableRouteName.of(name));
            }
        }
        log("After loading from storage, routes are now " + serialize());
    }

    public boolean routeIsSelected(RouteName routeName) {
        return routes.contains(routeName);
    }

    public Iterable<RouteName> routes() {
        return routes;
    }

    private void log(String message) {
        AppLogger.info(this, message);
    }
}
