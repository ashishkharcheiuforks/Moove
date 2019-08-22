package com.backdoor.moove.modern_ui.events_map

import androidx.lifecycle.ViewModel
import com.backdoor.moove.data.RoomDb
import org.koin.core.KoinComponent
import org.koin.core.inject

class EventsMapViewModel : ViewModel(), KoinComponent {

    private val db: RoomDb by inject()
    val reminders = db.reminderDao().loadAll(active = true, removed = false)

}
