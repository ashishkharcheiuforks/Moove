package com.backdoor.moove.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import timber.log.Timber

class LocationTracker(private val mContext: Context, private val mCallback: ((lat: Double, lng: Double) -> Unit)?) : LocationListener, KoinComponent {

    private var mLocationManager: LocationManager? = null
    private val prefs: Prefs by inject()

    init {
        updateListener()
    }

    fun removeUpdates() {
        mLocationManager?.removeUpdates(this)
    }

    @SuppressLint("MissingPermission")
    private fun updateListener() {
        Timber.d("updateListener: ")
        val time = (prefs.trackTime * 1000 * 2).toLong()
        val locationManager = mContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        if (locationManager != null) {
            val criteria = Criteria()
            val bestProvider = locationManager.getBestProvider(criteria, false)
            locationManager.requestLocationUpdates(
                    bestProvider,
                    time,
                    3.0f,
                    this,
                    Looper.getMainLooper()
            )
        }
        this.mLocationManager = locationManager
    }

    override fun onLocationChanged(location: Location?) {
        Timber.d("onLocationResult: $location")
        if (location != null) {
            val latitude = location.latitude
            val longitude = location.longitude
            mCallback?.invoke(latitude, longitude)
        }
    }

    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
        Timber.d("onStatusChanged: $provider")
        updateListener()
    }

    override fun onProviderEnabled(provider: String) {
        Timber.d("onProviderEnabled: $provider")
        updateListener()
    }

    override fun onProviderDisabled(provider: String) {
        Timber.d("onProviderDisabled: $provider")
        updateListener()
    }
}