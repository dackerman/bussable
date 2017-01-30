package com.dacklabs.bustracker.application.fakes;

import com.dacklabs.bustracker.application.BusApi;
import com.dacklabs.bustracker.data.BusLocations;
import com.dacklabs.bustracker.data.BusRoute;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

import java.util.ArrayList;
import java.util.List;

public final class FakeBusApi implements BusApi {

    private static class Buffer<T> {

        private List<SettableFuture<T>> requests = new ArrayList<>();
        private List<ListenableFuture<T>> responses = new ArrayList<>();

        public void provideValue(T value) {
            if (requests.isEmpty()) {
                responses.add(Futures.immediateFuture(value));
            } else {
                requests.remove(0).set(value);
            }
        }

        public ListenableFuture<T> getValue() {
            if (responses.isEmpty()) {
                SettableFuture<T> theFuture = SettableFuture.create();
                requests.add(theFuture);
                return theFuture;
            } else {
                return responses.remove(0);
            }
        }
    }


    private final Buffer<ApiResult<BusRoute>> routeBuffer = new Buffer<>();
    private final Buffer<ApiResult<BusLocations>> locationBuffer = new Buffer<>();

    @Override
    public ListenableFuture<ApiResult<BusRoute>> getRoute(String routeNumber) {
        return routeBuffer.getValue();
    }

    public void sendBusRouteResponse(BusRoute busRoute) {
        routeBuffer.provideValue(new ApiResult<>(busRoute));
    }

    public void sendLocationResponse(BusLocations locations) {
        locationBuffer.provideValue(new ApiResult<>(locations));
    }
}
