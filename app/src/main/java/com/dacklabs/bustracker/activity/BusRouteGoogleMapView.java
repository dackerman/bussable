package com.dacklabs.bustracker.activity;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.support.annotation.NonNull;

import com.dacklabs.bustracker.application.AppLogger;
import com.dacklabs.bustracker.application.RouteDatabase;
import com.dacklabs.bustracker.application.RouteList;
import com.dacklabs.bustracker.data.BusLocationsAvailable;
import com.dacklabs.bustracker.data.BusRouteUpdated;
import com.dacklabs.bustracker.data.RouteRemoved;
import com.dacklabs.bustracker.data.BusLocation;
import com.dacklabs.bustracker.data.BusRoute;
import com.dacklabs.bustracker.data.PathCoordinate;
import com.dacklabs.bustracker.data.RouteName;
import com.dacklabs.bustracker.data.RoutePath;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.common.base.Optional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class BusRouteGoogleMapView implements RouteDatabase.Listener, OnMapReadyCallback {

    private GoogleMap map;
    private final Map<RouteName, Map<String, Marker>> routeMarkers = new HashMap<>();
    private final Map<RouteName, List<Polyline>> routes = new HashMap<>();
    private Marker userPosition;

    private final RouteList routeList;
    private final RouteDatabase db;
    private final Bitmap icon;

    public BusRouteGoogleMapView(RouteList routeList, RouteDatabase db, Bitmap icon) {
        this.routeList = routeList;
        this.db = db;
        this.icon = icon;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        log("onMapReady");
        map = googleMap;
        LatLng sf = new LatLng(37.753205555699026, -122.4262607254285);
        map.moveCamera(CameraUpdateFactory.newLatLng(sf));
        map.moveCamera(CameraUpdateFactory.zoomTo(12));

        initializeMapWithCachedData();
    }

    @Override
    public void onBusLocationsUpdated(BusLocationsAvailable locationsUpdated) {
        updateBusLocations(locationsUpdated.routeName(), locationsUpdated.locations());
    }

    @Override
    public void onBusRouteUpdated(BusRouteUpdated routeUpdated) {
        updateRoute(routeUpdated.route());
    }

    @Override
    public void onBusRouteRemoved(RouteRemoved routeRemoved) {
        if (map == null) return;
        if (routeList.routeIsSelected(routeRemoved.routeName())) return;
        removeMarkersForRoute(routeRemoved);
        removePolylinesForRoute(routeRemoved.routeName());
    }

    public void updateUserLocation(Location location) {
        LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        if (userPosition != null) {
            userPosition.setPosition(userLatLng);
        } else {
            userPosition = map.addMarker(new MarkerOptions().position(userLatLng));
        }
    }

    public void removeUserLocation() {
        if (userPosition != null) {
            userPosition.remove();
            userPosition = null;
        }
    }

    private void initializeMapWithCachedData() {
        log("initializing map with cached data");
        for (RouteName routeName : routeList.routes()) {
            Optional<BusRoute> maybeRoute = db.getRoute(routeName);
            if (maybeRoute.isPresent()) {
                updateRoute(maybeRoute.get());
            }
            Map<String, BusLocation> locations = db.queryLocations(routeName);
            if (!locations.isEmpty()) {
                updateBusLocations(routeName, locations);
            }
        }
    }

    private String makeTitle(RouteName routeName, BusLocation location) {
        String route = routeName.displayName();
        String number = location.vehicleId();
        String direction = location.direction().name();
        return String.format("%s-%s (%s)", route, number, direction);
    }

    private void updateBusLocations(RouteName routeName, Map<String, BusLocation> locations) {
        if (map == null) return;
        if (!routeList.routeIsSelected(routeName)) return;

        Map<String, Marker> markers = getOrCreateMarkersForRoute(routeName);

        for (String busId : locations.keySet()) {
            BusLocation location = locations.get(busId);
            Marker existingMarker = markers.get(busId);
            if (existingMarker == null) {
                existingMarker = map.addMarker(new MarkerOptions()
                        .position(new LatLng(location.latitude(), location.longitude()))
                        .icon(BitmapDescriptorFactory.fromBitmap(icon))
                        .title(makeTitle(routeName, location)));
                markers.put(busId, existingMarker);
            } else {
                existingMarker.setPosition(new LatLng(location.latitude(), location.longitude()));
            }
        }
    }

    private void updateRoute(BusRoute route) {
        RouteName routeName = route.routeName();
        if (map == null) return;
        if (!routeList.routeIsSelected(routeName)) return;
        removePolylinesForRoute(routeName);

        List<Polyline> polylines = new ArrayList<>();
        for (RoutePath routePath : route.paths()) {
            PolylineOptions options = new PolylineOptions()
                    .color(colorForRoute(route.routeName()));
            for (PathCoordinate coord : routePath.coordinates()) {
                options.add(new LatLng(coord.lat(), coord.lon()));
            }
            polylines.add(map.addPolyline(options));
        }
        routes.put(routeName, polylines);
    }

    private int colorForRoute(RouteName route) {
        // TODO: make this dynamic at some point
        switch (route.displayName()) {
            case "10":
                return Color.RED;
            case "47":
                return Color.BLUE;
            case "19":
                return Color.GREEN;
            case "12":
                return Color.CYAN;
            default:
                return Color.GREEN;
        }
    }

    @NonNull
    private Map<String, Marker> getOrCreateMarkersForRoute(RouteName routeName) {
        Map<String, Marker> markers = routeMarkers.get(routeName);
        if (markers == null) {
            markers = new HashMap<>();
            routeMarkers.put(routeName, markers);
        }
        return markers;
    }

    private void removeMarkersForRoute(RouteRemoved routeRemoved) {
        Map<String, Marker> markers = routeMarkers.get(routeRemoved.routeName());
        if (markers == null) return;
        for (Marker marker : markers.values()) {
            marker.remove();
        }
    }

    private void removePolylinesForRoute(RouteName routeName) {
        List<Polyline> polylines = routes.get(routeName);
        if (polylines == null) return;
        for (Polyline polyline : polylines) {
            polyline.remove();
        }
    }

    private void log(String message) {
        AppLogger.info(this, message);
    }
}
