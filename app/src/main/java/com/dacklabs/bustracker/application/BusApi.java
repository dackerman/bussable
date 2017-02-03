package com.dacklabs.bustracker.application;

import com.dacklabs.bustracker.data.BusRoute;
import com.dacklabs.bustracker.util.Async;

public interface BusApi {
    Async<QueryResult<BusRoute>> getRoute(RouteName routeNumber);
}
