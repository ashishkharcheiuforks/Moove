package com.backdoor.moove;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
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
import com.backdoor.moove.core.helper.Coloring;
import com.backdoor.moove.core.helper.Dialogues;
import com.backdoor.moove.core.helper.SharedPrefs;

import java.io.File;

/**
 * Custom setting activity.
 */
public class SettingsActivity extends AppCompatActivity implements
        SettingsFragment.OnHeadlineSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Coloring cSetter = new Coloring(SettingsActivity.this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(cSetter.colorPrimaryDark());
        }
        setContentView(R.layout.category_layout);
        Toolbar toolbar = findViewById(R.id.toolbar);
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
     *
     * @param position list position.
     */
    public void onArticleSelected(int position) {
        if (position == 0) {
            attachFragment(new GeneralSettingsFragment());
        } else if (position == 1) {
            attachFragment(new NotificationSettingsFragment());
        } else if (position == 2) {
            attachFragment(new LocationSettingsFragment());
        } else if (position == 3) {
            attachFragment(new OtherSettingsFragment());
        }
    }

    private void attachFragment(Fragment fragment) {
        Bundle args = new Bundle();
        fragment.setArguments(args);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 103:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Dialogues.melodyType(SettingsActivity.this, Prefs.CUSTOM_SOUND, 201);
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
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