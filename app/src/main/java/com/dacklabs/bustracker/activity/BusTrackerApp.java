package com.dacklabs.bustracker.activity;

import com.dacklabs.bustracker.application.AppLogger;
import com.dacklabs.bustracker.application.ApplicationMap;
import com.dacklabs.bustracker.application.RouteDatabase;
import com.dacklabs.bustracker.application.RouteList;
import com.dacklabs.bustracker.application.Storage;
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

final class BusTrackerApp {

    private ScheduledExecutorService executor;
    private HttpService http;
    private NextBusApi nextBusApi;
    private RouteList routeList;
    private AsyncEventBus appEvents;
    private RouteDatabase db;
    private DataSyncProcess dataSyncProcess;
    private Storage storage;

    public void postMessage(Message message) {
        appEvents.post(message);
    }

    public void initialize(ApplicationMap map, Storage storage, RunOnMainThreadListener.Factory
            mainThreadListenerFactory) {
        log("initializing");
        executor = Executors.newScheduledThreadPool(10);
        appEvents = new AsyncEventBus("events", executor);

        http = new HttpService(new OkHttpClient());
        nextBusApi = new NextBusApi(http);

        this.storage = storage;

        routeList = new RouteList(appEvents);
        routeList.load(storage);
        appEvents.register(routeList);
        map.setRouteList(routeList);

        db = new RouteDatabase();

        dataSyncProcess = new DataSyncProcess(routeList, db, nextBusApi, new ExecutorProcessRunner(executor));

        db.listen(mainThreadListenerFactory.wrap(map));
    }

    public void show() {
        executor.execute(dataSyncProcess::startSyncingProcess);
    }

    public void save() {
        routeList.save(storage);
    }

    public void hide() {
        dataSyncProcess.stopSyncing();
        http.cancelInFlightRequests();
    }

    public void shutdown() {
        executor.shutdown();
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
