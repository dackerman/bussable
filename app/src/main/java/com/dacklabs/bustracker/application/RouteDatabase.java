package com.dacklabs.bustracker.application;

import android.util.Log;

import com.dacklabs.bustracker.application.requests.BusLocationsAvailable;
import com.dacklabs.bustracker.application.requests.BusRouteUpdated;
import com.dacklabs.bustracker.application.requests.ImmutableBusLocationsAvailable;
import com.dacklabs.bustracker.application.requests.ImmutableBusRouteUpdated;
import com.dacklabs.bustracker.application.requests.RouteRemoved;
import com.dacklabs.bustracker.data.BusLocation;
import com.dacklabs.bustracker.data.BusLocations;
import com.dacklabs.bustracker.data.BusRoute;
import com.dacklabs.bustracker.data.RouteName;
import com.google.common.collect.Sets;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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

    public void listen(Listener listener) {
        listeners.add(listener);
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
            existingLocations.put(entry.getKey(), entry.getValue());
        }

        for (Listener listener : listeners) {
            listener.onBusLocationsUpdated(ImmutableBusLocationsAvailable.of(
                    newLocations.routeName(), new ConcurrentHashMap<>(existingLocations)));
        }

    }

    private int log(String message) {
        return Log.d("DACK:RouteDatabase", message);
    }
}
