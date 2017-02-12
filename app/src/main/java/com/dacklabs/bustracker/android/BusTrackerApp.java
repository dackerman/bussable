package com.dacklabs.bustracker.android;

import com.dacklabs.bustracker.application.AgencyRoutesCache;
import com.dacklabs.bustracker.application.AppLogger;
import com.dacklabs.bustracker.application.BusApi;
import com.dacklabs.bustracker.application.RouteDatabase;
import com.dacklabs.bustracker.application.RouteList;
import com.dacklabs.bustracker.application.Storage;
import com.dacklabs.bustracker.data.Message;
import com.dacklabs.bustracker.http.HttpService;
import com.dacklabs.bustracker.http.NextBusApi;
import com.dacklabs.bustracker.application.ProcessRunner;
import com.google.common.eventbus.AsyncEventBus;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public final class BusTrackerApp {

    public static final BusTrackerApp app = new BusTrackerApp();
    public static final int IDLE_THREAD_POOL_SIZE = 5;

    static {
        app.initialize();
    }

    private ScheduledExecutorService executor;
    private HttpService http;
    private BusApi busApi;
    private RouteList routeList;
    private AsyncEventBus appEvents;
    private RouteDatabase db;
    private AgencyRoutesCache agencyRoutesCache;
    private final AppLogger logger = new AndroidTimberSentryLogger();

    public void initialize() {
        log("initializing");
        executor = Executors.newScheduledThreadPool(IDLE_THREAD_POOL_SIZE);
        appEvents = new AsyncEventBus("events", executor);

        http = new HttpService(new OkHttpClient(), logger);
        busApi = new NextBusApi(http, logger);

        routeList = new RouteList(appEvents, logger);
        appEvents.register(routeList);

        db = new RouteDatabase(logger);

        agencyRoutesCache = new AgencyRoutesCache(busApi, new ExecutorProcessRunner(executor), logger);
    }

    public void postMessage(Message message) {
        appEvents.post(message);
    }

    public RouteDatabase db() {
        return db;
    }

    public RouteList routeList() {
        return routeList;
    }

    public AgencyRoutesCache agencyRoutesCache() {
        return agencyRoutesCache;
    }

    public void load(Storage storage) {
        routeList.load(storage);
    }

    public void execute(Runnable command) {
        executor.execute(command);
    }

    public void save(Storage storage) {
        routeList.save(storage);
    }

    public void shutdown() {
        executor.shutdown();
    }

    public BusApi nextBusApi() {
        return busApi;
    }

    private void log(String message) {
        logger.info(this, message);
    }

    public AppLogger logger() {
        return logger;
    }

    public ExecutorProcessRunner processRunner() {
        return new ExecutorProcessRunner(executor);
    }

    public static final class ExecutorProcessRunner implements ProcessRunner {

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
