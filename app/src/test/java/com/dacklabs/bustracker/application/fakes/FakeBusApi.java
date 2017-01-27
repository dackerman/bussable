package com.dacklabs.bustracker.application.fakes;

import com.dacklabs.bustracker.application.BusApi;
import com.dacklabs.bustracker.data.BusRoute;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

public final class FakeBusApi implements BusApi {


    private SettableFuture<ApiResult<BusRoute>> pendingResponse;

    public void sendResponse(BusRoute busRoute) {
        pendingResponse.set(new ApiResult<>(busRoute));
    }

    @Override
    public ListenableFuture<ApiResult<BusRoute>> getRoute(String routeNumber) {
        pendingResponse = SettableFuture.create();
        return pendingResponse;
    }
}
