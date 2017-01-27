package com.dacklabs.bustracker.mapbox;

import android.support.annotation.Nullable;

import com.dacklabs.bustracker.data.BusLocation;
import com.dacklabs.bustracker.data.BusLocations;
import com.dacklabs.bustracker.data.BusRoute;
import com.dacklabs.bustracker.data.Direction;
import com.dacklabs.bustracker.data.PathCoordinate;
import com.dacklabs.bustracker.data.RoutePath;
import com.google.common.base.Function;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.gson.JsonObject;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.NoSuchLayerException;
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
import java.util.Collection;
import java.util.List;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineCap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;

/**
 * Created by dackerman on 1/22/2017.
 */
public class MapBoxRouteObjects {
    public final String route;
    private final String color;
    private final MapboxMap map;

    private GeoJsonSource busLocations;
    private GeoJsonSource busRoute;
    private GeoJsonSource busDirections;
    private LineLayer routeLayer;
    private LineLayer directionsLayer;
    private SymbolLayer locationsLayer;

    private final Multimap<String, BusLocation> locationCache = ArrayListMultimap.create();

    public MapBoxRouteObjects(String route, String color, MapboxMap map) {
        this.route = route;
        this.color = color;
        this.map = map;
    }

    public void updateBusses(BusLocations locations) {
        for (BusLocation busLocation : locations.locations()) {
            locationCache.put(busLocation.vehicleId(), busLocation);
        }

        Iterable<BusLocation> latestLocations = Iterables.transform(locationCache.asMap().values(), new Function<Collection<BusLocation>, BusLocation>() {
            @Nullable
            @Override
            public BusLocation apply(Collection<BusLocation> input) {
                return input.iterator().next();
            }
        });

        busLocations.setGeoJson(fromBusLocations(latestLocations));
    }

    private FeatureCollection fromBusLocations(Iterable<BusLocation> locations) {
        List<Feature> features = new ArrayList<>();
        for (BusLocation location : locations) {
            JsonObject props = new JsonObject();
            String color = location.direction().equals(Direction.INBOUND) ? "#00f" : "#ff0";
            props.addProperty("title", route + "-" + location.vehicleId() + " (" + location.direction().toString() + ")");
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
            for (PathCoordinate coordinate : path.coordinates()) {
                coordinates.add(Position.fromCoordinates(coordinate.lon(), coordinate.lat()));
            }
            features.add(Feature.fromGeometry(LineString.fromCoordinates(coordinates)));
        }
        return FeatureCollection.fromFeatures(features);
    }

    public void updateRoute(BusRoute route) {
        busRoute.setGeoJson(fromBusRoute(route));
    }

    public void addToMap() {
        busLocations = new GeoJsonSource("bus-locations_" + route);
        map.addSource(busLocations);

        busDirections = new GeoJsonSource("bus-directions_" + route);
        map.addSource(busDirections);

        busRoute = new GeoJsonSource("bus-route_" + route);
        map.addSource(busRoute);

        this.routeLayer = new LineLayer("bus-route_" + route, "bus-route_" + route);
        routeLayer.setProperties(
                lineCap("round"),
                lineColor(color),
                lineWidth(4f)
        );
        map.addLayer(routeLayer);

        this.directionsLayer = new LineLayer("bus-directions_" + route, "bus-directions_" + route);
        directionsLayer.setProperties(
                lineCap("round"),
                lineColor("#0f0"),
                lineWidth(4f)
        );
        map.addLayer(directionsLayer);

        this.locationsLayer = new SymbolLayer("bus-locations_" + route, "bus-locations_" + route);
        locationsLayer.setProperties(
                PropertyFactory.iconImage("bus-15"),
                PropertyFactory.iconColor("{busColor}"),
                PropertyFactory.textField("{title}"),
                PropertyFactory.textFont(new String[]{"Open Sans Semibold", "Arial Unicode MS Bold"}),
                PropertyFactory.textOffset(new Float[]{0f, 0.6f}),
                PropertyFactory.textAnchor("top")
        );
        map.addLayer(locationsLayer);
    }

    public void removeFromMap() {
        try {
            map.removeLayer(routeLayer);
            map.removeLayer(locationsLayer);
            map.removeLayer(directionsLayer);
            map.removeSource(busLocations);
            map.removeSource(busDirections);
            map.removeSource(busRoute);
        } catch (NoSuchLayerException | NoSuchSourceException e) {
            e.printStackTrace();
        }
    }
}
