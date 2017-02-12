package com.dacklabs.bustracker.android;

import android.util.Log;

import com.dacklabs.bustracker.BuildConfig;
import com.dacklabs.bustracker.application.AppLogger;
import com.joshdholtz.sentry.Sentry;

import timber.log.Timber;

public final class AndroidTimberSentryLogger implements AppLogger {

    private static final String TAG = "BTDACK:";

    @Override
    public void info(Object caller, String message, Object... args) {
        Timber.tag("BusTracker");
        Timber.tag(caller.getClass().getCanonicalName());
        Timber.i(message, args);
        Log.i(TAG + caller.getClass().getCanonicalName(), String.format(message, args));
    }

    @Override
    public void debug(Object caller, String message, Object... args) {
        Timber.tag("BusTracker");
        Timber.tag(caller.getClass().getCanonicalName());
        Timber.d(message, args);
        Log.d(TAG + caller.getClass().getCanonicalName(), String.format(message, args));
    }

    @Override
    public void verbose(Object caller, String message, Object... args) {
        Timber.tag("BusTracker");
        Timber.tag(caller.getClass().getCanonicalName());
        Timber.v(message, args);
        Log.v(TAG + caller.getClass().getCanonicalName(), String.format(message, args));
    }

    @Override
    public void error(Object caller, Throwable error, String message, Object... args) {
        Timber.tag("BusTracker");
        Timber.tag(caller.getClass().getCanonicalName());
        Timber.e(error, message, args);
        if (BuildConfig.ENABLE_SENTRY) {
            Sentry.captureException(error, message);
        }
        Log.e(TAG + caller.getClass().getCanonicalName(), String.format(message, args), error);
    }
}
