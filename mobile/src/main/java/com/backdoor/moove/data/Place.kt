package com.backdoor.moove.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class Place(
        @PrimaryKey
        var uuId: String = UUID.randomUUID().toString(),
        var name: String = "",
        var createdAt: String = "",
        var markerColor: Int = 0,
        var latitude: Double,
        var longitude: Double
)