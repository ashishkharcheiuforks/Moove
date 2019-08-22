package com.backdoor.moove.utils

import android.content.Context
import android.location.Address
import android.location.Geocoder
import kotlinx.coroutines.Job

object GeocoderTask {

    private var mJob: Job? = null

    fun findAddresses(context: Context, address: String, listener: ((List<Address>) -> Unit)?) {
        cancelJob()
        val geocoder = Geocoder(context)
        mJob = launchDefault {
            val addresses: MutableList<Address> = mutableListOf()
            try {
                addresses.addAll(geocoder.getFromLocationName(address, 5))
            } catch (e: Exception) {
            }
            withUIContext {
                listener?.invoke(addresses)
            }
            mJob = null
        }
    }

    fun findAddress(context: Context, lat: Double, lon: Double): Address? {
        val geocoder = Geocoder(context)
        var address: Address? = null
        try {
            address = geocoder.getFromLocation(lat, lon, 1)?.first()
        } catch (e: Exception) {
        }
        return address
    }

    fun cancelJob() {
        mJob?.cancel()
    }
}
