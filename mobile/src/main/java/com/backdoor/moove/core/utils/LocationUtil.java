package com.backdoor.moove.core.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;

import com.backdoor.moove.R;
import com.backdoor.moove.core.interfaces.ActionCallbacks;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Helper class for work with user coordinates.
 */
public class LocationUtil {

    /**
     * Check if user enable on device any location service.
     * @param context application context.
     * @return
     */
    public static boolean checkLocationEnable(Context context){
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean isGPSEnabled = locationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = locationManager
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        return !(!isGPSEnabled && !isNetworkEnabled);
    }

    /**
     * Show dialog for enabling location service on device.
     * @param context application context.
     */
    public static void showLocationAlert(final Context context, ActionCallbacks callbacks){
        callbacks.showSnackbar(R.string.gps_is_not_enabled, R.string.settings, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                context.startActivity(intent);
            }
        });
    }

    /**
     * Check if user has installed Google Play Services.
     * @param a activity.
     * @return
     */
    public static boolean checkGooglePlayServicesAvailability(Activity a) {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(a.getApplicationContext());
        if(resultCode != ConnectionResult.SUCCESS) {
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, a, 69);
            dialog.setCancelable(false);
            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    dialog.dismiss();
                }
            });
            dialog.show();
            return false;
        } else {
            Log.d("GooglePlayServicesUtil", "Result is: " + resultCode);
            return true;
        }
    }

    /**
     * Get shorter string coordinates.
     * @param currentLat latitude.
     * @param currentLong longitude.
     * @return
     */
    public static String getAddress(double currentLat, double currentLong){
        return String.format("%.5f", currentLat) + ", " +
                String.format("%.5f", currentLong);
    }

    /**
     * Get address from coordinates.
     * @param context application context.
     * @param lat latitude.
     * @param lon longitude.
     * @return
     */
    public static String getAddress(Context context, double lat, double lon){
        String place = null;
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> listAddresses = geocoder.getFromLocation(lat, lon, 1);
            if (null != listAddresses && listAddresses.size() > 0) {
                place = listAddresses.get(0).getAddressLine(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return place;
    }
}
