package com.dacklabs.bustracker.application;

import com.dacklabs.bustracker.application.requests.BusLocationsUpdated;
import com.dacklabs.bustracker.application.requests.ImmutableBusLocationsAvailable;
import com.dacklabs.bustracker.application.requests.Message;
import com.dacklabs.bustracker.data.BusLocation;
import com.dacklabs.bustracker.data.BusLocations;
import com.google.common.collect.Sets;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class BusLocationCache {

    private final Map<String, Map<String, BusLocation>> locationCache = new HashMap<>();

    public Set<Message> storeNewLocations(BusLocationsUpdated message) {
        BusLocations locations = message.busses();

        Map<String, BusLocation> existing = locationCache.get(locations.route());
        if (existing == null) {
            existing = new HashMap<>();
        }
        existing.putAll(message.busses().locations());
        locationCache.put(locations.route(), existing);

        return Sets.newHashSet(ImmutableBusLocationsAvailable.of(locations.route(), existing));
    }
}
