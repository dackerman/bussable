package com.dacklabs.bustracker.application;

import com.google.common.util.concurrent.ListenableFuture;

public interface ProcessRunner {
    ListenableFuture<?> execute(Runnable process);
}
