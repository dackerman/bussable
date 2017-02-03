package com.dacklabs.bustracker.application;

import com.dacklabs.bustracker.application.requests.AddRouteRequest;
import com.dacklabs.bustracker.data.BusRoute;

import java.util.concurrent.ExecutionException;

public class BusTrackerApp {

    private final BusApi busApi;
    private final UserRoutes userRoutes;
    private final ErrorReporter errorReporter;
    private MapState state = MapState.empty();

    public BusTrackerApp(BusApi busApi, UserRoutes userRoutes, ErrorReporter errorReporter) {
        this.busApi = busApi;
        this.userRoutes = userRoutes;
        this.errorReporter = errorReporter;
    }

    public MapState mapState() {
        return state;
    }

    public void addRoute(AddRouteRequest request) throws ExecutionException, InterruptedException {
        RouteName route = request.routeNumber();
        if (userRoutes.add(route)) {
            busApi.getRoute(request.routeNumber())
                    .map(this::onRouteDataReturned);
        }
    }

    private void onRouteDataReturned(QueryResult<BusRoute> result) {
        if (userRoutes.alreadyHas(result.result.routeName()))
        if (result.succeeded()) {
            BusRoute route = result.result;

            state = ImmutableMapState.copyOf(state).withRoutes(route);

        } else {
            errorReporter.report(result.failureMessage);
        }
    }
}
