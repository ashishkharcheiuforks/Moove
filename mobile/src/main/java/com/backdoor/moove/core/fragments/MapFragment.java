package com.backdoor.moove.core.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Address;
import android.location.Location;
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
import com.backdoor.moove.core.async.GeocoderTask;
import com.backdoor.moove.core.consts.Configs;
import com.backdoor.moove.core.consts.Constants;
import com.backdoor.moove.core.consts.Prefs;
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
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class MapFragment extends Fragment implements View.OnClickListener {

    /**
     * UI elements;
     */
    private GoogleMap map;
    private CardView layersContainer;
    private CardView styleCard;
    private CardView placesListCard;
    private AutoCompleteTextView cardSearch;
    private ImageButton zoomOut;
    private LinearLayout groupOne, groupTwo, groupThree;
    private RecyclerView placesList;
    private LinearLayout emptyItem;

    /**
     * Array of user frequently used places;
     */
    private ArrayList<String> spinnerArray = new ArrayList<>();

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
    private LatLng lastPos;
    private float strokeWidth = 3f;

    /**
     * UI helper class;
     */
    private Coloring cSetter;

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

    public static MapFragment newInstance() {
        return new MapFragment();
    }

    public MapFragment() {

    }

    /**
     * Set listener for map fragment;
     * @param listener listener for map fragment
     */
    public void setListener(MapListener listener) {
        this.listener = listener;
    }

    /**
     * Set title for markers;
     * @param markerTitle marker title
     */
    public void setMarkerTitle(String markerTitle) {
        this.markerTitle = markerTitle;
    }

    /**
     * Set radius for marker;
     * @param markerRadius radius for drawing circle around marker
     */
    public void setMarkerRadius(int markerRadius) {
        this.markerRadius = markerRadius;
    }

    public int getMarkerStyle() {
        return markerStyle;
    }

    /**
     * Add marker to map;
     * @param pos coordinates
     * @param title marker title
     * @param clear remove previous markers flag
     * @param animate animate to marker position
     * @param radius radius for circle around marker
     */
    public void addMarker(LatLng pos, String title, boolean clear, boolean animate, int radius) {
        if (map != null) {
            markerRadius = radius;
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
            if (radius != -1) {
                int[] circleColors = cSetter.getMarkerRadiusStyle(markerStyle);
                map.addCircle(new CircleOptions()
                        .center(pos)
                        .radius(radius)
                        .strokeWidth(strokeWidth)
                        .fillColor(ViewUtils.getColor(getActivity(), circleColors[0]))
                        .strokeColor(ViewUtils.getColor(getActivity(), circleColors[1])));
            }
            if (animate) {
                animate(pos);
            }
        }
    }

    /**
     * Add marker to map with custom marker icon;
     * @param pos coordinates
     * @param title marker title
     * @param clear remove previous markers flag
     * @param markerStyle marker icon
     * @param animate animate to marker position
     * @param radius radius for circle around marker
     */
    public void addMarker(LatLng pos, String title, boolean clear, int markerStyle, boolean animate, int radius) {
        if (map != null) {
            markerRadius = radius;
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
            if (radius != -1) {
                int[] circleColors = cSetter.getMarkerRadiusStyle(markerStyle);
                map.addCircle(new CircleOptions()
                        .center(pos)
                        .radius(radius)
                        .strokeWidth(strokeWidth)
                        .fillColor(ViewUtils.getColor(getActivity(), circleColors[0]))
                        .strokeColor(ViewUtils.getColor(getActivity(), circleColors[1])));
            }
            if (animate) {
                animate(pos);
            }
        } else {
            Log.d(Constants.LOG_TAG, "map is null");
        }
    }

    /**
     * Recreate last added marker with new circle radius;
     * @param radius radius for a circle
     */
    public void recreateMarker(int radius) {
        markerRadius = radius;
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
            if (radius != -1) {
                int[] circleColors = cSetter.getMarkerRadiusStyle(markerStyle);
                map.addCircle(new CircleOptions()
                        .center(lastPos)
                        .radius(radius)
                        .strokeWidth(strokeWidth)
                        .fillColor(ViewUtils.getColor(getActivity(), circleColors[0]))
                        .strokeColor(ViewUtils.getColor(getActivity(), circleColors[1])));
            }
            animate(lastPos);
        }
    }

    /**
     * Recreate last added marker with new marker style;
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
     * @param pos coordinates
     */
    public void moveCamera(LatLng pos) {
        if (map != null) {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 13));
        }
    }

    /**
     * Move camera to coordinates with animation;
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
        if (map != null && map.getMyLocation() != null) {
            double lat = map.getMyLocation().getLatitude();
            double lon = map.getMyLocation().getLongitude();
            LatLng pos = new LatLng(lat, lon);
            animate(pos);
        }
    }

    /**
     * Move camera to user current coordinates with animation;
     * @param animate animation flag
     */
    public void moveToMyLocation(boolean animate) {
        if (map != null && map.getMyLocation() != null) {
            double lat = map.getMyLocation().getLatitude();
            double lon = map.getMyLocation().getLongitude();
            LatLng pos = new LatLng(lat, lon);
            if (animate) {
                animate(pos);
            }
        }
    }

    /**
     * Enable/Disable on map click listener;
     * @param isTouch flag
     */
    public void enableTouch(boolean isTouch) {
        this.isTouch = isTouch;
    }

    /**
     * Enable/Disable zoom button on map;
     * @param isZoom flag
     */
    public void enableZoom(boolean isZoom) {
        this.isZoom = isZoom;
    }

    /**
     * Enable/Disable marker style button on map;
     * @param isStyles flag
     */
    public void enableStyles(boolean isStyles) {
        this.isStyles = isStyles;
    }

    /**
     * Enable/Disable search field button on map;
     * @param isSearch flag
     */
    public void enableSearch(boolean isSearch) {
        this.isSearch = isSearch;
    }

    /**
     * Enable/Disable places list button on map;
     * @param isPlaces flag
     */
    public void enablePlaces(boolean isPlaces) {
        this.isPlaces = isPlaces;
    }

    /**
     * Enable/Disable map close button on map;
     * @param isBack flag
     */
    public void enableBack(boolean isBack) {
        this.isBack = isBack;
    }

    public boolean isFullscreen() {
        return isFullscreen;
    }

    public void setFullscreen(boolean fullscreen) {
        isFullscreen = fullscreen;
    }

    /**
     * Clear map;
     */
    public void clear() {
        if (map != null) map.clear();
    }

    /**
     * On back pressed interface for map;
     * @return
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_map, container, false);

        final SharedPrefs prefs = new SharedPrefs(getActivity());
        cSetter = new Coloring(getActivity());

        markerRadius = prefs.loadInt(Prefs.LOCATION_RADIUS);

        map = ((SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map)).getMap();
        map.getUiSettings().setMyLocationButtonEnabled(false);
        int type = prefs.loadInt(Prefs.MAP_TYPE);
        map.setMapType(type);

        setMyLocation();

        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                hideLayers();
                hidePlaces();
                hideStyles();
                if (isTouch) {
                    addMarker(latLng, markerTitle, true, true, markerRadius);
                }
            }
        });

        if (lastPos != null) {
            addMarker(lastPos, lastPos.toString(), true, false, markerRadius);
        }

        initViews(view);

        cardSearch = (AutoCompleteTextView) view.findViewById(R.id.cardSearch);
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
                    task = new GeocoderTask(getActivity(), new GeocoderTask.GeocoderListener() {
                        @Override
                        public void onAddressReceived(List<Address> addresses) {
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
                        }
                    });
                    task.execute(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        cardSearch.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Address sel = foundPlaces.get(position);
                double lat = sel.getLatitude();
                double lon = sel.getLongitude();
                LatLng pos = new LatLng(lat, lon);
                addMarker(pos, markerTitle, true, true, markerRadius);
                if (listener != null) {
                    listener.placeName(namesList.get(position));
                }
            }
        });

        placesList = (RecyclerView) view.findViewById(R.id.placesList);
        loadPlaces();

        return view;
    }

    private void initViews(View view) {
        groupOne = (LinearLayout) view.findViewById(R.id.groupOne);
        groupTwo = (LinearLayout) view.findViewById(R.id.groupTwo);
        groupThree = (LinearLayout) view.findViewById(R.id.groupThree);
        emptyItem = (LinearLayout) view.findViewById(R.id.emptyItem);

        placesList = (RecyclerView) view.findViewById(R.id.placesList);

        CardView zoomCard = (CardView) view.findViewById(R.id.zoomCard);
        CardView searchCard = (CardView) view.findViewById(R.id.searchCard);
        CardView myCard = (CardView) view.findViewById(R.id.myCard);
        CardView layersCard = (CardView) view.findViewById(R.id.layersCard);
        CardView placesCard = (CardView) view.findViewById(R.id.placesCard);
        CardView backCard = (CardView) view.findViewById(R.id.backCard);
        styleCard = (CardView) view.findViewById(R.id.styleCard);
        placesListCard = (CardView) view.findViewById(R.id.placesListCard);
        CardView markersCard = (CardView) view.findViewById(R.id.markersCard);
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

        layersContainer = (CardView) view.findViewById(R.id.layersContainer);
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

        ImageButton cardClear = (ImageButton) view.findViewById(R.id.cardClear);
        zoomOut = (ImageButton) view.findViewById(R.id.mapZoom);
        ImageButton layers = (ImageButton) view.findViewById(R.id.layers);
        ImageButton myLocation = (ImageButton) view.findViewById(R.id.myLocation);
        ImageButton markers = (ImageButton) view.findViewById(R.id.markers);
        ImageButton places = (ImageButton) view.findViewById(R.id.places);
        ImageButton backButton = (ImageButton) view.findViewById(R.id.backButton);

        cardClear.setOnClickListener(this);
        zoomOut.setOnClickListener(this);
        layers.setOnClickListener(this);
        myLocation.setOnClickListener(this);
        markers.setOnClickListener(this);
        places.setOnClickListener(this);
        backButton.setOnClickListener(this);

        TextView typeNormal = (TextView) view.findViewById(R.id.typeNormal);
        TextView typeSatellite = (TextView) view.findViewById(R.id.typeSatellite);
        TextView typeHybrid = (TextView) view.findViewById(R.id.typeHybrid);
        TextView typeTerrain = (TextView) view.findViewById(R.id.typeTerrain);
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
            new SharedPrefs(getActivity()).saveInt(Prefs.MAP_TYPE, type);
            ViewUtils.hideOver(layersContainer);
        }
    }

    private void setMyLocation() {
        if (ActivityCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getActivity(),
                        Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {
            Permissions.requestPermission(getActivity(),
                    new String[]{Permissions.ACCESS_FINE_LOCATION,
                            Permissions.ACCESS_COARSE_LOCATION}, 205);
        } else {
            map.setMyLocationEnabled(true);
        }
    }

    private void loadPlaces(){
        DataBase DB = new DataBase(getActivity());
        DB.open();
        Cursor c = DB.queryPlaces();
        spinnerArray = new ArrayList<>();
        spinnerArray.clear();
        if (c != null && c.moveToFirst()){
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

        if (spinnerArray.isEmpty()){
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
                    if (position > 0) {
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
                    }
                }

                @Override
                public void onItemLongClicked(int position, View view) {

                }
            });
            placesList.setLayoutManager(new LinearLayoutManager(getActivity()));
            placesList.setAdapter(adapter);
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
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
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
                if (map != null) {
                    Location location = map.getMyLocation();
                    if (location != null) {
                        double lat = location.getLatitude();
                        double lon = location.getLongitude();
                        LatLng pos = new LatLng(lat, lon);
                        animate(pos);
                    }
                }
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
}
