package com.dacklabs.bustracker.mapbox;

import com.dacklabs.bustracker.data.BusLocation;
import com.dacklabs.bustracker.data.BusLocations;
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
    public void updateBusses(BusLocations busses) {
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

    private FeatureCollection fromBusLocations(BusLocations busses) {
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
        initSymbolLayer(locationId(routeName),
                PropertyFactory.iconImage("bus-15"),
                PropertyFactory.iconColor("{busColor}"),
                PropertyFactory.textField("{title}"),
                PropertyFactory.textFont(new String[]{"Open Sans Semibold", "Arial Unicode MS Bold"}),
                PropertyFactory.textOffset(new Float[]{0f, 0.6f}),
                PropertyFactory.textAnchor("top"));

        initLineLayer(routeId(routeName),
                lineCap("round"),
                lineColor("#f00"),
                lineWidth(4f));
    }

    private void initLineLayer(String id, Property<?>... properties) {
        initRoute(id, () -> new LineLayer(id, id), properties);
    }

    private void initSymbolLayer(String id, Property<?>... properties) {
        initRoute(id, () -> new SymbolLayer(id, id), properties);
    }

    private void initRoute(String id, Supplier<Layer> layerCreator, Property<?>... properties) {
        if (map.getSource(id) == null) {
            map.addSource(new GeoJsonSource(id));
        }
        if (map.getLayer(id) == null) {
            Layer layer = layerCreator.get();
            layer.setProperties(properties);
            map.addLayer(layer);
        }
    }

    private void removeLayer(String id) {
        if (map.getLayer(id) != null) {
            try {
                map.removeLayer(id);
            } catch (NoSuchLayerException e) {
                // shouldn't happen
            }
        }
    }

    private void removeSource(String id) {
        if (map.getSource(id) != null) {
            try {
                map.removeSource(id);
            } catch (NoSuchSourceException e) {
                // shouldn't happen
            }
        }
    }
}
