package com.backdoor.moove.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.android.gms.maps.model.LatLng
import java.util.*

@Entity
data class Reminder(
        @PrimaryKey
        var uuId: String = UUID.randomUUID().toString(),
        var summary: String = "",
        var type: String = "",
        var melody: String = "",
        var delayTime: String = "",
        var phoneNumber: String = "",
        var widgetId: String = "",
        var createdAt: String = "",
        var ledColor: Int = -1,
        var radius: Int = -1,
        var volume: Int = -1,
        var uniqueId: Int = Random().nextInt(Integer.MAX_VALUE),
        var markerColor: Int = 0,
        var latitude: Double = 0.0,
        var longitude: Double = 0.0,
        var isLocked: Boolean = false,
        var isActive: Boolean = false,
        var isRemoved: Boolean = false,
        var isNotificationShown: Boolean = false,
        var hasDelay: Boolean = false
) {

    fun latLng(): LatLng = LatLng(latitude, longitude)

    fun hasPlace(): Boolean {
        return latitude != 0.0 && longitude != 0.0
    }
}