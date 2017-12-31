package com.backdoor.moove;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.backdoor.moove.core.consts.Prefs;
import com.backdoor.moove.core.helper.Permissions;
import com.backdoor.moove.core.helper.SharedPrefs;
import com.backdoor.moove.core.utils.LocationUtil;
import com.google.android.gms.common.ConnectionResult;

public class StartHelpActivity extends AppCompatActivity {

    private RelativeLayout serviceShow, locationShow;
    private int resultCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_help);

        serviceShow = findViewById(R.id.serviceShow);
        locationShow = findViewById(R.id.locationShow);

        Button servicesFix = findViewById(R.id.servicesFix);
        Button permissionsFix = findViewById(R.id.permissionsFix);

        permissionsFix.setOnClickListener(v -> requestPermission());
        servicesFix.setOnClickListener(v -> LocationUtil.showPlayDialog(StartHelpActivity.this, resultCode));
        checkAll();
    }

    private void requestPermission() {
        Permissions.requestPermission(StartHelpActivity.this, 200, Permissions.ACCESS_COARSE_LOCATION,
                Permissions.ACCESS_FINE_LOCATION);
    }

    private void checkAll() {
        if (checkDevice() && checkPermissions()) {
            startActivity(new Intent(StartHelpActivity.this, MainActivity.class));
            SharedPrefs.getInstance(this).saveBoolean(Prefs.FIRST_LOAD, true);
            finish();
        } else {
            if (!checkDevice()) {
                serviceShow.setVisibility(View.VISIBLE);
            } else {
                serviceShow.setVisibility(View.GONE);
            }
            if (!checkPermissions()) {
                locationShow.setVisibility(View.VISIBLE);
            } else {
                locationShow.setVisibility(View.GONE);
            }
        }
    }

    private boolean checkPermissions() {
        return Permissions.checkPermission(StartHelpActivity.this, Permissions.ACCESS_COARSE_LOCATION,
                Permissions.ACCESS_FINE_LOCATION);
    }

    private boolean checkDevice() {
        resultCode = LocationUtil.checkPlay(StartHelpActivity.this);
        return resultCode == ConnectionResult.SUCCESS;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length == 0) return;
        if (requestCode == 200) {
            checkAll();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 99) {
            checkAll();
        }
    }
}
