package com.dacklabs.bustracker.application;

import com.dacklabs.bustracker.application.fakes.FakeBusApi;
import com.dacklabs.bustracker.application.requests.ImmutableAddRouteRequest;
import com.dacklabs.bustracker.data.ImmutableBusLocations;
import com.dacklabs.bustracker.data.ImmutableBusRoute;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class BusTrackerAppTest {

    private static ExecutorService es;
    private SynchronousQueue<Object> finishChan = new SynchronousQueue<>();
    private BlockingDeque<Throwable> errorChan = new LinkedBlockingDeque<>();

    private long expect;

    private MapState emptyMap() {
        return ImmutableMapState.builder().build();
    }

    @BeforeClass
    public static void startupThreadpool() {
        es = Executors.newFixedThreadPool(2);
    }

    @AfterClass
    public static void shutdownThreadpool() throws InterruptedException {
        es.shutdown();
        es.awaitTermination(10, TimeUnit.SECONDS);
    }

    @Test
    public void initiallyHasAnEmptyMap() {
        BusTrackerApp app = new BusTrackerApp(new FakeBusApi(), null, null);
        assertEquals(emptyMap(), app.mapState());
    }

    @Test
    public void requestsABusRouteWhenAdded() throws ExecutionException, InterruptedException {
        FakeBusApi fakeBusApi = new FakeBusApi();
        BusTrackerApp app = new BusTrackerApp(fakeBusApi, null, null);

        runApp(() -> app.addRoute(ImmutableAddRouteRequest.of(ImmutableRouteName.of("47"))));

        fakeBusApi.sendBusRouteResponse(ImmutableBusRoute.of(ImmutableRouteName.of("47"), new
                ArrayList<>()));

        finish();

        assertEquals(1, app.mapState().routes().size());
        assertEquals("47", app.mapState().routes().iterator().next().routeName().displayName());
    }

    @Ignore
    @Test
    public void periodicallyRequestsLocationAfterAddingRoute() throws ExecutionException, InterruptedException {
        FakeBusApi fakeBusApi = new FakeBusApi();
        BusTrackerApp app = new BusTrackerApp(fakeBusApi, null, null);
        fakeBusApi.sendLocationResponse(ImmutableBusLocations.builder()
                .route(ImmutableRouteName.of("10"))
                .lastQueryTime("1")
                .build());

        app.addRoute(ImmutableAddRouteRequest.of(ImmutableRouteName.of("10")));



    }

    @FunctionalInterface
    private interface Coroutine<T> {
        T run() throws Exception;
    }

    @FunctionalInterface
    private interface VoidCoroutine {
        void run() throws Exception;
    }

    private SynchronousQueue<Object> runApp(VoidCoroutine coroutine) {
        return runApp(() -> {
            coroutine.run();
            return new Object();
        });
    }

    private <T> SynchronousQueue<T> runApp(Coroutine<T> coroutine) {
        expect++;
        SynchronousQueue<T> chan = new SynchronousQueue<>();
        es.execute(() -> {
            try {
                chan.offer(coroutine.run());
                finishChan.add(new Object());
            } catch (Exception e) {
                errorChan.add(e);
            }
        });
        return chan;
    }

    private void finish() throws InterruptedException {
        if (!errorChan.isEmpty()) {
            StringWriter stringWriter = new StringWriter();
            PrintWriter pr = new PrintWriter(stringWriter, true);
            errorChan.poll().printStackTrace(pr);
            fail("App threw exception: " + stringWriter);
        }
        while (expect-- > 0) {
            finishChan.poll(5, TimeUnit.SECONDS);
        }
    }
}
