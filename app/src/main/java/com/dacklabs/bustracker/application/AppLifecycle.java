package com.dacklabs.bustracker.application;

public interface AppLifecycle {
    void create();
    void start();
    void resume();
    void pause();
    void stop();
    void save();
    void destroy();
}