package com.dacklabs.bustracker.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.dacklabs.bustracker.R;
import com.dacklabs.bustracker.application.AppLogger;
import com.github.tony19.timber.loggly.LogglyTree;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.RuntimeRemoteException;
import com.joshdholtz.sentry.Sentry;

import java.util.ArrayList;

import timber.log.Timber;

public class BusRouteGoogleMapActivity extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks, LocationListener, GoogleApiClient.OnConnectionFailedListener {

    private static final BusTrackerApp app = new BusTrackerApp();
    private BusRouteGoogleMapView map = new BusRouteGoogleMapView();
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Timber.plant(new LogglyTree(getString(R.string.loggly_api_key)));
        Sentry.init(this, getString(R.string.sentry_api_key));

        setContentView(R.layout.activity_bus_route_google_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(map);

        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        locationRequest = new LocationRequest();
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        Toolbar toolbar = (Toolbar) findViewById(R.id.google_map_toolbar);
        setSupportActionBar(toolbar);

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.tinybus);
        map.setIcon(bitmap);

        app.initialize(map, new AndroidStorage(this), new RunOnMainThreadListener.Factory(this));
    }

    public boolean requirePerms(String... permsNeeded) {
        ArrayList<String> permsMissing = new ArrayList<>();
        for (String perm : permsNeeded) {
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                permsMissing.add(perm);
            }
        }

        if (!permsMissing.isEmpty()) {
            ActivityCompat.requestPermissions(this, permsMissing.toArray(new String[]{}), 1);
            return false;
        }
        return true;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (locationPermissionsWereGranted()) {
            log("User has granted location permissions");

            whenSettingsAreAppropriateForTracking(() -> {
                log("Requesting location");
                LocationServices.FusedLocationApi.requestLocationUpdates(
                        googleApiClient, locationRequest, this);
            });
        }
    }

    private void whenSettingsAreAppropriateForTracking(Runnable callback) {
        PendingResult<LocationSettingsResult> settingsResult =
                LocationServices.SettingsApi.checkLocationSettings(googleApiClient,
                        new LocationSettingsRequest
                                .Builder()
                                .addLocationRequest(locationRequest).build());

        settingsResult.setResultCallback(result -> {
            Status status = result.getStatus();
            switch (status.getStatusCode()) {
                case LocationSettingsStatusCodes.SUCCESS:
                    log("Settings are correct for getting location");
                    callback.run();
                    break;
                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                    log("Settings need to be updated for better GPS precision");
                    break;
                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                    break;
            }
        });
    }

    private boolean locationPermissionsWereGranted() {
        return requirePerms(Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.INTERNET);
    }

    @Override
    public void onConnectionSuspended(int i) {
        log("Connection suspended");
    }

    @Override
    public void onLocationChanged(Location location) {
        log("Location changed!");
        map.updateUserLocation(location);
    }

    @Override
    public void onStart() {
        super.onStart();
        app.show();
        log("onStart: connecting google api");
        googleApiClient.connect();
    }

    public void onPause() {
        super.onPause();
        app.save();
    }

    @Override
    public void onStop() {
        super.onStop();
        app.hide();
        googleApiClient.disconnect();
        log("onStop: disconnecting google api");
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

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        log("Connection failed: " + connectionResult.getErrorMessage());
    }

    private void log(String message) {
        AppLogger.info(this, message);
    }
}
