package com.dacklabs.bustracker.activity;

import android.os.Bundle;
import android.util.Log;

import com.dacklabs.bustracker.application.MessageBus;
import com.dacklabs.bustracker.application.RouteList;
import com.dacklabs.bustracker.application.requests.AddRouteRequest;
import com.dacklabs.bustracker.application.requests.BusLocationsUpdated;
import com.dacklabs.bustracker.application.requests.BusRouteUpdated;
import com.dacklabs.bustracker.application.requests.ImmutableAddRouteRequest;
import com.dacklabs.bustracker.application.requests.ImmutableBusLocationsUpdated;
import com.dacklabs.bustracker.application.requests.ImmutableExceptionMessage;
import com.dacklabs.bustracker.application.requests.ImmutableQueryBusLocations;
import com.dacklabs.bustracker.application.requests.Message;
import com.dacklabs.bustracker.application.requests.QueryBusLocations;
import com.dacklabs.bustracker.data.BusLocations;
import com.dacklabs.bustracker.http.HttpService;
import com.dacklabs.bustracker.http.NextBusApi;
import com.dacklabs.bustracker.http.QueryResult;
import com.google.common.base.Optional;
import com.google.common.collect.Sets;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

final class Main implements ActivityLifecycle {

    private final MessageBus<Message> messageBus = new MessageBus<>(e -> ImmutableExceptionMessage.of(e, e.getMessage()));
    private final RouteList routeList = new RouteList();
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(5);
    private BusRouteMapActivity activity;
    private HttpService http;
    private MapBoxUI ui;
    private NextBusApi nextBusApi;

    Main() {
        messageBus.register(AddRouteRequest.class, routeList::requestRoute);
    }

    public void setActivity(BusRouteMapActivity busRouteMap) {
        this.activity = busRouteMap;
        executor.execute(messageBus::startProcessingMessages);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        log("onCreate");
        ui = new MapBoxUI(activity);
        ui.onCreate(savedInstanceState);
        messageBus.register(BusRouteUpdated.class, ui::onBusRouteUpdated);
        messageBus.register(BusLocationsUpdated.class, ui::onBusLocationsUpdated);

        http = new HttpService(new OkHttpClient());
        nextBusApi = new NextBusApi(http);

        messageBus.register(AddRouteRequest.class, request -> Sets.newHashSet(
                ImmutableQueryBusLocations.of("sf-muni", request.routeNumber(), Optional.absent())));

        messageBus.register(QueryBusLocations.class, (query) -> {
            executor.execute(() -> {
                QueryResult<BusLocations> result = nextBusApi.queryBusLocationsFor(query);
                if (result.succeeded()) {
                    messageBus.handle(ImmutableBusLocationsUpdated.of(result.result));
                } else {
                    messageBus.handle(ImmutableExceptionMessage.of(null, result.failureMessage));
                }
            });
            return Message.NONE;
        });

        executor.schedule(() -> messageBus.handle(ImmutableAddRouteRequest.of("10")), 10,
                TimeUnit.SECONDS);
    }

    @Override
    public void onStart() {
        log("onStart");
    }

    @Override
    public void onResume() {
        log("onResume");
        ui.onResume();
    }

    @Override
    public void onPause() {
        log("onPause");
        ui.onPause();
    }

    @Override
    public void onStop() {
        log("onStop");
        http.cancelInFlightRequests();
    }

    @Override
    public void onRestart() {
        log("onRestart");

    }

    @Override
    public void onDestroy() {
        log("onDestroy");
        ui.onDestroy();
        messageBus.stopProcessingMessages();
        executor.shutdown();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        log("onSaveInstanceState");
        ui.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        log("onLowMemory");
        ui.onLowMemory();
    }

    private void log(String message) {
        Log.d("Main App", message);
    }
}
