package com.dacklabs.bustracker.activity;

import android.Manifest;
import android.os.Bundle;

import com.dacklabs.bustracker.R;
import com.dacklabs.bustracker.application.RouteDatabase;
import com.dacklabs.bustracker.application.requests.BusLocationsAvailable;
import com.dacklabs.bustracker.application.requests.BusRouteUpdated;
import com.dacklabs.bustracker.application.requests.RouteRemoved;
import com.dacklabs.bustracker.mapbox.MapBoxRouteUIElements;
import com.dacklabs.bustracker.util.Consumer;
import com.google.common.base.Optional;
import com.mapbox.mapboxsdk.MapboxAccountManager;
import com.mapbox.mapboxsdk.maps.MapView;

final class MapBoxUI implements RouteDatabase.Listener {

    private final BusRouteMapActivity activity;
    private Optional<MapView> mapView;
    private Optional<MapBoxRouteUIElements> elements;

    public MapBoxUI(BusRouteMapActivity activity) {
        this.activity = activity;
    }

    public void onCreate(Bundle savedInstanceState) {
        activity.requirePerms(Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.INTERNET);

        MapboxAccountManager.start(activity, activity.getString(R.string.access_token));
        MapView mapView = (MapView) activity.findViewById(R.id.mapview);

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(mapboxMap ->
                this.elements = Optional.of(new MapBoxRouteUIElements(mapboxMap))
        );

        this.mapView = Optional.of(mapView);
    }

    @Override
    public void onBusLocationsUpdated(BusLocationsAvailable locationsUpdated) {
        ifPresent(elements, e ->
                activity.runOnUiThread(() -> e.updateBusses(locationsUpdated)));
    }

    @Override
    public void onBusRouteUpdated(BusRouteUpdated routeUpdated) {
        ifPresent(elements, e ->
                activity.runOnUiThread(() -> e.updateRoute(routeUpdated.route())));
    }

    @Override
    public void onBusRouteRemoved(RouteRemoved message) {
        ifPresent(elements, e ->
                activity.runOnUiThread(() -> e.removeRoute(message.routeNumber())));
    }

    public void onResume() {
        ifPresent(mapView, MapView::onResume);
    }

    public void onPause() {
        ifPresent(mapView, MapView::onPause);
    }

    public void onDestroy() {
        ifPresent(mapView, MapView::onDestroy);
    }

    public void onSaveInstanceState(Bundle outState) {
        ifPresent(mapView, mv -> mv.onSaveInstanceState(outState));
    }

    public void onLowMemory() {
        ifPresent(mapView, MapView::onLowMemory);
    }

    private <T> void ifPresent(Optional<T> opt, Consumer<T> cons) {
        if (opt != null && opt.isPresent()) {
            cons.accept(opt.get());
        }
    }
}
