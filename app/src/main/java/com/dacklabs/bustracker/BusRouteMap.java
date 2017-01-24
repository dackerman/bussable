package com.dacklabs.bustracker;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.dacklabs.bustracker.data.BusLocations;
import com.dacklabs.bustracker.data.BusRoute;
import com.mapbox.mapboxsdk.MapboxAccountManager;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import java.util.ArrayList;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineCap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;

public class BusRouteMap extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final short MAPBOX_PERMS = 5465;
    private MapView mapView;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MAPBOX_PERMS:

                break;
        }
    }

    MapBoxRouteObjects route10;
    NextBusApi api = new NextBusApi(new HttpService());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MapboxAccountManager.start(this, getString(R.string.access_token));

        String[] permsNeeded = new String[]{
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.INTERNET
        };
        ArrayList<String> permsMissing = new ArrayList<>();
        for (String perm : permsNeeded) {
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                permsMissing.add(perm);
            }
        }

        if (!permsMissing.isEmpty()) {
            ActivityCompat.requestPermissions(this, permsMissing.toArray(new String[]{}), MAPBOX_PERMS);
        }

        setContentView(R.layout.activity_bus_route_map);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        MapView mapView = (MapView) findViewById(R.id.mapview);
        this.mapView = mapView;
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                route10 = new MapBoxRouteObjects("10", mapboxMap);
                route10.addToMap();
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new UpdateRouteTask(route10).updateRouteAsynchronously();
                new UpdateLocationsTask(route10).updateLocationsAsynchronously();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    private class UpdateRouteTask extends AsyncTask<Void, Void, NextBusApi.QueryResult<BusRoute>> {

        private final MapBoxRouteObjects busRoute;

        public UpdateRouteTask(MapBoxRouteObjects busRoute) {
            super();
            this.busRoute = busRoute;
        }

        @Override
        protected NextBusApi.QueryResult<BusRoute> doInBackground(Void... params) {
            return api.queryBusRouteFor("sf-muni", busRoute.route);
        }

        public void updateRouteAsynchronously() {
            this.execute((Void)null);
        }

        @Override
        public void onPostExecute(NextBusApi.QueryResult<BusRoute> routeResult) {
            String snackMessage;
            if (routeResult.result == null) {
                snackMessage = routeResult.failureMessage;
            } else {
                busRoute.updateRoute(routeResult.result);
                snackMessage = "Success! Route has " + routeResult.result.paths.size() + " paths!";
            }
            Snackbar.make(findViewById(R.id.fab), snackMessage, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }

    private class UpdateLocationsTask extends AsyncTask<Void, Void, NextBusApi.QueryResult<BusLocations>> {

        private final MapBoxRouteObjects busRoute;

        public UpdateLocationsTask(MapBoxRouteObjects busRoute) {
            super();
            this.busRoute = busRoute;
        }

        @Override
        protected NextBusApi.QueryResult<BusLocations> doInBackground(Void... params) {
            return api.queryBusLocationsFor("sf-muni", busRoute.route);
        }

        public void updateLocationsAsynchronously() {
            this.execute((Void)null);
        }

        @Override
        public void onPostExecute(NextBusApi.QueryResult<BusLocations> routeResult) {
            String snackMessage;
            if (routeResult.result == null) {
                snackMessage = routeResult.failureMessage;
            } else {
                busRoute.updateBusses(routeResult.result);
                snackMessage = "Success! Route has " + routeResult.result.locations().size() + " locations!";
            }
            Snackbar.make(findViewById(R.id.fab), snackMessage, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.bus_route_map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }
}
