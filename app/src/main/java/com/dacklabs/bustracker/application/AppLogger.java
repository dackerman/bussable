package com.dacklabs.bustracker.application;

import com.joshdholtz.sentry.Sentry;

import timber.log.Timber;

public final class AppLogger {
    public static void info(Object caller, String message, Object... args) {
        Timber.tag("BusTracker");
        Timber.tag(caller.getClass().getCanonicalName());
        Timber.i(message, args);
    }

    public static void debug(Object caller, String message, Object... args) {
        Timber.tag("BusTracker");
        Timber.tag(caller.getClass().getCanonicalName());
        Timber.d(message, args);
    }

    public static void verbose(Object caller, String message, Object... args) {
        Timber.tag("BusTracker");
        Timber.tag(caller.getClass().getCanonicalName());
        Timber.v(message, args);
    }

    public static void error(Object caller, Throwable error, String message, Object... args) {
        Timber.tag("BusTracker");
        Timber.tag(caller.getClass().getCanonicalName());
        Timber.e(error, message, args);
        Sentry.captureException(error, message);
    }
}
