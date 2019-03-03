package com.backdoor.moove.utils

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import com.google.android.gms.location.*
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import timber.log.Timber

class LocationTracker(private val mContext: Context, private val mCallback: ((lat: Double, lng: Double) -> Unit)?) : KoinComponent {
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            Timber.d("onLocationResult: $locationResult")
            for (location in locationResult!!.locations) {
                val latitude = location.latitude
                val longitude = location.longitude
                mCallback?.invoke(latitude, longitude)
                break
            }
        }
    }

    private val prefs: Prefs by inject()

    init {
        updateListener()
    }

    fun removeUpdates() {
        mFusedLocationClient?.removeLocationUpdates(mLocationCallback)
    }

    @SuppressLint("MissingPermission")
    private fun updateListener() {
        Timber.d("updateListener: ")
        val time = (prefs.trackTime * 1000 * 2).toLong()
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(mContext)
        val locationRequest = LocationRequest()
        locationRequest.interval = time
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        val builder = LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
        val client = LocationServices.getSettingsClient(mContext)
        val task = client.checkLocationSettings(builder.build())
        task.addOnSuccessListener { mFusedLocationClient?.requestLocationUpdates(locationRequest, mLocationCallback, Looper.myLooper()) }
    }
}