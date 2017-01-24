package com.dacklabs.bustracker;

import com.dacklabs.bustracker.data.BusLocation;
import com.dacklabs.bustracker.data.BusLocations;
import com.dacklabs.bustracker.data.BusRoute;
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
import java.util.List;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineCap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;

/**
 * Created by dackerman on 1/22/2017.
 */
public class MapBoxRouteObjects {
    public final String route;
    private final MapboxMap map;

    private GeoJsonSource busLocations;
    private GeoJsonSource busRoute;
    private GeoJsonSource busDirections;
    private LineLayer routeLayer;
    private LineLayer directionsLayer;
    private SymbolLayer locationsLayer;

    public MapBoxRouteObjects(String route, MapboxMap map) {
        this.route = route;
        this.map = map;
    }

    public void updateBusses(BusLocations locations) {
        busLocations.setGeoJson(fromBusLocations(locations));
    }

    private FeatureCollection fromBusLocations(BusLocations locations) {
        List<Feature> features = new ArrayList<>();
        for (BusLocation location : locations.locations()) {
            JsonObject props = new JsonObject();
            props.addProperty("title", "Bus " + location.vehicleId());
            features.add(Feature.fromGeometry(Point.fromCoordinates(
                    Position.fromCoordinates(location.longitude(), location.latitude())), props));
        }

        return FeatureCollection.fromFeatures(features);
    }

    private FeatureCollection fromBusRoute(BusRoute route) {
        List<Feature> features = new ArrayList<>();
        for (BusRoute.RoutePath path : route.paths) {
            List<Position> coordinates = new ArrayList<>();
            for (BusRoute.PathCoordinate coordinate : path.coordinates) {
                coordinates.add(Position.fromCoordinates(coordinate.lon, coordinate.lat));
            }
            features.add(Feature.fromGeometry(LineString.fromCoordinates(coordinates)));
        }
        return FeatureCollection.fromFeatures(features);
    }

    public void updateRoute(BusRoute route) {
        busRoute.setGeoJson(fromBusRoute(route));
    }

    public void addToMap() {
        busLocations = new GeoJsonSource("bus-locations");
        map.addSource(busLocations);

        busDirections = new GeoJsonSource("bus-directions");
        map.addSource(busDirections);

        busRoute = new GeoJsonSource("bus-route");
        map.addSource(busRoute);

        this.routeLayer = new LineLayer("bus-route", "bus-route");
        routeLayer.setProperties(
                lineCap("round"),
                lineColor("#f00"),
                lineWidth(4f)
        );
        map.addLayer(routeLayer);

        this.directionsLayer = new LineLayer("bus-directions", "bus-directions");
        directionsLayer.setProperties(
                lineCap("round"),
                lineColor("#0f0"),
                lineWidth(4f)
        );
        map.addLayer(directionsLayer);

        this.locationsLayer = new SymbolLayer("bus-locations", "bus-locations");
        locationsLayer.setProperties(
                PropertyFactory.iconImage("bus-15"),
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
