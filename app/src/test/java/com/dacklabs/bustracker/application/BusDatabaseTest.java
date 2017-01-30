package com.dacklabs.bustracker.application;

import org.junit.Test;

import static org.junit.Assert.*;

public class BusDatabaseTest {

    @Test
    public void initiallyContainsAnEmptyState() {
        BusDatabase db = new BusDatabase();
        assertEquals(MapState.empty(), db.state());
    }
}