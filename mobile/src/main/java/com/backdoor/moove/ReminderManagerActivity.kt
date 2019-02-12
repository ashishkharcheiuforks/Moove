package com.backdoor.moove

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.location.Address
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.speech.RecognizerIntent
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.text.Editable
import android.text.TextWatcher
import android.transition.Fade
import android.transition.Slide
import android.transition.Visibility
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView

import com.backdoor.moove.core.adapters.TitleNavigationAdapter
import com.backdoor.moove.core.consts.Constants
import com.backdoor.moove.core.consts.LED
import com.backdoor.moove.core.consts.Prefs
import com.backdoor.moove.core.data.SpinnerItem
import com.backdoor.moove.core.dialogs.LedColor
import com.backdoor.moove.core.dialogs.SelectVolume
import com.backdoor.moove.core.dialogs.TargetRadius
import com.backdoor.moove.core.file_explorer.FileExplorerActivity
import com.backdoor.moove.core.fragments.MapFragment
import com.backdoor.moove.core.helper.Coloring
import com.backdoor.moove.core.helper.DataBase
import com.backdoor.moove.core.helper.LocationType
import com.backdoor.moove.core.helper.Module
import com.backdoor.moove.core.helper.Permissions
import com.backdoor.moove.core.helper.Place
import com.backdoor.moove.core.helper.Reminder
import com.backdoor.moove.core.helper.SharedPrefs
import com.backdoor.moove.core.helper.Type
import com.backdoor.moove.core.helper.Widget
import com.backdoor.moove.core.interfaces.ActionCallbacksExtended
import com.backdoor.moove.core.interfaces.MapListener
import com.backdoor.moove.core.services.GeolocationService
import com.backdoor.moove.core.services.PositionDelayReceiver
import com.backdoor.moove.core.utils.LocationUtil
import com.backdoor.moove.core.utils.SuperUtil
import com.backdoor.moove.core.utils.ViewUtils
import com.backdoor.moove.core.views.ActionView
import com.backdoor.moove.core.views.AddressAutoCompleteView
import com.backdoor.moove.core.views.DateTimeView
import com.backdoor.moove.core.views.FloatingEditText
import com.google.android.gms.maps.model.LatLng

import java.io.File
import java.util.ArrayList
import java.util.Calendar
import java.util.UUID

/**
 * Reminder creation activity.
 */
class ReminderManagerActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener, MapListener, DateTimeView.OnSelectListener, ActionView.OnActionListener, CompoundButton.OnCheckedChangeListener, ActionCallbacksExtended {

    /**
     * Location reminder variables.
     */
    private var delayLayout: LinearLayout? = null
    private var attackDelay: CheckBox? = null
    private var mapContainer: RelativeLayout? = null
    private var specsContainer: ScrollView? = null
    private var map: MapFragment? = null
    private var addressField: AddressAutoCompleteView? = null
    private var actionViewLocation: ActionView? = null

    /**
     * LocationOut reminder type variables.
     */
    private var delayLayoutOut: LinearLayout? = null
    private var mapContainerOut: RelativeLayout? = null
    private var specsContainerOut: ScrollView? = null
    private var currentLocation: TextView? = null
    private var mapLocation: TextView? = null
    private var radiusMark: TextView? = null
    private var attachDelayOut: CheckBox? = null
    private var currentCheck: RadioButton? = null
    private var mapCheck: RadioButton? = null
    private var mapOut: MapFragment? = null
    private var actionViewLocationOut: ActionView? = null
    private var pointRadius: SeekBar? = null

    /**
     * General views.
     */
    private var toolbar: Toolbar? = null
    private var spinner: Spinner? = null
    private var taskField: FloatingEditText? = null
    private var mFab: FloatingActionButton? = null
    private var navContainer: LinearLayout? = null

    /**
     * Reminder preferences flags.
     */
    private var myHour = 0
    private var myMinute = 0
    private var myYear = 0
    private var myMonth = 0
    private var myDay = 1
    private var volume = -1
    private var id: Long = 0
    private var type: String? = null
    private var melody: String? = null
    private var radius = -1
    private var ledColor = 0
    private var curPlace: LatLng? = null

    private var mPrefs: SharedPrefs? = null

    private var mControl: Type? = null
    private var mReminder: Reminder? = null
    private var mLocationManager: LocationManager? = null
    private var mLocList: LocationListener? = null

    private var mItem: Item? = null
    private var isReady: Boolean = false
    private var isReadyOut: Boolean = false
    private val mMapCallback = object : MapFragment.MapCallback {
        override fun onMapReady() {
            Log.d(TAG, "onMapReady: " + mItem!!)
            isReady = true
            if (mItem != null) {
                map!!.addMarker(mItem!!.pos, mItem!!.title, true, mItem!!.style, true, mItem!!.radius)
            }
        }
    }
    private val mMapOutCallback = MapFragment.MapCallback {
        isReadyOut = true
        if (mItem != null) {
            mapOut!!.addMarker(mItem!!.pos, mItem!!.title, true, mItem!!.style, true, mItem!!.radius)
        }
    }

    /**
     * Check if selected reminder in spinner matches type that was edited.
     *
     * @return Boolean
     */
    private val isSame: Boolean
        get() {
            if (type == null) return false
            var `is` = false
            if (spinner!!.selectedItemPosition == 0 && type!!.startsWith(Constants.TYPE_LOCATION))
                `is` = true
            if (spinner!!.selectedItemPosition == 1 && type!!.startsWith(Constants.TYPE_LOCATION_OUT))
                `is` = true
            return `is`
        }

    private val isMapVisible: Boolean
        get() = if (isLocationAttached) {
            mapContainer != null && mapContainer!!.visibility == View.VISIBLE
        } else isLocationOutAttached && mapContainerOut != null &&
                mapContainerOut!!.visibility == View.VISIBLE

    /**
     * Check if location reminder type layout visible.
     *
     * @return Boolean
     */
    private val isLocationAttached: Boolean
        get() = mControl != null && mControl!!.type != null &&
                mControl!!.type!!.startsWith(Constants.TYPE_LOCATION)

    /**
     * Check if location out reminder type layout visible.
     *
     * @return Boolean
     */
    private val isLocationOutAttached: Boolean
        get() = mControl != null && mControl!!.type != null &&
                mControl!!.type!!.startsWith(Constants.TYPE_LOCATION_OUT)

    /**
     * Create reminder object.
     *
     * @return Reminder object
     */
    private val data: Reminder?
        get() {
            val type = getType()
            Log.d(Constants.LOG_TAG, "Task type " + (type ?: "no type"))
            if (type != null) {
                val task = taskField!!.text!!.toString().trim { it <= ' ' }
                if (!type.contains(Constants.TYPE_CALL)) {
                    if (task.matches("".toRegex())) {
                        taskField!!.error = getString(R.string.empty_field)
                        return null
                    }
                }
                if (checkNumber()) {
                    return null
                }
                val number = number
                Log.d(Constants.LOG_TAG, "Task number " + (number ?: "no number"))
                val uuId = UUID.randomUUID().toString()

                if (!LocationUtil.checkLocationEnable(this)) {
                    LocationUtil.showLocationAlert(this, this)
                    return null
                }
                var dest: LatLng? = null
                var isNull = true
                if (curPlace != null) {
                    dest = curPlace
                    isNull = false
                }
                if (isNull) {
                    showSnackbar(R.string.no_place_selected)
                    return null
                } else {
                    if (mPrefs != null && mPrefs!!.loadBoolean(Prefs.PLACES_AUTO)) {
                        Place.addPlace(this, dest!!)
                    }
                }

                val latitude = dest!!.latitude
                val longitude = dest.longitude
                Log.d(Constants.LOG_TAG, "Place coords $latitude, $longitude")

                val calendar = Calendar.getInstance()
                calendar.timeInMillis = System.currentTimeMillis()
                calendar.set(myYear, myMonth, myDay, myHour, myMinute, 0)
                var startTime = calendar.timeInMillis
                if (isLocationAttached && !attackDelay!!.isChecked || isLocationOutAttached && !attachDelayOut!!.isChecked) {
                    startTime = -1
                }

                var marker = -1
                if (isLocationAttached) {
                    marker = map!!.markerStyle
                }
                if (isLocationOutAttached) {
                    marker = mapOut!!.markerStyle
                }

                Log.d(Constants.LOG_TAG, "Start time $startTime")
                Log.d(Constants.LOG_TAG, "Marker $marker")

                return Reminder(0, task, type, melody, uuId, doubleArrayOf(latitude, longitude),
                        number, radius, startTime, ledColor, marker, volume)
            } else {
                return null
            }
        }


    /**
     * Get number for reminder.
     *
     * @return String
     */
    private val number: String?
        get() = if (isLocationAttached && actionViewLocation!!.hasAction()) {
            actionViewLocation!!.number
        } else if (isLocationOutAttached && actionViewLocationOut!!.hasAction()) {
            actionViewLocationOut!!.number
        } else {
            null
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mPrefs = SharedPrefs.getInstance(this)
        mControl = Type(this)
        val cSetter = Coloring(this@ReminderManagerActivity)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = cSetter.colorPrimaryDark()
        }
        setContentView(R.layout.create_edit_layout)

        if (Module.isLollipop) {
            val enterTransition = Fade()
            enterTransition.duration = resources.getInteger(R.integer.anim_duration_long).toLong()
            window.enterTransition = enterTransition
        }

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        toolbar?.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_add -> {
                    save()
                    return@setOnMenuItemClickListener true
                }
                R.id.action_custom_melody -> {
                    if (Permissions.checkPermission(this@ReminderManagerActivity, Permissions.READ_EXTERNAL)) {
                        startActivityForResult(Intent(this@ReminderManagerActivity, FileExplorerActivity::class.java),
                                Constants.REQUEST_CODE_SELECTED_MELODY)
                    } else {
                        Permissions.requestPermission(this@ReminderManagerActivity, 200,
                                Permissions.MANAGE_DOCUMENTS,
                                Permissions.READ_EXTERNAL)
                    }
                    return@setOnMenuItemClickListener true
                }
                R.id.action_custom_radius -> {
                    selectRadius()
                    return@setOnMenuItemClickListener true
                }
                R.id.action_custom_color -> {
                    chooseLEDColor()
                    return@setOnMenuItemClickListener true
                }
                R.id.action_volume -> {
                    selectVolume()
                    return@setOnMenuItemClickListener true
                }
                MENU_ITEM_DELETE -> {
                    deleteReminder()
                    return@setOnMenuItemClickListener true
                }
            }
            true
        }

        navContainer = findViewById(R.id.navContainer)
        spinner = findViewById(R.id.navSpinner)
        taskField = findViewById(R.id.task_message)
        taskField!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (map != null) map!!.setMarkerTitle(s.toString())
            }

            override fun afterTextChanged(s: Editable) {

            }
        })

        val insertVoice = findViewById<ImageButton>(R.id.insertVoice)
        insertVoice.setOnClickListener { v -> SuperUtil.startVoiceRecognitionActivity(this@ReminderManagerActivity, VOICE_RECOGNITION_REQUEST_CODE) }

        setUpNavigation()

        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        toolbar?.visibility = View.GONE

        Handler().postDelayed({ toolbar!!.visibility = View.VISIBLE }, 500)

        mFab = findViewById(R.id.fab)
        mFab!!.setOnClickListener { v -> save() }

        val intent = intent
        id = intent.getLongExtra(Constants.EDIT_ID, 0)

        clearViews()

        if (mPrefs != null) {
            spinner!!.setSelection(mPrefs!!.loadInt(Prefs.LAST_USED_REMINDER))
        }

        if (id != 0L) {
            mReminder = mControl!!.getItem(id)
            if (mReminder != null) {
                type = mReminder!!.type
                radius = mReminder!!.radius
                ledColor = mReminder!!.color
                melody = mReminder!!.melody
                if (radius == 0) {
                    radius = -1
                }
            }

            if (type != null && type!!.startsWith(Constants.TYPE_LOCATION)) {
                spinner!!.setSelection(0)
            } else {
                spinner!!.setSelection(1)
            }
        }
    }

    private fun selectVolume() {
        val i = Intent(this@ReminderManagerActivity, SelectVolume::class.java)
        startActivityForResult(i, Constants.REQUEST_CODE_VOLUME)
    }

    /**
     * Hide all reminder types layouts.
     */
    private fun clearViews() {
        findViewById<View>(R.id.geolocationlayout).visibility = View.GONE
        findViewById<View>(R.id.locationOutLayout).visibility = View.GONE

        map = MapFragment()
        map!!.setListener(this)
        map!!.setMapReadyCallback(mMapCallback)
        if (mPrefs != null) {
            map!!.setMarkerRadius(mPrefs!!.loadInt(Prefs.LOCATION_RADIUS))
            map!!.markerStyle = mPrefs!!.loadInt(Prefs.MARKER_STYLE)
        }

        mapOut = MapFragment()
        mapOut!!.setListener(this)
        mapOut!!.setMapReadyCallback(mMapOutCallback)
        if (mPrefs != null) {
            mapOut!!.setMarkerRadius(mPrefs!!.loadInt(Prefs.LOCATION_RADIUS))
            mapOut!!.markerStyle = mPrefs!!.loadInt(Prefs.MARKER_STYLE)
        }

        addFragment(R.id.map, map)
        addFragment(R.id.mapOut, mapOut)
    }

    private fun addFragment(res: Int, fragment: MapFragment) {
        val fragMan = supportFragmentManager
        val fragTransaction = fragMan.beginTransaction()
        fragTransaction.add(res, fragment)
        fragTransaction.commitAllowingStateLoss()
    }

    /**
     * Set selecting reminder type spinner adapter.
     */
    private fun setUpNavigation() {
        val navSpinner = ArrayList<SpinnerItem>()
        navSpinner.add(SpinnerItem(getString(R.string.location), R.drawable.ic_place_white_24dp))
        navSpinner.add(SpinnerItem(getString(R.string.place_out), R.drawable.ic_beenhere_white_24dp))

        val adapter = TitleNavigationAdapter(applicationContext, navSpinner)
        spinner!!.adapter = adapter
        spinner!!.onItemSelectedListener = this
    }

    /**
     * Delete or move to trash reminder.
     */
    private fun deleteReminder() {
        Reminder.delete(id, this)
        closeWindow()
    }

    private fun closeWindow() {
        if (Module.isLollipop) {
            val enterTransition = Slide()
            enterTransition.duration = resources.getInteger(R.integer.anim_duration_long).toLong()
            window.returnTransition = enterTransition

            finishAfterTransition()
        }
    }

    /**
     * Show location radius selection dialog.
     */
    private fun selectRadius() {
        val i = Intent(this@ReminderManagerActivity, TargetRadius::class.java)
        i.putExtra("mReminder", 1)
        startActivityForResult(i, Constants.REQUEST_CODE_SELECTED_RADIUS)
    }

    /**
     * Open LED indicator color selecting window.
     */
    private fun chooseLEDColor() {
        val i = Intent(this@ReminderManagerActivity, LedColor::class.java)
        startActivityForResult(i, Constants.REQUEST_CODE_LED_COLOR)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                restoreTask()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        when (buttonView.id) {
            R.id.currentCheck -> if (currentCheck!!.isChecked) {
                mapCheck!!.isChecked = false
                mLocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
                mLocList = CurrentLocation()
                setLocationUpdates()
            }
            R.id.mapCheck -> if (mapCheck!!.isChecked) {
                currentCheck!!.isChecked = false
                toggleMap()
                removeUpdates()
            }
        }
    }

    private fun removeUpdates() {
        if (mLocList != null && mLocationManager != null) {
            if (Permissions.checkPermission(this@ReminderManagerActivity, Permissions.ACCESS_COARSE_LOCATION, Permissions.ACCESS_FINE_LOCATION)) {
                mLocationManager!!.removeUpdates(mLocList)
            } else {
                Permissions.requestPermission(this@ReminderManagerActivity, 201,
                        Permissions.ACCESS_FINE_LOCATION, Permissions.ACCESS_COARSE_LOCATION)
            }
        }
    }

    override fun placeChanged(place: LatLng) {
        curPlace = place
        if (isLocationOutAttached) {
            mapLocation!!.text = LocationUtil.getAddress(place.latitude, place.longitude)
        }
    }

    override fun onZoomClick(isFull: Boolean) {
        if (isFull) {
            toolbar!!.visibility = View.GONE
        } else {
            toolbar!!.visibility = View.VISIBLE
        }
    }

    override fun placeName(name: String) {

    }

    override fun onBackClick() {
        if (isLocationAttached) {
            if (map!!.isFullscreen) {
                map!!.isFullscreen = false
                toolbar!!.visibility = View.VISIBLE
            }
        }
        if (isLocationOutAttached) {
            if (mapOut!!.isFullscreen) {
                mapOut!!.isFullscreen = false
                toolbar!!.visibility = View.GONE
            }
        }
        toggleMap()
    }

    /**
     * Show location reminder type creation layout.
     */
    private fun attachLocation() {
        taskField!!.hint = getString(R.string.remind_me)

        val geolocationlayout = findViewById<LinearLayout>(R.id.geolocationlayout)
        ViewUtils.fadeInAnimation(geolocationlayout)

        mControl = LocationType(this, Constants.TYPE_LOCATION)

        delayLayout = findViewById(R.id.delayLayout)
        mapContainer = findViewById(R.id.mapContainer)
        specsContainer = findViewById(R.id.specsContainer)
        delayLayout!!.visibility = View.GONE
        mapContainer!!.visibility = View.GONE

        attackDelay = findViewById(R.id.attackDelay)
        attackDelay!!.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                delayLayout!!.visibility = View.VISIBLE
            } else {
                delayLayout!!.visibility = View.GONE
            }
        }

        if (attackDelay!!.isChecked) {
            delayLayout!!.visibility = View.VISIBLE
        }

        val clearField = findViewById<ImageButton>(R.id.clearButton)
        val mapButton = findViewById<ImageButton>(R.id.mapButton)

        clearField.setImageResource(R.drawable.ic_backspace_white_24dp)
        mapButton.setImageResource(R.drawable.ic_map_white_24dp)

        clearField.setOnClickListener { v -> addressField!!.setText("") }
        mapButton.setOnClickListener { v -> toggleMap() }

        addressField = findViewById(R.id.searchField)
        addressField!!.setListener { address -> if (address != null) showMarker(address) }

        actionViewLocation = findViewById(R.id.actionViewLocation)
        actionViewLocation!!.setListener(this)
        actionViewLocation!!.setActivity(this)

        val cal = Calendar.getInstance()
        cal.timeInMillis = System.currentTimeMillis()
        if (myYear > 0) {
            cal.set(myYear, myMonth, myDay, myHour, myMinute)

        } else {
            myYear = cal.get(Calendar.YEAR)
            myMonth = cal.get(Calendar.MONTH)
            myDay = cal.get(Calendar.DAY_OF_MONTH)
            myHour = cal.get(Calendar.HOUR_OF_DAY)
            myMinute = cal.get(Calendar.MINUTE)
        }

        val dateViewLocation = findViewById<DateTimeView>(R.id.dateViewLocation)
        dateViewLocation.setListener(this)
        dateViewLocation.setDateTime(cal.timeInMillis)

        if (curPlace != null) {
            if (map != null) {
                map!!.addMarker(curPlace, null, true, true, radius)
                toggleMap()
            }
        }

        if (id != 0L && isSame) {
            val text: String?
            val number: String?
            val remType: String?
            val latitude: Double
            val longitude: Double
            val style: Int
            if (mReminder != null) {
                text = mReminder!!.title
                number = mReminder!!.number
                remType = mReminder!!.type
                latitude = mReminder!!.place!![0]
                longitude = mReminder!!.place!![1]
                radius = mReminder!!.radius
                volume = mReminder!!.volume
                ledColor = mReminder!!.color
                style = mReminder!!.marker

                if (mReminder!!.startTime > 0) {
                    cal.timeInMillis = mReminder!!.startTime
                    dateViewLocation.setDateTime(cal.timeInMillis)
                    attackDelay!!.isChecked = true
                } else {
                    attackDelay!!.isChecked = false
                }

                if (remType!!.matches(Constants.TYPE_LOCATION_CALL.toRegex()) || remType.matches(Constants.TYPE_LOCATION_MESSAGE.toRegex())) {
                    actionViewLocation!!.setAction(true)
                    actionViewLocation!!.number = number
                    if (remType.matches(Constants.TYPE_LOCATION_CALL.toRegex())) {
                        actionViewLocation!!.type = ActionView.TYPE_CALL
                    } else {
                        actionViewLocation!!.type = ActionView.TYPE_MESSAGE
                    }
                } else {
                    actionViewLocation!!.setAction(false)
                }

                Log.d(Constants.LOG_TAG, "Lat $latitude, $longitude")
                taskField!!.setText(text)
                mItem = Item(text, LatLng(latitude, longitude), radius, style)
                if (isReady) {
                    map!!.addMarker(mItem!!.pos, mItem!!.title, true, mItem!!.style, true, mItem!!.radius)
                }
                toggleMap()
            }
        }
    }

    private fun showMarker(address: Address) {
        val lat = address.latitude
        val lon = address.longitude
        val pos = LatLng(lat, lon)
        curPlace = pos
        var title = taskField!!.text!!.toString().trim { it <= ' ' }
        if (title.matches("".toRegex())) {
            title = pos.toString()
        }
        if (map != null) {
            map!!.addMarker(pos, title, true, true, radius)
        }
    }

    private fun toggleMap() {
        if (isLocationAttached) {
            if (isMapVisible) {
                ViewUtils.fadeOutAnimation(mapContainer!!)
                ViewUtils.fadeInAnimation(specsContainer!!)
                ViewUtils.show(this, mFab!!)
            } else {
                ViewUtils.fadeOutAnimation(specsContainer!!)
                ViewUtils.fadeInAnimation(mapContainer!!)
                ViewUtils.hide(this, mFab!!)
                if (map != null) {
                    map!!.showShowcase()
                }
            }
        }
        if (isLocationOutAttached) {
            if (isMapVisible) {
                ViewUtils.fadeOutAnimation(mapContainerOut!!)
                ViewUtils.fadeInAnimation(specsContainerOut!!)
                ViewUtils.show(this, mFab!!)
            } else {
                ViewUtils.fadeOutAnimation(specsContainerOut!!)
                ViewUtils.fadeInAnimation(mapContainerOut!!)
                ViewUtils.hide(this, mFab!!)
                if (mapOut != null) {
                    mapOut!!.showShowcase()
                }
            }
        }
    }

    /**
     * Show location out reminder type creation layout.
     */
    private fun attachLocationOut() {
        taskField!!.hint = getString(R.string.remind_me)

        val locationOutLayout = findViewById<LinearLayout>(R.id.locationOutLayout)
        ViewUtils.fadeInAnimation(locationOutLayout)

        mControl = LocationType(this, Constants.TYPE_LOCATION_OUT)

        delayLayoutOut = findViewById(R.id.delayLayoutOut)
        specsContainerOut = findViewById(R.id.specsContainerOut)
        mapContainerOut = findViewById(R.id.mapContainerOut)
        delayLayoutOut!!.visibility = View.GONE
        mapContainerOut!!.visibility = View.GONE

        attachDelayOut = findViewById(R.id.attachDelayOut)
        attachDelayOut!!.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                delayLayoutOut!!.visibility = View.VISIBLE
            } else {
                delayLayoutOut!!.visibility = View.GONE
            }
        }

        if (attachDelayOut!!.isChecked) {
            delayLayoutOut!!.visibility = View.VISIBLE
        }
        val mapButtonOut = findViewById<ImageButton>(R.id.mapButtonOut)
        mapButtonOut.setImageResource(R.drawable.ic_map_white_24dp)

        mapButtonOut.setOnClickListener { v ->
            if (mapCheck!!.isChecked) {
                toggleMap()
            }
            mapCheck!!.isChecked = true
        }
        currentLocation = findViewById(R.id.currentLocation)
        mapLocation = findViewById(R.id.mapLocation)
        radiusMark = findViewById(R.id.radiusMark)

        currentCheck = findViewById(R.id.currentCheck)
        mapCheck = findViewById(R.id.mapCheck)
        currentCheck!!.setOnCheckedChangeListener(this)
        mapCheck!!.setOnCheckedChangeListener(this)
        currentCheck!!.isChecked = true

        pointRadius = findViewById(R.id.pointRadius)
        pointRadius!!.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                radiusMark!!.text = String.format(getString(R.string.selected_radius_meters), progress.toString())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {

            }
        })
        if (pointRadius!!.progress == 0 && mPrefs != null) {
            pointRadius!!.progress = mPrefs!!.loadInt(Prefs.LOCATION_RADIUS)
        }

        actionViewLocationOut = findViewById(R.id.actionViewLocationOut)
        actionViewLocationOut!!.setListener(this)
        actionViewLocationOut!!.setActivity(this)

        val cal = Calendar.getInstance()
        cal.timeInMillis = System.currentTimeMillis()
        if (myYear > 0) {
            cal.set(myYear, myMonth, myDay, myHour, myMinute)
        } else {
            myYear = cal.get(Calendar.YEAR)
            myMonth = cal.get(Calendar.MONTH)
            myDay = cal.get(Calendar.DAY_OF_MONTH)
            myHour = cal.get(Calendar.HOUR_OF_DAY)
            myMinute = cal.get(Calendar.MINUTE)
        }

        val dateViewLocationOut = findViewById<DateTimeView>(R.id.dateViewLocationOut)
        dateViewLocationOut.setListener(this)
        dateViewLocationOut.setDateTime(cal.timeInMillis)

        if (curPlace != null) {
            if (mapOut != null) {
                mapOut!!.addMarker(curPlace, null, true, true, radius)
            }
            mapLocation!!.text = LocationUtil.getAddress(curPlace!!.latitude, curPlace!!.longitude)
        }

        if (id != 0L && isSame) {
            val text: String?
            val number: String?
            val remType: String?
            val latitude: Double
            val longitude: Double
            val style: Int
            if (mReminder != null) {
                text = mReminder!!.title
                number = mReminder!!.number
                remType = mReminder!!.type
                latitude = mReminder!!.place!![0]
                longitude = mReminder!!.place!![1]
                radius = mReminder!!.radius
                volume = mReminder!!.volume
                ledColor = mReminder!!.color
                style = mReminder!!.marker

                if (mReminder!!.startTime > 0) {
                    cal.set(myYear, myMonth, myDay, myHour, myMinute)

                    dateViewLocationOut.setDateTime(cal.timeInMillis)
                    attachDelayOut!!.isChecked = true
                } else {
                    attachDelayOut!!.isChecked = false
                }

                if (remType!!.matches(Constants.TYPE_LOCATION_OUT_CALL.toRegex()) || remType.matches(Constants.TYPE_LOCATION_OUT_MESSAGE.toRegex())) {
                    actionViewLocationOut!!.setAction(true)
                    actionViewLocationOut!!.number = number
                    if (remType.matches(Constants.TYPE_LOCATION_OUT_CALL.toRegex())) {
                        actionViewLocationOut!!.type = ActionView.TYPE_CALL
                    } else {
                        actionViewLocationOut!!.type = ActionView.TYPE_MESSAGE
                    }
                } else {
                    actionViewLocationOut!!.setAction(false)
                }

                taskField!!.setText(text)
                val pos = LatLng(latitude, longitude)
                mItem = Item(text, pos, radius, style)
                if (isReadyOut) {
                    mapOut!!.addMarker(mItem!!.pos, mItem!!.title, true, mItem!!.style, true, mItem!!.radius)
                }
                mapLocation!!.text = LocationUtil.getAddress(pos.latitude, pos.longitude)
                mapCheck!!.isChecked = true
            }
        }
    }

    /**
     * Save new or update current reminder.
     */
    private fun save() {
        if (mControl == null) return
        val item = data ?: return
        if (id != 0L) {
            mControl!!.save(id, item)
        } else {
            mControl!!.save(item)
        }
        closeWindow()
    }

    /**
     * Get reminder type string.
     *
     * @return String
     */
    private fun getType(): String {
        val type: String
        if (mControl != null && mControl!!.type!!.startsWith(Constants.TYPE_LOCATION_OUT)) {
            if (actionViewLocationOut!!.hasAction()) {
                if (actionViewLocationOut!!.type == ActionView.TYPE_CALL) {
                    type = Constants.TYPE_LOCATION_OUT_CALL
                } else {
                    type = Constants.TYPE_LOCATION_OUT_MESSAGE
                }
            } else {
                type = Constants.TYPE_LOCATION_OUT
            }
        } else {
            if (actionViewLocation!!.hasAction()) {
                if (actionViewLocation!!.type == ActionView.TYPE_CALL) {
                    type = Constants.TYPE_LOCATION_CALL
                } else {
                    type = Constants.TYPE_LOCATION_MESSAGE
                }
            } else {
                type = Constants.TYPE_LOCATION
            }
        }
        return type
    }

    /**
     * Check if number inserted.
     *
     * @return Boolean
     */
    private fun checkNumber(): Boolean {
        if (isLocationAttached && actionViewLocation!!.hasAction()) {
            val `is` = actionViewLocation!!.number.matches("".toRegex())
            if (`is`) {
                actionViewLocation!!.showError()
                return true
            } else {
                return false
            }
        } else if (isLocationOutAttached && actionViewLocationOut!!.hasAction()) {
            val `is` = actionViewLocationOut!!.number.matches("".toRegex())
            if (`is`) {
                actionViewLocationOut!!.showError()
                return true
            } else {
                return false
            }
        } else {
            return false
        }
    }

    override fun onBackPressed() {
        if (map != null && !map!!.onBackPressed()) {
            return
        }
        if (mapOut != null && !mapOut!!.onBackPressed()) {
            return
        }

        restoreTask()
    }

    /**
     * Restore currently edited reminder.
     */
    private fun restoreTask() {
        if (id != 0L) {
            val db = DataBase(this)
            db.open()
            val c = db.getReminder(id)
            if (c != null && c.moveToFirst()) {
                val startTime = c.getLong(c.getColumnIndex(DataBase.START_TIME))
                val status = c.getInt(c.getColumnIndex(DataBase.STATUS_DB))
                if (status == Constants.ENABLE) {
                    if (startTime != 1L) {
                        PositionDelayReceiver().setAlarm(this, id)
                    } else {
                        if (!SuperUtil.isServiceRunning(this@ReminderManagerActivity, GeolocationService::class.java)) {
                            startService(Intent(this@ReminderManagerActivity, GeolocationService::class.java)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                        }
                    }
                }
            }
            c?.close()
            db.close()
        }
        val db = DataBase(this)
        db.open()
        val c = db.getReminders(Constants.ENABLE)
        if (c != null && c.moveToFirst()) {
            var i = 0
            do {
                val isDone = c.getInt(c.getColumnIndex(DataBase.STATUS_DB))
                if (isDone == Constants.ENABLE) {
                    i++
                }
            } while (c.moveToNext())
            if (i > 0) {
                if (!SuperUtil.isServiceRunning(this@ReminderManagerActivity, GeolocationService::class.java)) {
                    startService(Intent(this@ReminderManagerActivity, GeolocationService::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                }
            }
        }
        c?.close()
        closeWindow()
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        if (navContainer!!.visibility == View.VISIBLE) {
            switchIt(position)
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>) {

    }

    /**
     * Show reminder layout.
     *
     * @param position spinner position.
     */
    private fun switchIt(position: Int) {
        radius = -1
        when (position) {
            0 -> {
                detachCurrentView()
                if (LocationUtil.playServicesFullCheck(this@ReminderManagerActivity)) {
                    if (Permissions.checkPermission(this@ReminderManagerActivity, Permissions.ACCESS_FINE_LOCATION,
                                    Permissions.CALL_PHONE, Permissions.SEND_SMS, Permissions.ACCESS_COARSE_LOCATION,
                                    Permissions.READ_CONTACTS)) {
                        attachLocation()
                    } else {
                        Permissions.requestPermission(this@ReminderManagerActivity, 105,
                                Permissions.ACCESS_COARSE_LOCATION,
                                Permissions.ACCESS_FINE_LOCATION, Permissions.CALL_PHONE,
                                Permissions.SEND_SMS, Permissions.READ_CONTACTS)
                    }
                } else {
                    spinner!!.setSelection(0)
                }
            }
            1 -> {
                detachCurrentView()
                if (LocationUtil.playServicesFullCheck(this@ReminderManagerActivity)) {
                    if (Permissions.checkPermission(this@ReminderManagerActivity, Permissions.ACCESS_FINE_LOCATION,
                                    Permissions.CALL_PHONE, Permissions.SEND_SMS, Permissions.ACCESS_COARSE_LOCATION,
                                    Permissions.READ_CONTACTS)) {
                        attachLocationOut()
                    } else {
                        Permissions.requestPermission(this@ReminderManagerActivity, 106,
                                Permissions.ACCESS_COARSE_LOCATION,
                                Permissions.ACCESS_FINE_LOCATION, Permissions.CALL_PHONE,
                                Permissions.SEND_SMS, Permissions.READ_CONTACTS)
                    }
                } else {
                    spinner!!.setSelection(0)
                }
            }
        }
        if (mPrefs != null) mPrefs!!.saveInt(Prefs.LAST_USED_REMINDER, position)
        invalidateOptionsMenu()
    }

    private fun detachCurrentView() {
        if (mFab!!.visibility != View.VISIBLE) {
            ViewUtils.show(this, mFab!!)
        }
        if (toolbar!!.visibility == View.GONE) {
            toolbar!!.visibility = View.VISIBLE
        }
        if (isLocationAttached) {
            findViewById<View>(R.id.geolocationlayout).visibility = View.GONE
        }
        if (isLocationOutAttached) {
            findViewById<View>(R.id.locationOutLayout).visibility = View.GONE
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        if (grantResults.size == 0) return
        when (requestCode) {
            105 -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                attachLocation()
            } else {
                spinner!!.setSelection(0)
            }
            106 -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                attachLocationOut()
            } else {
                spinner!!.setSelection(0)
            }
            107 -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                SuperUtil.selectContact(this@ReminderManagerActivity, Constants.REQUEST_CODE_CONTACTS)
            } else {
                showSnackbar(R.string.cant_access_to_contacts)
            }
            200 -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startActivityForResult(Intent(this@ReminderManagerActivity, FileExplorerActivity::class.java),
                        Constants.REQUEST_CODE_SELECTED_MELODY)
            } else {
                showSnackbar(R.string.cant_read_external_storage)
            }
            201 -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                removeUpdates()
            } else {
                showSnackbar(R.string.cant_access_location_services)
            }
            202 -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setLocationUpdates()
            } else {
                showSnackbar(R.string.cant_access_location_services)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == Constants.REQUEST_CODE_CONTACTS) {
            if (resultCode == Activity.RESULT_OK) {
                //Use Data to get string
                val number = data!!.getStringExtra(Constants.SELECTED_CONTACT_NUMBER)
                if (isLocationAttached && actionViewLocation!!.hasAction()) {
                    actionViewLocation!!.number = number
                }
                if (isLocationOutAttached && actionViewLocationOut!!.hasAction()) {
                    actionViewLocationOut!!.number = number
                }
            }
        }

        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val matches = data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (matches != null) {
                val text = matches[0].toString()
                taskField!!.setText(text)
            }
        }

        if (requestCode == Constants.REQUEST_CODE_SELECTED_MELODY) {
            if (resultCode == Activity.RESULT_OK) {
                melody = data!!.getStringExtra(Constants.FILE_PICKED)
                if (melody != null) {
                    val musicFile = File(melody)
                    val str = getString(R.string.selected_melody) + " " + musicFile.name
                    showSnackbar(str, R.string.dismiss) { v -> melody = null }
                }
            }
        }

        if (requestCode == Constants.REQUEST_CODE_SELECTED_RADIUS) {
            if (resultCode == Activity.RESULT_OK) {
                radius = data!!.getIntExtra(Constants.SELECTED_RADIUS, -1)
                if (radius != -1) {
                    val str = String.format(getString(R.string.selected_radius_meters), radius.toString())
                    showSnackbar(str, R.string.dismiss) { v -> radius = -1 }
                    if (isLocationAttached) {
                        map!!.recreateMarker(radius)
                    }
                    if (isLocationOutAttached) {
                        mapOut!!.recreateMarker(radius)
                        pointRadius!!.progress = radius
                    }
                }
            }
        }

        if (requestCode == Constants.REQUEST_CODE_LED_COLOR) {
            if (resultCode == Activity.RESULT_OK) {
                val position = data!!.getIntExtra(Constants.SELECTED_LED_COLOR, -1)
                val selColor = LED.getTitle(this, position)
                ledColor = LED.getLED(position)

                val str = String.format(getString(R.string.selected_led_color), selColor)
                showSnackbar(str, R.string.dismiss) { v -> ledColor = -1 }
            }
        }

        if (requestCode == Constants.REQUEST_CODE_VOLUME) {
            if (resultCode == Activity.RESULT_OK) {
                volume = data!!.getIntExtra(Constants.SELECTED_VOLUME, -1)

                val str = String.format(getString(R.string.set_volume_for_reminder), volume.toString())
                showSnackbar(str, R.string.dismiss) { v -> volume = -1 }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.create_menu, menu)
        if (isLocationAttached) {
            menu.getItem(1).isVisible = true
        }
        if (mPrefs != null && mPrefs!!.loadBoolean(Prefs.LED_STATUS)) {
            menu.getItem(2).isVisible = true
        }
        if (id != 0L) {
            menu.add(Menu.NONE, MENU_ITEM_DELETE, 100, getString(R.string.delete))
        }
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        if (isLocationAttached) {
            menu.getItem(1).isVisible = true
        }
        if (mPrefs != null && mPrefs!!.loadBoolean(Prefs.LED_STATUS)) {
            menu.getItem(2).isVisible = true
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onDestroy() {
        removeUpdates()
        val imm = getSystemService(
                Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm?.hideSoftInputFromWindow(taskField!!.windowToken, 0)
        Widget.updateWidgets(this@ReminderManagerActivity)
        super.onDestroy()
    }

    override fun onDateSelect(mills: Long, day: Int, month: Int, year: Int) {
        myDay = day
        myMonth = month
        myYear = year
    }

    override fun onTimeSelect(mills: Long, hour: Int, minute: Int) {
        myHour = hour
        myMinute = minute
    }

    override fun onActionChange(b: Boolean) {
        if (!b) {
            taskField!!.hint = getString(R.string.remind_me)
        }
    }

    override fun onTypeChange(type: Boolean) {
        if (type) {
            taskField!!.setHint(R.string.message)
        } else {
            taskField!!.hint = getString(R.string.remind_me)
        }
    }

    override fun showSnackbar(message: Int, actionTitle: Int, listener: View.OnClickListener) {
        Snackbar.make(mFab!!, message, Snackbar.LENGTH_LONG)
                .setAction(actionTitle, listener)
                .show()
    }

    override fun showSnackbar(message: Int) {
        Snackbar.make(mFab!!, message, Snackbar.LENGTH_LONG)
                .show()
    }

    override fun showSnackbar(message: String) {
        Snackbar.make(mFab!!, message, Snackbar.LENGTH_LONG)
                .show()
    }

    override fun showSnackbar(message: String, actionTitle: Int, listener: View.OnClickListener) {
        Snackbar.make(mFab!!, message, Snackbar.LENGTH_LONG)
                .setAction(actionTitle, listener)
                .show()
    }

    private fun setLocationUpdates() {
        if (mLocList != null) {
            val time = (if (mPrefs != null) mPrefs!!.loadInt(Prefs.TRACK_TIME) * 1000 else 10000).toLong()
            val distance = if (mPrefs != null) mPrefs!!.loadInt(Prefs.TRACK_DISTANCE) else 10
            if (Permissions.checkPermission(this@ReminderManagerActivity, Permissions.ACCESS_COARSE_LOCATION,
                            Permissions.ACCESS_FINE_LOCATION)) {
                mLocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
                if (mLocationManager != null) {
                    mLocationManager!!.requestLocationUpdates(LocationManager.GPS_PROVIDER, time,
                            distance.toFloat(), mLocList)
                    mLocationManager!!.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, time,
                            distance.toFloat(), mLocList)
                }
            } else {
                Permissions.requestPermission(this@ReminderManagerActivity, 202,
                        Permissions.ACCESS_FINE_LOCATION, Permissions.ACCESS_COARSE_LOCATION)
            }
        }
    }

    private inner class CurrentLocation : LocationListener {

        override fun onLocationChanged(location: Location) {
            val currentLat = location.latitude
            val currentLong = location.longitude
            curPlace = LatLng(currentLat, currentLong)
            val _Location = LocationUtil.getAddress(currentLat, currentLong)
            var text = taskField!!.text!!.toString().trim { it <= ' ' }
            if (text.matches("".toRegex())) text = _Location
            if (isLocationOutAttached) {
                currentLocation!!.text = _Location
                if (mapOut != null) {
                    mapOut!!.addMarker(LatLng(currentLat, currentLong), text, true, true, radius)
                }
            }
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
            setLocationUpdates()
        }

        override fun onProviderEnabled(provider: String) {
            setLocationUpdates()
        }

        override fun onProviderDisabled(provider: String) {
            setLocationUpdates()
        }
    }

    private inner class Item constructor(val title: String, val pos: LatLng, val radius: Int, val style: Int)

    companion object {

        private val TAG = "ReminderManagerActivity"

        private val VOICE_RECOGNITION_REQUEST_CODE = 109
        private val MENU_ITEM_DELETE = 12
    }
}