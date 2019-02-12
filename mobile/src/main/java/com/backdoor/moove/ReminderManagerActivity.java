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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.Fade;
import android.transition.Slide;
import android.transition.Visibility;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
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
import com.backdoor.moove.core.consts.Constants;
import com.backdoor.moove.core.consts.LED;
import com.backdoor.moove.core.consts.Prefs;
import com.backdoor.moove.core.data.SpinnerItem;
import com.backdoor.moove.core.dialogs.LedColor;
import com.backdoor.moove.core.dialogs.SelectVolume;
import com.backdoor.moove.core.dialogs.TargetRadius;
import com.backdoor.moove.core.file_explorer.FileExplorerActivity;
import com.backdoor.moove.core.fragments.MapFragment;
import com.backdoor.moove.core.helper.Coloring;
import com.backdoor.moove.core.helper.DataBase;
import com.backdoor.moove.core.helper.LocationType;
import com.backdoor.moove.core.helper.Module;
import com.backdoor.moove.core.helper.Permissions;
import com.backdoor.moove.core.helper.Place;
import com.backdoor.moove.core.helper.Reminder;
import com.backdoor.moove.core.helper.SharedPrefs;
import com.backdoor.moove.core.helper.Type;
import com.backdoor.moove.core.helper.Widget;
import com.backdoor.moove.core.interfaces.ActionCallbacksExtended;
import com.backdoor.moove.core.interfaces.MapListener;
import com.backdoor.moove.core.services.GeolocationService;
import com.backdoor.moove.core.services.PositionDelayReceiver;
import com.backdoor.moove.core.utils.LocationUtil;
import com.backdoor.moove.core.utils.SuperUtil;
import com.backdoor.moove.core.utils.ViewUtils;
import com.backdoor.moove.core.views.ActionView;
import com.backdoor.moove.core.views.AddressAutoCompleteView;
import com.backdoor.moove.core.views.DateTimeView;
import com.backdoor.moove.core.views.FloatingEditText;
import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.UUID;

/**
 * Reminder creation activity.
 */
public class ReminderManagerActivity extends AppCompatActivity implements
        AdapterView.OnItemSelectedListener, MapListener,
        DateTimeView.OnSelectListener, ActionView.OnActionListener,
        CompoundButton.OnCheckedChangeListener, ActionCallbacksExtended {

    private static final String TAG = "ReminderManagerActivity";

    /**
     * Location reminder variables.
     */
    private LinearLayout delayLayout;
    private CheckBox attackDelay;
    private RelativeLayout mapContainer;
    private ScrollView specsContainer;
    private MapFragment map;
    private AddressAutoCompleteView addressField;
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
    private SeekBar pointRadius;

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
    private int volume = -1;
    private long id;
    @Nullable
    private String type = null;
    @Nullable
    private String melody = null;
    private int radius = -1, ledColor = 0;
    @Nullable
    private LatLng curPlace;

    @Nullable
    private SharedPrefs mPrefs;

    private static final int VOICE_RECOGNITION_REQUEST_CODE = 109;
    private static final int MENU_ITEM_DELETE = 12;

    @Nullable
    private Type mControl;
    @Nullable
    private Reminder mReminder;
    @Nullable
    private LocationManager mLocationManager;
    @Nullable
    private LocationListener mLocList;

    private Item mItem;
    private boolean isReady;
    private boolean isReadyOut;
    private MapFragment.MapCallback mMapCallback = new MapFragment.MapCallback() {
        @Override
        public void onMapReady() {
            Log.d(TAG, "onMapReady: " + mItem);
            isReady = true;
            if (mItem != null) {
                map.addMarker(mItem.pos, mItem.title, true, mItem.style, true, mItem.radius);
            }
        }
    };
    private MapFragment.MapCallback mMapOutCallback = new MapFragment.MapCallback() {
        @Override
        public void onMapReady() {
            isReadyOut = true;
            if (mItem != null) {
                mapOut.addMarker(mItem.pos, mItem.title, true, mItem.style, true, mItem.radius);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPrefs = SharedPrefs.getInstance(this);
        mControl = new Type(this);
        Coloring cSetter = new Coloring(ReminderManagerActivity.this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(cSetter.colorPrimaryDark());
        }
        setContentView(R.layout.create_edit_layout);

        if (Module.isLollipop()) {
            Fade enterTransition = new Fade();
            enterTransition.setDuration(getResources().getInteger(R.integer.anim_duration_long));
            getWindow().setEnterTransition(enterTransition);
        }

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setOnMenuItemClickListener(
                item -> {
                    switch (item.getItemId()) {
                        case R.id.action_add:
                            save();
                            return true;
                        case R.id.action_custom_melody:
                            if (Permissions.checkPermission(ReminderManagerActivity.this, Permissions.READ_EXTERNAL)) {
                                startActivityForResult(new Intent(ReminderManagerActivity.this, FileExplorerActivity.class),
                                        Constants.REQUEST_CODE_SELECTED_MELODY);
                            } else {
                                Permissions.requestPermission(ReminderManagerActivity.this, 200,
                                        Permissions.MANAGE_DOCUMENTS,
                                        Permissions.READ_EXTERNAL);
                            }
                            return true;
                        case R.id.action_custom_radius:
                            selectRadius();
                            return true;
                        case R.id.action_custom_color:
                            chooseLEDColor();
                            return true;
                        case R.id.action_volume:
                            selectVolume();
                            return true;
                        case MENU_ITEM_DELETE:
                            deleteReminder();
                            return true;
                    }
                    return true;
                });

        navContainer = findViewById(R.id.navContainer);
        spinner = findViewById(R.id.navSpinner);
        taskField = findViewById(R.id.task_message);
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

        ImageButton insertVoice = findViewById(R.id.insertVoice);
        insertVoice.setOnClickListener(v -> SuperUtil.startVoiceRecognitionActivity(ReminderManagerActivity.this, VOICE_RECOGNITION_REQUEST_CODE));

        setUpNavigation();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        toolbar.setVisibility(View.GONE);

        new Handler().postDelayed(() -> toolbar.setVisibility(View.VISIBLE), 500);

        mFab = findViewById(R.id.fab);
        mFab.setOnClickListener(v -> save());

        Intent intent = getIntent();
        id = intent.getLongExtra(Constants.EDIT_ID, 0);

        clearViews();

        if (mPrefs != null) {
            spinner.setSelection(mPrefs.loadInt(Prefs.LAST_USED_REMINDER));
        }

        if (id != 0) {
            mReminder = mControl.getItem(id);
            if (mReminder != null) {
                type = mReminder.getType();
                radius = mReminder.getRadius();
                ledColor = mReminder.getColor();
                melody = mReminder.getMelody();
                if (radius == 0) {
                    radius = -1;
                }
            }

            if (type != null && type.startsWith(Constants.TYPE_LOCATION)) {
                spinner.setSelection(0);
            } else {
                spinner.setSelection(1);
            }
        }
    }

    private void selectVolume() {
        Intent i = new Intent(ReminderManagerActivity.this, SelectVolume.class);
        startActivityForResult(i, Constants.REQUEST_CODE_VOLUME);
    }

    /**
     * Hide all reminder types layouts.
     */
    private void clearViews() {
        findViewById(R.id.geolocationlayout).setVisibility(View.GONE);
        findViewById(R.id.locationOutLayout).setVisibility(View.GONE);

        map = new MapFragment();
        map.setListener(this);
        map.setMapReadyCallback(mMapCallback);
        if (mPrefs != null) {
            map.setMarkerRadius(mPrefs.loadInt(Prefs.LOCATION_RADIUS));
            map.setMarkerStyle(mPrefs.loadInt(Prefs.MARKER_STYLE));
        }

        mapOut = new MapFragment();
        mapOut.setListener(this);
        mapOut.setMapReadyCallback(mMapOutCallback);
        if (mPrefs != null) {
            mapOut.setMarkerRadius(mPrefs.loadInt(Prefs.LOCATION_RADIUS));
            mapOut.setMarkerStyle(mPrefs.loadInt(Prefs.MARKER_STYLE));
        }

        addFragment(R.id.map, map);
        addFragment(R.id.mapOut, mapOut);
    }

    private void addFragment(int res, MapFragment fragment) {
        FragmentManager fragMan = getSupportFragmentManager();
        FragmentTransaction fragTransaction = fragMan.beginTransaction();
        fragTransaction.add(res, fragment);
        fragTransaction.commitAllowingStateLoss();
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
        Reminder.delete(id, this);
        closeWindow();
    }

    private void closeWindow() {
        if (Module.isLollipop()) {
            Visibility enterTransition = new Slide();
            enterTransition.setDuration(getResources().getInteger(R.integer.anim_duration_long));
            getWindow().setReturnTransition(enterTransition);

            finishAfterTransition();
        }
    }

    /**
     * Show location radius selection dialog.
     */
    private void selectRadius() {
        Intent i = new Intent(ReminderManagerActivity.this, TargetRadius.class);
        i.putExtra("mReminder", 1);
        startActivityForResult(i, Constants.REQUEST_CODE_SELECTED_RADIUS);
    }

    /**
     * Open LED indicator color selecting window.
     */
    private void chooseLEDColor() {
        Intent i = new Intent(ReminderManagerActivity.this, LedColor.class);
        startActivityForResult(i, Constants.REQUEST_CODE_LED_COLOR);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                restoreTask();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Check if selected reminder in spinner matches type that was edited.
     *
     * @return Boolean
     */
    private boolean isSame() {
        if (type == null) return false;
        boolean is = false;
        if (spinner.getSelectedItemPosition() == 0 && type.startsWith(Constants.TYPE_LOCATION))
            is = true;
        if (spinner.getSelectedItemPosition() == 1 && type.startsWith(Constants.TYPE_LOCATION_OUT))
            is = true;
        return is;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.currentCheck:
                if (currentCheck.isChecked()) {
                    mapCheck.setChecked(false);
                    mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                    mLocList = new CurrentLocation();
                    setLocationUpdates();
                }
                break;
            case R.id.mapCheck:
                if (mapCheck.isChecked()) {
                    currentCheck.setChecked(false);
                    toggleMap();
                    removeUpdates();
                }
                break;
        }
    }

    private void removeUpdates() {
        if (mLocList != null && mLocationManager != null) {
            if (Permissions.checkPermission(ReminderManagerActivity.this, Permissions.ACCESS_COARSE_LOCATION, Permissions.ACCESS_FINE_LOCATION)) {
                mLocationManager.removeUpdates(mLocList);
            } else {
                Permissions.requestPermission(ReminderManagerActivity.this, 201,
                        Permissions.ACCESS_FINE_LOCATION, Permissions.ACCESS_COARSE_LOCATION);
            }
        }
    }

    @Override
    public void placeChanged(LatLng place) {
        curPlace = place;
        if (isLocationOutAttached()) {
            mapLocation.setText(LocationUtil.getAddress(place.latitude, place.longitude));
        }
    }

    @Override
    public void onZoomClick(boolean isFull) {
        if (isFull) {
            toolbar.setVisibility(View.GONE);
        } else {
            toolbar.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void placeName(String name) {

    }

    @Override
    public void onBackClick() {
        if (isLocationAttached()) {
            if (map.isFullscreen()) {
                map.setFullscreen(false);
                toolbar.setVisibility(View.VISIBLE);
            }
        }
        if (isLocationOutAttached()) {
            if (mapOut.isFullscreen()) {
                mapOut.setFullscreen(false);
                toolbar.setVisibility(View.GONE);
            }
        }
        toggleMap();
    }

    /**
     * Show location reminder type creation layout.
     */
    private void attachLocation() {
        taskField.setHint(getString(R.string.remind_me));

        LinearLayout geolocationlayout = findViewById(R.id.geolocationlayout);
        ViewUtils.fadeInAnimation(geolocationlayout);

        mControl = new LocationType(this, Constants.TYPE_LOCATION);

        delayLayout = findViewById(R.id.delayLayout);
        mapContainer = findViewById(R.id.mapContainer);
        specsContainer = findViewById(R.id.specsContainer);
        delayLayout.setVisibility(View.GONE);
        mapContainer.setVisibility(View.GONE);

        attackDelay = findViewById(R.id.attackDelay);
        attackDelay.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                delayLayout.setVisibility(View.VISIBLE);
            } else {
                delayLayout.setVisibility(View.GONE);
            }
        });

        if (attackDelay.isChecked()) {
            delayLayout.setVisibility(View.VISIBLE);
        }

        ImageButton clearField = findViewById(R.id.clearButton);
        ImageButton mapButton = findViewById(R.id.mapButton);

        clearField.setImageResource(R.drawable.ic_backspace_white_24dp);
        mapButton.setImageResource(R.drawable.ic_map_white_24dp);

        clearField.setOnClickListener(v -> addressField.setText(""));
        mapButton.setOnClickListener(v -> toggleMap());

        addressField = findViewById(R.id.searchField);
        addressField.setListener(address -> {
            if (address != null) showMarker(address);
        });

        actionViewLocation = findViewById(R.id.actionViewLocation);
        actionViewLocation.setListener(this);
        actionViewLocation.setActivity(this);

        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        if (myYear > 0) {
            cal.set(myYear, myMonth, myDay, myHour, myMinute);

        } else {
            myYear = cal.get(Calendar.YEAR);
            myMonth = cal.get(Calendar.MONTH);
            myDay = cal.get(Calendar.DAY_OF_MONTH);
            myHour = cal.get(Calendar.HOUR_OF_DAY);
            myMinute = cal.get(Calendar.MINUTE);
        }

        DateTimeView dateViewLocation = findViewById(R.id.dateViewLocation);
        dateViewLocation.setListener(this);
        dateViewLocation.setDateTime(cal.getTimeInMillis());

        if (curPlace != null) {
            if (map != null) {
                map.addMarker(curPlace, null, true, true, radius);
                toggleMap();
            }
        }

        if (id != 0 && isSame()) {
            String text, number, remType;
            double latitude, longitude;
            int style;
            if (mReminder != null) {
                text = mReminder.getTitle();
                number = mReminder.getNumber();
                remType = mReminder.getType();
                latitude = mReminder.getPlace()[0];
                longitude = mReminder.getPlace()[1];
                radius = mReminder.getRadius();
                volume = mReminder.getVolume();
                ledColor = mReminder.getColor();
                style = mReminder.getMarker();

                if (mReminder.getStartTime() > 0) {
                    cal.setTimeInMillis(mReminder.getStartTime());
                    dateViewLocation.setDateTime(cal.getTimeInMillis());
                    attackDelay.setChecked(true);
                } else {
                    attackDelay.setChecked(false);
                }

                if (remType.matches(Constants.TYPE_LOCATION_CALL) || remType.matches(Constants.TYPE_LOCATION_MESSAGE)) {
                    actionViewLocation.setAction(true);
                    actionViewLocation.setNumber(number);
                    if (remType.matches(Constants.TYPE_LOCATION_CALL)) {
                        actionViewLocation.setType(ActionView.TYPE_CALL);
                    } else {
                        actionViewLocation.setType(ActionView.TYPE_MESSAGE);
                    }
                } else {
                    actionViewLocation.setAction(false);
                }

                Log.d(Constants.LOG_TAG, "Lat " + latitude + ", " + longitude);
                taskField.setText(text);
                mItem = new Item(text, new LatLng(latitude, longitude), radius, style);
                if (isReady) {
                    map.addMarker(mItem.pos, mItem.title, true, mItem.style, true, mItem.radius);
                }
                toggleMap();
            }
        }
    }

    private void showMarker(Address address) {
        double lat = address.getLatitude();
        double lon = address.getLongitude();
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

    private boolean isMapVisible() {
        if (isLocationAttached()) {
            return mapContainer != null && mapContainer.getVisibility() == View.VISIBLE;
        }
        return isLocationOutAttached() && mapContainerOut != null &&
                mapContainerOut.getVisibility() == View.VISIBLE;
    }

    private void toggleMap() {
        if (isLocationAttached()) {
            if (isMapVisible()) {
                ViewUtils.fadeOutAnimation(mapContainer);
                ViewUtils.fadeInAnimation(specsContainer);
                ViewUtils.show(this, mFab);
            } else {
                ViewUtils.fadeOutAnimation(specsContainer);
                ViewUtils.fadeInAnimation(mapContainer);
                ViewUtils.hide(this, mFab);
                if (map != null) {
                    map.showShowcase();
                }
            }
        }
        if (isLocationOutAttached()) {
            if (isMapVisible()) {
                ViewUtils.fadeOutAnimation(mapContainerOut);
                ViewUtils.fadeInAnimation(specsContainerOut);
                ViewUtils.show(this, mFab);
            } else {
                ViewUtils.fadeOutAnimation(specsContainerOut);
                ViewUtils.fadeInAnimation(mapContainerOut);
                ViewUtils.hide(this, mFab);
                if (mapOut != null) {
                    mapOut.showShowcase();
                }
            }
        }
    }

    /**
     * Show location out reminder type creation layout.
     */
    private void attachLocationOut() {
        taskField.setHint(getString(R.string.remind_me));

        LinearLayout locationOutLayout = findViewById(R.id.locationOutLayout);
        ViewUtils.fadeInAnimation(locationOutLayout);

        mControl = new LocationType(this, Constants.TYPE_LOCATION_OUT);

        delayLayoutOut = findViewById(R.id.delayLayoutOut);
        specsContainerOut = findViewById(R.id.specsContainerOut);
        mapContainerOut = findViewById(R.id.mapContainerOut);
        delayLayoutOut.setVisibility(View.GONE);
        mapContainerOut.setVisibility(View.GONE);

        attachDelayOut = findViewById(R.id.attachDelayOut);
        attachDelayOut.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                delayLayoutOut.setVisibility(View.VISIBLE);
            } else {
                delayLayoutOut.setVisibility(View.GONE);
            }
        });

        if (attachDelayOut.isChecked()) {
            delayLayoutOut.setVisibility(View.VISIBLE);
        }
        ImageButton mapButtonOut = findViewById(R.id.mapButtonOut);
        mapButtonOut.setImageResource(R.drawable.ic_map_white_24dp);

        mapButtonOut.setOnClickListener(v -> {
            if (mapCheck.isChecked()) {
                toggleMap();
            }
            mapCheck.setChecked(true);
        });
        currentLocation = findViewById(R.id.currentLocation);
        mapLocation = findViewById(R.id.mapLocation);
        radiusMark = findViewById(R.id.radiusMark);

        currentCheck = findViewById(R.id.currentCheck);
        mapCheck = findViewById(R.id.mapCheck);
        currentCheck.setOnCheckedChangeListener(this);
        mapCheck.setOnCheckedChangeListener(this);
        currentCheck.setChecked(true);

        pointRadius = findViewById(R.id.pointRadius);
        pointRadius.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                radiusMark.setText(String.format(getString(R.string.selected_radius_meters), String.valueOf(progress)));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        if (pointRadius.getProgress() == 0 && mPrefs != null) {
            pointRadius.setProgress(mPrefs.loadInt(Prefs.LOCATION_RADIUS));
        }

        actionViewLocationOut = findViewById(R.id.actionViewLocationOut);
        actionViewLocationOut.setListener(this);
        actionViewLocationOut.setActivity(this);

        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        if (myYear > 0) {
            cal.set(myYear, myMonth, myDay, myHour, myMinute);
        } else {
            myYear = cal.get(Calendar.YEAR);
            myMonth = cal.get(Calendar.MONTH);
            myDay = cal.get(Calendar.DAY_OF_MONTH);
            myHour = cal.get(Calendar.HOUR_OF_DAY);
            myMinute = cal.get(Calendar.MINUTE);
        }

        DateTimeView dateViewLocationOut = findViewById(R.id.dateViewLocationOut);
        dateViewLocationOut.setListener(this);
        dateViewLocationOut.setDateTime(cal.getTimeInMillis());

        if (curPlace != null) {
            if (mapOut != null) {
                mapOut.addMarker(curPlace, null, true, true, radius);
            }
            mapLocation.setText(LocationUtil.getAddress(curPlace.latitude, curPlace.longitude));
        }

        if (id != 0 && isSame()) {
            String text, number, remType;
            double latitude, longitude;
            int style;
            if (mReminder != null) {
                text = mReminder.getTitle();
                number = mReminder.getNumber();
                remType = mReminder.getType();
                latitude = mReminder.getPlace()[0];
                longitude = mReminder.getPlace()[1];
                radius = mReminder.getRadius();
                volume = mReminder.getVolume();
                ledColor = mReminder.getColor();
                style = mReminder.getMarker();

                if (mReminder.getStartTime() > 0) {
                    cal.set(myYear, myMonth, myDay, myHour, myMinute);

                    dateViewLocationOut.setDateTime(cal.getTimeInMillis());
                    attachDelayOut.setChecked(true);
                } else {
                    attachDelayOut.setChecked(false);
                }

                if (remType.matches(Constants.TYPE_LOCATION_OUT_CALL) ||
                        remType.matches(Constants.TYPE_LOCATION_OUT_MESSAGE)) {
                    actionViewLocationOut.setAction(true);
                    actionViewLocationOut.setNumber(number);
                    if (remType.matches(Constants.TYPE_LOCATION_OUT_CALL)) {
                        actionViewLocationOut.setType(ActionView.TYPE_CALL);
                    } else {
                        actionViewLocationOut.setType(ActionView.TYPE_MESSAGE);
                    }
                } else {
                    actionViewLocationOut.setAction(false);
                }

                taskField.setText(text);
                LatLng pos = new LatLng(latitude, longitude);
                mItem = new Item(text, pos, radius, style);
                if (isReadyOut) {
                    mapOut.addMarker(mItem.pos, mItem.title, true, mItem.style, true, mItem.radius);
                }
                mapLocation.setText(LocationUtil.getAddress(pos.latitude, pos.longitude));
                mapCheck.setChecked(true);
            }
        }
    }

    /**
     * Save new or update current reminder.
     */
    private void save() {
        if (mControl == null) return;
        Reminder item = getData();
        if (item == null) {
            return;
        }
        if (id != 0) {
            mControl.save(id, item);
        } else {
            mControl.save(item);
        }
        closeWindow();
    }

    /**
     * Check if location reminder type layout visible.
     *
     * @return Boolean
     */
    private boolean isLocationAttached() {
        return mControl != null && mControl.getType() != null &&
                mControl.getType().startsWith(Constants.TYPE_LOCATION);
    }

    /**
     * Check if location out reminder type layout visible.
     *
     * @return Boolean
     */
    private boolean isLocationOutAttached() {
        return mControl != null && mControl.getType() != null &&
                mControl.getType().startsWith(Constants.TYPE_LOCATION_OUT);
    }

    /**
     * Get reminder type string.
     *
     * @return String
     */
    private String getType() {
        String type;
        if (mControl != null && mControl.getType().startsWith(Constants.TYPE_LOCATION_OUT)) {
            if (actionViewLocationOut.hasAction()) {
                if (actionViewLocationOut.getType() == ActionView.TYPE_CALL) {
                    type = Constants.TYPE_LOCATION_OUT_CALL;
                } else {
                    type = Constants.TYPE_LOCATION_OUT_MESSAGE;
                }
            } else {
                type = Constants.TYPE_LOCATION_OUT;
            }
        } else {
            if (actionViewLocation.hasAction()) {
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
     *
     * @return Reminder object
     */
    private Reminder getData() {
        String type = getType();
        Log.d(Constants.LOG_TAG, "Task type " + (type != null ? type : "no type"));
        if (type != null) {
            String task = taskField.getText().toString().trim();
            if (!type.contains(Constants.TYPE_CALL)) {
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
                LocationUtil.showLocationAlert(this, this);
                return null;
            }
            LatLng dest = null;
            boolean isNull = true;
            if (curPlace != null) {
                dest = curPlace;
                isNull = false;
            }
            if (isNull) {
                showSnackbar(R.string.no_place_selected);
                return null;
            } else {
                if (mPrefs != null && mPrefs.loadBoolean(Prefs.PLACES_AUTO)) {
                    Place.addPlace(this, dest);
                }
            }

            Double latitude = dest.latitude;
            Double longitude = dest.longitude;
            Log.d(Constants.LOG_TAG, "Place coords " + latitude + ", " + longitude);

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(myYear, myMonth, myDay, myHour, myMinute, 0);
            long startTime = calendar.getTimeInMillis();
            if ((isLocationAttached() && !attackDelay.isChecked()) ||
                    (isLocationOutAttached() && !attachDelayOut.isChecked())) {
                startTime = -1;
            }

            int marker = -1;
            if (isLocationAttached()) {
                marker = map.getMarkerStyle();
            }
            if (isLocationOutAttached()) {
                marker = mapOut.getMarkerStyle();
            }

            Log.d(Constants.LOG_TAG, "Start time " + startTime);
            Log.d(Constants.LOG_TAG, "Marker " + marker);

            return new Reminder(0, task, type, melody, uuId, new double[]{latitude, longitude},
                    number, radius, startTime, ledColor, marker, volume);
        } else {
            return null;
        }
    }


    /**
     * Get number for reminder.
     *
     * @return String
     */
    private String getNumber() {
        if (isLocationAttached() && actionViewLocation.hasAction()) {
            return actionViewLocation.getNumber();
        } else if (isLocationOutAttached() && actionViewLocationOut.hasAction()) {
            return actionViewLocationOut.getNumber();
        } else {
            return null;
        }
    }

    /**
     * Check if number inserted.
     *
     * @return Boolean
     */
    private boolean checkNumber() {
        if (isLocationAttached() && actionViewLocation.hasAction()) {
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

        restoreTask();
    }

    /**
     * Restore currently edited reminder.
     */
    private void restoreTask() {
        if (id != 0) {
            DataBase db = new DataBase(this);
            db.open();
            Cursor c = db.getReminder(id);
            if (c != null && c.moveToFirst()) {
                long startTime = c.getLong(c.getColumnIndex(DataBase.START_TIME));
                int status = c.getInt(c.getColumnIndex(DataBase.STATUS_DB));
                if (status == Constants.ENABLE) {
                    if (startTime != 1) {
                        new PositionDelayReceiver().setAlarm(this, id);
                    } else {
                        if (!SuperUtil.isServiceRunning(ReminderManagerActivity.this, GeolocationService.class)) {
                            startService(new Intent(ReminderManagerActivity.this, GeolocationService.class)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                        }
                    }
                }
            }
            if (c != null) {
                c.close();
            }
            db.close();
        }
        DataBase db = new DataBase(this);
        db.open();
        Cursor c = db.getReminders(Constants.ENABLE);
        if (c != null && c.moveToFirst()) {
            int i = 0;
            do {
                int isDone = c.getInt(c.getColumnIndex(DataBase.STATUS_DB));
                if (isDone == Constants.ENABLE) {
                    i++;
                }
            } while (c.moveToNext());
            if (i > 0) {
                if (!SuperUtil.isServiceRunning(ReminderManagerActivity.this, GeolocationService.class)) {
                    startService(new Intent(ReminderManagerActivity.this, GeolocationService.class)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                }
            }
        }
        if (c != null) {
            c.close();
        }
        closeWindow();
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
     *
     * @param position spinner position.
     */
    private void switchIt(int position) {
        radius = -1;
        switch (position) {
            case 0:
                detachCurrentView();
                if (LocationUtil.playServicesFullCheck(ReminderManagerActivity.this)) {
                    if (Permissions.checkPermission(ReminderManagerActivity.this, Permissions.ACCESS_FINE_LOCATION,
                            Permissions.CALL_PHONE, Permissions.SEND_SMS, Permissions.ACCESS_COARSE_LOCATION,
                            Permissions.READ_CONTACTS)) {
                        attachLocation();
                    } else {
                        Permissions.requestPermission(ReminderManagerActivity.this, 105,
                                Permissions.ACCESS_COARSE_LOCATION,
                                Permissions.ACCESS_FINE_LOCATION, Permissions.CALL_PHONE,
                                Permissions.SEND_SMS, Permissions.READ_CONTACTS);
                    }
                } else {
                    spinner.setSelection(0);
                }
                break;
            case 1:
                detachCurrentView();
                if (LocationUtil.playServicesFullCheck(ReminderManagerActivity.this)) {
                    if (Permissions.checkPermission(ReminderManagerActivity.this, Permissions.ACCESS_FINE_LOCATION,
                            Permissions.CALL_PHONE, Permissions.SEND_SMS, Permissions.ACCESS_COARSE_LOCATION,
                            Permissions.READ_CONTACTS)) {
                        attachLocationOut();
                    } else {
                        Permissions.requestPermission(ReminderManagerActivity.this, 106,
                                Permissions.ACCESS_COARSE_LOCATION,
                                Permissions.ACCESS_FINE_LOCATION, Permissions.CALL_PHONE,
                                Permissions.SEND_SMS, Permissions.READ_CONTACTS);
                    }
                } else {
                    spinner.setSelection(0);
                }
                break;
        }
        if (mPrefs != null) mPrefs.saveInt(Prefs.LAST_USED_REMINDER, position);
        invalidateOptionsMenu();
    }

    private void detachCurrentView() {
        if (mFab.getVisibility() != View.VISIBLE) {
            ViewUtils.show(this, mFab);
        }
        if (toolbar.getVisibility() == View.GONE) {
            toolbar.setVisibility(View.VISIBLE);
        }
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
        if (grantResults.length == 0) return;
        switch (requestCode) {
            case 105:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    attachLocation();
                } else {
                    spinner.setSelection(0);
                }
                break;
            case 106:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    attachLocationOut();
                } else {
                    spinner.setSelection(0);
                }
                break;
            case 107:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    SuperUtil.selectContact(ReminderManagerActivity.this, Constants.REQUEST_CODE_CONTACTS);
                } else {
                    showSnackbar(R.string.cant_access_to_contacts);
                }
                break;
            case 200:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startActivityForResult(new Intent(ReminderManagerActivity.this, FileExplorerActivity.class),
                            Constants.REQUEST_CODE_SELECTED_MELODY);
                } else {
                    showSnackbar(R.string.cant_read_external_storage);
                }
                break;
            case 201:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    removeUpdates();
                } else {
                    showSnackbar(R.string.cant_access_location_services);
                }
                break;
            case 202:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setLocationUpdates();
                } else {
                    showSnackbar(R.string.cant_access_location_services);
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
                if (isLocationAttached() && actionViewLocation.hasAction()) {
                    actionViewLocation.setNumber(number);
                }
                if (isLocationOutAttached() && actionViewLocationOut.hasAction()) {
                    actionViewLocationOut.setNumber(number);
                }
            }
        }

        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
            ArrayList matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (matches != null) {
                String text = matches.get(0).toString();
                taskField.setText(text);
            }
        }

        if (requestCode == Constants.REQUEST_CODE_SELECTED_MELODY) {
            if (resultCode == RESULT_OK) {
                melody = data.getStringExtra(Constants.FILE_PICKED);
                if (melody != null) {
                    File musicFile = new File(melody);
                    String str = getString(R.string.selected_melody) + " " + musicFile.getName();
                    showSnackbar(str, R.string.dismiss, v -> melody = null);
                }
            }
        }

        if (requestCode == Constants.REQUEST_CODE_SELECTED_RADIUS) {
            if (resultCode == RESULT_OK) {
                radius = data.getIntExtra(Constants.SELECTED_RADIUS, -1);
                if (radius != -1) {
                    String str = String.format(getString(R.string.selected_radius_meters), String.valueOf(radius));
                    showSnackbar(str, R.string.dismiss, v -> radius = -1);
                    if (isLocationAttached()) {
                        map.recreateMarker(radius);
                    }
                    if (isLocationOutAttached()) {
                        mapOut.recreateMarker(radius);
                        pointRadius.setProgress(radius);
                    }
                }
            }
        }

        if (requestCode == Constants.REQUEST_CODE_LED_COLOR) {
            if (resultCode == RESULT_OK) {
                int position = data.getIntExtra(Constants.SELECTED_LED_COLOR, -1);
                String selColor = LED.getTitle(this, position);
                ledColor = LED.getLED(position);

                String str = String.format(getString(R.string.selected_led_color), selColor);
                showSnackbar(str, R.string.dismiss, v -> ledColor = -1);
            }
        }

        if (requestCode == Constants.REQUEST_CODE_VOLUME) {
            if (resultCode == RESULT_OK) {
                volume = data.getIntExtra(Constants.SELECTED_VOLUME, -1);

                String str = String.format(getString(R.string.set_volume_for_reminder), String.valueOf(volume));
                showSnackbar(str, R.string.dismiss, v -> volume = -1);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.create_menu, menu);
        if (isLocationAttached()) {
            menu.getItem(1).setVisible(true);
        }
        if (mPrefs != null && mPrefs.loadBoolean(Prefs.LED_STATUS)) {
            menu.getItem(2).setVisible(true);
        }
        if (id != 0) {
            menu.add(Menu.NONE, MENU_ITEM_DELETE, 100, getString(R.string.delete));
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (isLocationAttached()) {
            menu.getItem(1).setVisible(true);
        }
        if (mPrefs != null && mPrefs.loadBoolean(Prefs.LED_STATUS)) {
            menu.getItem(2).setVisible(true);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void onDestroy() {
        removeUpdates();
        InputMethodManager imm = (InputMethodManager) getSystemService(
                Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(taskField.getWindowToken(), 0);
        Widget.updateWidgets(ReminderManagerActivity.this);
        super.onDestroy();
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
        if (!b) {
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
    }

    @Override
    public void showSnackbar(int message, int actionTitle, View.OnClickListener listener) {
        Snackbar.make(mFab, message, Snackbar.LENGTH_LONG)
                .setAction(actionTitle, listener)
                .show();
    }

    @Override
    public void showSnackbar(int message) {
        Snackbar.make(mFab, message, Snackbar.LENGTH_LONG)
                .show();
    }

    @Override
    public void showSnackbar(String message) {
        Snackbar.make(mFab, message, Snackbar.LENGTH_LONG)
                .show();
    }

    @Override
    public void showSnackbar(String message, int actionTitle, View.OnClickListener listener) {
        Snackbar.make(mFab, message, Snackbar.LENGTH_LONG)
                .setAction(actionTitle, listener)
                .show();
    }

    private void setLocationUpdates() {
        if (mLocList != null) {
            long time = mPrefs != null ? (mPrefs.loadInt(Prefs.TRACK_TIME) * 1000) : 10000;
            int distance = mPrefs != null ? mPrefs.loadInt(Prefs.TRACK_DISTANCE) : 10;
            if (Permissions.checkPermission(ReminderManagerActivity.this, Permissions.ACCESS_COARSE_LOCATION,
                    Permissions.ACCESS_FINE_LOCATION)) {
                mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                if (mLocationManager != null) {
                    mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, time,
                            distance, mLocList);
                    mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, time,
                            distance, mLocList);
                }
            } else {
                Permissions.requestPermission(ReminderManagerActivity.this, 202,
                        Permissions.ACCESS_FINE_LOCATION, Permissions.ACCESS_COARSE_LOCATION);
            }
        }
    }

    private class CurrentLocation implements LocationListener {

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
            setLocationUpdates();
        }

        @Override
        public void onProviderEnabled(String provider) {
            setLocationUpdates();
        }

        @Override
        public void onProviderDisabled(String provider) {
            setLocationUpdates();
        }
    }

    private class Item {

        private final String title;
        private final LatLng pos;
        private final int radius;
        private final int style;

        Item(String title, LatLng pos, int radius, int style) {
            this.title = title;
            this.pos = pos;
            this.radius = radius;
            this.style = style;
        }
    }
}