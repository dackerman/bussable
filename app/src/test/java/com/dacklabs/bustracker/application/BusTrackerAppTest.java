package com.dacklabs.bustracker.application;

import com.dacklabs.bustracker.application.fakes.FakeBusApi;
import com.dacklabs.bustracker.application.fakes.FakeTime;
import com.dacklabs.bustracker.application.requests.ImmutableAddRouteRequest;
import com.dacklabs.bustracker.data.ImmutableBusLocations;
import com.dacklabs.bustracker.data.ImmutableBusRoute;
import com.dacklabs.bustracker.data.RoutePath;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class BusTrackerAppTest {

    private FakeBusApi fakeBusApi;
    private FakeTime fakeTime;
    private BusTrackerApp app;

    private MapState emptyMap() {
        return ImmutableMapState.builder().build();
    }

    @Before
    public void beforeTest() {
        this.fakeBusApi = new FakeBusApi();
        this.fakeTime = new FakeTime();
        this.app = new BusTrackerApp(fakeBusApi, fakeTime);
    }

    @Test
    public void initiallyHasAnEmptyMap() {
        assertEquals(emptyMap(), app.mapState());
    }

    @Test
    public void requestsABusRouteWhenAdded() {
        app.addRoute(ImmutableAddRouteRequest.of("47"));

        fakeTime.moveForwardInSeconds(5);

        fakeBusApi.sendBusRouteResponse(ImmutableBusRoute.of("47", new ArrayList<RoutePath>()));

        assertEquals(1, app.mapState().routes().size());
        assertEquals("47", app.mapState().routes().iterator().next().routeName());
    }

    @Ignore
    @Test
    public void periodicallyRequestsLocationAfterAddingRoute() {
        app.addRoute(ImmutableAddRouteRequest.of("10"));

        fakeBusApi.sendLocationResponse(ImmutableBusLocations.builder()
                .route("10")
                .lastQueryTime("1")
                .build());

        fakeBusApi.sendLocationResponse(ImmutableBusLocations.builder()
                .route("10")
                .lastQueryTime("3")
                .build());



    }
}