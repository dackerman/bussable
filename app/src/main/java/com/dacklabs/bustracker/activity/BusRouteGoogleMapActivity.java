package com.dacklabs.bustracker.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.dacklabs.bustracker.R;
import com.google.android.gms.maps.SupportMapFragment;

public class BusRouteGoogleMapActivity extends FragmentActivity {

    private static final BusTrackerApp app = new BusTrackerApp();
    private BusRouteGoogleMapView map = new BusRouteGoogleMapView();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_route_google_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(map);

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.tinybus);
        map.setIcon(bitmap);

        app.initialize(map, new AndroidStorage(this), new RunOnMainThreadListener.Factory(this));
    }

    @Override
    public void onStart() {
        super.onStart();
        app.show();
    }

    public void onPause() {
        super.onPause();
        app.save();
    }

    @Override
    public void onStop() {
        super.onStop();
        app.hide();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        app.save();
    }

    public void onDestroy() {
        super.onDestroy();
        app.shutdown();
    }
}
