package com.dacklabs.bustracker.application;

import com.google.common.base.Optional;

public interface Storage {
    Optional<String> read(String key);
    void write(String key, String data);
}
