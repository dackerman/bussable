package com.dacklabs.bustracker.application;

public interface AppLogger {
    void info(Object caller, String message, Object... args);

    void debug(Object caller, String message, Object... args);

    void verbose(Object caller, String message, Object... args);

    void error(Object caller, Throwable error, String message, Object... args);
}
