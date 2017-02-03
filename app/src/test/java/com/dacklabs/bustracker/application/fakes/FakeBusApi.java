package com.dacklabs.bustracker.application.fakes;

import com.dacklabs.bustracker.application.BusApi;
import com.dacklabs.bustracker.application.QueryResult;
import com.dacklabs.bustracker.application.RouteName;
import com.dacklabs.bustracker.data.BusLocations;
import com.dacklabs.bustracker.data.BusRoute;
import com.dacklabs.bustracker.util.Async;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

import java.util.ArrayList;
import java.util.List;

public final class FakeBusApi implements BusApi {

    private static class Buffer<T> {

        private List<SettableFuture<T>> requests = new ArrayList<>();
        private List<ListenableFuture<T>> responses = new ArrayList<>();

        synchronized void provideValue(T value) {
            if (requests.isEmpty()) {
                responses.add(Futures.immediateFuture(value));
            } else {
                requests.remove(0).set(value);
            }
        }

        synchronized ListenableFuture<T> getValue() {
            if (responses.isEmpty()) {
                SettableFuture<T> theFuture = SettableFuture.create();
                requests.add(theFuture);
                return theFuture;
            } else {
                return responses.remove(0);
            }
        }
    }


    private final Buffer<QueryResult<BusRoute>> routeBuffer = new Buffer<>();
    private final Buffer<QueryResult<BusLocations>> locationBuffer = new Buffer<>();

    @Override
    public Async<QueryResult<BusRoute>> getRoute(RouteName routeNumber) {
        return new Async<>(routeBuffer.getValue(), (a) -> null);
    }

    public void sendBusRouteResponse(BusRoute busRoute) {
        routeBuffer.provideValue(QueryResult.success(busRoute));
    }

    public void sendLocationResponse(BusLocations locations) {
        locationBuffer.provideValue(QueryResult.success(locations));
    }
}
