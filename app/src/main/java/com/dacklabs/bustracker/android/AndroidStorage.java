package com.dacklabs.bustracker.android;

import android.app.Activity;
import android.content.SharedPreferences;

import com.dacklabs.bustracker.application.Storage;
import com.google.common.base.Optional;

public final class AndroidStorage implements Storage {

    private final Activity activity;

    AndroidStorage(Activity activity) {
        this.activity = activity;
    }

    @Override
    public Optional<String> read(String key) {
        SharedPreferences prefs = activity.getSharedPreferences("BusTracker", 0);
        return Optional.fromNullable(prefs.getString(key, null));
    }

    @Override
    public void write(String key, String data) {
        SharedPreferences prefs = activity.getSharedPreferences("BusTracker", 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, data);
        editor.commit();
    }
}
