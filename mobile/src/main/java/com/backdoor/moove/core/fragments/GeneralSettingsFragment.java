package com.backdoor.moove.core.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.backdoor.moove.R;
import com.backdoor.moove.core.consts.Prefs;
import com.backdoor.moove.core.helper.SharedPrefs;
import com.backdoor.moove.core.views.PrefsView;

public class GeneralSettingsFragment extends Fragment implements View.OnClickListener {
    
    private SharedPrefs sPrefs;
    private ActionBar ab;
    
    private PrefsView use24TimePrefs, wearEnablePrefs;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView =  inflater.inflate(R.layout.settings_general, container, false);

        ab = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (ab != null){
            ab.setTitle(R.string.general);
        }

        getActivity().getIntent().setAction("General attached");
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());

        use24TimePrefs = (PrefsView) rootView.findViewById(R.id.use24TimePrefs);
        use24TimePrefs.setChecked(sPrefs.loadBoolean(Prefs.IS_24_TIME_FORMAT));
        use24TimePrefs.setOnClickListener(this);

        wearEnablePrefs = (PrefsView) rootView.findViewById(R.id.wearEnablePrefs);
        wearEnablePrefs.setChecked(sPrefs.loadBoolean(Prefs.WEAR_NOTIFICATION));
        wearEnablePrefs.setOnClickListener(this);
        
        return rootView;
    }

    private void _24Change (){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        if (use24TimePrefs.isChecked()){
            sPrefs.saveBoolean(Prefs.IS_24_TIME_FORMAT, false);
            use24TimePrefs.setChecked(false);
        } else {
            sPrefs.saveBoolean(Prefs.IS_24_TIME_FORMAT, true);
            use24TimePrefs.setChecked(true);
        }
    }

    private void wearChange (){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        if (wearEnablePrefs.isChecked()){
            sPrefs.saveBoolean(Prefs.WEAR_NOTIFICATION, false);
            wearEnablePrefs.setChecked(false);
        } else {
            sPrefs.saveBoolean(Prefs.WEAR_NOTIFICATION, true);
            wearEnablePrefs.setChecked(true);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        ab = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (ab != null){
            ab.setTitle(R.string.settings);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.use24TimePrefs:
                _24Change();
                break;
            case R.id.wearEnablePrefs:
                wearChange();
                break;
        }
    }
}
