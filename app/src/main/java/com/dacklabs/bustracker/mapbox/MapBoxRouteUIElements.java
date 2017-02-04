package com.dacklabs.bustracker.mapbox;

import com.dacklabs.bustracker.application.AppLogger;
import com.dacklabs.bustracker.application.BusRouteUIElements;
import com.dacklabs.bustracker.application.requests.BusLocationsAvailable;
import com.dacklabs.bustracker.data.BusLocation;
import com.dacklabs.bustracker.data.BusRoute;
import com.dacklabs.bustracker.data.Direction;
import com.dacklabs.bustracker.data.PathCoordinate;
import com.dacklabs.bustracker.data.RouteName;
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

import org.immutables.value.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineCap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;

public class MapBoxRouteUIElements implements BusRouteUIElements {
    private final MapboxMap map;

    public MapBoxRouteUIElements(MapboxMap map) {
        this.map = map;
    }

    @Override
    public void updateBusses(BusLocationsAvailable busses) {
        initRoute(busses.routeName());
        getSourceFor(locationId(busses.routeName())).setGeoJson(fromBusLocations(busses));
    }

    @Override
    public void updateRoute(BusRoute route) {
        initRoute(route.routeName());
        getSourceFor(routeId(route.routeName())).setGeoJson(fromBusRoute(route));
    }

    @Override
    public void removeRoute(RouteName routeName) {
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
                    busses.routeName().displayName(), location.vehicleId(), location.direction().toString()
                            .toLowerCase());
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

    @Value.Immutable(builder = false, intern = true, copy = false)
    public static abstract class SourceID {
        @Value.Parameter
        public abstract String value();
    }

    @Value.Immutable(builder = false, intern = true, copy = false)
    public static abstract class LayerID {
        @Value.Parameter
        public abstract String value();
    }

    private String locationId(RouteName routeName) {
        return routeName.displayName() + "_bus-locations";
    }

    private String routeId(RouteName routeName) {
        return routeName.displayName() + "_bus-route";
    }

    private void initRoute(RouteName routeName) {
        ImmutableLayerID routeLayerId = ImmutableLayerID.of(routeId(routeName));
        ImmutableSourceID routeSourceId = ImmutableSourceID.of(routeId(routeName));
        ImmutableLayerID locationsLayerId = ImmutableLayerID.of(locationId(routeName));
        ImmutableSourceID locationsSourceId = ImmutableSourceID.of(locationId(routeName));

        initLayer(routeLayerId, routeSourceId,
                () -> new LineLayer(routeLayerId.value(), routeSourceId.value()),
                lineCap("round"),
                lineColor("#000"),
                lineWidth(2f));

        initLayer(locationsLayerId, locationsSourceId,
                () -> new SymbolLayer(locationsLayerId.value(), locationsSourceId.value()),
                PropertyFactory.iconImage("bus-15"),
                PropertyFactory.iconAllowOverlap(true),
                PropertyFactory.textAllowOverlap(true),
                PropertyFactory.fillColor("{busColor}"),
                PropertyFactory.textField("{title}"),
                PropertyFactory.iconSize(1f),
                PropertyFactory.textFont(new String[]{"Open Sans Semibold", "Arial Unicode MS Bold"}),
                PropertyFactory.textOffset(new Float[]{0f, 0.6f}),
                PropertyFactory.textAnchor("top"));
    }

    private void initLayer(LayerID layerId, SourceID sourceId, Supplier<Layer> layerCreator,
                           Property<?>... properties) {
        if (map.getSource(sourceId.value()) == null) {
            log("creating source " + sourceId.value());
            map.addSource(new GeoJsonSource(sourceId.value()));
        }
        if (map.getLayer(layerId.value()) == null) {
            log("creating layer " + layerId.value());
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
        AppLogger.info(this, message);
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
