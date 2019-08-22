package com.backdoor.moove.modern_ui.places

import androidx.lifecycle.ViewModel
import com.backdoor.moove.data.Place
import com.backdoor.moove.data.RoomDb
import com.backdoor.moove.utils.launchDefault
import kotlinx.coroutines.runBlocking
import org.koin.core.KoinComponent
import org.koin.core.inject

class PlacesViewModel : ViewModel(), KoinComponent {

    private val db: RoomDb by inject()

    val places = db.placeDao().loadAll()

    fun deletePlace(place: Place) {
        launchDefault {
            runBlocking {
                db.placeDao().delete(place)
            }
        }
    }
}
