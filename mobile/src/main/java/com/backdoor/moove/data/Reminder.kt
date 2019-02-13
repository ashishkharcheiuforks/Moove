package com.backdoor.moove.data

import androidx.room.Entity
import androidx.room.PrimaryKey
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
        var markerColor: Int = 0,
        var latitude: Double,
        var longitude: Double,
        var isLocked: Boolean,
        var isActive: Boolean,
        var isRemoved: Boolean,
        var isNotificationShown: Boolean

)