package com.dacklabs.bustracker.http;

import android.util.Xml;

import com.dacklabs.bustracker.application.AppLogger;
import com.dacklabs.bustracker.data.QueryResult;
import com.dacklabs.bustracker.data.AgencyRoutes;
import com.dacklabs.bustracker.data.BusLocations;
import com.dacklabs.bustracker.data.BusRoute;
import com.dacklabs.bustracker.data.Direction;
import com.dacklabs.bustracker.data.ImmutableAgencyRoutes;
import com.dacklabs.bustracker.data.ImmutableBusLocation;
import com.dacklabs.bustracker.data.ImmutableBusLocations;
import com.dacklabs.bustracker.data.ImmutableBusRoute;
import com.dacklabs.bustracker.data.ImmutablePathCoordinate;
import com.dacklabs.bustracker.data.ImmutableRouteInfo;
import com.dacklabs.bustracker.data.ImmutableRouteName;
import com.dacklabs.bustracker.data.ImmutableRoutePath;
import com.dacklabs.bustracker.data.QueryBusLocations;
import com.dacklabs.bustracker.data.QueryBusRoute;
import com.dacklabs.bustracker.data.RouteName;
import com.dacklabs.bustracker.data.RoutePath;
import com.google.common.collect.ImmutableMap;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NextBusApi implements com.dacklabs.bustracker.application.BusApi {

    private final HttpService http;

    public NextBusApi(HttpService http) {
        this.http = http;
    }

    @Override
    public QueryResult<BusLocations> queryBusLocationsFor(QueryBusLocations query) {
        try {
            ImmutableMap.Builder<String, String> params = ImmutableMap.builder();
            params.put("a", query.provider()).put("r", query.routeName().displayName());
            if (query.lastQueryTime().isPresent()) {
                params.put("t", query.lastQueryTime().get());
            }

            String response = nextBusCall("vehicleLocations", params.build());
            BusLocations busRoute = toBusLocations(query.routeName(), response);
            return QueryResult.success(busRoute);
        } catch (IOException e) {
            return QueryResult.failure(e.getMessage());
        } catch (XmlPullParserException e) {
            return QueryResult.failure(e.getMessage());
        }
    }

    @Override
    public QueryResult<BusRoute> queryBusRouteFor(QueryBusRoute query) {
        return handlingExceptions(() -> {
            String response = nextBusCall("routeConfig", ImmutableMap.of(
                    "a", query.provider(), "r", query.routeName().displayName()));
            return toBusRoute(query.routeName(), response);
        });
    }

    @Override
    public QueryResult<AgencyRoutes> queryProvider(String provider) {
        return handlingExceptions(() -> {
            String response = nextBusCall("routeList", ImmutableMap.of("a", provider));
            return toAgencyRoutes(provider, response);
        });
    }

    private BusLocations toBusLocations(RouteName routeName, String response) throws IOException, XmlPullParserException {
        ImmutableBusLocations.Builder locationsBuilder = ImmutableBusLocations.builder();

        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(new StringReader(response));
        locationsBuilder.routeName(routeName);

        loop: while (true) {
            switch (parser.next()) {
                case XmlPullParser.START_TAG:
                    if (parser.getName().equals("vehicle")) {

                        String dirTag = parser.getAttributeValue(parser.getNamespace(), "dirTag");
                        Direction direction = (dirTag != null && dirTag.contains("O")) ? Direction.OUTBOUND : Direction.INBOUND;
                        ImmutableBusLocation location = ImmutableBusLocation.builder()
                                .vehicleId(parser.getAttributeValue(parser.getNamespace(), "id"))
                                .direction(direction)
                                .latitude(Double.parseDouble(parser.getAttributeValue(parser.getNamespace(), "lat")))
                                .longitude(Double.parseDouble(parser.getAttributeValue(parser.getNamespace(), "lon")))
                                .speedInKph(Double.parseDouble(parser.getAttributeValue(parser.getNamespace(), "speedKmHr")))
                                .heading(Double.parseDouble(parser.getAttributeValue(parser.getNamespace(), "heading")))
                                .build();
                        locationsBuilder.putLocations(location.vehicleId(), location);
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

    private BusRoute toBusRoute(RouteName routeName, String response) throws IOException,
            XmlPullParserException {
        List<RoutePath> paths = new ArrayList<>();
        ImmutableRoutePath.Builder currentPath = null;

        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(new StringReader(response));

        loop: while (true) {
            switch (parser.next()) {
                case XmlPullParser.START_TAG:
                    if (parser.getName().equals("path")) {
                        if (currentPath != null) {
                            paths.add(currentPath.build());
                        }
                        currentPath = ImmutableRoutePath.builder();
                    } else if (parser.getName().equals("point")) {
                        float lat = Float.parseFloat(parser.getAttributeValue(parser.getNamespace(), "lat"));
                        float lon = Float.parseFloat(parser.getAttributeValue(parser.getNamespace(), "lon"));
                        currentPath.addCoordinates(ImmutablePathCoordinate.of(lat, lon));
                    }
                    break;
                case XmlPullParser.END_DOCUMENT:
                    if (currentPath != null) {
                        paths.add(currentPath.build());
                    }
                    break loop;
            }
        }
        return ImmutableBusRoute.of(routeName, paths);
    }

    private AgencyRoutes toAgencyRoutes(String provider, String response) throws XmlPullParserException, IOException {
        ImmutableAgencyRoutes.Builder builder = ImmutableAgencyRoutes.builder()
                .provider(provider);
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(new StringReader(response));

        while (parser.next() != XmlPullParser.END_DOCUMENT) {
            switch (parser.getEventType()) {
                case XmlPullParser.START_TAG:
                    if (parser.getName().equals("route")) {
                        String tag = parser.getAttributeValue(parser.getNamespace(), "tag");
                        String title = parser.getAttributeValue(parser.getNamespace(), "title");
                        builder.addRoutes(ImmutableRouteInfo.of(ImmutableRouteName.of(tag), title));
                    }
                    break;
            }
        }

        return builder.build();
    }

    private String nextBusCall(String command, Map<String, String> params) throws IOException {
        String nextBusUrl = "http://webservices.nextbus.com/service/publicXMLFeed?command=" + command;
        for (String key : params.keySet()) {
            nextBusUrl += String.format("&%s=%s", key, params.get(key));
        }

        return http.get(nextBusUrl);
    }

    @FunctionalInterface
    interface UnsafeNextbusCall<T> {
        T doCall() throws XmlPullParserException, IOException;
    }

    private <T> QueryResult<T> handlingExceptions(UnsafeNextbusCall<T> call) {
        try {
            return QueryResult.success(call.doCall());
        } catch (XmlPullParserException e) {
            AppLogger.error(this, e, "Failed XML Parsing");
            return QueryResult.failure(e.getMessage());
        } catch (IOException e) {
            AppLogger.error(this, e, "IO Exception while calling backend");
            return QueryResult.failure(e.getMessage());
        }
    }
}
