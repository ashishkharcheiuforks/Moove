package com.backdoor.moove.utils

import com.backdoor.moove.data.RoomDb
import org.koin.core.KoinComponent
import org.koin.core.inject

class EnableThread : KoinComponent {

    private val roomDb: RoomDb by inject()
    private val locationEvent: LocationEvent by inject()

    fun run() {
        launchDefault {
            for (item in roomDb.reminderDao().getAll(active = true, removed = false)) {
                locationEvent.withReminder(item).start()
            }
        }
    }
}