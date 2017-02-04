package com.dacklabs.bustracker.activity;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.util.Log;

import com.dacklabs.bustracker.application.ApplicationMap;
import com.dacklabs.bustracker.application.RouteList;
import com.dacklabs.bustracker.application.requests.BusLocationsAvailable;
import com.dacklabs.bustracker.application.requests.BusRouteUpdated;
import com.dacklabs.bustracker.application.requests.RouteRemoved;
import com.dacklabs.bustracker.data.BusLocation;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class BusRouteGoogleMapView implements ApplicationMap, OnMapReadyCallback {

    private GoogleMap map;
    private final Map<RouteName, Map<String, Marker>> routeMarkers = new HashMap<>();
    private final Map<RouteName, List<Polyline>> routes = new HashMap<>();

    private RouteList routeList;
    private Bitmap icon;

    @Override
    public void setRouteList(RouteList routeList) {
        this.routeList = routeList;
    }

    public void setIcon(Bitmap icon) {
        this.icon = icon;
    }

    private String makeTitle(RouteName routeName, BusLocation location) {
        String route = routeName.displayName();
        String number = location.vehicleId();
        String direction = location.direction().name();
        return String.format("%s-%s (%s)", route, number, direction);
    }

    @Override
    public void onBusLocationsUpdated(BusLocationsAvailable locationsUpdated) {
        if (map == null) return;
        if (!routeList.routeIsSelected(locationsUpdated.routeName())) return;

        Map<String, Marker> markers = getOrCreateMarkersForRoute(locationsUpdated.routeName());

        Map<String, BusLocation> locations = locationsUpdated.locations();
        for (String busId : locations.keySet()) {
            BusLocation location = locations.get(busId);
            Marker existingMarker = markers.get(busId);
            if (existingMarker == null) {
                existingMarker = map.addMarker(new MarkerOptions()
                        .position(new LatLng(location.latitude(), location.longitude()))
                        .icon(BitmapDescriptorFactory.fromBitmap(icon))
                        .title(makeTitle(locationsUpdated.routeName(), location)));
                markers.put(busId, existingMarker);
            } else {
                existingMarker.setPosition(new LatLng(location.latitude(), location.longitude()));
            }
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

    @Override
    public void onBusRouteUpdated(BusRouteUpdated routeUpdated) {
        RouteName routeName = routeUpdated.route().routeName();
        if (map == null) return;
        if (!routeList.routeIsSelected(routeName)) return;
        removePolylinesForRoute(routeName);

        List<Polyline> polylines = new ArrayList<>();
        for (RoutePath routePath : routeUpdated.route().paths()) {
            PolylineOptions options = new PolylineOptions()
                    .color(Color.GREEN);
            for (PathCoordinate coord : routePath.coordinates()) {
                options.add(new LatLng(coord.lat(), coord.lon()));
            }
            polylines.add(map.addPolyline(options));
        }
        routes.put(routeName, polylines);
    }

    @Override
    public void onBusRouteRemoved(RouteRemoved routeRemoved) {
        if (map == null) return;
        if (routeList.routeIsSelected(routeRemoved.routeName())) return;
        removeMarkersForRoute(routeRemoved);
        removePolylinesForRoute(routeRemoved.routeName());
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        log("onMapReady");
        map = googleMap;
        LatLng sf = new LatLng(37.753205555699026, -122.4262607254285);
        map.moveCamera(CameraUpdateFactory.newLatLng(sf));
        map.moveCamera(CameraUpdateFactory.zoomTo(12));
    }

    private int log(String message) {
        return Log.d("DACK:MapView", message);
    }
}
