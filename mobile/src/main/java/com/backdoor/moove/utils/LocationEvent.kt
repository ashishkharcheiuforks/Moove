package com.backdoor.moove.utils

import android.content.Context
import android.text.TextUtils
import com.backdoor.moove.data.Reminder
import com.backdoor.moove.data.RoomDb
import com.backdoor.moove.services.GeolocationService
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject

class LocationEvent(val context: Context) : KoinComponent {

    val db: RoomDb by inject()
    val prefs: Prefs by inject()
    var reminder: Reminder = Reminder()
        private set

    val isActive: Boolean
        get() = reminder.isActive

    fun withReminder(reminder: Reminder): LocationEvent {
        this.reminder = reminder
        return this
    }

    fun start(): Boolean {
        return if (Module.hasLocation(context)) {
            reminder.isActive = true
            reminder.isRemoved = false
            save()
            if (EventJobService.enablePositionDelay(context, reminder.uuId)) {
                true
            } else {
                SuperUtil.startGpsTracking(context)
                true
            }
        } else {
            stop()
            remove()
            false
        }
    }

    fun stop(): Boolean {
        EventJobService.cancelReminder(reminder.uuId)
        reminder.isActive = false
        save()
        Notifier.hideNotification(context, reminder.uniqueId)
        stopTracking(false)
        return true
    }

    fun pause(): Boolean {
        EventJobService.cancelReminder(reminder.uuId)
        stopTracking(true)
        return true
    }

    fun resume(): Boolean {
        if (reminder.isActive) {
            val b = EventJobService.enablePositionDelay(context, reminder.uuId)
            if (!b) SuperUtil.startGpsTracking(context)
        }
        return true
    }

    fun onOff(): Boolean {
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
                if (TextUtils.isEmpty(item.delayTime) || !TimeCount.isCurrent(item.delayTime)) {
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