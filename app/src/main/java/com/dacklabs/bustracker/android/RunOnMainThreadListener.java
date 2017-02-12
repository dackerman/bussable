package com.dacklabs.bustracker.android;

import android.app.Activity;

import com.dacklabs.bustracker.application.RouteDatabase;
import com.dacklabs.bustracker.data.BusLocationsAvailable;
import com.dacklabs.bustracker.data.BusRouteUpdated;
import com.dacklabs.bustracker.data.RouteRemoved;

public final class RunOnMainThreadListener implements RouteDatabase.Listener {

    private final Activity activity;
    private final RouteDatabase.Listener otherListener;

    public RunOnMainThreadListener(Activity activity, RouteDatabase.Listener otherListener) {
        this.activity = activity;
        this.otherListener = otherListener;
    }

    @Override
    public void onBusLocationsUpdated(BusLocationsAvailable locationsUpdated) {
        activity.runOnUiThread(() -> otherListener.onBusLocationsUpdated(locationsUpdated));
    }

    @Override
    public void onBusRouteUpdated(BusRouteUpdated routeUpdated) {
        activity.runOnUiThread(() -> otherListener.onBusRouteUpdated(routeUpdated));
    }

    @Override
    public void onBusRouteRemoved(RouteRemoved message) {
        activity.runOnUiThread(() -> otherListener.onBusRouteRemoved(message));
    }
}
