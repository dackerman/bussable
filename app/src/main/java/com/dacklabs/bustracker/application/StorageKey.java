package com.dacklabs.bustracker.application;

public class StorageKey {
    private final String baseKey;

    public StorageKey(String baseKey) {
        this.baseKey = baseKey;
    }

    public String subKey(String subKey) {
        return String.format("%s.%s", baseKey, subKey);
    }
}
