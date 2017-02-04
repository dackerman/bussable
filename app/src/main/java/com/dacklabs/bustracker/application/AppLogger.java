package com.dacklabs.bustracker.application;

import android.util.Log;

import com.joshdholtz.sentry.Sentry;

import timber.log.Timber;

public final class AppLogger {
    public static void info(Object caller, String message, Object... args) {
        Timber.tag("BusTracker");
        Timber.tag(caller.getClass().getCanonicalName());
        Timber.i(message, args);
        Log.i("DACK:" + caller.getClass().getCanonicalName(), String.format(message, args));
    }

    public static void debug(Object caller, String message, Object... args) {
        Timber.tag("BusTracker");
        Timber.tag(caller.getClass().getCanonicalName());
        Timber.d(message, args);
        Log.d("DACK:" + caller.getClass().getCanonicalName(), String.format(message, args));
    }

    public static void verbose(Object caller, String message, Object... args) {
        Timber.tag("BusTracker");
        Timber.tag(caller.getClass().getCanonicalName());
        Timber.v(message, args);
        Log.v("DACK:" + caller.getClass().getCanonicalName(), String.format(message, args));
    }

    public static void error(Object caller, Throwable error, String message, Object... args) {
        Timber.tag("BusTracker");
        Timber.tag(caller.getClass().getCanonicalName());
        Timber.e(error, message, args);
        Sentry.captureException(error, message);
        Log.e("DACK:" + caller.getClass().getCanonicalName(), String.format(message, args), error);
    }
}
