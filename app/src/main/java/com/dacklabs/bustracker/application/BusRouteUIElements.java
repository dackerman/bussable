package com.dacklabs.bustracker.application;

import com.dacklabs.bustracker.data.BusLocations;
import com.dacklabs.bustracker.data.BusRoute;

public interface BusRouteUIElements {
    void updateBusses(BusLocations busses);

    void updateRoute(BusRoute route);

    void removeRoute(String routeName);
}
