package com.dacklabs.bustracker.activity;

import android.app.Activity;

import com.dacklabs.bustracker.application.RouteDatabase;
import com.dacklabs.bustracker.data.BusLocationsAvailable;
import com.dacklabs.bustracker.data.BusRouteUpdated;
import com.dacklabs.bustracker.data.RouteRemoved;

public final class RunOnMainThreadListener implements RouteDatabase.Listener {

    public static class Factory {
        private final Activity activity;

        public Factory(Activity activity) {
            this.activity = activity;
        }

        public RunOnMainThreadListener wrap(RouteDatabase.Listener otherListener) {
            return new RunOnMainThreadListener(activity, otherListener);
        }
    }

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
