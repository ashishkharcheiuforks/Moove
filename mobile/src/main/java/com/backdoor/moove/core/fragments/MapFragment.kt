package com.backdoor.moove.core.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.location.Address
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView

import com.backdoor.moove.R
import com.backdoor.moove.core.adapters.PlaceAdapter
import com.backdoor.moove.modern_ui.places.list.PlacesAdapter
import com.backdoor.moove.core.async.GeocoderTask
import com.backdoor.moove.core.consts.Configs
import com.backdoor.moove.core.consts.Constants
import com.backdoor.moove.core.consts.Prefs
import com.backdoor.moove.core.data.PlaceDataProvider
import com.backdoor.moove.utils.Coloring
import com.backdoor.moove.core.helper.DataBase
import com.backdoor.moove.core.helper.Messages
import com.backdoor.moove.core.helper.Module
import com.backdoor.moove.core.helper.Permissions
import com.backdoor.moove.core.helper.SharedPrefs
import com.backdoor.moove.core.interfaces.MapListener
import com.backdoor.moove.core.interfaces.SimpleListener
import com.backdoor.moove.core.utils.QuickReturnUtils
import com.backdoor.moove.core.utils.ViewUtils
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

import java.util.ArrayList

import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig

class MapFragment : Fragment(), View.OnClickListener {

    /**
     * UI elements;
     */
    private var map: GoogleMap? = null
    private var layersContainer: CardView? = null
    private var styleCard: CardView? = null
    private var placesListCard: CardView? = null
    private var cardSearch: AutoCompleteTextView? = null
    private var zoomOut: ImageButton? = null
    private var backButton: ImageButton? = null
    private var places: ImageButton? = null
    private var markers: ImageButton? = null
    private var groupOne: LinearLayout? = null
    private var groupTwo: LinearLayout? = null
    private var groupThree: LinearLayout? = null
    private var placesList: RecyclerView? = null
    private var emptyItem: LinearLayout? = null

    /**
     * Array of user frequently used places;
     */
    private var spinnerArray = ArrayList<String>()

    private var placesAdapter: PlacesAdapter? = null

    /**
     * init variables and flags;
     */
    private var isTouch = true
    private var isZoom = true
    private var isBack = true
    private var isStyles = true
    private var isPlaces = true
    private var isSearch = true
    var isFullscreen = false
    private var markerTitle: String? = null
    private var markerRadius = -1
    /**
     * Get currently used marker style.
     *
     * @return marker code.
     */
    /**
     * Set style for marker;
     *
     * @param markerStyle code of style for marker
     */
    var markerStyle = -1
    private var type: Int = 0
    private var lastPos: LatLng? = null
    private val strokeWidth = 3f

    /**
     * UI helper class;
     */
    private var cSetter: Coloring? = null
    private var mCallback: MapCallback? = null

    /**
     * Arrays of place search results;
     */
    private var foundPlaces: List<Address>? = null
    private var adapter: ArrayAdapter<String>? = null
    private var task: GeocoderTask? = null
    private var namesList: ArrayList<String>? = null

    /**
     * MapListener link;
     */
    private var listener: MapListener? = null

    private val mMapReadyCallback = OnMapReadyCallback { googleMap ->
        map = googleMap
        map!!.uiSettings.isMyLocationButtonEnabled = false
        map!!.uiSettings.isCompassEnabled = true
        map!!.mapType = type
        setMyLocation()
        map!!.setOnMapClickListener { latLng ->
            hideLayers()
            hidePlaces()
            hideStyles()
            if (isTouch) {
                addMarker(latLng, markerTitle, true, true, markerRadius)
            }
        }
        if (mCallback != null) {
            mCallback!!.onMapReady()
        }
    }

    private val isMarkersVisible: Boolean
        get() = styleCard != null && styleCard!!.visibility == View.VISIBLE

    private val isPlacesVisible: Boolean
        get() = placesListCard != null && placesListCard!!.visibility == View.VISIBLE

    private val isLayersVisible: Boolean
        get() = layersContainer != null && layersContainer!!.visibility == View.VISIBLE

    fun setMapReadyCallback(callback: MapCallback) {
        this.mCallback = callback
    }

    fun setAdapter(adapter: PlacesAdapter) {
        this.placesAdapter = adapter
    }

    /**
     * Set listener for map fragment;
     *
     * @param listener listener for map fragment
     */
    fun setListener(listener: MapListener) {
        this.listener = listener
    }

    /**
     * Set title for markers;
     *
     * @param markerTitle marker title
     */
    fun setMarkerTitle(markerTitle: String) {
        this.markerTitle = markerTitle
    }

    /**
     * Set radius for marker;
     *
     * @param markerRadius radius for drawing circle around marker
     */
    fun setMarkerRadius(markerRadius: Int) {
        this.markerRadius = markerRadius
    }

    /**
     * Add marker to map;
     *
     * @param pos     coordinates
     * @param title   marker title
     * @param clear   remove previous markers flag
     * @param animate animate to marker position
     * @param radius  radius for circle around marker
     */
    fun addMarker(pos: LatLng, title: String?, clear: Boolean, animate: Boolean, radius: Int) {
        var title = title
        if (map != null) {
            markerRadius = radius
            if (markerRadius == -1) {
                markerRadius = SharedPrefs.getInstance(activity)!!.loadInt(Prefs.LOCATION_RADIUS)
            }
            if (clear) {
                map!!.clear()
            }
            if (title == null || title.matches("".toRegex())) {
                title = pos.toString()
            }
            lastPos = pos
            if (listener != null) {
                listener!!.placeChanged(pos)
            }
            map!!.addMarker(MarkerOptions()
                    .position(pos)
                    .title(title)
                    .icon(BitmapDescriptorFactory.fromResource(cSetter!!.getMarkerStyle(markerStyle)))
                    .draggable(clear))
            val circleColors = cSetter!!.getMarkerRadiusStyle(markerStyle)
            map!!.addCircle(CircleOptions()
                    .center(pos)
                    .radius(markerRadius.toDouble())
                    .strokeWidth(strokeWidth)
                    .fillColor(ViewUtils.getColor(activity, circleColors[0]))
                    .strokeColor(ViewUtils.getColor(activity, circleColors[1])))
            if (animate) {
                animate(pos)
            }
        }
    }

    /**
     * Add marker to map with custom marker icon;
     *
     * @param pos         coordinates
     * @param title       marker title
     * @param clear       remove previous markers flag
     * @param markerStyle marker icon
     * @param animate     animate to marker position
     * @param radius      radius for circle around marker
     */
    fun addMarker(pos: LatLng, title: String?, clear: Boolean, markerStyle: Int, animate: Boolean, radius: Int) {
        var title = title
        if (map != null) {
            markerRadius = radius
            if (markerRadius == -1) {
                markerRadius = SharedPrefs.getInstance(activity)!!.loadInt(Prefs.LOCATION_RADIUS)
            }
            this.markerStyle = markerStyle
            if (clear) {
                map!!.clear()
            }
            if (title == null || title.matches("".toRegex())) {
                title = pos.toString()
            }
            lastPos = pos
            if (listener != null) {
                listener!!.placeChanged(pos)
            }
            map!!.addMarker(MarkerOptions()
                    .position(pos)
                    .title(title)
                    .icon(BitmapDescriptorFactory.fromResource(cSetter!!.getMarkerStyle(markerStyle)))
                    .draggable(clear))
            val circleColors = cSetter!!.getMarkerRadiusStyle(markerStyle)
            map!!.addCircle(CircleOptions()
                    .center(pos)
                    .radius(markerRadius.toDouble())
                    .strokeWidth(strokeWidth)
                    .fillColor(ViewUtils.getColor(activity, circleColors[0]))
                    .strokeColor(ViewUtils.getColor(activity, circleColors[1])))
            if (animate) {
                animate(pos)
            }
        } else {
            Log.d(Constants.LOG_TAG, "map is null")
        }
    }

    /**
     * Recreate last added marker with new circle radius;
     *
     * @param radius radius for a circle
     */
    fun recreateMarker(radius: Int) {
        markerRadius = radius
        if (markerRadius == -1) {
            markerRadius = SharedPrefs.getInstance(activity)!!.loadInt(Prefs.LOCATION_RADIUS)
        }
        if (map != null && lastPos != null) {
            map!!.clear()
            if (markerTitle == null || markerTitle!!.matches("".toRegex())) {
                markerTitle = lastPos!!.toString()
            }
            if (listener != null) {
                listener!!.placeChanged(lastPos)
            }
            map!!.addMarker(MarkerOptions()
                    .position(lastPos!!)
                    .title(markerTitle)
                    .icon(BitmapDescriptorFactory.fromResource(cSetter!!.getMarkerStyle(markerStyle)))
                    .draggable(true))
            val circleColors = cSetter!!.getMarkerRadiusStyle(markerStyle)
            map!!.addCircle(CircleOptions()
                    .center(lastPos)
                    .radius(markerRadius.toDouble())
                    .strokeWidth(strokeWidth)
                    .fillColor(ViewUtils.getColor(activity, circleColors[0]))
                    .strokeColor(ViewUtils.getColor(activity, circleColors[1])))
            animate(lastPos)
        }
    }

    /**
     * Recreate last added marker with new marker style;
     *
     * @param style marker style.
     */
    fun recreateStyle(style: Int) {
        markerStyle = style
        if (map != null && lastPos != null) {
            map!!.clear()
            if (markerTitle == null || markerTitle!!.matches("".toRegex())) {
                markerTitle = lastPos!!.toString()
            }
            if (listener != null) {
                listener!!.placeChanged(lastPos)
            }
            map!!.addMarker(MarkerOptions()
                    .position(lastPos!!)
                    .title(markerTitle)
                    .icon(BitmapDescriptorFactory.fromResource(cSetter!!.getMarkerStyle(style)))
                    .draggable(true))
            if (style >= 0) {
                val circleColors = cSetter!!.getMarkerRadiusStyle(style)
                if (markerRadius == -1) {
                    markerRadius = SharedPrefs.getInstance(activity)!!.loadInt(Prefs.LOCATION_RADIUS)
                }
                map!!.addCircle(CircleOptions()
                        .center(lastPos)
                        .radius(markerRadius.toDouble())
                        .strokeWidth(strokeWidth)
                        .fillColor(ViewUtils.getColor(activity, circleColors[0]))
                        .strokeColor(ViewUtils.getColor(activity, circleColors[1])))
            }
            animate(lastPos)
        }
    }

    /**
     * Move camera to coordinates;
     *
     * @param pos coordinates
     */
    fun moveCamera(pos: LatLng) {
        if (map != null) {
            animate(pos)
        }
    }

    /**
     * Move camera to coordinates with animation;
     *
     * @param latLng coordinates
     */
    fun animate(latLng: LatLng) {
        val update = CameraUpdateFactory.newLatLngZoom(latLng, 13f)
        if (map != null) {
            map!!.animateCamera(update)
        }
    }

    /**
     * Move camera to user current coordinates with animation;
     */
    fun moveToMyLocation() {
        try {
            if (map != null && map!!.myLocation != null) {
                val lat = map!!.myLocation.latitude
                val lon = map!!.myLocation.longitude
                val pos = LatLng(lat, lon)
                animate(pos)
            }
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }

    }

    /**
     * On back pressed interface for map;
     *
     * @return boolean
     */
    fun onBackPressed(): Boolean {
        if (isLayersVisible) {
            hideLayers()
            return false
        } else if (isMarkersVisible) {
            hideStyles()
            return false
        } else if (isPlacesVisible) {
            hidePlaces()
            return false
        } else {
            return true
        }
    }

    fun showShowcase() {
        if (!SharedPrefs.getInstance(activity)!!.loadBoolean(HAS_SHOWCASE) && isBack) {
            SharedPrefs.getInstance(activity)!!.saveBoolean(HAS_SHOWCASE, true)
            val coloring = Coloring(activity)
            val config = ShowcaseConfig()
            config.delay = 350
            config.maskColor = coloring.colorAccent()
            config.contentTextColor = coloring.getColor(R.color.whitePrimary)
            config.dismissTextColor = coloring.getColor(R.color.whitePrimary)

            val sequence = MaterialShowcaseSequence(activity)
            sequence.setConfig(config)

            sequence.addSequenceItem(zoomOut,
                    activity!!.getString(R.string.click_to_expand_collapse_map),
                    activity!!.getString(R.string.got_it))

            sequence.addSequenceItem(backButton,
                    activity!!.getString(R.string.click_when_add_place),
                    activity!!.getString(R.string.got_it))

            sequence.addSequenceItem(markers,
                    activity!!.getString(R.string.select_style_for_marker),
                    activity!!.getString(R.string.got_it))

            sequence.addSequenceItem(places,
                    activity!!.getString(R.string.select_place_from_list),
                    activity!!.getString(R.string.got_it))
            sequence.start()
        }
    }

    private fun initArgs() {
        val args = arguments
        if (args != null) {
            isTouch = args.getBoolean(ENABLE_TOUCH, true)
            isPlaces = args.getBoolean(ENABLE_PLACES, true)
            isSearch = args.getBoolean(ENABLE_SEARCH, true)
            isStyles = args.getBoolean(ENABLE_STYLES, true)
            isBack = args.getBoolean(ENABLE_BACK, true)
            isZoom = args.getBoolean(ENABLE_ZOOM, true)
            markerStyle = args.getInt(MARKER_STYLE,
                    SharedPrefs.getInstance(activity)!!.loadInt(Prefs.MARKER_STYLE))
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        initArgs()
        val view = inflater.inflate(R.layout.fragment_map, container, false)
        val prefs = SharedPrefs.getInstance(activity)
        cSetter = Coloring(activity)
        type = prefs!!.loadInt(Prefs.MAP_TYPE)
        markerRadius = prefs.loadInt(Prefs.LOCATION_RADIUS)
        (childFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment)
                .getMapAsync(mMapReadyCallback)
        if (lastPos != null) {
            addMarker(lastPos, lastPos!!.toString(), true, false, markerRadius)
        }

        initViews(view)

        cardSearch = view.findViewById(R.id.cardSearch)
        cardSearch!!.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_search_white_24dp, 0, 0, 0)
        cardSearch!!.threshold = 3
        adapter = ArrayAdapter(activity!!, android.R.layout.simple_dropdown_item_1line, namesList!!)
        adapter!!.setNotifyOnChange(true)
        cardSearch!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                hideLayers()
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (task != null && !task!!.isCancelled) {
                    task!!.cancel(true)
                }
                if (s.length != 0) {
                    task = GeocoderTask(activity) { addresses ->
                        foundPlaces = addresses

                        namesList = ArrayList()
                        namesList!!.clear()
                        for (selected in addresses) {
                            val addressText = String.format("%s, %s%s",
                                    if (selected.maxAddressLineIndex > 0) selected.getAddressLine(0) else "",
                                    if (selected.maxAddressLineIndex > 1) selected.getAddressLine(1) + ", " else "",
                                    selected.countryName)
                            namesList!!.add(addressText)
                        }
                        adapter = ArrayAdapter(activity!!, android.R.layout.simple_dropdown_item_1line, namesList!!)
                        cardSearch!!.setAdapter<ArrayAdapter<String>>(adapter)
                        adapter!!.notifyDataSetChanged()
                    }
                    task!!.execute(s.toString())
                }
            }

            override fun afterTextChanged(s: Editable) {

            }
        })
        cardSearch!!.setOnItemClickListener { parent, view1, position, id ->
            val sel = foundPlaces!![position]
            val lat = sel.latitude
            val lon = sel.longitude
            val pos = LatLng(lat, lon)
            addMarker(pos, markerTitle, true, true, markerRadius)
            if (listener != null) {
                listener!!.placeName(namesList!![position])
            }
        }

        placesList = view.findViewById(R.id.placesList)
        loadPlaces()

        return view
    }

    private fun initViews(view: View) {
        groupOne = view.findViewById(R.id.groupOne)
        groupTwo = view.findViewById(R.id.groupTwo)
        groupThree = view.findViewById(R.id.groupThree)
        emptyItem = view.findViewById(R.id.emptyItem)

        placesList = view.findViewById(R.id.placesList)

        val zoomCard = view.findViewById<CardView>(R.id.zoomCard)
        val searchCard = view.findViewById<CardView>(R.id.searchCard)
        val myCard = view.findViewById<CardView>(R.id.myCard)
        val layersCard = view.findViewById<CardView>(R.id.layersCard)
        val placesCard = view.findViewById<CardView>(R.id.placesCard)
        val backCard = view.findViewById<CardView>(R.id.backCard)
        styleCard = view.findViewById(R.id.styleCard)
        placesListCard = view.findViewById(R.id.placesListCard)
        val markersCard = view.findViewById<CardView>(R.id.markersCard)
        placesListCard!!.visibility = View.GONE
        styleCard!!.visibility = View.GONE

        zoomCard.setCardBackgroundColor(cSetter!!.cardStyle)
        searchCard.setCardBackgroundColor(cSetter!!.cardStyle)
        myCard.setCardBackgroundColor(cSetter!!.cardStyle)
        layersCard.setCardBackgroundColor(cSetter!!.cardStyle)
        placesCard.setCardBackgroundColor(cSetter!!.cardStyle)
        styleCard!!.setCardBackgroundColor(cSetter!!.cardStyle)
        placesListCard!!.setCardBackgroundColor(cSetter!!.cardStyle)
        markersCard.setCardBackgroundColor(cSetter!!.cardStyle)
        backCard.setCardBackgroundColor(cSetter!!.cardStyle)

        layersContainer = view.findViewById(R.id.layersContainer)
        layersContainer!!.visibility = View.GONE
        layersContainer!!.setCardBackgroundColor(cSetter!!.cardStyle)

        if (Module.isLollipop) {
            zoomCard.cardElevation = Configs.CARD_ELEVATION
            searchCard.cardElevation = Configs.CARD_ELEVATION
            myCard.cardElevation = Configs.CARD_ELEVATION
            layersContainer!!.cardElevation = Configs.CARD_ELEVATION
            layersCard.cardElevation = Configs.CARD_ELEVATION
            placesCard.cardElevation = Configs.CARD_ELEVATION
            styleCard!!.cardElevation = Configs.CARD_ELEVATION
            placesListCard!!.cardElevation = Configs.CARD_ELEVATION
            markersCard.cardElevation = Configs.CARD_ELEVATION
            backCard.cardElevation = Configs.CARD_ELEVATION
        }

        val cardClear = view.findViewById<ImageButton>(R.id.cardClear)
        zoomOut = view.findViewById(R.id.mapZoom)
        val layers = view.findViewById<ImageButton>(R.id.layers)
        val myLocation = view.findViewById<ImageButton>(R.id.myLocation)
        markers = view.findViewById(R.id.markers)
        places = view.findViewById(R.id.places)
        backButton = view.findViewById(R.id.backButton)

        cardClear.setOnClickListener(this)
        zoomOut!!.setOnClickListener(this)
        layers.setOnClickListener(this)
        myLocation.setOnClickListener(this)
        markers!!.setOnClickListener(this)
        places!!.setOnClickListener(this)
        backButton!!.setOnClickListener(this)

        val typeNormal = view.findViewById<TextView>(R.id.typeNormal)
        val typeSatellite = view.findViewById<TextView>(R.id.typeSatellite)
        val typeHybrid = view.findViewById<TextView>(R.id.typeHybrid)
        val typeTerrain = view.findViewById<TextView>(R.id.typeTerrain)
        typeNormal.setOnClickListener(this)
        typeSatellite.setOnClickListener(this)
        typeHybrid.setOnClickListener(this)
        typeTerrain.setOnClickListener(this)

        if (!isPlaces) {
            placesCard.visibility = View.GONE
        }

        if (!isBack) {
            backCard.visibility = View.GONE
        }

        if (!isSearch) {
            searchCard.visibility = View.GONE
        }

        if (!isStyles) {
            markersCard.visibility = View.GONE
        }

        if (!isZoom) {
            zoomCard.visibility = View.GONE
        }

        loadMarkers()
    }

    private fun loadMarkers() {
        groupOne!!.removeAllViewsInLayout()
        groupTwo!!.removeAllViewsInLayout()
        groupThree!!.removeAllViewsInLayout()

        for (i in 0 until Coloring.NUM_OF_MARKERS) {
            val ib = ImageButton(activity)
            ib.setBackgroundResource(android.R.color.transparent)
            ib.setImageResource(Coloring(activity).getMarkerStyle(i))
            ib.id = i + Coloring.NUM_OF_MARKERS
            ib.setOnClickListener(this)
            val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT)
            val px = QuickReturnUtils.dp2px(activity!!, 2)
            params.setMargins(px, px, px, px)
            ib.layoutParams = params

            if (i < 5) {
                groupOne!!.addView(ib)
            } else if (i < 10) {
                groupTwo!!.addView(ib)
            } else {
                groupThree!!.addView(ib)
            }
        }
    }

    private fun setMapType(type: Int) {
        if (map != null) {
            map!!.mapType = type
            SharedPrefs.getInstance(activity)!!.saveInt(Prefs.MAP_TYPE, type)
            ViewUtils.hideOver(layersContainer!!)
        }
    }

    private fun setMyLocation() {
        if (Module.isMarshmallow) {
            if (ActivityCompat.checkSelfPermission(activity!!,
                            Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity!!,
                            Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Permissions.requestPermission(activity, 205,
                        Permissions.ACCESS_FINE_LOCATION, Permissions.ACCESS_COARSE_LOCATION)
            } else {
                map!!.isMyLocationEnabled = true
            }
        } else {
            map!!.isMyLocationEnabled = true
        }
    }

    private fun loadPlaces() {
        if (placesAdapter == null) {
            val DB = DataBase(activity)
            DB.open()
            val c = DB.queryPlaces()
            spinnerArray = ArrayList()
            spinnerArray.clear()
            if (c != null && c.moveToFirst()) {
                do {
                    val namePlace = c.getString(c.getColumnIndex(DataBase.NAME))
                    spinnerArray.add(namePlace)

                } while (c.moveToNext())
            } else {
                spinnerArray.clear()
            }
            c?.close()
            DB.close()

            if (spinnerArray.isEmpty()) {
                placesList!!.visibility = View.GONE
                emptyItem!!.visibility = View.VISIBLE
            } else {
                emptyItem!!.visibility = View.GONE
                placesList!!.visibility = View.VISIBLE
                val adapter = PlaceAdapter(activity, spinnerArray)
                adapter.eventListener = object : SimpleListener {
                    override fun onItemClicked(position: Int, view: View) {
                        hideLayers()
                        hidePlaces()
                        val placeName = spinnerArray[position]
                        val db = DataBase(activity)
                        db.open()
                        val c = db.getPlace(placeName)
                        if (c != null && c.moveToFirst()) {
                            val latitude = c.getDouble(c.getColumnIndex(DataBase.LATITUDE))
                            val longitude = c.getDouble(c.getColumnIndex(DataBase.LONGITUDE))
                            val latLng = LatLng(latitude, longitude)
                            addMarker(latLng, markerTitle, true, true, markerRadius)
                        }
                        c?.close()
                        db.close()
                    }

                    override fun onItemLongClicked(position: Int, view: View) {

                    }
                }
                placesList!!.layoutManager = LinearLayoutManager(activity)
                placesList!!.adapter = adapter
            }
        } else {
            if (placesAdapter!!.itemCount > 0) {
                emptyItem!!.visibility = View.GONE
                placesList!!.visibility = View.VISIBLE
                placesList!!.layoutManager = LinearLayoutManager(activity)
                placesList!!.adapter = placesAdapter
                addMarkers(placesAdapter!!.provider)
            } else {
                placesList!!.visibility = View.GONE
                emptyItem!!.visibility = View.VISIBLE
            }
        }
    }

    private fun addMarkers(provider: PlaceDataProvider) {
        val list = provider.data
        if (list != null && list.size > 0) {
            for (model in list) {
                addMarker(model.position, model.title, false,
                        model.icon, false, model.radius)
            }
        }
    }

    private fun toggleMarkers() {
        if (isLayersVisible) {
            hideLayers()
        }
        if (isPlacesVisible) {
            hidePlaces()
        }
        if (isMarkersVisible) {
            hideStyles()
        } else {
            ViewUtils.slideInUp(activity, styleCard!!)
        }
    }

    private fun hideStyles() {
        if (isMarkersVisible) {
            ViewUtils.slideOutDown(activity, styleCard!!)
        }
    }

    private fun togglePlaces() {
        if (isMarkersVisible) {
            hideStyles()
        }
        if (isLayersVisible) {
            hideLayers()
        }
        if (isPlacesVisible) {
            hidePlaces()
        } else {
            ViewUtils.slideInUp(activity, placesListCard!!)
        }
    }

    private fun hidePlaces() {
        if (isPlacesVisible) {
            ViewUtils.slideOutDown(activity, placesListCard!!)
        }
    }

    private fun toggleLayers() {
        if (isMarkersVisible) {
            hideStyles()
        }
        if (isPlacesVisible) {
            hidePlaces()
        }
        if (isLayersVisible) {
            hideLayers()
        } else {
            ViewUtils.showOver(layersContainer!!)
        }
    }

    private fun hideLayers() {
        if (isLayersVisible) {
            ViewUtils.hideOver(layersContainer!!)
        }
    }

    private fun zoomClick() {
        isFullscreen = !isFullscreen
        if (listener != null) {
            listener!!.onZoomClick(isFullscreen)
        }
        if (isFullscreen) {
            zoomOut!!.setImageResource(R.drawable.ic_arrow_downward_white_24dp)
        } else {
            zoomOut!!.setImageResource(R.drawable.ic_arrow_upward_white_24dp)
        }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (grantResults.size == 0) return
        when (requestCode) {
            205 -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setMyLocation()
            } else {
                Messages.toast(activity, R.string.cant_access_location_services)
            }
        }
    }

    override fun onClick(v: View) {
        val id = v.id
        if (id >= Coloring.NUM_OF_MARKERS && id < Coloring.NUM_OF_MARKERS * 2) {
            recreateStyle(v.id - Coloring.NUM_OF_MARKERS)
            hideStyles()
        }

        when (id) {
            R.id.cardClear -> cardSearch!!.setText("")
            R.id.mapZoom -> zoomClick()
            R.id.layers -> toggleLayers()
            R.id.myLocation -> {
                hideLayers()
                moveToMyLocation()
            }
            R.id.typeNormal -> setMapType(GoogleMap.MAP_TYPE_NORMAL)
            R.id.typeHybrid -> setMapType(GoogleMap.MAP_TYPE_HYBRID)
            R.id.typeSatellite -> setMapType(GoogleMap.MAP_TYPE_SATELLITE)
            R.id.typeTerrain -> setMapType(GoogleMap.MAP_TYPE_TERRAIN)
            R.id.places -> togglePlaces()
            R.id.markers -> toggleMarkers()
            R.id.backButton -> if (listener != null) {
                listener!!.onBackClick()
            }
        }
    }

    interface MapCallback {
        fun onMapReady()
    }

    companion object {

        private val HAS_SHOWCASE = "has_showcase"

        val ENABLE_TOUCH = "enable_touch"
        val ENABLE_PLACES = "enable_places"
        val ENABLE_SEARCH = "enable_search"
        val ENABLE_STYLES = "enable_styles"
        val ENABLE_BACK = "enable_back"
        val ENABLE_ZOOM = "enable_zoom"
        val MARKER_STYLE = "marker_style"

        fun newInstance(isTouch: Boolean, isPlaces: Boolean,
                        isSearch: Boolean, isStyles: Boolean,
                        isBack: Boolean, isZoom: Boolean): MapFragment {
            val fragment = MapFragment()
            val args = Bundle()
            args.putBoolean(ENABLE_TOUCH, isTouch)
            args.putBoolean(ENABLE_PLACES, isPlaces)
            args.putBoolean(ENABLE_SEARCH, isSearch)
            args.putBoolean(ENABLE_STYLES, isStyles)
            args.putBoolean(ENABLE_BACK, isBack)
            args.putBoolean(ENABLE_ZOOM, isZoom)
            fragment.arguments = args
            return fragment
        }

        fun newInstance(isPlaces: Boolean, isStyles: Boolean,
                        isBack: Boolean, isZoom: Boolean, markerStyle: Int): MapFragment {
            val fragment = MapFragment()
            val args = Bundle()
            args.putBoolean(ENABLE_PLACES, isPlaces)
            args.putBoolean(ENABLE_STYLES, isStyles)
            args.putBoolean(ENABLE_BACK, isBack)
            args.putBoolean(ENABLE_ZOOM, isZoom)
            args.putInt(MARKER_STYLE, markerStyle)
            fragment.arguments = args
            return fragment
        }
    }
}
