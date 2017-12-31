package com.backdoor.moove.core.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.backdoor.moove.PlacesListActivity;
import com.backdoor.moove.R;
import com.backdoor.moove.core.consts.Configs;
import com.backdoor.moove.core.consts.Prefs;
import com.backdoor.moove.core.dialogs.MarkerStyle;
import com.backdoor.moove.core.dialogs.TargetRadius;
import com.backdoor.moove.core.dialogs.TrackerOption;
import com.backdoor.moove.core.helper.Dialogues;
import com.backdoor.moove.core.helper.Module;
import com.backdoor.moove.core.helper.SharedPrefs;
import com.backdoor.moove.core.views.PrefsView;

public class LocationSettingsFragment extends Fragment implements View.OnClickListener {

    private SharedPrefs sPrefs;
    private ActionBar ab;

    private PrefsView notificationOptionPrefs, radiusPrefs, autoFill;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.settings_location, container, false);
        sPrefs = SharedPrefs.getInstance(getActivity());

        ab = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (ab != null) {
            ab.setTitle(R.string.location);
        }

        if (Module.isLollipop()) {
            rootView.findViewById(R.id.locationCard).setElevation(Configs.CARD_ELEVATION);
        }

        TextView mapType = rootView.findViewById(R.id.mapType);
        mapType.setOnClickListener(this);

        notificationOptionPrefs = rootView.findViewById(R.id.notificationOptionPrefs);
        notificationOptionPrefs.setChecked(sPrefs.loadBoolean(Prefs.TRACKING_NOTIFICATION));
        notificationOptionPrefs.setOnClickListener(this);

        autoFill = rootView.findViewById(R.id.autoFill);
        autoFill.setChecked(sPrefs.loadBoolean(Prefs.PLACES_AUTO));
        autoFill.setOnClickListener(this);

        radiusPrefs = rootView.findViewById(R.id.radiusPrefs);
        radiusPrefs.setOnClickListener(this);

        TextView places = rootView.findViewById(R.id.places);
        places.setOnClickListener(this);

        TextView tracker = rootView.findViewById(R.id.tracker);
        tracker.setOnClickListener(this);

        TextView markerStyle = rootView.findViewById(R.id.markerStyle);
        markerStyle.setOnClickListener(this);

        return rootView;
    }

    private void notificationChange() {
        if (notificationOptionPrefs.isChecked()) {
            sPrefs.saveBoolean(Prefs.TRACKING_NOTIFICATION, false);
            notificationOptionPrefs.setChecked(false);
        } else {
            sPrefs.saveBoolean(Prefs.TRACKING_NOTIFICATION, true);
            notificationOptionPrefs.setChecked(true);
        }
    }

    private void placesChange() {
        if (autoFill.isChecked()) {
            sPrefs.saveBoolean(Prefs.PLACES_AUTO, false);
            autoFill.setChecked(false);
        } else {
            sPrefs.saveBoolean(Prefs.PLACES_AUTO, true);
            autoFill.setChecked(true);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        radiusPrefs.setValueText(sPrefs.loadInt(Prefs.LOCATION_RADIUS) + getString(R.string.m));
    }

    @Override
    public void onDetach() {
        super.onDetach();
        ab = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (ab != null) {
            ab.setTitle(R.string.settings);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mapType:
                Dialogues.mapType(getActivity());
                break;
            case R.id.notificationOptionPrefs:
                notificationChange();
                break;
            case R.id.radiusPrefs:
                getActivity().getApplicationContext()
                        .startActivity(new Intent(getActivity().getApplicationContext(),
                                TargetRadius.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                break;
            case R.id.places:
                getActivity().getApplicationContext()
                        .startActivity(new Intent(getActivity().getApplicationContext(),
                                PlacesListActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                break;
            case R.id.tracker:
                getActivity().getApplicationContext()
                        .startActivity(new Intent(getActivity().getApplicationContext(),
                                TrackerOption.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                break;
            case R.id.markerStyle:
                getActivity().getApplicationContext()
                        .startActivity(new Intent(getActivity().getApplicationContext(),
                                MarkerStyle.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                break;
            case R.id.autoFill:
                placesChange();
                break;
        }
    }
}
