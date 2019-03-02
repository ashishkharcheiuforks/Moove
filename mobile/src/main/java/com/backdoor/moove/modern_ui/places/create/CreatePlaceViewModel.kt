package com.backdoor.moove.modern_ui.places.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.backdoor.moove.data.Place
import com.backdoor.moove.data.RoomDb
import com.backdoor.moove.utils.launchDefault
import kotlinx.coroutines.runBlocking
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject

class CreatePlaceViewModel(val id: String) : ViewModel(), KoinComponent {

    private val db: RoomDb by inject()

    val loadedPlace = db.placeDao().loadById(id)
    var place: Place = Place()
    var isPlaceEdited = false

    fun deletePlace(place: Place) {
        launchDefault {
            runBlocking {
                db.placeDao().delete(place)
            }
        }
    }

    fun savePlace(place: Place) {
        launchDefault {
            runBlocking {
                db.placeDao().insert(place)
            }
        }
    }

    class Factory(val id: String) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return CreatePlaceViewModel(id) as T
        }
    }
}
