package com.backdoor.moove.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.backdoor.moove.data.RoomDb
import com.backdoor.moove.utils.LocationEvent
import com.backdoor.moove.utils.launchDefault
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import timber.log.Timber

class BootReceiver : BroadcastReceiver(), KoinComponent {

    val locationEvent: LocationEvent by inject()

    override fun onReceive(context: Context, intent: Intent) {
        Timber.d("onReceive: ")
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            launchDefault {
                for (item in RoomDb.getInMemoryDatabase(context).reminderDao().getAll(active = true, removed = false)) {
                    locationEvent.withReminder(item).start()
                }
            }
        }
    }
}