package com.dacklabs.bustracker.activity;

import android.Manifest;
import android.os.Bundle;

import com.dacklabs.bustracker.R;
import com.dacklabs.bustracker.application.requests.BusLocationsUpdated;
import com.dacklabs.bustracker.application.requests.BusRouteUpdated;
import com.dacklabs.bustracker.application.requests.Message;
import com.dacklabs.bustracker.mapbox.MapBoxRouteUIElements;
import com.google.common.collect.Sets;
import com.mapbox.mapboxsdk.MapboxAccountManager;
import com.mapbox.mapboxsdk.maps.MapView;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

final class MapBoxUI {

    public static final HashSet<Message> EMPTY_MESSAGES = Sets.newHashSet();
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

    public Set<Message> onBusLocationsUpdated(BusLocationsUpdated locationsUpdated) {
        this.elements.ifPresent(e -> e.updateBusses(locationsUpdated.busses()));
        return Message.NONE;
    }

    public Set<Message> onBusRouteUpdated(BusRouteUpdated routeUpdated) {
        this.elements.ifPresent(e -> e.updateRoute(routeUpdated.route()));
        return Message.NONE;
    }

    public void onResume() {
        this.mapView.ifPresent(MapView::onResume);
    }

    public void onPause() {
        this.mapView.ifPresent(MapView::onPause);
    }

    public void onDestroy() {
        this.mapView.ifPresent(MapView::onDestroy);
    }

    public void onSaveInstanceState(Bundle outState) {
        this.mapView.ifPresent(mv -> mv.onSaveInstanceState(outState));
    }

    public void onLowMemory() {
        this.mapView.ifPresent(MapView::onLowMemory);
    }
}
