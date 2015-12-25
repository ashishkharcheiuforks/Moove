package com.backdoor.moove;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.backdoor.moove.core.consts.Constants;
import com.backdoor.moove.core.consts.Prefs;
import com.backdoor.moove.core.fragments.GeneralSettingsFragment;
import com.backdoor.moove.core.fragments.LocationSettingsFragment;
import com.backdoor.moove.core.fragments.NotificationSettingsFragment;
import com.backdoor.moove.core.fragments.OtherSettingsFragment;
import com.backdoor.moove.core.fragments.SettingsFragment;
import com.backdoor.moove.core.helper.ColorSetter;
import com.backdoor.moove.core.helper.SharedPrefs;

import java.io.File;
import java.util.Calendar;

/**
 * Custom setting activity.
 */
public class SettingsActivity extends AppCompatActivity implements
        SettingsFragment.OnHeadlineSelectedListener {

    private ColorSetter cSetter = new ColorSetter(SettingsActivity.this);
    private boolean isCreate = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cSetter = new ColorSetter(SettingsActivity.this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(cSetter.colorPrimaryDark());
        }
        setContentView(R.layout.category_layout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.settings);

        if (findViewById(R.id.fragment_container) != null) {
            if (savedInstanceState != null) {
                return;
            }
            SettingsFragment firstFragment = new SettingsFragment();
            firstFragment.setArguments(getIntent().getExtras());
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, firstFragment).commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Attach settings fragment.
     * @param position list position.
     */
    public void onArticleSelected(int position) {
        if (position == 0){
            GeneralSettingsFragment newFragment = new GeneralSettingsFragment();
            Bundle args = new Bundle();
            newFragment.setArguments(args);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, newFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        } else if (position == 1){
            NotificationSettingsFragment newFragment = new NotificationSettingsFragment();
            Bundle args = new Bundle();
            newFragment.setArguments(args);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, newFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        } else if (position == 2){
            LocationSettingsFragment newFragment = new LocationSettingsFragment();
            Bundle args = new Bundle();
            newFragment.setArguments(args);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, newFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        } else if (position == 3){
            OtherSettingsFragment newFragment = new OtherSettingsFragment();
            Bundle args = new Bundle();
            newFragment.setArguments(args);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, newFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case 201:
                if (resultCode == RESULT_OK) {
                    new SharedPrefs(this).saveBoolean(Prefs.CUSTOM_SOUND, true);
                    String pathStr = data.getStringExtra(Constants.FILE_PICKED);
                    if (pathStr != null) {
                        File fileC = new File(pathStr);
                        if (fileC.exists()) {
                            new SharedPrefs(this).savePrefs(Prefs.CUSTOM_SOUND_FILE, fileC.toString());
                        }
                    }
                }
                break;
            case Constants.ACTION_REQUEST_GALLERY:
                if (resultCode == RESULT_OK) {
                    Uri selectedImage = data.getData();
                    new SharedPrefs(this).savePrefs(Prefs.REMINDER_IMAGE, selectedImage.toString());
                }
                break;
        }
    }
}