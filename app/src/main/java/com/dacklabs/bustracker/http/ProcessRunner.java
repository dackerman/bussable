package com.dacklabs.bustracker.http;

import com.google.common.util.concurrent.ListenableFuture;

public interface ProcessRunner {
    ListenableFuture<?> execute(Runnable process);
}
