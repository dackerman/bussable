package com.dacklabs.bustracker.mapbox;

import android.util.Log;

import com.dacklabs.bustracker.application.requests.BusLocationsAvailable;
import com.dacklabs.bustracker.data.BusLocation;
import com.dacklabs.bustracker.data.BusRoute;
import com.dacklabs.bustracker.data.Direction;
import com.dacklabs.bustracker.data.PathCoordinate;
import com.dacklabs.bustracker.data.RoutePath;
import com.dacklabs.bustracker.util.Supplier;
import com.google.gson.JsonObject;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.NoSuchLayerException;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.style.sources.NoSuchSourceException;
import com.mapbox.services.commons.geojson.Feature;
import com.mapbox.services.commons.geojson.FeatureCollection;
import com.mapbox.services.commons.geojson.LineString;
import com.mapbox.services.commons.geojson.Point;
import com.mapbox.services.commons.models.Position;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineCap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;

public class MapBoxRouteUIElements implements com.dacklabs.bustracker.application.BusRouteUIElements {
    private final MapboxMap map;

    public MapBoxRouteUIElements(MapboxMap map) {
        this.map = map;
    }

    @Override
    public void updateBusses(BusLocationsAvailable busses) {
        initRoute(busses.route());
        getSourceFor(locationId(busses.route())).setGeoJson(fromBusLocations(busses));
    }

    @Override
    public void updateRoute(BusRoute route) {
        initRoute(route.routeName());
        getSourceFor(routeId(route.routeName())).setGeoJson(fromBusRoute(route));
    }

    @Override
    public void removeRoute(String routeName) {
        removeLayer(locationId(routeName));
        removeSource(locationId(routeName));

        removeLayer(routeId(routeName));
        removeSource(routeId(routeName));
    }

    private FeatureCollection fromBusLocations(BusLocationsAvailable busses) {
        List<Feature> features = new ArrayList<>();
        Map<String, BusLocation> locations = busses.locations();
        for (String busIdentifier : locations.keySet()) {
            BusLocation location = locations.get(busIdentifier);
            String formattedTitle = String.format("%s-%s (%s)",
                    busses.route(), location.vehicleId(), location.direction().toString().toLowerCase());
            String color = location.direction().equals(Direction.INBOUND) ? "#00f" : "#ff0";

            JsonObject props = new JsonObject();
            props.addProperty("title", formattedTitle);
            props.addProperty("busColor", color);
            features.add(Feature.fromGeometry(Point.fromCoordinates(
                    Position.fromCoordinates(location.longitude(), location.latitude())), props));
        }

        return FeatureCollection.fromFeatures(features);
    }

    private FeatureCollection fromBusRoute(BusRoute route) {
        List<Feature> features = new ArrayList<>();
        for (RoutePath path : route.paths()) {
            List<Position> coordinates = new ArrayList<>();
            for (PathCoordinate coord : path.coordinates()) {
                coordinates.add(Position.fromCoordinates(coord.lon(), coord.lat()));
            }
            features.add(Feature.fromGeometry(LineString.fromCoordinates(coordinates)));
        }
        return FeatureCollection.fromFeatures(features);
    }

    private GeoJsonSource getSourceFor(String id) {
        return map.getSourceAs(id);
    }

    private String locationId(String routeName) {
        return routeName + "_bus-locations";
    }

    private String routeId(String routeName) {
        return routeName + "_bus-route";
    }

    private void initRoute(String routeName) {
        initLineLayer(routeId(routeName),
                lineCap("round"),
                lineColor("#000"),
                lineWidth(2f));

        initSymbolLayer(locationId(routeName),
                PropertyFactory.iconImage("bus-15"),
                PropertyFactory.iconAllowOverlap(true),
                PropertyFactory.textAllowOverlap(true),
//                PropertyFactory.iconColor("{busColor}"),
                PropertyFactory.fillColor("{busColor}"),
                PropertyFactory.textField("{title}"),
                PropertyFactory.iconSize(1f),
                PropertyFactory.textFont(new String[]{"Open Sans Semibold", "Arial Unicode MS Bold"}),
                PropertyFactory.textOffset(new Float[]{0f, 0.6f}),
                PropertyFactory.textAnchor("top"));
    }

    private void initLineLayer(String id, Property<?>... properties) {
        initRoute(id, () -> new LineLayer(id, id), properties);
    }

    private void initSymbolLayer(String id, Property<?>... properties) {
        initRoute(id, () -> new SymbolLayer(id, id), properties);
    }

    private void initRoute(String id, Supplier<Layer> layerCreator, Property<?>... properties) {
        if (map.getSource(id) == null) {
            log("creating source " + id);
            map.addSource(new GeoJsonSource(id));
        }
        if (map.getLayer(id) == null) {
            log("creating layer " + id);
            Layer layer = layerCreator.get();
            layer.setProperties(properties);
            map.addLayer(layer);
        }
    }

    private void removeLayer(String id) {
        log("attempting to remove layer " + id);
        if (map.getLayer(id) != null) {
            log("layer exists, removing " + id);
            try {
                map.removeLayer(id);
            } catch (NoSuchLayerException e) {
                log("Exception while removing layer " + id + " " + e.getMessage());
                // shouldn't happen
            }
        }
    }

    private void log(String message) {
        Log.d("DACK:MapElements", message);
    }

    private void removeSource(String id) {
        log("attempting to remove source " + id);
        if (map.getSource(id) != null) {
            log("source exists, removing " + id);
            try {
                map.removeSource(id);
            } catch (NoSuchSourceException e) {
                log("Exception while removing source " + id + " " + e.getMessage());
                // shouldn't happen
            }
        }
    }
}
