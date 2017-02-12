package com.dacklabs.bustracker.application;

import com.dacklabs.bustracker.data.AgencyRoutes;
import com.dacklabs.bustracker.data.QueryBusLocations;
import com.dacklabs.bustracker.data.QueryBusRoute;
import com.dacklabs.bustracker.data.BusLocations;
import com.dacklabs.bustracker.data.BusRoute;
import com.dacklabs.bustracker.data.QueryResult;

public interface BusApi {
    QueryResult<BusLocations> queryBusLocationsFor(QueryBusLocations query);

    QueryResult<BusRoute> queryBusRouteFor(QueryBusRoute query);

    QueryResult<AgencyRoutes> queryProvider(String provider);
}
