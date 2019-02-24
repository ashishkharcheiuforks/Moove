package com.backdoor.moove.modern_ui.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.backdoor.moove.data.Reminder
import com.backdoor.moove.data.RoomDb
import com.backdoor.moove.utils.LocationEvent
import com.backdoor.moove.utils.launchDefault
import kotlinx.coroutines.runBlocking
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import java.util.*

class CreateReminderViewModel(val uuId: String) : ViewModel(), KoinComponent {

    val db: RoomDb by inject()
    val locationEvent: LocationEvent by inject()

    val loadedReminder = db.reminderDao().loadById(uuId)
    var reminder: Reminder = Reminder()
    var isReminderEdited = false
    var isMessage: Boolean = false
    var isDelayAdded: Boolean = false
    var isLeave: Boolean = false

    var day: Int = 0
    var month: Int = 0
    var year: Int = 0

    var hour: Int = 0
    var minute: Int = 0

    init {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        day = calendar.get(Calendar.DAY_OF_MONTH)
        month = calendar.get(Calendar.MONTH)
        year = calendar.get(Calendar.YEAR)
        hour = calendar.get(Calendar.HOUR)
        minute = calendar.get(Calendar.MINUTE)
    }

    fun saveAndStart(reminder: Reminder) {
        launchDefault {
            runBlocking {
                db.reminderDao().insert(reminder)
            }
            locationEvent.withReminder(reminder).start()
        }
    }

    fun pauseReminder(reminder: Reminder) {
        if (reminder.isActive) {
            launchDefault {
                locationEvent.withReminder(reminder).pause()
            }
        }
    }

    fun resumeReminder(reminder: Reminder) {
        if (reminder.isActive) {
            launchDefault {
                locationEvent.withReminder(reminder).resume()
            }
        }
    }

    fun deleteReminder(reminder: Reminder) {
        launchDefault {
            runBlocking {
                locationEvent.withReminder(reminder).stop()
                db.reminderDao().delete(reminder)
            }
        }
    }

    class Factory(val id: String) : ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return CreateReminderViewModel(id) as T
        }
    }
}
