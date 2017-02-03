package com.dacklabs.bustracker.http;

import android.util.Log;

import com.dacklabs.bustracker.application.QueryResult;
import com.dacklabs.bustracker.application.RouteDatabase;
import com.dacklabs.bustracker.application.RouteList;
import com.dacklabs.bustracker.application.RouteName;
import com.dacklabs.bustracker.application.requests.ImmutableQueryBusLocations;
import com.dacklabs.bustracker.application.requests.ImmutableQueryBusRoute;
import com.dacklabs.bustracker.data.BusLocations;
import com.dacklabs.bustracker.data.BusRoute;
import com.google.common.base.Optional;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

public final class DataSyncProcess {

    public static final int SYNC_FREQUENCY = 10000;

    private final RouteList routeList;
    private final RouteDatabase db;
    private final NextBusApi api;
    private final ProcessRunner runner;
    private final AtomicBoolean isRunning = new AtomicBoolean(true);
    private final ConcurrentHashMap<RouteName, String> lastQueryTimes = new ConcurrentHashMap<>();

    public DataSyncProcess(RouteList routeList, RouteDatabase db, NextBusApi api, ProcessRunner
            runner) {
        this.routeList = routeList;
        this.db = db;
        this.api = api;
        this.runner = runner;
    }

    public interface ProcessRunner {
        ListenableFuture<?> execute(Runnable process);
    }

    public void stopSyncing() {
        log("Got a shutdown request, shutting down");
        isRunning.set(false);
    }

    public synchronized void startSyncingProcess() {
        log("Starting up data syncing process");
        isRunning.set(true);
        while (isRunning.get()) {
            List<ListenableFuture<?>> processes = new ArrayList<>();
            for (RouteName routeName : routeList.routes()) {
                if (!db.hasRoute(routeName)) {
                    log(String.format("Route %s not in database, syncing...",
                            routeName.displayName()));
                    processes.add(runner.execute(new QueryRoutes(routeName)));
                }
                log(String.format("Syncing bus locations for route %s", routeName.displayName()));
                processes.add(runner.execute(new QueryLocations(routeName)));
            }
            sleep();
            waitForProcessesToFinish(processes);
        }
    }

    private final class QueryLocations implements Runnable {
        private final RouteName routeName;

        private QueryLocations(RouteName routeName) {
            this.routeName = routeName;
        }

        @Override
        public void run() {
            ImmutableQueryBusLocations request = ImmutableQueryBusLocations.of("sf-muni", routeName,
                    Optional.fromNullable(lastQueryTimes.get(routeName)));
            QueryResult<BusLocations> locationData = api.queryBusLocationsFor(request);
            if (locationData.succeeded()) {
                db.updateLocations(locationData.result);
                lastQueryTimes.put(routeName, locationData.result.lastQueryTime());
            } else {
                log("Got error while querying for locations: " + locationData.failureMessage);
            }
        }
    }

    private final class QueryRoutes implements Runnable {
        private final RouteName routeName;

        private QueryRoutes(RouteName routeName) {
            this.routeName = routeName;
        }

        @Override
        public void run() {
            QueryResult<BusRoute> routeData = api.queryBusRouteFor(ImmutableQueryBusRoute.of("sf-muni", routeName));
            if (routeData.succeeded()) {
                db.updateRoute(routeData.result);
            } else {
                log("Got error while querying for routes: " + routeData.failureMessage);
            }
        }
    }

    private void waitForProcessesToFinish(List<ListenableFuture<?>> processes) {
        try {
            Futures.allAsList(processes).get(3000, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            isRunning.set(false);
            logE("Thread interrupted while waiting on HTTP", e);
        } catch (ExecutionException e) {
            logE("Exception waiting on HTTP", e);
        } catch (TimeoutException e) {
            logE("Timed out waiting for HTTP request, continuing", e);
        }
    }

    private void sleep() {
        log("Sleeping for " + SYNC_FREQUENCY + "ms");
        try {
            Thread.sleep(SYNC_FREQUENCY);
        } catch (InterruptedException e) {
            isRunning.set(false);
            logE("Thread interrupted, shuttong down", e);
        }
    }

    private void logE(String message, Throwable e) {
        Log.e("DACK:DataSyncProcess", message, e);
    }

    private void log(String message) {
        Log.d("DACK:DataSyncProcess", message);
    }
}
