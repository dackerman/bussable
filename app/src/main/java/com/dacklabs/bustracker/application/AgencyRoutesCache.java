package com.dacklabs.bustracker.application;

import com.dacklabs.bustracker.data.AgencyRoutes;
import com.dacklabs.bustracker.data.ImmutableAgencyRoutes;
import com.dacklabs.bustracker.data.QueryResult;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class AgencyRoutesCache {

    private final Map<String, AgencyRoutes> routeCache = new ConcurrentHashMap<>();
    private final BusApi busApi;
    private final ProcessRunner processRunner;
    private final AppLogger log;

    public AgencyRoutesCache(BusApi busApi, ProcessRunner processRunner, AppLogger log) {
        this.busApi = busApi;
        this.processRunner = processRunner;
        this.log = log;
    }

    public ListenableFuture<AgencyRoutes> routesForAgency(String agency) {
        AgencyRoutes agencyRoutes = routeCache.get(agency);
        if (agencyRoutes != null) {
            log.debug(this, "Already had agency list for %s, returning immediately", agency);
            return Futures.immediateFuture(agencyRoutes);
        }

        log.debug(this, "Querying for agency list %s not yet in cache", agency);
        SettableFuture<AgencyRoutes> future = SettableFuture.create();
        processRunner.execute(() -> {
            QueryResult<AgencyRoutes> result = busApi.queryProvider(agency);
            if (result.succeeded()) {
                log.debug(this, "Got agency list successfully, returning");
                routeCache.put(agency, result.result);
                future.set(result.result);
            }
            log.info(this, "Failed to get agency routes! error %s", result.failureMessage);
            future.set(ImmutableAgencyRoutes.of(agency, new ArrayList<>()));
        });
        return future;
    }
}
