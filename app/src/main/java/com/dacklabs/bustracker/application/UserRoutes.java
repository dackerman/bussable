package com.dacklabs.bustracker.application;

public interface UserRoutes {
    boolean add(RouteName route);
    boolean alreadyHas(RouteName routeName);
}
