package com.dacklabs.bustracker.storage;

import android.os.Bundle;

import com.dacklabs.bustracker.application.MapStorage;
import com.dacklabs.bustracker.application.StorageKey;

public class AndroidStorage implements MapStorage {

    public static final String LAST_TIME_QUERIED_FOR_LOCATION = "lastTimeQueriedForLocation";

    private final StorageKey baseKey;
    private final Bundle bundle;

    public AndroidStorage(StorageKey baseKey, Bundle bundle) {
        this.baseKey = baseKey;
        this.bundle = bundle;
    }

    @Override
    public String getLastTimeQueriedForLocation() {
        return this.bundle.getString(baseKey.subKey(LAST_TIME_QUERIED_FOR_LOCATION));
    }

    @Override
    public void updateLastTimeQueriedForLocation(String newTime) {
        this.bundle.putString(baseKey.subKey(LAST_TIME_QUERIED_FOR_LOCATION), newTime);
    }
}
