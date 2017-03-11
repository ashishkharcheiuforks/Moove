package com.backdoor.moove;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.backdoor.moove.core.adapters.PlaceRecyclerAdapter;
import com.backdoor.moove.core.data.MarkerModel;
import com.backdoor.moove.core.data.PlaceDataProvider;
import com.backdoor.moove.core.fragments.MapFragment;
import com.backdoor.moove.core.helper.Coloring;
import com.backdoor.moove.core.helper.Permissions;
import com.backdoor.moove.core.helper.Reminder;
import com.backdoor.moove.core.interfaces.SimpleListener;

public class LocationsMap extends AppCompatActivity implements SimpleListener {

    private Coloring cs = new Coloring(LocationsMap.this);

    private PlaceDataProvider provider;
    private MapFragment fragment;
    private MapFragment.MapCallback mMapCallback = new MapFragment.MapCallback() {
        @Override
        public void onMapReady() {
            showMarkers();
        }
    };

    private void showMarkers() {
        for (MarkerModel markerModel : provider.getData()) {
            fragment.addMarker(markerModel.getPosition(), markerModel.getTitle(), false, markerModel.getIcon(), false, markerModel.getRadius());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(cs.colorPrimaryDark());
        }
        setContentView(R.layout.activity_locations_layout);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setTitle(R.string.directions);

        fragment = MapFragment.newInstance(false, true, false, false, false, false);
        fragment.setAdapter(loadPlaces());
        fragment.setMapReadyCallback(mMapCallback);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    private PlaceRecyclerAdapter loadPlaces(){
        provider = new PlaceDataProvider(this, false);
        PlaceRecyclerAdapter adapter = new PlaceRecyclerAdapter(this, provider, true);
        adapter.setEventListener(this);
        return adapter;
    }

    private void editPlace(int position){
        Reminder.edit(provider.getItem(position).getId(), LocationsMap.this);
    }

    private void moveToPlace(int position){
        fragment.moveCamera(provider.getItem(position).getPosition());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPlaces();
    }

    @Override
    public void onBackPressed() {
        if (!fragment.onBackPressed()) {
            return;
        }
        finish();
    }

    @Override
    public void onItemClicked(int position, View view) {
        moveToPlace(position);
    }

    @Override
    public void onItemLongClicked(final int position, View view) {
        final CharSequence[] items = {getString(R.string.edit)};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                dialog.dismiss();
                if (item == 0) {
                    editPlace(position);
                }
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 101:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    startActivity(new Intent(LocationsMap.this, NewPlace.class));
                } else {
                    Permissions.showInfo(LocationsMap.this, Permissions.READ_CALENDAR);
                }
                break;
        }
    }
}
