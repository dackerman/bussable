package com.dacklabs.bustracker.application;

import com.dacklabs.bustracker.application.requests.AddRouteRequest;
import com.dacklabs.bustracker.data.BusRoute;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

public class BusTrackerApp {

    private final BusApi busApi;
    private MapState state = MapState.empty();

    public BusTrackerApp(BusApi busApi) {
        this.busApi = busApi;
    }

    public MapState mapState() {
        return state;
    }

    public void addRoute(AddRouteRequest request) {
        ListenableFuture<BusApi.ApiResult<BusRoute>> response = busApi.getRoute(request.routeNumber());

        Futures.addCallback(response, new FutureCallback<BusApi.ApiResult<BusRoute>>() {
            @Override
            public void onSuccess(BusApi.ApiResult<BusRoute> result) {
                BusRoute route = result.getResult();
                state = ImmutableMapState.copyOf(state).withRoutes(route);
            }

            @Override
            public void onFailure(Throwable t) {
            }
        });
    }
}
