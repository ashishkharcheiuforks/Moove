package com.backdoor.moove.core.services;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.backdoor.moove.core.consts.Constants;
import com.backdoor.moove.core.consts.Prefs;
import com.backdoor.moove.core.helper.SharedPrefs;

public class GeolocationService extends Service {

    private LocationManager mLocationManager;
    private LocationListener mLocList;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(Constants.LOG_TAG, "geo service started");
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mLocList = new MyLocation();
        updateListener();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mLocationManager.removeUpdates(mLocList);
        mLocationManager = null;
        stopService(new Intent(getApplicationContext(), CheckPosition.class));
        Log.d(Constants.LOG_TAG, "geo service stop");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public class MyLocation implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            startService(new Intent(getApplicationContext(), CheckPosition.class)
                    //.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .putExtra("lat", latitude)
                    .putExtra("lon", longitude));
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            updateListener();
        }

        @Override
        public void onProviderEnabled(String provider) {
            updateListener();
        }

        @Override
        public void onProviderDisabled(String provider) {
            updateListener();
        }
    }

    @SuppressLint("MissingPermission")
    private void updateListener() {
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        SharedPrefs prefs = new SharedPrefs(getApplicationContext());
        long time = (prefs.loadInt(Prefs.TRACK_TIME) * 1000);
        int distance = prefs.loadInt(Prefs.TRACK_DISTANCE);
        if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, time, distance, mLocList);
        } else {
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, time, distance, mLocList);
        }
    }
}
