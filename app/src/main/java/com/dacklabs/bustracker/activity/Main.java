package com.dacklabs.bustracker.activity;

import android.os.Bundle;

import com.dacklabs.bustracker.application.AppLogger;
import com.dacklabs.bustracker.application.BusApi;
import com.dacklabs.bustracker.application.RouteDatabase;
import com.dacklabs.bustracker.application.RouteList;
import com.dacklabs.bustracker.application.requests.Message;
import com.dacklabs.bustracker.http.DataSyncProcess;
import com.dacklabs.bustracker.http.HttpService;
import com.dacklabs.bustracker.http.NextBusApi;
import com.dacklabs.bustracker.http.ProcessRunner;
import com.google.common.eventbus.AsyncEventBus;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

final class Main implements ActivityLifecycle {

    private ScheduledExecutorService executor;
    private BusRouteMapActivity activity;
    private HttpService http;
    private MapBoxUI ui;
    private BusApi busApi;
    private RouteList routeList;
    private AsyncEventBus eventBus;
    private RouteDatabase db;
    private DataSyncProcess dataSyncProcess;
    private AndroidStorage storage;

    public void setActivity(BusRouteMapActivity busRouteMap) {
        this.activity = busRouteMap;
    }

    public void postMessage(Message message) {
        eventBus.post(message);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        log("onCreate");
        executor = Executors.newScheduledThreadPool(10);
        eventBus = new AsyncEventBus("events", executor);

        http = new HttpService(new OkHttpClient());
        busApi = new NextBusApi(http);

        storage = new AndroidStorage(activity);

        routeList = new RouteList(eventBus);
        routeList.load(storage);
        eventBus.register(routeList);

        db = new RouteDatabase();

        dataSyncProcess = new DataSyncProcess(routeList, db, busApi, new ExecutorProcessRunner(executor));

        ui = new MapBoxUI(activity);
        ui.onCreate(savedInstanceState);
        db.registerListener(ui);
    }

    @Override
    public void onStart() {
        log("onStart");
        executor.execute(dataSyncProcess::startSyncingProcess);
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
        routeList.save(storage);
        dataSyncProcess.stopSyncing();
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
        AppLogger.info(this, message);
    }

    private static final class ExecutorProcessRunner implements ProcessRunner {

        private final ScheduledExecutorService executor;

        private ExecutorProcessRunner(ScheduledExecutorService executor) {
            this.executor = executor;
        }

        @Override
        public ListenableFuture<?> execute(Runnable process) {
            return MoreExecutors.listeningDecorator(executor).schedule(process, 0, TimeUnit.MILLISECONDS);
        }
    }
}
