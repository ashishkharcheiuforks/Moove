package com.backdoor.moove.utils

import android.content.Context
import com.backdoor.moove.data.Reminder
import com.backdoor.moove.data.RoomDb
import com.backdoor.moove.services.GeolocationService
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import timber.log.Timber

class LocationEvent(val context: Context) : KoinComponent {

    private val db: RoomDb by inject()
    private var reminder: Reminder = Reminder()

    val isActive: Boolean
        get() = reminder.isActive

    fun withReminder(reminder: Reminder): LocationEvent {
        this.reminder = reminder
        return this
    }

    fun start(): Boolean {
        Timber.d("start: $reminder")
        return if (Module.hasLocation(context)) {
            reminder.isActive = true
            reminder.isRemoved = false
            save()
            val b = EventJobService.enablePositionDelay(reminder)
            if (!b) SuperUtil.startGpsTracking(context)
            true
        } else {
            stop()
            remove()
            false
        }
    }

    fun stop(): Boolean {
        Timber.d("stop: $reminder")
        EventJobService.cancelReminder(reminder.uuId)
        reminder.isActive = false
        save()
        Notifier.hideNotification(context, reminder.uniqueId)
        stopTracking(false)
        return true
    }

    fun pause(): Boolean {
        Timber.d("pause: $reminder")
        EventJobService.cancelReminder(reminder.uuId)
        stopTracking(true)
        return true
    }

    fun resume(): Boolean {
        Timber.d("resume: $reminder")
        if (reminder.isActive) {
            val b = EventJobService.enablePositionDelay(reminder)
            if (!b) SuperUtil.startGpsTracking(context)
        }
        return true
    }

    fun onOff(): Boolean {
        Timber.d("onOff: $reminder")
        return if (isActive) {
            stop()
        } else {
            reminder.isLocked = false
            reminder.isNotificationShown = false
            save()
            start()
        }
    }

    private fun stopTracking(isPaused: Boolean) {
        val list = db.reminderDao().getAll()
        if (list.isEmpty()) {
            SuperUtil.stopService(context, GeolocationService::class.java)
        }
        var hasActive = false
        for (item in list) {
            if (isPaused) {
                if (item.uniqueId == reminder.uniqueId) {
                    continue
                }
                if (!item.hasDelay || !TimeUtils.isCurrent(item.delayTime)) {
                    if (!item.isNotificationShown) {
                        hasActive = true
                        break
                    }
                } else {
                    if (!item.isNotificationShown) {
                        hasActive = true
                        break
                    }
                }
            } else {
                if (!item.isNotificationShown) {
                    hasActive = true
                    break
                }
            }
        }
        if (!hasActive) {
            SuperUtil.stopService(context, GeolocationService::class.java)
        }
    }

    private fun save() {
        db.reminderDao().insert(reminder)
        WidgetUtil.updateWidgets(context)
    }

    private fun remove() {
        db.reminderDao().delete(reminder)
        WidgetUtil.updateWidgets(context)
    }
}