package com.backdoor.moove.utils

import com.google.android.gms.maps.model.LatLng

interface MapListener {
    fun placeChanged(place: LatLng, address: String)

    fun onBackClick()
}
