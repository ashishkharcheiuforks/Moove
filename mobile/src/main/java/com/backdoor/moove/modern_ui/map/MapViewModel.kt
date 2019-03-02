package com.backdoor.moove.modern_ui.map

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.backdoor.moove.data.Place
import com.backdoor.moove.data.RoomDb
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject

class MapViewModel : ViewModel(), KoinComponent {

    private val db: RoomDb by inject()

    val places: LiveData<List<Place>> = db.placeDao().loadAll()
}
