package com.backdoor.moove.core.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Address;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.backdoor.moove.R;
import com.backdoor.moove.core.adapters.PlaceAdapter;
import com.backdoor.moove.core.adapters.PlaceRecyclerAdapter;
import com.backdoor.moove.core.async.GeocoderTask;
import com.backdoor.moove.core.consts.Configs;
import com.backdoor.moove.core.consts.Constants;
import com.backdoor.moove.core.consts.Prefs;
import com.backdoor.moove.core.data.MarkerModel;
import com.backdoor.moove.core.data.PlaceDataProvider;
import com.backdoor.moove.core.helper.Coloring;
import com.backdoor.moove.core.helper.DataBase;
import com.backdoor.moove.core.helper.Messages;
import com.backdoor.moove.core.helper.Module;
import com.backdoor.moove.core.helper.Permissions;
import com.backdoor.moove.core.helper.SharedPrefs;
import com.backdoor.moove.core.interfaces.MapListener;
import com.backdoor.moove.core.interfaces.SimpleListener;
import com.backdoor.moove.core.utils.QuickReturnUtils;
import com.backdoor.moove.core.utils.ViewUtils;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

public class MapFragment extends Fragment implements View.OnClickListener {

    private static final String HAS_SHOWCASE = "has_showcase";

    /**
     * UI elements;
     */
    private GoogleMap map;
    private CardView layersContainer;
    private CardView styleCard;
    private CardView placesListCard;
    private AutoCompleteTextView cardSearch;
    private ImageButton zoomOut;
    private ImageButton backButton;
    private ImageButton places;
    private ImageButton markers;
    private LinearLayout groupOne, groupTwo, groupThree;
    private RecyclerView placesList;
    private LinearLayout emptyItem;

    /**
     * Array of user frequently used places;
     */
    private ArrayList<String> spinnerArray = new ArrayList<>();

    private PlaceRecyclerAdapter placeRecyclerAdapter;

    /**
     * init variables and flags;
     */
    private boolean isTouch = true;
    private boolean isZoom = true;
    private boolean isBack = true;
    private boolean isStyles = true;
    private boolean isPlaces = true;
    private boolean isSearch = true;
    private boolean isFullscreen = false;
    private String markerTitle;
    private int markerRadius = -1;
    private int markerStyle = -1;
    private int type;
    private LatLng lastPos;
    private float strokeWidth = 3f;

    /**
     * UI helper class;
     */
    private Coloring cSetter;
    private MapCallback mCallback;

    /**
     * Arrays of place search results;
     */
    private List<Address> foundPlaces;
    private ArrayAdapter<String> adapter;
    private GeocoderTask task;
    private ArrayList<String> namesList;

    /**
     * MapListener link;
     */
    private MapListener listener;

    public static final String ENABLE_TOUCH = "enable_touch";
    public static final String ENABLE_PLACES = "enable_places";
    public static final String ENABLE_SEARCH = "enable_search";
    public static final String ENABLE_STYLES = "enable_styles";
    public static final String ENABLE_BACK = "enable_back";
    public static final String ENABLE_ZOOM = "enable_zoom";
    public static final String MARKER_STYLE = "marker_style";

    private OnMapReadyCallback mMapReadyCallback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(GoogleMap googleMap) {
            map = googleMap;
            map.getUiSettings().setMyLocationButtonEnabled(false);
            map.getUiSettings().setCompassEnabled(true);
            map.setMapType(type);
            setMyLocation();
            map.setOnMapClickListener(latLng -> {
                hideLayers();
                hidePlaces();
                hideStyles();
                if (isTouch) {
                    addMarker(latLng, markerTitle, true, true, markerRadius);
                }
            });
            if (mCallback != null) {
                mCallback.onMapReady();
            }
        }
    };

    public static MapFragment newInstance(boolean isTouch, boolean isPlaces,
                                          boolean isSearch, boolean isStyles,
                                          boolean isBack, boolean isZoom) {
        MapFragment fragment = new MapFragment();
        Bundle args = new Bundle();
        args.putBoolean(ENABLE_TOUCH, isTouch);
        args.putBoolean(ENABLE_PLACES, isPlaces);
        args.putBoolean(ENABLE_SEARCH, isSearch);
        args.putBoolean(ENABLE_STYLES, isStyles);
        args.putBoolean(ENABLE_BACK, isBack);
        args.putBoolean(ENABLE_ZOOM, isZoom);
        fragment.setArguments(args);
        return fragment;
    }

    public static MapFragment newInstance(boolean isPlaces, boolean isStyles,
                                          boolean isBack, boolean isZoom, int markerStyle) {
        MapFragment fragment = new MapFragment();
        Bundle args = new Bundle();
        args.putBoolean(ENABLE_PLACES, isPlaces);
        args.putBoolean(ENABLE_STYLES, isStyles);
        args.putBoolean(ENABLE_BACK, isBack);
        args.putBoolean(ENABLE_ZOOM, isZoom);
        args.putInt(MARKER_STYLE, markerStyle);
        fragment.setArguments(args);
        return fragment;
    }

    public MapFragment() {

    }

    public void setMapReadyCallback(MapCallback callback) {
        this.mCallback = callback;
    }

    public void setAdapter(PlaceRecyclerAdapter adapter) {
        this.placeRecyclerAdapter = adapter;
    }

    /**
     * Set listener for map fragment;
     *
     * @param listener listener for map fragment
     */
    public void setListener(MapListener listener) {
        this.listener = listener;
    }

    /**
     * Set title for markers;
     *
     * @param markerTitle marker title
     */
    public void setMarkerTitle(String markerTitle) {
        this.markerTitle = markerTitle;
    }

    /**
     * Set radius for marker;
     *
     * @param markerRadius radius for drawing circle around marker
     */
    public void setMarkerRadius(int markerRadius) {
        this.markerRadius = markerRadius;
    }

    /**
     * Set style for marker;
     *
     * @param markerStyle code of style for marker
     */
    public void setMarkerStyle(int markerStyle) {
        this.markerStyle = markerStyle;
    }

    /**
     * Get currently used marker style.
     *
     * @return marker code.
     */
    public int getMarkerStyle() {
        return markerStyle;
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
    public void addMarker(LatLng pos, String title, boolean clear, boolean animate, int radius) {
        if (map != null) {
            markerRadius = radius;
            if (markerRadius == -1) {
                markerRadius = SharedPrefs.getInstance(getActivity()).loadInt(Prefs.LOCATION_RADIUS);
            }
            if (clear) {
                map.clear();
            }
            if (title == null || title.matches("")) {
                title = pos.toString();
            }
            lastPos = pos;
            if (listener != null) {
                listener.placeChanged(pos);
            }
            map.addMarker(new MarkerOptions()
                    .position(pos)
                    .title(title)
                    .icon(BitmapDescriptorFactory.fromResource(cSetter.getMarkerStyle(markerStyle)))
                    .draggable(clear));
            int[] circleColors = cSetter.getMarkerRadiusStyle(markerStyle);
            map.addCircle(new CircleOptions()
                    .center(pos)
                    .radius(markerRadius)
                    .strokeWidth(strokeWidth)
                    .fillColor(ViewUtils.getColor(getActivity(), circleColors[0]))
                    .strokeColor(ViewUtils.getColor(getActivity(), circleColors[1])));
            if (animate) {
                animate(pos);
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
    public void addMarker(LatLng pos, String title, boolean clear, int markerStyle, boolean animate, int radius) {
        if (map != null) {
            markerRadius = radius;
            if (markerRadius == -1) {
                markerRadius = SharedPrefs.getInstance(getActivity()).loadInt(Prefs.LOCATION_RADIUS);
            }
            this.markerStyle = markerStyle;
            if (clear) {
                map.clear();
            }
            if (title == null || title.matches("")) {
                title = pos.toString();
            }
            lastPos = pos;
            if (listener != null) {
                listener.placeChanged(pos);
            }
            map.addMarker(new MarkerOptions()
                    .position(pos)
                    .title(title)
                    .icon(BitmapDescriptorFactory.fromResource(cSetter.getMarkerStyle(markerStyle)))
                    .draggable(clear));
            int[] circleColors = cSetter.getMarkerRadiusStyle(markerStyle);
            map.addCircle(new CircleOptions()
                    .center(pos)
                    .radius(markerRadius)
                    .strokeWidth(strokeWidth)
                    .fillColor(ViewUtils.getColor(getActivity(), circleColors[0]))
                    .strokeColor(ViewUtils.getColor(getActivity(), circleColors[1])));
            if (animate) {
                animate(pos);
            }
        } else {
            Log.d(Constants.LOG_TAG, "map is null");
        }
    }

    /**
     * Recreate last added marker with new circle radius;
     *
     * @param radius radius for a circle
     */
    public void recreateMarker(int radius) {
        markerRadius = radius;
        if (markerRadius == -1) {
            markerRadius = SharedPrefs.getInstance(getActivity()).loadInt(Prefs.LOCATION_RADIUS);
        }
        if (map != null && lastPos != null) {
            map.clear();
            if (markerTitle == null || markerTitle.matches("")) {
                markerTitle = lastPos.toString();
            }
            if (listener != null) {
                listener.placeChanged(lastPos);
            }
            map.addMarker(new MarkerOptions()
                    .position(lastPos)
                    .title(markerTitle)
                    .icon(BitmapDescriptorFactory.fromResource(cSetter.getMarkerStyle(markerStyle)))
                    .draggable(true));
            int[] circleColors = cSetter.getMarkerRadiusStyle(markerStyle);
            map.addCircle(new CircleOptions()
                    .center(lastPos)
                    .radius(markerRadius)
                    .strokeWidth(strokeWidth)
                    .fillColor(ViewUtils.getColor(getActivity(), circleColors[0]))
                    .strokeColor(ViewUtils.getColor(getActivity(), circleColors[1])));
            animate(lastPos);
        }
    }

    /**
     * Recreate last added marker with new marker style;
     *
     * @param style marker style.
     */
    public void recreateStyle(int style) {
        markerStyle = style;
        if (map != null && lastPos != null) {
            map.clear();
            if (markerTitle == null || markerTitle.matches("")) {
                markerTitle = lastPos.toString();
            }
            if (listener != null) {
                listener.placeChanged(lastPos);
            }
            map.addMarker(new MarkerOptions()
                    .position(lastPos)
                    .title(markerTitle)
                    .icon(BitmapDescriptorFactory.fromResource(cSetter.getMarkerStyle(style)))
                    .draggable(true));
            if (style >= 0) {
                int[] circleColors = cSetter.getMarkerRadiusStyle(style);
                if (markerRadius == -1) {
                    markerRadius = SharedPrefs.getInstance(getActivity()).loadInt(Prefs.LOCATION_RADIUS);
                }
                map.addCircle(new CircleOptions()
                        .center(lastPos)
                        .radius(markerRadius)
                        .strokeWidth(strokeWidth)
                        .fillColor(ViewUtils.getColor(getActivity(), circleColors[0]))
                        .strokeColor(ViewUtils.getColor(getActivity(), circleColors[1])));
            }
            animate(lastPos);
        }
    }

    /**
     * Move camera to coordinates;
     *
     * @param pos coordinates
     */
    public void moveCamera(LatLng pos) {
        if (map != null) {
            animate(pos);
        }
    }

    /**
     * Move camera to coordinates with animation;
     *
     * @param latLng coordinates
     */
    public void animate(LatLng latLng) {
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLng, 13);
        if (map != null) {
            map.animateCamera(update);
        }
    }

    /**
     * Move camera to user current coordinates with animation;
     */
    public void moveToMyLocation() {
        try {
            if (map != null && map.getMyLocation() != null) {
                double lat = map.getMyLocation().getLatitude();
                double lon = map.getMyLocation().getLongitude();
                LatLng pos = new LatLng(lat, lon);
                animate(pos);
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    public boolean isFullscreen() {
        return isFullscreen;
    }

    public void setFullscreen(boolean fullscreen) {
        isFullscreen = fullscreen;
    }

    /**
     * On back pressed interface for map;
     *
     * @return boolean
     */
    public boolean onBackPressed() {
        if (isLayersVisible()) {
            hideLayers();
            return false;
        } else if (isMarkersVisible()) {
            hideStyles();
            return false;
        } else if (isPlacesVisible()) {
            hidePlaces();
            return false;
        } else {
            return true;
        }
    }

    public void showShowcase() {
        if (!SharedPrefs.getInstance(getActivity()).loadBoolean(HAS_SHOWCASE) && isBack) {
            SharedPrefs.getInstance(getActivity()).saveBoolean(HAS_SHOWCASE, true);
            Coloring coloring = new Coloring(getActivity());
            ShowcaseConfig config = new ShowcaseConfig();
            config.setDelay(350);
            config.setMaskColor(coloring.colorAccent());
            config.setContentTextColor(coloring.getColor(R.color.whitePrimary));
            config.setDismissTextColor(coloring.getColor(R.color.whitePrimary));

            MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(getActivity());
            sequence.setConfig(config);

            sequence.addSequenceItem(zoomOut,
                    getActivity().getString(R.string.click_to_expand_collapse_map),
                    getActivity().getString(R.string.got_it));

            sequence.addSequenceItem(backButton,
                    getActivity().getString(R.string.click_when_add_place),
                    getActivity().getString(R.string.got_it));

            sequence.addSequenceItem(markers,
                    getActivity().getString(R.string.select_style_for_marker),
                    getActivity().getString(R.string.got_it));

            sequence.addSequenceItem(places,
                    getActivity().getString(R.string.select_place_from_list),
                    getActivity().getString(R.string.got_it));
            sequence.start();
        }
    }

    private void initArgs() {
        Bundle args = getArguments();
        if (args != null) {
            isTouch = args.getBoolean(ENABLE_TOUCH, true);
            isPlaces = args.getBoolean(ENABLE_PLACES, true);
            isSearch = args.getBoolean(ENABLE_SEARCH, true);
            isStyles = args.getBoolean(ENABLE_STYLES, true);
            isBack = args.getBoolean(ENABLE_BACK, true);
            isZoom = args.getBoolean(ENABLE_ZOOM, true);
            markerStyle = args.getInt(MARKER_STYLE,
                    SharedPrefs.getInstance(getActivity()).loadInt(Prefs.MARKER_STYLE));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        initArgs();
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        final SharedPrefs prefs = SharedPrefs.getInstance(getActivity());
        cSetter = new Coloring(getActivity());
        type = prefs.loadInt(Prefs.MAP_TYPE);
        markerRadius = prefs.loadInt(Prefs.LOCATION_RADIUS);
        ((SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map))
                .getMapAsync(mMapReadyCallback);
        if (lastPos != null) {
            addMarker(lastPos, lastPos.toString(), true, false, markerRadius);
        }

        initViews(view);

        cardSearch = view.findViewById(R.id.cardSearch);
        cardSearch.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_search_white_24dp, 0, 0, 0);
        cardSearch.setThreshold(3);
        adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_dropdown_item_1line, namesList);
        adapter.setNotifyOnChange(true);
        cardSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                hideLayers();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (task != null && !task.isCancelled()) {
                    task.cancel(true);
                }
                if (s.length() != 0) {
                    task = new GeocoderTask(getActivity(), addresses -> {
                        foundPlaces = addresses;

                        namesList = new ArrayList<>();
                        namesList.clear();
                        for (Address selected : addresses) {
                            String addressText = String.format("%s, %s%s",
                                    selected.getMaxAddressLineIndex() > 0 ? selected.getAddressLine(0) : "",
                                    selected.getMaxAddressLineIndex() > 1 ? selected.getAddressLine(1) + ", " : "",
                                    selected.getCountryName());
                            namesList.add(addressText);
                        }
                        adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_dropdown_item_1line, namesList);
                        cardSearch.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                    });
                    task.execute(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        cardSearch.setOnItemClickListener((parent, view1, position, id) -> {
            Address sel = foundPlaces.get(position);
            double lat = sel.getLatitude();
            double lon = sel.getLongitude();
            LatLng pos = new LatLng(lat, lon);
            addMarker(pos, markerTitle, true, true, markerRadius);
            if (listener != null) {
                listener.placeName(namesList.get(position));
            }
        });

        placesList = view.findViewById(R.id.placesList);
        loadPlaces();

        return view;
    }

    private void initViews(View view) {
        groupOne = view.findViewById(R.id.groupOne);
        groupTwo = view.findViewById(R.id.groupTwo);
        groupThree = view.findViewById(R.id.groupThree);
        emptyItem = view.findViewById(R.id.emptyItem);

        placesList = view.findViewById(R.id.placesList);

        CardView zoomCard = view.findViewById(R.id.zoomCard);
        CardView searchCard = view.findViewById(R.id.searchCard);
        CardView myCard = view.findViewById(R.id.myCard);
        CardView layersCard = view.findViewById(R.id.layersCard);
        CardView placesCard = view.findViewById(R.id.placesCard);
        CardView backCard = view.findViewById(R.id.backCard);
        styleCard = view.findViewById(R.id.styleCard);
        placesListCard = view.findViewById(R.id.placesListCard);
        CardView markersCard = view.findViewById(R.id.markersCard);
        placesListCard.setVisibility(View.GONE);
        styleCard.setVisibility(View.GONE);

        zoomCard.setCardBackgroundColor(cSetter.getCardStyle());
        searchCard.setCardBackgroundColor(cSetter.getCardStyle());
        myCard.setCardBackgroundColor(cSetter.getCardStyle());
        layersCard.setCardBackgroundColor(cSetter.getCardStyle());
        placesCard.setCardBackgroundColor(cSetter.getCardStyle());
        styleCard.setCardBackgroundColor(cSetter.getCardStyle());
        placesListCard.setCardBackgroundColor(cSetter.getCardStyle());
        markersCard.setCardBackgroundColor(cSetter.getCardStyle());
        backCard.setCardBackgroundColor(cSetter.getCardStyle());

        layersContainer = view.findViewById(R.id.layersContainer);
        layersContainer.setVisibility(View.GONE);
        layersContainer.setCardBackgroundColor(cSetter.getCardStyle());

        if (Module.isLollipop()) {
            zoomCard.setCardElevation(Configs.CARD_ELEVATION);
            searchCard.setCardElevation(Configs.CARD_ELEVATION);
            myCard.setCardElevation(Configs.CARD_ELEVATION);
            layersContainer.setCardElevation(Configs.CARD_ELEVATION);
            layersCard.setCardElevation(Configs.CARD_ELEVATION);
            placesCard.setCardElevation(Configs.CARD_ELEVATION);
            styleCard.setCardElevation(Configs.CARD_ELEVATION);
            placesListCard.setCardElevation(Configs.CARD_ELEVATION);
            markersCard.setCardElevation(Configs.CARD_ELEVATION);
            backCard.setCardElevation(Configs.CARD_ELEVATION);
        }

        ImageButton cardClear = view.findViewById(R.id.cardClear);
        zoomOut = view.findViewById(R.id.mapZoom);
        ImageButton layers = view.findViewById(R.id.layers);
        ImageButton myLocation = view.findViewById(R.id.myLocation);
        markers = view.findViewById(R.id.markers);
        places = view.findViewById(R.id.places);
        backButton = view.findViewById(R.id.backButton);

        cardClear.setOnClickListener(this);
        zoomOut.setOnClickListener(this);
        layers.setOnClickListener(this);
        myLocation.setOnClickListener(this);
        markers.setOnClickListener(this);
        places.setOnClickListener(this);
        backButton.setOnClickListener(this);

        TextView typeNormal = view.findViewById(R.id.typeNormal);
        TextView typeSatellite = view.findViewById(R.id.typeSatellite);
        TextView typeHybrid = view.findViewById(R.id.typeHybrid);
        TextView typeTerrain = view.findViewById(R.id.typeTerrain);
        typeNormal.setOnClickListener(this);
        typeSatellite.setOnClickListener(this);
        typeHybrid.setOnClickListener(this);
        typeTerrain.setOnClickListener(this);

        if (!isPlaces) {
            placesCard.setVisibility(View.GONE);
        }

        if (!isBack) {
            backCard.setVisibility(View.GONE);
        }

        if (!isSearch) {
            searchCard.setVisibility(View.GONE);
        }

        if (!isStyles) {
            markersCard.setVisibility(View.GONE);
        }

        if (!isZoom) {
            zoomCard.setVisibility(View.GONE);
        }

        loadMarkers();
    }

    private void loadMarkers() {
        groupOne.removeAllViewsInLayout();
        groupTwo.removeAllViewsInLayout();
        groupThree.removeAllViewsInLayout();

        for (int i = 0; i < Coloring.NUM_OF_MARKERS; i++) {
            ImageButton ib = new ImageButton(getActivity());
            ib.setBackgroundResource(android.R.color.transparent);
            ib.setImageResource(new Coloring(getActivity()).getMarkerStyle(i));
            ib.setId(i + Coloring.NUM_OF_MARKERS);
            ib.setOnClickListener(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            int px = QuickReturnUtils.dp2px(getActivity(), 2);
            params.setMargins(px, px, px, px);
            ib.setLayoutParams(params);

            if (i < 5) {
                groupOne.addView(ib);
            } else if (i < 10) {
                groupTwo.addView(ib);
            } else {
                groupThree.addView(ib);
            }
        }
    }

    private void setMapType(int type) {
        if (map != null) {
            map.setMapType(type);
            SharedPrefs.getInstance(getActivity()).saveInt(Prefs.MAP_TYPE, type);
            ViewUtils.hideOver(layersContainer);
        }
    }

    private void setMyLocation() {
        if (Module.isMarshmallow()) {
            if (ActivityCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(getActivity(),
                            Manifest.permission.ACCESS_COARSE_LOCATION) !=
                            PackageManager.PERMISSION_GRANTED) {
                Permissions.requestPermission(getActivity(), 205,
                        Permissions.ACCESS_FINE_LOCATION, Permissions.ACCESS_COARSE_LOCATION);
            } else {
                map.setMyLocationEnabled(true);
            }
        } else {
            map.setMyLocationEnabled(true);
        }
    }

    private void loadPlaces() {
        if (placeRecyclerAdapter == null) {
            DataBase DB = new DataBase(getActivity());
            DB.open();
            Cursor c = DB.queryPlaces();
            spinnerArray = new ArrayList<>();
            spinnerArray.clear();
            if (c != null && c.moveToFirst()) {
                do {
                    String namePlace = c.getString(c.getColumnIndex(DataBase.NAME));
                    spinnerArray.add(namePlace);

                } while (c.moveToNext());
            } else {
                spinnerArray.clear();
            }
            if (c != null) {
                c.close();
            }
            DB.close();

            if (spinnerArray.isEmpty()) {
                placesList.setVisibility(View.GONE);
                emptyItem.setVisibility(View.VISIBLE);
            } else {
                emptyItem.setVisibility(View.GONE);
                placesList.setVisibility(View.VISIBLE);
                PlaceAdapter adapter = new PlaceAdapter(getActivity(), spinnerArray);
                adapter.setEventListener(new SimpleListener() {
                    @Override
                    public void onItemClicked(int position, View view) {
                        hideLayers();
                        hidePlaces();
                        String placeName = spinnerArray.get(position);
                        DataBase db = new DataBase(getActivity());
                        db.open();
                        Cursor c = db.getPlace(placeName);
                        if (c != null && c.moveToFirst()) {
                            double latitude = c.getDouble(c.getColumnIndex(DataBase.LATITUDE));
                            double longitude = c.getDouble(c.getColumnIndex(DataBase.LONGITUDE));
                            LatLng latLng = new LatLng(latitude, longitude);
                            addMarker(latLng, markerTitle, true, true, markerRadius);
                        }
                        if (c != null) {
                            c.close();
                        }
                        db.close();
                    }

                    @Override
                    public void onItemLongClicked(int position, View view) {

                    }
                });
                placesList.setLayoutManager(new LinearLayoutManager(getActivity()));
                placesList.setAdapter(adapter);
            }
        } else {
            if (placeRecyclerAdapter.getItemCount() > 0) {
                emptyItem.setVisibility(View.GONE);
                placesList.setVisibility(View.VISIBLE);
                placesList.setLayoutManager(new LinearLayoutManager(getActivity()));
                placesList.setAdapter(placeRecyclerAdapter);
                addMarkers(placeRecyclerAdapter.getProvider());
            } else {
                placesList.setVisibility(View.GONE);
                emptyItem.setVisibility(View.VISIBLE);
            }
        }
    }

    private void addMarkers(PlaceDataProvider provider) {
        List<MarkerModel> list = provider.getData();
        if (list != null && list.size() > 0) {
            for (MarkerModel model : list) {
                addMarker(model.getPosition(), model.getTitle(), false,
                        model.getIcon(), false, model.getRadius());
            }
        }
    }

    private void toggleMarkers() {
        if (isLayersVisible()) {
            hideLayers();
        }
        if (isPlacesVisible()) {
            hidePlaces();
        }
        if (isMarkersVisible()) {
            hideStyles();
        } else {
            ViewUtils.slideInUp(getActivity(), styleCard);
        }
    }

    private void hideStyles() {
        if (isMarkersVisible()) {
            ViewUtils.slideOutDown(getActivity(), styleCard);
        }
    }

    private boolean isMarkersVisible() {
        return styleCard != null && styleCard.getVisibility() == View.VISIBLE;
    }

    private void togglePlaces() {
        if (isMarkersVisible()) {
            hideStyles();
        }
        if (isLayersVisible()) {
            hideLayers();
        }
        if (isPlacesVisible()) {
            hidePlaces();
        } else {
            ViewUtils.slideInUp(getActivity(), placesListCard);
        }
    }

    private void hidePlaces() {
        if (isPlacesVisible()) {
            ViewUtils.slideOutDown(getActivity(), placesListCard);
        }
    }

    private boolean isPlacesVisible() {
        return placesListCard != null && placesListCard.getVisibility() == View.VISIBLE;
    }

    private void toggleLayers() {
        if (isMarkersVisible()) {
            hideStyles();
        }
        if (isPlacesVisible()) {
            hidePlaces();
        }
        if (isLayersVisible()) {
            hideLayers();
        } else {
            ViewUtils.showOver(layersContainer);
        }
    }

    private void hideLayers() {
        if (isLayersVisible()) {
            ViewUtils.hideOver(layersContainer);
        }
    }

    private void zoomClick() {
        isFullscreen = !isFullscreen;
        if (listener != null) {
            listener.onZoomClick(isFullscreen);
        }
        if (isFullscreen) {
            zoomOut.setImageResource(R.drawable.ic_arrow_downward_white_24dp);
        } else {
            zoomOut.setImageResource(R.drawable.ic_arrow_upward_white_24dp);
        }
    }

    private boolean isLayersVisible() {
        return layersContainer != null && layersContainer.getVisibility() == View.VISIBLE;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 205:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setMyLocation();
                } else {
                    Messages.toast(getActivity(), R.string.cant_access_location_services);
                }
                break;
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id >= Coloring.NUM_OF_MARKERS && id < Coloring.NUM_OF_MARKERS * 2) {
            recreateStyle(v.getId() - Coloring.NUM_OF_MARKERS);
            hideStyles();
        }

        switch (id) {
            case R.id.cardClear:
                cardSearch.setText("");
                break;
            case R.id.mapZoom:
                zoomClick();
                break;
            case R.id.layers:
                toggleLayers();
                break;
            case R.id.myLocation:
                hideLayers();
                moveToMyLocation();
                break;
            case R.id.typeNormal:
                setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
            case R.id.typeHybrid:
                setMapType(GoogleMap.MAP_TYPE_HYBRID);
                break;
            case R.id.typeSatellite:
                setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;
            case R.id.typeTerrain:
                setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                break;
            case R.id.places:
                togglePlaces();
                break;
            case R.id.markers:
                toggleMarkers();
                break;
            case R.id.backButton:
                if (listener != null) {
                    listener.onBackClick();
                }
                break;
        }
    }

    public interface MapCallback {
        void onMapReady();
    }
}
