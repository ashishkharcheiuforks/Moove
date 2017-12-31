package com.backdoor.moove;

import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.backdoor.moove.core.consts.Constants;
import com.backdoor.moove.core.consts.Prefs;
import com.backdoor.moove.core.fragments.MapFragment;
import com.backdoor.moove.core.helper.Coloring;
import com.backdoor.moove.core.helper.DataBase;
import com.backdoor.moove.core.helper.SharedPrefs;
import com.backdoor.moove.core.interfaces.MapListener;
import com.google.android.gms.maps.model.LatLng;

public class NewPlaceActivity extends AppCompatActivity implements MapListener {

    private Coloring cs = new Coloring(NewPlaceActivity.this);
    private EditText placeName;
    private MapFragment fragment;

    private LatLng place;
    private String placeTitle;
    private long id;
    private Item mItem;
    private MapFragment.MapCallback mMapCallback = new MapFragment.MapCallback() {
        @Override
        public void onMapReady() {
            if (mItem != null) {
                fragment.addMarker(mItem.pos, mItem.title, true, true, mItem.radius);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(cs.colorPrimaryDark());
        }
        setContentView(R.layout.new_place_activity_layout);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);

        id = getIntent().getLongExtra(Constants.ITEM_ID_INTENT, 0);
        placeName = findViewById(R.id.placeName);

        fragment = MapFragment.newInstance(false, false, false, false,
                SharedPrefs.getInstance(this).loadInt(Prefs.MARKER_STYLE));
        fragment.setListener(this);
        fragment.setMapReadyCallback(mMapCallback);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void addPlace() {
        if (place != null) {
            String task = placeName.getText().toString().trim();
            if (task.matches("")) {
                task = placeTitle;
            }
            if (task == null || task.matches("")) {
                placeName.setError(getString(R.string.empty_field));
                return;
            }
            Double latitude = place.latitude;
            Double longitude = place.longitude;

            DataBase db = new DataBase(NewPlaceActivity.this);
            db.open();
            if (id != 0) {
                db.updatePlace(id, task, latitude, longitude);
            } else {
                db.insertPlace(task, latitude, longitude);
            }
            db.close();
            finish();
        } else {
            Toast.makeText(NewPlaceActivity.this, getString(R.string.no_place_selected), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_add:
                addPlace();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.save_menu, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPlace();
    }

    private void loadPlace() {
        if (id != 0) {
            int radius = SharedPrefs.getInstance(this).loadInt(Prefs.LOCATION_RADIUS);
            DataBase db = new DataBase(NewPlaceActivity.this);
            db.open();
            Cursor c = db.getPlace(id);
            if (c != null && c.moveToFirst()) {
                String text = c.getString(c.getColumnIndex(DataBase.NAME));
                double latitude = c.getDouble(c.getColumnIndex(DataBase.LATITUDE));
                double longitude = c.getDouble(c.getColumnIndex(DataBase.LONGITUDE));
                mItem = new Item(text, new LatLng(latitude, longitude), radius);
                placeName.setText(text);
            }
            if (c != null) c.close();
            db.close();
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public void placeChanged(LatLng place) {
        this.place = place;
    }

    @Override
    public void onZoomClick(boolean isFull) {

    }

    @Override
    public void placeName(String name) {
        this.placeTitle = name;
    }

    @Override
    public void onBackClick() {

    }

    private class Item {

        private final String title;
        private final LatLng pos;
        private final int radius;

        public Item(String title, LatLng pos, int radius) {
            this.title = title;
            this.pos = pos;
            this.radius = radius;
        }
    }
}
