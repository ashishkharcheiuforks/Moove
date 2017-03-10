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

public class NewPlace extends AppCompatActivity implements MapListener {

    private Coloring cs = new Coloring(NewPlace.this);
    private EditText placeName;
    private MapFragment fragment;
    private SharedPrefs sPrefs = new SharedPrefs(NewPlace.this);

    private LatLng place;
    private String placeTitle;
    private long id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(cs.colorPrimaryDark());
        }
        setContentView(R.layout.new_place_activity_layout);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);


        id = getIntent().getLongExtra(Constants.ITEM_ID_INTENT, 0);

        placeName = (EditText) findViewById(R.id.placeName);

        fragment = MapFragment.newInstance(false, false, false, false, sPrefs.loadInt(Prefs.MARKER_STYLE));
        fragment.setListener(this);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void addPlace(){
        if (place != null){
            String task = placeName.getText().toString().trim();
            if (task.matches("")){
                task = placeTitle;
            }
            if (task == null || task.matches("")) {
                placeName.setError(getString(R.string.empty_field));
                return;
            }
            Double latitude = place.latitude;
            Double longitude = place.longitude;

            DataBase db = new DataBase(NewPlace.this);
            db.open();
            if (id != 0){
                db.updatePlace(id, task, latitude, longitude);
            } else {
                db.insertPlace(task, latitude, longitude);
            }
            db.close();
            finish();
        } else {
            Toast.makeText(NewPlace.this, getString(R.string.no_place_selected), Toast.LENGTH_SHORT).show();
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
        if (id != 0){
            int radius = sPrefs.loadInt(Prefs.LOCATION_RADIUS);
            DataBase db = new DataBase(NewPlace.this);
            db.open();
            Cursor c = db.getPlace(id);
            if (c != null && c.moveToFirst()){
                String text = c.getString(c.getColumnIndex(DataBase.NAME));
                double latitude = c.getDouble(c.getColumnIndex(DataBase.LATITUDE));
                double longitude = c.getDouble(c.getColumnIndex(DataBase.LONGITUDE));
                fragment.addMarker(new LatLng(latitude, longitude), text, true, true, radius);
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
}
