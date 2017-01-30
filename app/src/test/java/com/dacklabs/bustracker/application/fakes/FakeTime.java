package com.dacklabs.bustracker.application.fakes;

import com.dacklabs.bustracker.application.Clock;

public final class FakeTime implements Clock {

    private long time;

    @Override
    public long now() {
        return this.time;
    }

    public void moveForwardInSeconds(double seconds) {
        long previousTime = time;
        time = previousTime + (long)(seconds / 1000);

    }
}
