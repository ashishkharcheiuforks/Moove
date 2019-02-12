package com.backdoor.moove.core.utils

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.provider.Settings
import android.util.Log

import com.backdoor.moove.R
import com.backdoor.moove.core.interfaces.ActionCallbacks
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GooglePlayServicesUtil

import java.io.IOException
import java.util.Locale

/**
 * Helper class for work with user coordinates.
 */
object LocationUtil {

    /**
     * Check if user enable on device any location service.
     *
     * @param context application context.
     * @return boolean
     */
    fun checkLocationEnable(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        return !(!isGPSEnabled && !isNetworkEnabled)
    }

    /**
     * Show dialog for enabling location service on device.
     *
     * @param context application context.
     */
    fun showLocationAlert(context: Context, callbacks: ActionCallbacks) {
        callbacks.showSnackbar(R.string.gps_is_not_enabled, R.string.settings) { v ->
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            context.startActivity(intent)
        }
    }

    /**
     * Check if device has installed Google Play Services.
     *
     * @param a Activity
     * @return boolean
     */
    fun checkPlay(a: Activity): Int {
        return GooglePlayServicesUtil.isGooglePlayServicesAvailable(a.applicationContext)
    }

    /**
     * Show alert dialog for Play Services.
     *
     * @param a          Activity.
     * @param resultCode result code.
     */
    fun showPlayDialog(a: Activity, resultCode: Int) {
        val dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, a, 99)
        dialog.setCancelable(false)
        dialog.setOnDismissListener { it.dismiss() }
        dialog.show()
    }

    /**
     * Check if user has installed Google Play Services.
     *
     * @param a activity.
     * @return boolean
     */
    fun playServicesFullCheck(a: Activity): Boolean {
        val resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(a.applicationContext)
        if (resultCode != ConnectionResult.SUCCESS) {
            showPlayDialog(a, resultCode)
            return false
        } else {
            Log.d("GooglePlayServicesUtil", "Result is: $resultCode")
            return true
        }
    }

    /**
     * Get shorter string coordinates.
     *
     * @param latitude  latitude.
     * @param longitude longitude.
     * @return parsed latitude and longitude to String
     */
    fun getAddress(latitude: Double, longitude: Double): String {
        return String.format("%.5f", latitude) + ", " +
                String.format("%.5f", longitude)
    }

    /**
     * Get address from coordinates.
     *
     * @param context application context.
     * @param lat     latitude.
     * @param lon     longitude.
     * @return address string
     */
    fun getAddress(context: Context, lat: Double, lon: Double): String? {
        var place: String? = null
        val geocoder = Geocoder(context, Locale.getDefault())
        try {
            val listAddresses = geocoder.getFromLocation(lat, lon, 1)
            if (null != listAddresses && listAddresses.size > 0) {
                place = listAddresses[0].getAddressLine(0)
            }
        } catch (e: IOException) {
            place = getAddress(lat, lon)
        }

        return place
    }
}
