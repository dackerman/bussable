package com.dacklabs.bustracker.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dackerman on 1/22/2017.
 */

public class BusRoute {
    public final String routeName;
    public final List<RoutePath> paths;

    public BusRoute(String routeName, List<RoutePath> paths) {
        this.routeName = routeName;
        this.paths = paths;
    }

    public static class RoutePath {
        public final List<PathCoordinate> coordinates = new ArrayList<>();
    }

    public static class PathCoordinate {
        public final float lat;
        public final float lon;

        public PathCoordinate(float lat, float lon) {
            this.lat = lat;
            this.lon = lon;
        }
    }
}
