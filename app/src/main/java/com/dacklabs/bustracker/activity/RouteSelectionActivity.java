package com.dacklabs.bustracker.activity;

import android.content.Context;
import android.content.Intent;
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
import com.dacklabs.bustracker.application.requests.ImmutableAddRouteRequest;
import com.dacklabs.bustracker.application.requests.ImmutableRemoveRouteRequest;
import com.dacklabs.bustracker.data.ImmutableRouteName;
import com.dacklabs.bustracker.data.RouteName;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.dacklabs.bustracker.activity.BusTrackerApp.app;

public class RouteSelectionActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private RouteSelectionItemArrayAdapter dataAdapter;
    private ListView routeSelectionView;
    private List<RouteName> data = new ArrayList<>();
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

        data.add(ImmutableRouteName.of("10"));
        data.add(ImmutableRouteName.of("47"));
        data.add(ImmutableRouteName.of("12"));
        data.add(ImmutableRouteName.of("19"));

        RouteList routeList = app.routeList();
        for (RouteName routeName : data) {
            if (routeList.routeIsSelected(routeName)) {
                selectedItems.add(routeName);
            }
        }

        dataAdapter.addAll(data);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            for (int i = 0; i < data.size(); i++) {
                RouteName routeName = data.get(i);
                if (selectedItems.contains(routeName)) {
                    app.postMessage(ImmutableAddRouteRequest.of(routeName));
                } else {
                    app.postMessage(ImmutableRemoveRouteRequest.of(routeName));
                }
            }
            RouteSelectionActivity.this.navigateUpTo(new Intent(this, BusRouteGoogleMapActivity.class));
        });

        routeSelectionView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        RouteName routeName = data.get(position);
        CheckedTextView checkView = (CheckedTextView) view.findViewById(R.id.route_selection_item_checkbox);
        if (selectedItems.contains(routeName)) {
            checkView.setChecked(false);
            selectedItems.remove(routeName);
        } else {
            checkView.setChecked(true);
            selectedItems.add(routeName);
        }
    }


    private final class RouteSelectionItemArrayAdapter extends ArrayAdapter<RouteName> {

        public RouteSelectionItemArrayAdapter(Context context) {
            super(context, R.layout.fragment_route_selection_item, R.id.route_selection_item_checkbox);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            CheckedTextView checkView = (CheckedTextView) view.findViewById(R.id.route_selection_item_checkbox);
            RouteName routeName = data.get(position);
            checkView.setText(routeName.displayName());
            if (selectedItems.contains(routeName)) {
                checkView.setChecked(true);
            } else {
                checkView.setChecked(false);
            }
            return view;
        }
    }

}
