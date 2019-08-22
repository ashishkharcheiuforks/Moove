package com.backdoor.moove.modern_ui.home

import androidx.lifecycle.ViewModel
import com.backdoor.moove.data.Reminder
import com.backdoor.moove.data.RoomDb
import com.backdoor.moove.utils.LocationEvent
import com.backdoor.moove.utils.launchDefault
import kotlinx.coroutines.runBlocking
import org.koin.core.KoinComponent
import org.koin.core.inject

class HomeViewModel : ViewModel(), KoinComponent {

    private val db: RoomDb by inject()
    private val locationEvent: LocationEvent by inject()

    val reminders = db.reminderDao().loadAll()

    fun deleteReminder(reminder: Reminder) {
        launchDefault {
            runBlocking {
                locationEvent.withReminder(reminder).stop()
                db.reminderDao().delete(reminder)
            }
        }
    }

    fun toggle(reminder: Reminder) {
        launchDefault {
            runBlocking {
                locationEvent.withReminder(reminder).onOff()
            }
        }
    }

    fun save(reminder: Reminder) {
        launchDefault {
            db.reminderDao().insert(reminder)
        }
    }
}
