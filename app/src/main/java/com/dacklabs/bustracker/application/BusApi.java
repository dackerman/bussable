package com.dacklabs.bustracker.application;

import com.dacklabs.bustracker.data.BusRoute;
import com.google.common.util.concurrent.ListenableFuture;

public interface BusApi {
    ListenableFuture<ApiResult<BusRoute>> getRoute(String routeNumber);

    class ApiResult<T> {
        private final T value;

        public ApiResult(T value) {
            this.value = value;
        }

        public T getResult() {
            return value;
        }
    }
}
