package com.dacklabs.bustracker.android;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;

import com.dacklabs.bustracker.R;
import com.dacklabs.bustracker.application.RouteList;
import com.dacklabs.bustracker.data.AgencyRoutes;
import com.dacklabs.bustracker.data.ImmutableAddRouteRequest;
import com.dacklabs.bustracker.data.ImmutableAgencyRoutes;
import com.dacklabs.bustracker.data.ImmutableRemoveRouteRequest;
import com.dacklabs.bustracker.data.RouteInfo;
import com.dacklabs.bustracker.data.RouteName;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.dacklabs.bustracker.android.BusTrackerApp.app;

public class RouteSelectionActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private RouteSelectionItemArrayAdapter dataAdapter;
    private ListView routeSelectionView;
    private List<RouteInfo> data = new ArrayList<>();
    private Set<RouteName> selectedItems = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_selection);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        routeSelectionView = (ListView) findViewById(R.id.route_list);

        dataAdapter = new RouteSelectionItemArrayAdapter(this);
        routeSelectionView.setAdapter(dataAdapter);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            for (int i = 0; i < data.size(); i++) {
                RouteName routeName = data.get(i).routeName();
                if (selectedItems.contains(routeName)) {
                    app.postMessage(ImmutableAddRouteRequest.of(routeName));
                } else {
                    app.postMessage(ImmutableRemoveRouteRequest.of(routeName));
                }
            }
            RouteSelectionActivity.this.navigateUpTo(new Intent(this, BusRouteGoogleMapActivity.class));
        });

        routeSelectionView.setOnItemClickListener(this);

        new GetRouteListTask().execute("sf-muni");
    }

    private class GetRouteListTask extends AsyncTask<String, Integer, AgencyRoutes> {

        @Override
        protected AgencyRoutes doInBackground(String... agencies) {
            String agency = agencies[0];
            ListenableFuture<AgencyRoutes> future = app.agencyRoutesCache().routesForAgency(agency);
            try {
                return future.get(20, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                app.logger().error(this, e, "Failed to get agency routes");
            }
            return ImmutableAgencyRoutes.of(agency, new ArrayList<>());
        }

        @Override
        protected void onPostExecute(AgencyRoutes agencyRoutes) {
            RouteList routeList = app.routeList();
            for (RouteInfo routeInfo : agencyRoutes.routes()) {
                if (routeList.routeIsSelected(routeInfo.routeName())) {
                    data.add(routeInfo);
                    selectedItems.add(routeInfo.routeName());
                }
            }
            for (RouteInfo routeInfo : agencyRoutes.routes()) {
                if (!routeList.routeIsSelected(routeInfo.routeName())) {
                    data.add(routeInfo);
                }
            }
            dataAdapter.addAll(data);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        RouteName routeName = data.get(position).routeName();
        CheckedTextView checkView = (CheckedTextView) view.findViewById(R.id.route_selection_item_checkbox);
        if (selectedItems.contains(routeName)) {
            checkView.setChecked(false);
            selectedItems.remove(routeName);
        } else {
            checkView.setChecked(true);
            selectedItems.add(routeName);
        }
    }


    private final class RouteSelectionItemArrayAdapter extends ArrayAdapter<RouteInfo> {

        public RouteSelectionItemArrayAdapter(Context context) {
            super(context, R.layout.fragment_route_selection_item, R.id.route_selection_item_checkbox);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            CheckedTextView checkView = (CheckedTextView) view.findViewById(R.id.route_selection_item_checkbox);
            RouteInfo routeInfo = data.get(position);
            checkView.setText(routeInfo.routeTitle());
            if (selectedItems.contains(routeInfo.routeName())) {
                checkView.setChecked(true);
            } else {
                checkView.setChecked(false);
            }
            return view;
        }
    }

}
