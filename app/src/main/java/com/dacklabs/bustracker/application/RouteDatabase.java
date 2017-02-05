package com.dacklabs.bustracker.application;

import com.dacklabs.bustracker.application.requests.BusLocationsAvailable;
import com.dacklabs.bustracker.application.requests.BusRouteUpdated;
import com.dacklabs.bustracker.application.requests.ImmutableBusLocationsAvailable;
import com.dacklabs.bustracker.application.requests.ImmutableBusRouteUpdated;
import com.dacklabs.bustracker.application.requests.RouteRemoved;
import com.dacklabs.bustracker.data.BusLocation;
import com.dacklabs.bustracker.data.BusLocationUpdate;
import com.dacklabs.bustracker.data.BusLocations;
import com.dacklabs.bustracker.data.BusRoute;
import com.dacklabs.bustracker.data.ImmutableBusLocationUpdate;
import com.dacklabs.bustracker.data.RouteName;
import com.google.common.base.Optional;
import com.google.common.collect.Sets;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;

public final class RouteDatabase {

    public interface Listener {

        void onBusLocationsUpdated(BusLocationsAvailable locationsUpdated);

        void onBusRouteUpdated(BusRouteUpdated routeUpdated);
        void onBusRouteRemoved(RouteRemoved message);
    }
    private Set<Listener> listeners = Sets.newConcurrentHashSet();
    private final ConcurrentHashMap<RouteName, BusRoute> routes = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<RouteName, Map<String, BusLocation>> locations = new
            ConcurrentHashMap<>();

    private final BlockingDeque<BusLocationUpdate> updateQueue = new LinkedBlockingDeque<>();

    public void registerListener(Listener listener) {
        listeners.add(listener);
    }

    public void unregisterListener(Listener listener) {
        listeners.remove(listener);
    }

    public Map<String, BusLocation> queryLocations(RouteName routeName) {
        Map<String, BusLocation> existingLocations = locations.get(routeName);
        if (existingLocations == null) return new HashMap<>();
        return existingLocations;
    }

    public Optional<BusRoute> getRoute(RouteName routeName) {
        return Optional.fromNullable(routes.get(routeName));
    }

    public boolean hasRoute(RouteName routeName) {
        return routes.get(routeName) != null;
    }

    public void updateRoute(BusRoute route) {
        log("Updating Route in database " + route.routeName().displayName());
        if (routes.put(route.routeName(), route) == null) {
            for (Listener listener : listeners) {
                listener.onBusRouteUpdated(ImmutableBusRouteUpdated.of(route));
            }
        }
    }

    public void updateLocations(BusLocations newLocations) {
        log("Updating locations in database " + newLocations.routeName().displayName());
        Map<String, BusLocation> existingLocations = locations.get(newLocations.routeName());
        if (existingLocations == null) existingLocations = new ConcurrentHashMap<>();

        for (Map.Entry<String, BusLocation> entry : newLocations.locations().entrySet()) {
            BusLocation newLocation = entry.getValue();
            BusLocation previousLocation = existingLocations.put(entry.getKey(), newLocation);
            if (!newLocation.equals(previousLocation)) {
                updateQueue.add(ImmutableBusLocationUpdate.of(newLocations.routeName(), newLocation));
            }
        }

        for (Listener listener : listeners) {
            listener.onBusLocationsUpdated(ImmutableBusLocationsAvailable.of(
                    newLocations.routeName(), new ConcurrentHashMap<>(existingLocations)));
        }

    }

    private void log(String message) {
        AppLogger.info(this, message);
    }
}
