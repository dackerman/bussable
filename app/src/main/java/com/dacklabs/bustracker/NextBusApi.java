package com.dacklabs.bustracker;

import android.util.Xml;

import com.dacklabs.bustracker.data.BusLocations;
import com.dacklabs.bustracker.data.BusRoute;
import com.dacklabs.bustracker.data.Direction;
import com.dacklabs.bustracker.data.ImmutableBusLocation;
import com.dacklabs.bustracker.data.ImmutableBusLocations;
import com.google.common.collect.ImmutableMap;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NextBusApi {

    public static class QueryResult<T> {
        public final T result;
        public final String failureMessage;

        private QueryResult(T value, String message) {
            this.result = value;
            this.failureMessage = message;
        }

        public static <A> QueryResult<A> success(A value) {
            return new QueryResult<>(value, null);
        }

        public static <A> QueryResult<A> failure(String failureMessage) {
            return new QueryResult<>(null, failureMessage);
        }
    }

    private final HttpService http;

    public NextBusApi(HttpService http) {
        this.http = http;
    }

    public QueryResult<BusLocations> queryBusLocationsFor(String provider, String route) {
        try {
            String response = nextBusCall("vehicleLocations", ImmutableMap.of("a", provider, "r", route));
            BusLocations busRoute = toBusLocations(response);
            return QueryResult.success(busRoute);
        } catch (IOException e) {
            return QueryResult.failure(e.getMessage());
        } catch (XmlPullParserException e) {
            return QueryResult.failure(e.getMessage());
        }
    }

    public QueryResult<BusRoute> queryBusRouteFor(String provider, String route) {
        try {
            String response = nextBusCall("routeConfig", ImmutableMap.of("a", provider, "r", route));
            BusRoute busRoute = toBusRoute(response);
            return QueryResult.success(busRoute);
        } catch (IOException e) {
            return QueryResult.failure(e.getMessage());
        } catch (XmlPullParserException e) {
            return QueryResult.failure(e.getMessage());
        }
    }

    private BusLocations toBusLocations(String response) throws IOException, XmlPullParserException {
        ImmutableBusLocations.Builder locationsBuilder = ImmutableBusLocations.builder();

        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(new StringReader(response));

        loop: while (true) {
            switch (parser.next()) {
                case XmlPullParser.START_TAG:
                    if (parser.getName().equals("vehicle")) {
                        ImmutableBusLocation location = ImmutableBusLocation.builder()
                                .vehicleId(parser.getAttributeValue(parser.getNamespace(), "id"))
                                .direction(parser.getAttributeValue(parser.getNamespace(), "dirTag").contains("O") ? Direction.OUTBOUND : Direction.INBOUND)
                                .latitude(Double.parseDouble(parser.getAttributeValue(parser.getNamespace(), "lat")))
                                .longitude(Double.parseDouble(parser.getAttributeValue(parser.getNamespace(), "lon")))
                                .speedInKph(Double.parseDouble(parser.getAttributeValue(parser.getNamespace(), "speedKmHr")))
                                .heading(Double.parseDouble(parser.getAttributeValue(parser.getNamespace(), "heading")))
                                .build();
                        locationsBuilder.route(parser.getAttributeValue(parser.getNamespace(), "routeTag"));
                        locationsBuilder.addLocations(location);
                    } else if (parser.getName().equals("lastTime")) {
                        locationsBuilder.lastQueryTime(parser.getAttributeValue(parser.getNamespace(), "time"));
                    }
                    break;
                case XmlPullParser.END_DOCUMENT:
                    break loop;
            }
        }
        return locationsBuilder.build();
    }

    private BusRoute toBusRoute(String response) throws IOException, XmlPullParserException {
        String routeName = "";
        List<BusRoute.RoutePath> paths = new ArrayList<>();
        BusRoute.RoutePath currentPath = new BusRoute.RoutePath();

        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(new StringReader(response));
        loop: while (true) {
            switch (parser.next()) {
                case XmlPullParser.START_TAG:
                    if (parser.getName().equals("route")) {
                        routeName = parser.getAttributeValue(parser.getNamespace(), "title");
                    } else if (parser.getName().equals("path")) {
                        currentPath = new BusRoute.RoutePath();
                        paths.add(currentPath);
                    } else if (parser.getName().equals("point")) {
                        float lat = Float.parseFloat(parser.getAttributeValue(parser.getNamespace(), "lat"));
                        float lon = Float.parseFloat(parser.getAttributeValue(parser.getNamespace(), "lon"));
                        currentPath.coordinates.add(new BusRoute.PathCoordinate(lat, lon));
                    }
                    break;
                case XmlPullParser.END_DOCUMENT:
                    break loop;
            }
        }
        return new BusRoute(routeName, paths);
    }

    private String nextBusCall(String command, Map<String, String> params) throws IOException {
        String nextBusUrl = "http://webservices.nextbus.com/service/publicXMLFeed?command=" + command;
        for (String key : params.keySet()) {
            nextBusUrl += String.format("&%s=%s", key, params.get(key));
        }

        return http.get(nextBusUrl);
    }
}
