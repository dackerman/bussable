package com.dacklabs.bustracker.application;

import com.dacklabs.bustracker.application.requests.BusLocationsAvailable;
import com.dacklabs.bustracker.data.BusRoute;
import com.dacklabs.bustracker.data.RouteName;

public interface BusRouteUIElements {
    void updateBusses(BusLocationsAvailable busses);

    void updateRoute(BusRoute route);

    void removeRoute(RouteName routeName);
}
