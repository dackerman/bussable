package com.dacklabs.bustracker.application;

import com.dacklabs.bustracker.application.fakes.FakeBusApi;
import com.dacklabs.bustracker.application.requests.ImmutableAddRouteRequest;
import com.dacklabs.bustracker.data.ImmutableBusRoute;
import com.dacklabs.bustracker.data.RoutePath;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class BusTrackerAppTest {

    private MapState emptyMap() {
        return ImmutableMapState.builder().build();
    }

    @Test
    public void initiallyHasAnEmptyMap() {
        BusTrackerApp app = new BusTrackerApp(new FakeBusApi());

        assertEquals(emptyMap(), app.mapState());
    }

    @Test
    public void requestsABusRouteWhenAdded() {
        FakeBusApi fakeBusApi = new FakeBusApi();
        BusTrackerApp app = new BusTrackerApp(fakeBusApi);

        app.addRoute(ImmutableAddRouteRequest.of("47"));

        fakeBusApi.sendResponse(ImmutableBusRoute.of("47", new ArrayList<RoutePath>()));

        assertEquals(1, app.mapState().routes().size());
        assertEquals("47", app.mapState().routes().iterator().next().routeName());
    }
}