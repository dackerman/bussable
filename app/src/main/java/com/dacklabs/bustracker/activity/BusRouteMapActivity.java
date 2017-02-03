package com.dacklabs.bustracker.activity;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.dacklabs.bustracker.R;
import com.dacklabs.bustracker.application.requests.ImmutableAddRouteRequest;
import com.dacklabs.bustracker.application.requests.ImmutableRemoveRouteRequest;
import com.dacklabs.bustracker.data.ImmutableRouteName;

import java.util.ArrayList;

public class BusRouteMapActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final short MAPBOX_PERMS = 5465;

    private static final Main main = new Main();

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MAPBOX_PERMS:
                break;
        }
    }

    public void requirePerms(String... permsNeeded) {
        ArrayList<String> permsMissing = new ArrayList<>();
        for (String perm : permsNeeded) {
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                permsMissing.add(perm);
            }
        }

        if (!permsMissing.isEmpty()) {
            ActivityCompat.requestPermissions(this, permsMissing.toArray(new String[]{}), MAPBOX_PERMS);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_bus_route_map);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        main.setActivity(this);
        main.onCreate(savedInstanceState);
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
        // automatically fire clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean number10Selected;
    private boolean number47Selected;
    private boolean number19Selected;

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.toggle_10) {
            fireFor(number10Selected, "10");
            number10Selected = !number10Selected;
        } else if (id == R.id.toggle_47) {
            fireFor(number47Selected, "47");
            number47Selected = !number47Selected;
        } else if (id == R.id.toggle_19) {
            fireFor(number19Selected, "19");
            number19Selected = !number19Selected;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void fireFor(boolean routeSelected, String route) {
        if (routeSelected) {
            main.postMessage(ImmutableRemoveRouteRequest.of(ImmutableRouteName.of(route)));
        } else {
            main.postMessage(ImmutableAddRouteRequest.of(ImmutableRouteName.of(route)));
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        main.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        main.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        main.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        main.onStop();
    }

    @Override
    public void onRestart() {
        super.onRestart();
        main.onRestart();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        main.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        main.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        main.onDestroy();
    }
}
