package com.dacklabs.bustracker.android;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
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
import android.view.Menu;
import android.view.MenuItem;

import com.dacklabs.bustracker.BuildConfig;
import com.dacklabs.bustracker.R;
import com.dacklabs.bustracker.application.DataSyncProcess;
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
import com.google.common.base.Preconditions;
import com.joshdholtz.sentry.Sentry;

import java.util.ArrayList;

import timber.log.Timber;

import static com.dacklabs.bustracker.android.BusTrackerApp.app;

public class BusRouteGoogleMapActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        LocationListener,
        GoogleApiClient.OnConnectionFailedListener {

    private BusRouteGoogleMapView map;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private DataSyncProcess dataSyncProcess;
    private RunOnMainThreadListener mapDbListener;
    private boolean trackingLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Timber.plant(new LogglyTree(getString(R.string.loggly_api_key)));
        if (BuildConfig.ENABLE_SENTRY) {
            Sentry.init(this, getString(R.string.sentry_api_key));
        }
        Bitmap routeIcon = Preconditions.checkNotNull(BitmapFactory.decodeResource(getResources(), R.drawable.tinybus), "Couldn't load bitmap resource");
        map = new BusRouteGoogleMapView(app.routeList(), app.db(), routeIcon);

        setContentView(R.layout.activity_bus_route_google_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(map);

        googleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();

        locationRequest = new LocationRequest();
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        Toolbar toolbar = (Toolbar) findViewById(R.id.google_map_toolbar);
        setSupportActionBar(toolbar);

        setupApp();
    }

    private void setupApp() {
        dataSyncProcess = new DataSyncProcess(app.routeList(), app.db(), app.nextBusApi(), app.processRunner(), app.logger());

        mapDbListener = new RunOnMainThreadListener(this, map);
        app.db().registerListener(mapDbListener);
        app.load(new AndroidStorage(this));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.bus_route_map_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit_routes:
                Intent intent = new Intent(this, RouteSelectionActivity.class);
                startActivity(intent);
                break;
            case R.id.action_toggle_geo:
                trackingLocation = !trackingLocation;
                if (trackingLocation) {
                    startLocationTracking();
                } else {
                    stopLocationTracking();
                    map.removeUserLocation();
                }
                break;
        }
        return true;
    }

    public boolean requirePerms(String... permsNeeded) {
        ArrayList<String> permsMissing = new ArrayList<>();
        for (String perm : permsNeeded) {
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                permsMissing.add(perm);
            }
        }

        if (!permsMissing.isEmpty()) {
            ActivityCompat.requestPermissions(this, permsMissing.toArray(new String[]{}), PERMISSIONS_REQUEST);
            return false;
        }
        return true;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
    }

    private void stopLocationTracking() {
        if (googleApiClient == null) return;
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
    }

    private void startLocationTracking() {
        if (locationPermissionsWereGranted()) {
            log("User has granted location permissions");

            whenSettingsAreAppropriateForTracking(() -> {
                log("Requesting location");
                LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
            });
        }
    }

    private static final int LOCATION_STATUS_REQUEST = 2;
    private static final int PERMISSIONS_REQUEST = 1;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case LOCATION_STATUS_REQUEST:
                log("Got result for location status request");
                if (resultCode == RESULT_OK) {
                    log("Location status is updated, retrying");
                    startLocationTracking();
                } else {
                    log("Result code wasn't OK (" + resultCode + "), not doing anything");
                }
                break;
            case PERMISSIONS_REQUEST:
                log("Got result for location permissions");
                if (resultCode == RESULT_OK) {
                    log("Location permissions granted, retrying");
                    startLocationTracking();
                } else {
                    log("Result code wasn't OK (" + resultCode + "), not doing anything");
                }
                break;
        }
    }

    private void whenSettingsAreAppropriateForTracking(Runnable callback) {
        PendingResult<LocationSettingsResult> settingsResult = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, new LocationSettingsRequest.Builder().addLocationRequest(locationRequest).build());

        settingsResult.setResultCallback(result -> {
            Status status = result.getStatus();
            switch (status.getStatusCode()) {
                case LocationSettingsStatusCodes.SUCCESS:
                    log("Settings are correct for getting location");
                    callback.run();
                    break;
                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                    log("Settings need to be updated for better GPS precision");
                    try {
                        status.startResolutionForResult(this, LOCATION_STATUS_REQUEST);
                    } catch (IntentSender.SendIntentException e) {
                        logE("Failed to send intent", e);
                    }
                    break;
                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                    log("Can't change settings for GPS... Ignoring");
                    break;
            }
        });
    }

    private boolean locationPermissionsWereGranted() {
        return requirePerms(Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET);
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
        log("onStart: connecting google api");
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    protected void onResume() {
        log("onResume");
        super.onResume();
        app.execute(dataSyncProcess::startSyncingProcess);
    }

    public void onPause() {
        log("onPause");
        super.onPause();
        app.save(new AndroidStorage(this));
        dataSyncProcess.stopSyncing();
    }

    @Override
    public void onStop() {
        log("onStop: disconnecting google api");
        super.onStop();
        stopLocationTracking();
        googleApiClient.disconnect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        app.db().unregisterListener(mapDbListener);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        app.save(new AndroidStorage(this));
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        log("Connection failed: " + connectionResult.getErrorMessage());
    }

    private void log(String message) {
        app.logger().info(this, message);
    }

    private void logE(String message, Throwable e) {
        app.logger().error(this, e, message);
    }
}
