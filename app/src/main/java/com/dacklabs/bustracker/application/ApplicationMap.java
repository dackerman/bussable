package com.dacklabs.bustracker.application;

public interface ApplicationMap extends RouteDatabase.Listener {
    void setRouteList(RouteList routeList);
}
