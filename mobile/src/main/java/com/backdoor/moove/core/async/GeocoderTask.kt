package com.backdoor.moove.core.async

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.AsyncTask
import android.util.Log

import java.io.IOException

/**
 * Task that helps find place by name.
 */
class GeocoderTask(mContext: Context, private val mListener: GeocoderListener?) : AsyncTask<String, Void, List<Address>>() {
    private val geocoder: Geocoder

    init {
        geocoder = Geocoder(mContext)
    }

    override fun doInBackground(vararg locationName: String): List<Address>? {
        var addresses: List<Address>? = null
        try {
            addresses = geocoder.getFromLocationName(locationName[0], 5)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return addresses
    }

    override fun onPostExecute(addresses: List<Address>?) {
        if (addresses == null || addresses.size == 0) {
            Log.d(TAG, "No Location found")
        } else {
            mListener?.onAddressReceived(addresses)
        }
    }

    /**
     * Listener for found places list.
     */
    interface GeocoderListener {
        fun onAddressReceived(addresses: List<Address>)
    }

    companion object {

        private val TAG = "GeocoderTask"
    }
}
