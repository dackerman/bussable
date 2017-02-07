package com.dacklabs.bustracker.application;

import com.dacklabs.bustracker.activity.AgencyRoutes;
import com.dacklabs.bustracker.application.requests.QueryBusLocations;
import com.dacklabs.bustracker.application.requests.QueryBusRoute;
import com.dacklabs.bustracker.data.BusLocations;
import com.dacklabs.bustracker.data.BusRoute;

public interface BusApi {
    QueryResult<BusLocations> queryBusLocationsFor(QueryBusLocations query);

    QueryResult<BusRoute> queryBusRouteFor(QueryBusRoute query);

    QueryResult<AgencyRoutes> queryProvider(String provider);
}
