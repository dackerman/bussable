package com.dacklabs.bustracker.application;

import com.dacklabs.bustracker.data.AddRouteRequest;
import com.dacklabs.bustracker.data.ImmutableRouteAdded;
import com.dacklabs.bustracker.data.ImmutableRouteName;
import com.dacklabs.bustracker.data.ImmutableRouteRemoved;
import com.dacklabs.bustracker.data.RemoveRouteRequest;
import com.dacklabs.bustracker.data.RouteName;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import java.util.Set;

public final class RouteList {

    private boolean loaded = false;
    private final Set<RouteName> routes = Sets.newConcurrentHashSet();
    private final EventBus eventBus;
    private final AppLogger log;

    public RouteList(EventBus eventBus, AppLogger log) {
        this.eventBus = eventBus;
        this.log = log;
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
        log("Saving RouteList to storage: [" + serialized + "]");
        storage.write("routelist", serialized);
    }

    private String serialize() {
        return Joiner.on(",").join(
                Iterables.transform(routes, RouteName::displayName));
    }

    public void load(Storage storage) {
        if (loaded) return;
        log("Loading RouteList from storage");
        Optional<String> data = storage.read("routelist");
        if (data.isPresent()) {
            for (String name : data.get().split(",")) {
                if (!name.trim().isEmpty()) {
                    routes.add(ImmutableRouteName.of(name));
                }
            }
        }
        log("After loading from storage, routes are now [" + serialize() + "]");
        loaded = true;
    }

    public boolean routeIsSelected(RouteName routeName) {
        return routes.contains(routeName);
    }

    public Iterable<RouteName> routes() {
        return routes;
    }

    private void log(String message) {
        log.info(this, message);
    }
}
