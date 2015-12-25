package com.backdoor.moove;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Address;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.backdoor.moove.core.adapters.TitleNavigationAdapter;
import com.backdoor.moove.core.async.DisableAsync;
import com.backdoor.moove.core.async.GeocoderTask;
import com.backdoor.moove.core.consts.Configs;
import com.backdoor.moove.core.consts.Constants;
import com.backdoor.moove.core.consts.LED;
import com.backdoor.moove.core.consts.Prefs;
import com.backdoor.moove.core.data.SpinnerItem;
import com.backdoor.moove.core.dialogs.LedColor;
import com.backdoor.moove.core.dialogs.TargetRadius;
import com.backdoor.moove.core.fragments.MapFragment;
import com.backdoor.moove.core.helper.ColorSetter;
import com.backdoor.moove.core.helper.DataBase;
import com.backdoor.moove.core.helper.LocationType;
import com.backdoor.moove.core.helper.Messages;
import com.backdoor.moove.core.helper.Permissions;
import com.backdoor.moove.core.helper.Reminder;
import com.backdoor.moove.core.helper.SharedPrefs;
import com.backdoor.moove.core.helper.Type;
import com.backdoor.moove.core.interfaces.MapListener;
import com.backdoor.moove.core.services.GeolocationService;
import com.backdoor.moove.core.services.PositionDelayReceiver;
import com.backdoor.moove.core.utils.LocationUtil;
import com.backdoor.moove.core.utils.SuperUtil;
import com.backdoor.moove.core.utils.ViewUtils;
import com.backdoor.moove.core.views.ActionView;
import com.backdoor.moove.core.views.DateTimeView;
import com.backdoor.moove.core.views.FloatingEditText;
import com.google.android.gms.maps.model.LatLng;
import com.melnykov.fab.FloatingActionButton;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

/**
 * Reminder creation activity.
 */
public class ReminderManager extends AppCompatActivity implements
        AdapterView.OnItemSelectedListener, MapListener, GeocoderTask.GeocoderListener,
        DateTimeView.OnSelectListener, ActionView.OnActionListener,
        CompoundButton.OnCheckedChangeListener {

    /**
     * Location reminder variables.
     */
    private LinearLayout delayLayout;
    private CheckBox attackDelay;
    private RelativeLayout mapContainer;
    private ScrollView specsContainer;
    private MapFragment map;
    private AutoCompleteTextView searchField;
    private ActionView actionViewLocation;

    /**
     * LocationOut reminder type variables.
     */
    private LinearLayout delayLayoutOut;
    private RelativeLayout mapContainerOut;
    private ScrollView specsContainerOut;
    private TextView currentLocation, mapLocation, radiusMark;
    private CheckBox attachDelayOut;
    private RadioButton currentCheck, mapCheck;
    private MapFragment mapOut;
    private ActionView actionViewLocationOut;

    /**
     * General views.
     */
    private Toolbar toolbar;
    private Spinner spinner;
    private FloatingEditText taskField;
    private FloatingActionButton mFab;
    private LinearLayout navContainer;

    /**
     * Reminder preferences flags.
     */
    private int myHour = 0;
    private int myMinute = 0;
    private int myYear = 0;
    private int myMonth = 0;
    private int myDay = 1;
    private int volume = Configs.MAX_VOLUME;
    private long id;
    private String type, melody = null;
    private int radius = -1, ledColor = 0;
    private List<Address> foundPlaces;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> namesList;
    private LatLng curPlace;

    private ColorSetter cSetter = new ColorSetter(ReminderManager.this);
    private SharedPrefs sPrefs = new SharedPrefs(ReminderManager.this);

    private static final int VOICE_RECOGNITION_REQUEST_CODE = 109;
    private static final int MENU_ITEM_DELETE = 12;
    private boolean isMessage = false;
    private boolean isDelayed = false;

    private Type remControl = new Type(this);
    private Reminder item;
    private Handler handler = new Handler();
    private GeocoderTask task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cSetter = new ColorSetter(ReminderManager.this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(cSetter.colorPrimaryDark());
        }
        setContentView(R.layout.create_edit_layout);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setOnMenuItemClickListener(
            new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.action_add:
                            save();
                            return true;
                        case R.id.action_custom_melody:
                            startActivityForResult(new Intent(ReminderManager.this, FileExplore.class),
                                    Constants.REQUEST_CODE_SELECTED_MELODY);
                            return true;
                        case R.id.action_custom_radius:
                            selectRadius();
                            return true;
                        case R.id.action_custom_color:
                            chooseLEDColor();
                            return true;
                        case MENU_ITEM_DELETE:
                            deleteReminder();
                            return true;
                    }
                    return true;
                }
            });

        navContainer = (LinearLayout) findViewById(R.id.navContainer);
        spinner = (Spinner) findViewById(R.id.navSpinner);
        taskField = (FloatingEditText) findViewById(R.id.task_message);
        taskField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (map != null) map.setMarkerTitle(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        ImageButton insertVoice = (ImageButton) findViewById(R.id.insertVoice);
        insertVoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SuperUtil.startVoiceRecognitionActivity(ReminderManager.this, VOICE_RECOGNITION_REQUEST_CODE);
            }
        });

        setUpNavigation();

        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setVisibility(View.GONE);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Animation slide = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_down);
                toolbar.startAnimation(slide);
                toolbar.setVisibility(View.VISIBLE);
            }
        }, 500);

        mFab = new FloatingActionButton(ReminderManager.this);
        mFab.setColorNormal(cSetter.colorAccent());
        mFab.setColorPressed(cSetter.colorAccent());
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save();
            }
        });
        mFab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ViewUtils.hide(ReminderManager.this, mFab);
                return false;
            }
        });
        mFab.setType(FloatingActionButton.TYPE_NORMAL);
        mFab.setImageResource(R.drawable.ic_done_white_24dp);

        RelativeLayout wrapper = (RelativeLayout) findViewById(R.id.wrapper);
        wrapper.addView(mFab);

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mFab.getLayoutParams();
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);

        Intent intent = getIntent();
        id = intent.getLongExtra(Constants.EDIT_ID, 0);
        int i = intent.getIntExtra(Constants.EDIT_WIDGET, 0);
        if (i != 0){
            new PositionDelayReceiver().cancelAlarm(ReminderManager.this, id);
            new DisableAsync(ReminderManager.this).execute();
        }

        spinner.setSelection(sPrefs.loadInt(Prefs.LAST_USED_REMINDER));

        if (id != 0){
            item = remControl.getItem(id);
            if (item != null) {
                type = item.getType();
                radius = item.getRadius();
                ledColor = item.getColor();
                melody = item.getMelody();
                if (radius == 0) {
                    radius = -1;
                }
            }

            if (type.startsWith(Constants.TYPE_LOCATION)){
                spinner.setSelection(0);
            } else {
                spinner.setSelection(1);
            }
        }
        clearViews();
    }

    /**
     * Delayed handler for hiding extra options container.
     */
    private void addHandler(){
        handler.removeCallbacks(runnable);
        handler.postDelayed(runnable, 5000);
    }

    /**
     * Runnable for hiding extra options container.
     */
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {

        }
    };

    /**
     * Hide all reminder types layouts.
     */
    private void clearViews() {
        findViewById(R.id.geolocationlayout).setVisibility(View.GONE);
        findViewById(R.id.locationOutLayout).setVisibility(View.GONE);
    }

    /**
     * Set selecting reminder type spinner adapter.
     */
    private void setUpNavigation() {
        ArrayList<SpinnerItem> navSpinner = new ArrayList<>();
        navSpinner.add(new SpinnerItem(getString(R.string.location), R.drawable.ic_place_white_24dp));
        navSpinner.add(new SpinnerItem(getString(R.string.place_out), R.drawable.ic_beenhere_white_24dp));

        TitleNavigationAdapter adapter = new TitleNavigationAdapter(getApplicationContext(), navSpinner);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
    }

    /**
     * Delete or move to trash reminder.
     */
    private void deleteReminder() {
        DataBase db = new DataBase(this);
        db.open();
        Cursor c = db.getReminder(id);
        if (c != null && c.moveToFirst()) {
            int isArchived = c.getInt(c.getColumnIndex(DataBase.STATUS_DB));
            if (isArchived == Constants.ARCHIVE) {
                Reminder.delete(id, this);
                Messages.toast(ReminderManager.this, getString(R.string.deleted));
            } else Reminder.moveToTrash(id, this, null);
            finish();
        }
        if (c != null) c.close();
        db.close();
    }

    /**
     * Show location radius selection dialog.
     */
    private void selectRadius() {
        Intent i = new Intent(ReminderManager.this, TargetRadius.class);
        i.putExtra("item", 1);
        startActivityForResult(i, Constants.REQUEST_CODE_SELECTED_RADIUS);
    }

    /**
     * Open LED indicator color selecting window.
     */
    private void chooseLEDColor() {
        Intent i = new Intent(ReminderManager.this, LedColor.class);
        startActivityForResult(i, Constants.REQUEST_CODE_SELECTED_COLOR);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (mFab.getVisibility() == View.GONE){
                    ViewUtils.show(ReminderManager.this, mFab);
                } else {
                    restoreTask();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Check if selected reminder in spinner matches type that was edited.
     * @return Boolean
     */
    private boolean isSame(){
        boolean is = false;
        if (spinner.getSelectedItemPosition() == 0 && type.startsWith(Constants.TYPE_LOCATION)) is = true;
        if (spinner.getSelectedItemPosition() == 1 && type.startsWith(Constants.TYPE_LOCATION_OUT)) is = true;
        return is;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()){
            case R.id.currentCheck:
                if (currentCheck.isChecked()) {
                    mapCheck.setChecked(false);
                    mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                    mLocList = new CurrentLocation();
                    SharedPrefs prefs = new SharedPrefs(getApplicationContext());
                    long time;
                    time = (prefs.loadInt(Prefs.TRACK_TIME) * 1000);
                    int distance;
                    distance = prefs.loadInt(Prefs.TRACK_DISTANCE);
                    mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, time,
                            distance, mLocList);
                    mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, time,
                            distance, mLocList);
                }
                break;
            case R.id.mapCheck:
                if (mapCheck.isChecked()) {
                    currentCheck.setChecked(false);
                    ViewUtils.fadeOutAnimation(specsContainerOut);
                    ViewUtils.fadeInAnimation(mapContainerOut);
                    if (mLocList != null) {
                        mLocationManager.removeUpdates(mLocList);
                    }
                }
                break;
        }
    }

    @Override
    public void place(LatLng place) {
        curPlace = place;
        if (isLocationOutAttached()) mapLocation.setText(LocationUtil.getAddress(place.latitude, place.longitude));
    }

    @Override
    public void onZoomOutClick() {
        if (isLocationAttached()) {
            ViewUtils.fadeOutAnimation(mapContainer);
            ViewUtils.fadeInAnimation(specsContainer);
        }
        if (isLocationOutAttached()) {
            ViewUtils.fadeOutAnimation(mapContainerOut);
            ViewUtils.fadeInAnimation(specsContainerOut);
        }
    }

    @Override
    public void placeName(String name) {

    }

    /**
     * Show location reminder type creation layout.
     */
    private void attachLocation() {
        taskField.setHint(getString(R.string.remind_me));

        LinearLayout geolocationlayout = (LinearLayout) findViewById(R.id.geolocationlayout);
        ViewUtils.fadeInAnimation(geolocationlayout);

        LocationType dateType = new LocationType(this, Constants.TYPE_LOCATION);
        remControl = dateType;

        delayLayout = (LinearLayout) findViewById(R.id.delayLayout);
        mapContainer = (RelativeLayout) findViewById(R.id.mapContainer);
        specsContainer = (ScrollView) findViewById(R.id.specsContainer);
        delayLayout.setVisibility(View.GONE);
        mapContainer.setVisibility(View.GONE);

        map = new MapFragment();
        map.setListener(this);
        map.enableTouch(true);
        map.setMarkerRadius(sPrefs.loadInt(Prefs.LOCATION_RADIUS));

        FragmentManager fragMan = getSupportFragmentManager();
        FragmentTransaction fragTransaction = fragMan.beginTransaction();
        fragTransaction.add(R.id.map, map);
        fragTransaction.commitAllowingStateLoss();

        attackDelay = (CheckBox) findViewById(R.id.attackDelay);
        attackDelay.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    ViewUtils.expand(delayLayout);
                } else {
                    ViewUtils.collapse(delayLayout);
                }
            }
        });

        if (attackDelay.isChecked()) {
            ViewUtils.expand(delayLayout);
        }

        ImageButton clearField = (ImageButton) findViewById(R.id.clearButton);
        ImageButton mapButton = (ImageButton) findViewById(R.id.mapButton);

        clearField.setImageResource(R.drawable.ic_backspace_white_24dp);
        mapButton.setImageResource(R.drawable.ic_map_white_24dp);

        clearField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchField.setText("");
            }
        });
        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewUtils.fadeOutAnimation(specsContainer);
                ViewUtils.fadeInAnimation(mapContainer);
            }
        });

        searchField = (AutoCompleteTextView) findViewById(R.id.searchField);
        searchField.setThreshold(3);
        adapter = new ArrayAdapter<>(
                ReminderManager.this, android.R.layout.simple_dropdown_item_1line, namesList);
        adapter.setNotifyOnChange(true);
        searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (task != null && !task.isCancelled()) {
                    task.cancel(true);
                }
                task = new GeocoderTask(ReminderManager.this, ReminderManager.this);
                task.execute(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        searchField.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Address sel = foundPlaces.get(position);
                double lat = sel.getLatitude();
                double lon = sel.getLongitude();
                LatLng pos = new LatLng(lat, lon);
                curPlace = pos;
                String title = taskField.getText().toString().trim();
                if (title.matches("")) {
                    title = pos.toString();
                }
                if (map != null) {
                    map.addMarker(pos, title, true, true, radius);
                }
            }
        });

        actionViewLocation = (ActionView) findViewById(R.id.actionViewLocation);
        actionViewLocation.setListener(this);
        actionViewLocation.setActivity(this);

        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        if (myYear > 0){
            cal.set(myYear, myMonth, myDay, myHour, myMinute);

        } else {
            myYear = cal.get(Calendar.YEAR);
            myMonth = cal.get(Calendar.MONTH);
            myDay = cal.get(Calendar.DAY_OF_MONTH);
            myHour = cal.get(Calendar.HOUR_OF_DAY);
            myMinute = cal.get(Calendar.MINUTE);
        }

        DateTimeView dateViewLocation = (DateTimeView) findViewById(R.id.dateViewLocation);
        dateViewLocation.setListener(this);
        dateViewLocation.setDateTime(cal.getTimeInMillis());

        if (id != 0 && isSame()) {
            String text = "", number = null, remType = "";
            double latitude = 0.0, longitude = 0.0;
            if (item != null){
                text = item.getTitle();
                number = item.getNumber();
                remType = item.getType();
                latitude = item.getPlace()[0];
                longitude = item.getPlace()[1];
                radius = item.getRadius();
            }

            if (item != null && item.getStartTime() > 0) {
                cal.setTimeInMillis(item.getStartTime());

                dateViewLocation.setDateTime(cal.getTimeInMillis());
                attackDelay.setChecked(true);
                isDelayed = true;
            } else {
                attackDelay.setChecked(false);
            }

            if (remType.matches(Constants.TYPE_LOCATION_CALL) || remType.matches(Constants.TYPE_LOCATION_MESSAGE)){
                actionViewLocation.setAction(true);
                actionViewLocation.setNumber(number);
                if (remType.matches(Constants.TYPE_LOCATION_CALL)){
                    actionViewLocation.setType(ActionView.TYPE_CALL);
                } else {
                    actionViewLocation.setType(ActionView.TYPE_MESSAGE);
                }
            } else {
                actionViewLocation.setAction(false);
            }

            taskField.setText(text);
            if (map != null) {
                map.addMarker(new LatLng(latitude, longitude), text, true, false, radius);
            }
        }
    }

    /**
     * Show location out reminder type creation layout.
     */
    private void attachLocationOut() {
        taskField.setHint(getString(R.string.remind_me));

        LinearLayout locationOutLayout = (LinearLayout) findViewById(R.id.locationOutLayout);
        ViewUtils.fadeInAnimation(locationOutLayout);

        LocationType dateType = new LocationType(this, Constants.TYPE_LOCATION_OUT);
        remControl = dateType;

        delayLayoutOut = (LinearLayout) findViewById(R.id.delayLayoutOut);
        specsContainerOut = (ScrollView) findViewById(R.id.specsContainerOut);
        mapContainerOut = (RelativeLayout) findViewById(R.id.mapContainerOut);
        delayLayoutOut.setVisibility(View.GONE);
        mapContainerOut.setVisibility(View.GONE);

        mapOut = new MapFragment();
        mapOut.setListener(this);
        mapOut.enableTouch(true);
        mapOut.setMarkerRadius(sPrefs.loadInt(Prefs.LOCATION_RADIUS));

        FragmentManager fragMan = getSupportFragmentManager();
        FragmentTransaction fragTransaction = fragMan.beginTransaction();
        fragTransaction.add(R.id.mapOut, mapOut);
        fragTransaction.commitAllowingStateLoss();

        attachDelayOut = (CheckBox) findViewById(R.id.attachDelayOut);
        attachDelayOut.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    ViewUtils.expand(delayLayoutOut);
                } else {
                    ViewUtils.collapse(delayLayoutOut);
                }
            }
        });

        if (attachDelayOut.isChecked()) {
            ViewUtils.expand(delayLayoutOut);
        }
        ImageButton mapButtonOut = (ImageButton) findViewById(R.id.mapButtonOut);
        mapButtonOut.setImageResource(R.drawable.ic_map_white_24dp);

        mapButtonOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewUtils.fadeOutAnimation(specsContainerOut);
                ViewUtils.fadeInAnimation(mapContainerOut);
                mapCheck.setChecked(true);
            }
        });
        currentLocation = (TextView) findViewById(R.id.currentLocation);
        mapLocation = (TextView) findViewById(R.id.mapLocation);
        radiusMark = (TextView) findViewById(R.id.radiusMark);

        currentCheck = (RadioButton) findViewById(R.id.currentCheck);
        mapCheck = (RadioButton) findViewById(R.id.mapCheck);
        currentCheck.setOnCheckedChangeListener(this);
        mapCheck.setOnCheckedChangeListener(this);
        currentCheck.setChecked(true);

        SeekBar pointRadius = (SeekBar) findViewById(R.id.pointRadius);
        pointRadius.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                radiusMark.setText(String.format(getString(R.string.selected_radius_meters), progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        if (pointRadius.getProgress() == 0) {
            pointRadius.setProgress(sPrefs.loadInt(Prefs.LOCATION_RADIUS));
        }

        actionViewLocationOut = (ActionView) findViewById(R.id.actionViewLocationOut);
        actionViewLocationOut.setListener(this);
        actionViewLocationOut.setActivity(this);

        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        if (myYear > 0){
            cal.set(myYear, myMonth, myDay, myHour, myMinute);
        } else {
            myYear = cal.get(Calendar.YEAR);
            myMonth = cal.get(Calendar.MONTH);
            myDay = cal.get(Calendar.DAY_OF_MONTH);
            myHour = cal.get(Calendar.HOUR_OF_DAY);
            myMinute = cal.get(Calendar.MINUTE);
        }

        DateTimeView dateViewLocationOut = (DateTimeView) findViewById(R.id.dateViewLocationOut);
        dateViewLocationOut.setListener(this);
        dateViewLocationOut.setDateTime(cal.getTimeInMillis());

        if (curPlace != null) {
            if (mapOut != null) {
                mapOut.addMarker(curPlace, null, true, true, radius);
            }
            mapLocation.setText(LocationUtil.getAddress(curPlace.latitude, curPlace.longitude));
        }

        if (id != 0 && isSame()) {
            String text = "", number = null, remType = "";
            double latitude = 0, longitude = 0;
            if (item != null){
                text = item.getTitle();
                number = item.getNumber();
                remType = item.getType();
                latitude = item.getPlace()[0];
                longitude = item.getPlace()[1];
                radius = item.getRadius();
            }

            if (item != null && item.getStartTime() > 0) {
                cal.set(myYear, myMonth, myDay, myHour, myMinute);

                dateViewLocationOut.setDateTime(cal.getTimeInMillis());
                attachDelayOut.setChecked(true);
                isDelayed = true;
            } else {
                attachDelayOut.setChecked(false);
            }

            if (remType.matches(Constants.TYPE_LOCATION_OUT_CALL) || remType.matches(Constants.TYPE_LOCATION_OUT_MESSAGE)){
                actionViewLocationOut.setAction(true);
                actionViewLocationOut.setNumber(number);
                if (remType.matches(Constants.TYPE_LOCATION_OUT_CALL)){
                    actionViewLocationOut.setType(ActionView.TYPE_CALL);
                } else {
                    actionViewLocationOut.setType(ActionView.TYPE_MESSAGE);
                }
            } else {
                actionViewLocationOut.setAction(false);
            }

            taskField.setText(text);
            LatLng pos = new LatLng(latitude, longitude);
            if (mapOut != null) {
                mapOut.addMarker(pos, text, true, true, radius);
            }
            mapLocation.setText(LocationUtil.getAddress(pos.latitude, pos.longitude));
            mapCheck.setChecked(true);
        }
    }

    /**
     * Save new or update current reminder.
     */
    private void save() {
        Reminder item = getData();
        if (item == null) {
            return;
        }
        if (id != 0){
            remControl.save(id, item);
        } else {
            remControl.save(item);
        }
        finish();
    }

    /**
     * Check if location reminder type layout visible.
     * @return Boolean
     */
    private boolean isLocationAttached() {
        return remControl.getType() != null &&
                remControl.getType().startsWith(Constants.TYPE_LOCATION);
    }

    /**
     * Check if location out reminder type layout visible.
     * @return Boolean
     */
    private boolean isLocationOutAttached() {
        return remControl.getType() != null &&
                remControl.getType().startsWith(Constants.TYPE_LOCATION_OUT);
    }

    /**
     * Get reminder type string.
     * @return String
     */
    private String getType(){
        String type;
        if (remControl.getType().startsWith(Constants.TYPE_LOCATION_OUT)){
            if (actionViewLocationOut.hasAction()){
                if (actionViewLocationOut.getType() == ActionView.TYPE_CALL) {
                    type = Constants.TYPE_LOCATION_OUT_CALL;
                } else {
                    type = Constants.TYPE_LOCATION_OUT_MESSAGE;
                }
            } else {
                type = Constants.TYPE_LOCATION_OUT;
            }
        } else {
            if (actionViewLocation.hasAction()){
                if (actionViewLocation.getType() == ActionView.TYPE_CALL) {
                    type = Constants.TYPE_LOCATION_CALL;
                } else {
                    type = Constants.TYPE_LOCATION_MESSAGE;
                }
            } else {
                type = Constants.TYPE_LOCATION;
            }
        }
        return type;
    }

    /**
     * Create reminder object.
     * @return Reminder object
     */
    private Reminder getData() {
        String type = getType();
        Log.d(Constants.LOG_TAG, "Task type " + (type != null ? type : "no type"));
        if (type != null) {
            String task = taskField.getText().toString().trim();
            if (type.matches(Constants.TYPE_LOCATION_CALL) ||
                    type.matches(Constants.TYPE_LOCATION_OUT_CALL)) {

            } else {
                if (task.matches("")) {
                    taskField.setError(getString(R.string.empty_field));
                    return null;
                }
            }
            if (checkNumber()) {
                return null;
            }
            String number = getNumber();
            Log.d(Constants.LOG_TAG, "Task number " + (number != null ? number : "no number"));
            String uuId = UUID.randomUUID().toString();

            if (!LocationUtil.checkLocationEnable(this)) {
                LocationUtil.showLocationAlert(this);
                return null;
            }
            LatLng dest = null;
            boolean isNull = true;
            if (curPlace != null) {
                dest = curPlace;
                isNull = false;
            }
            if (isNull) {
                Messages.toast(ReminderManager.this, getString(R.string.no_place_selected));
                return null;
            }

            Double latitude = dest.latitude;
            Double longitude = dest.longitude;
            Log.d(Constants.LOG_TAG, "Place coords " + latitude + "," + longitude);

            if (isLocationAttached() && !attackDelay.isChecked()) {
                myDay = 0;
                myMonth = 0;
                myYear = 0;
                myHour = 0;
                myMinute = 0;
            }
            if (isLocationOutAttached() && !attachDelayOut.isChecked()) {
                myDay = 0;
                myMonth = 0;
                myYear = 0;
                myHour = 0;
                myMinute = 0;
            }

            Calendar calendar = Calendar.getInstance();
            calendar.set(myYear, myMonth, myDay, myHour, myMinute, 0);

            return new Reminder(0, task, type, melody, uuId, new double[]{latitude, longitude},
                    number, radius, calendar.getTimeInMillis(), ledColor);
        } else {
            return null;
        }
    }



    /**
     * Get number for reminder.
     * @return String
     */
    private String getNumber() {
        if (isLocationAttached() && actionViewLocation.hasAction()){
            return actionViewLocation.getNumber();
        } else if (isLocationOutAttached() && actionViewLocationOut.hasAction()) {
            return actionViewLocationOut.getNumber();
        } else {
            return null;
        }
    }

    /**
     * Check if number inserted.
     * @return Boolean
     */
    private boolean checkNumber(){
        if (isLocationAttached() && actionViewLocation.hasAction()){
            boolean is = actionViewLocation.getNumber().matches("");
            if (is) {
                actionViewLocation.showError();
                return true;
            } else {
                return false;
            }
        } else if (isLocationOutAttached() && actionViewLocationOut.hasAction()) {
            boolean is = actionViewLocationOut.getNumber().matches("");
            if (is) {
                actionViewLocationOut.showError();
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public void onBackPressed() {
        if (map != null && !map.onBackPressed()) {
            return;
        }
        if (mapOut != null && !mapOut.onBackPressed()) {
            return;
        }

        if (mFab.getVisibility() == View.GONE){
            ViewUtils.show(ReminderManager.this, mFab);
            return;
        }

        restoreTask();
    }

    /**
     * Restore currently edited reminder.
     */
    private void restoreTask(){
        if (id != 0) {
            DataBase db = new DataBase(this);
            db.open();
            Cursor c = db.getReminders(Constants.ENABLE);
            if (c != null && c.moveToFirst()) {
                ArrayList<String> types = new ArrayList<>();
                do {
                    String tp = c.getString(c.getColumnIndex(DataBase.TYPE));
                    int isDone = c.getInt(c.getColumnIndex(DataBase.STATUS_DB));
                    if (isDone == Constants.ENABLE) {
                        types.add(tp);
                    }
                } while (c.moveToNext());
                if (types.contains(Constants.TYPE_LOCATION)) {
                    startService(new Intent(ReminderManager.this, GeolocationService.class)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                }
            }
            if (c != null) {
                c.close();
            }
            c = db.getReminder(id);
            if (c != null && c.moveToFirst()) {
                long startTime = c.getLong(c.getColumnIndex(DataBase.START_TIME));
                int status = c.getInt(c.getColumnIndex(DataBase.STATUS_DB));
                if (status == Constants.ENABLE) {
                    if (startTime > 0){
                        new PositionDelayReceiver().setAlarm(this, id);
                    }
                }
                new DisableAsync(this).execute();
            }
            if (c != null) {
                c.close();
            }
            db.close();
        }
        finish();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (navContainer.getVisibility() == View.VISIBLE) {
            switchIt(position);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    /**
     * Show reminder layout.
     * @param position spinner position.
     */
    private void switchIt(int position){
        radius = -1;
        switch (position){
            case 0:
                detachCurrentView();
                if (LocationUtil.checkGooglePlayServicesAvailability(ReminderManager.this)) {
                    if (new Permissions(ReminderManager.this).checkPermission(Permissions.ACCESS_FINE_LOCATION) &&
                            new Permissions(ReminderManager.this).checkPermission(Permissions.CALL_PHONE)
                            && new Permissions(ReminderManager.this).checkPermission(Permissions.SEND_SMS)
                            && new Permissions(ReminderManager.this).checkPermission(Permissions.ACCESS_COURSE_LOCATION)
                            && new Permissions(ReminderManager.this).checkPermission(Permissions.READ_CONTACTS)) {
                        attachLocation();
                    } else {
                        new Permissions(ReminderManager.this)
                                .requestPermission(ReminderManager.this,
                                        new String[]{Permissions.ACCESS_COURSE_LOCATION,
                                                Permissions.ACCESS_FINE_LOCATION, Permissions.CALL_PHONE,
                                                Permissions.SEND_SMS, Permissions.READ_CONTACTS}, 105);
                    }
                } else {
                    spinner.setSelection(0);
                }
                break;
            case 1:
                detachCurrentView();
                if (LocationUtil.checkGooglePlayServicesAvailability(ReminderManager.this)) {
                    if (new Permissions(ReminderManager.this).checkPermission(Permissions.ACCESS_FINE_LOCATION) &&
                            new Permissions(ReminderManager.this).checkPermission(Permissions.CALL_PHONE)
                            && new Permissions(ReminderManager.this).checkPermission(Permissions.SEND_SMS)
                            && new Permissions(ReminderManager.this).checkPermission(Permissions.ACCESS_COURSE_LOCATION)
                            && new Permissions(ReminderManager.this).checkPermission(Permissions.READ_CONTACTS)) {
                        attachLocationOut();
                    } else {
                        new Permissions(ReminderManager.this)
                                .requestPermission(ReminderManager.this,
                                        new String[]{Permissions.ACCESS_COURSE_LOCATION,
                                                Permissions.ACCESS_FINE_LOCATION, Permissions.CALL_PHONE,
                                                Permissions.SEND_SMS, Permissions.READ_CONTACTS}, 106);
                    }
                } else {
                    spinner.setSelection(0);
                }
                break;
        }
        sPrefs.saveInt(Prefs.LAST_USED_REMINDER, position);
        invalidateOptionsMenu();
    }

    private void detachCurrentView() {
        if (isLocationAttached()) {
            findViewById(R.id.geolocationlayout).setVisibility(View.GONE);
        }
        if (isLocationOutAttached()) {
            findViewById(R.id.locationOutLayout).setVisibility(View.GONE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode){
            case 105:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    attachLocation();
                } else {
                    spinner.setSelection(0);
                }
                break;
            case 106:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    attachLocationOut();
                } else {
                    spinner.setSelection(0);
                }
                break;
            case 107:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    SuperUtil.selectContact(ReminderManager.this, Constants.REQUEST_CODE_CONTACTS);
                } else {
                    new Permissions(ReminderManager.this)
                            .showInfo(ReminderManager.this, Permissions.READ_CONTACTS);
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_CODE_CONTACTS) {
            if (resultCode == RESULT_OK) {
                //Use Data to get string
                String number = data.getStringExtra(Constants.SELECTED_CONTACT_NUMBER);
                if (isLocationAttached() && actionViewLocation.hasAction()){
                    actionViewLocation.setNumber(number);
                }
                if (isLocationOutAttached() && actionViewLocationOut.hasAction()){
                    actionViewLocationOut.setNumber(number);
                }
            }
        }
        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
            ArrayList matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (matches != null){
                String text = matches.get(0).toString();
                taskField.setText(text);
            }
        }
        if (requestCode == Constants.REQUEST_CODE_SELECTED_MELODY) {
            if (resultCode == RESULT_OK){
                melody = data.getStringExtra(Constants.FILE_PICKED);
                if (melody != null) {
                    File musicFile = new File(melody);
                    Messages.toast(ReminderManager.this, getString(R.string.selected_melody) +
                            " " + musicFile.getName());
                }
            }
        }

        if (requestCode == Constants.REQUEST_CODE_SELECTED_RADIUS) {
            if (resultCode == RESULT_OK){
                radius = data.getIntExtra(Constants.SELECTED_RADIUS, -1);
                if (radius != -1) {
                    Messages.toast(ReminderManager.this,
                            String.format(getString(R.string.selected_radius_meters), radius));
                    mapOut.recreateMarker(radius);
                }
            }
        }

        if (requestCode == Constants.REQUEST_CODE_SELECTED_COLOR) {
            if (resultCode == RESULT_OK){
                int position = data.getIntExtra(Constants.SELECTED_LED_COLOR, -1);
                String selColor = null;
                if (position == 0) {
                    ledColor = LED.WHITE;
                    selColor = getString(R.string.white);
                } else if (position == 1) {
                    ledColor = LED.RED;
                    selColor = getString(R.string.red);
                } else if (position == 2) {
                    ledColor = LED.GREEN;
                    selColor = getString(R.string.green);
                } else if (position == 3) {
                    ledColor = LED.BLUE;
                    selColor = getString(R.string.blue);
                } else if (position == 4) {
                    ledColor = LED.ORANGE;
                    selColor = getString(R.string.orange);
                } else if (position == 5) {
                    ledColor = LED.YELLOW;
                    selColor = getString(R.string.yellow);
                } else if (position == 6) {
                    ledColor = LED.PINK;
                    selColor = getString(R.string.pink);
                } else if (position == 7) {
                    ledColor = LED.GREEN_LIGHT;
                    selColor = getString(R.string.light_green);
                } else if (position == 8) {
                    ledColor = LED.BLUE_LIGHT;
                    selColor = getString(R.string.light_blue);
                }
                Messages.toast(ReminderManager.this,
                        String.format(getString(R.string.selected_led_color), selColor));
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.create_menu, menu);
        if (isLocationAttached()){
            menu.getItem(1).setVisible(true);
        }
        sPrefs = new SharedPrefs(ReminderManager.this);
        if (sPrefs.loadBoolean(Prefs.LED_STATUS)){
            menu.getItem(2).setVisible(true);
        }
        if (id != 0) {
            menu.add(Menu.NONE, MENU_ITEM_DELETE, 100, getString(R.string.delete));
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (isLocationAttached()){
            menu.getItem(1).setVisible(true);
        }
        sPrefs = new SharedPrefs(ReminderManager.this);
        if (sPrefs.loadBoolean(Prefs.LED_STATUS)){
            menu.getItem(2).setVisible(true);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onDestroy() {
        if (mLocList != null) {
            mLocationManager.removeUpdates(mLocList);
        }
        InputMethodManager imm = (InputMethodManager)getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(taskField.getWindowToken(), 0);
        super.onDestroy();
    }

    private LocationManager mLocationManager;
    private LocationListener mLocList;

    @Override
    public void onAddressReceived(List<Address> addresses) {
        foundPlaces = addresses;

        namesList = new ArrayList<>();
        namesList.clear();
        for (Address selected:addresses){
            String addressText = String.format("%s, %s%s",
                    selected.getMaxAddressLineIndex() > 0 ? selected.getAddressLine(0) : "",
                    selected.getMaxAddressLineIndex() > 1 ? selected.getAddressLine(1) + ", " : "",
                    selected.getCountryName());
            namesList.add(addressText);
        }
        adapter = new ArrayAdapter<>(
                ReminderManager.this, android.R.layout.simple_dropdown_item_1line, namesList);
        if (isLocationAttached()){
            searchField.setAdapter(adapter);
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onDateSelect(long mills, int day, int month, int year) {
        myDay = day;
        myMonth = month;
        myYear = year;
    }

    @Override
    public void onTimeSelect(long mills, int hour, int minute) {
        myHour = hour;
        myMinute = minute;
    }

    @Override
    public void onActionChange(boolean b) {
        if (!b){
            taskField.setHint(getString(R.string.remind_me));
        }
    }

    @Override
    public void onTypeChange(boolean type) {
        if (type) {
            taskField.setHint(R.string.message);
        } else {
            taskField.setHint(getString(R.string.remind_me));
        }
        isMessage = type;
    }

    public class CurrentLocation implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            double currentLat = location.getLatitude();
            double currentLong = location.getLongitude();
            curPlace = new LatLng(currentLat, currentLong);
            String _Location = LocationUtil.getAddress(currentLat, currentLong);
            String text = taskField.getText().toString().trim();
            if (text.matches("")) text = _Location;
            if (isLocationOutAttached()) {
                currentLocation.setText(_Location);
                if (mapOut != null) {
                    mapOut.addMarker(new LatLng(currentLat, currentLong), text, true, true, radius);
                }
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            SharedPrefs prefs = new SharedPrefs(getApplicationContext());
            long time = (prefs.loadInt(Prefs.TRACK_TIME) * 1000) * 2;
            int distance = prefs.loadInt(Prefs.TRACK_DISTANCE) * 2;
            if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, time, distance, mLocList);
            } else {
                mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, time, distance, mLocList);
            }
        }

        @Override
        public void onProviderEnabled(String provider) {
            mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            SharedPrefs prefs = new SharedPrefs(getApplicationContext());
            long time = (prefs.loadInt(Prefs.TRACK_TIME) * 1000) * 2;
            int distance = prefs.loadInt(Prefs.TRACK_DISTANCE) * 2;
            if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, time, distance, mLocList);
            } else {
                mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, time, distance, mLocList);
            }
        }

        @Override
        public void onProviderDisabled(String provider) {
            mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            SharedPrefs prefs = new SharedPrefs(getApplicationContext());
            long time = (prefs.loadInt(Prefs.TRACK_TIME) * 1000) * 2;
            int distance = prefs.loadInt(Prefs.TRACK_DISTANCE) * 2;
            if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, time, distance, mLocList);
            } else {
                mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, time, distance, mLocList);
            }
        }
    }
}