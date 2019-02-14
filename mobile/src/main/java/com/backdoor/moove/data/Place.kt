package com.backdoor.moove.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.android.gms.maps.model.LatLng
import java.util.*

@Entity
data class Place(
        @PrimaryKey
        var uuId: String = UUID.randomUUID().toString(),
        var name: String = "",
        var createdAt: String = "",
        var markerColor: Int = 0,
        var latitude: Double = 0.0,
        var longitude: Double = 0.0
) {
    fun latLng(): LatLng = LatLng(latitude, longitude)

    fun hasLatLng(): Boolean = latitude != 0.0 && longitude != 0.0
}