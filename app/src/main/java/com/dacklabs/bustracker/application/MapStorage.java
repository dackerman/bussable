package com.dacklabs.bustracker.application;

public interface MapStorage {
    String getLastTimeQueriedForLocation();
    void updateLastTimeQueriedForLocation(String newTime);
}
