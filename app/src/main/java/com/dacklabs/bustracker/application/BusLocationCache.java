package com.dacklabs.bustracker.application;

import com.dacklabs.bustracker.application.requests.BusLocationsUpdated;
import com.dacklabs.bustracker.application.requests.ImmutableBusLocationsAvailable;
import com.dacklabs.bustracker.application.requests.Message;
import com.dacklabs.bustracker.data.BusLocations;
import com.dacklabs.bustracker.data.ImmutableBusLocations;
import com.google.common.collect.Sets;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class BusLocationCache {

    private final Map<String, BusLocations> locationCache = new HashMap<>();

    public Set<Message> storeNewLocations(BusLocationsUpdated message) {
        BusLocations locations = message.busses();
        ImmutableBusLocations.Builder builder = ImmutableBusLocations.builder();
        if (locationCache.containsKey(locations.route())) {
            builder.from(locationCache.get(locations.route()));
        }
        BusLocations updatedLocations = builder.from(locations).build();
        locationCache.put(locations.route(), updatedLocations);

        return Sets.newHashSet(ImmutableBusLocationsAvailable.of(updatedLocations));
    }
}
