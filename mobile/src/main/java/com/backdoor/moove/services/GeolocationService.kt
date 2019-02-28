package com.backdoor.moove.services

import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.backdoor.moove.R
import com.backdoor.moove.data.Reminder
import com.backdoor.moove.data.RoomDb
import com.backdoor.moove.modern_ui.ReminderDialogActivity
import com.backdoor.moove.utils.*
import com.backdoor.moove.widgets.LeftDistanceWidgetConfigureActivity
import com.backdoor.moove.widgets.SimpleWidgetConfigureActivity
import org.koin.android.ext.android.inject
import timber.log.Timber

class GeolocationService : Service() {

    private var mTracker: LocationTracker? = null
    private var isNotificationEnabled: Boolean = false
    private var stockRadius: Int = 0

    val prefs: Prefs by inject()
    val db: RoomDb by inject()

    override fun onDestroy() {
        super.onDestroy()
        mTracker?.removeUpdates()
        stopForeground(true)
        Timber.d("onDestroy: ")
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Timber.d("onStartCommand: ")
        isNotificationEnabled = prefs.isDistanceNotificationEnabled
        stockRadius = prefs.radius
        mTracker = LocationTracker(applicationContext) { lat, lng ->
            val locationA = Location("point A")
            locationA.latitude = lat
            locationA.longitude = lng
            checkReminders(locationA)
        }
        showDefaultNotification()
        return Service.START_STICKY
    }

    private fun checkReminders(locationA: Location) {
        launchDefault {
            for (reminder in db.reminderDao().getAll()) {
                checkDistance(locationA, reminder)
            }
        }
    }

    private suspend fun checkDistance(locationA: Location, reminder: Reminder) {
        if (reminder.hasDelay) {
            if (!TimeUtils.isCurrent(reminder.delayTime)) {
                selectBranch(locationA, reminder)
            }
        } else {
            selectBranch(locationA, reminder)
        }
    }

    private suspend fun selectBranch(locationA: Location, reminder: Reminder) {
        if (reminder.isNotificationShown) return
        when {
            reminder.type.startsWith(ReminderUtils.TYPE_LOCATION_OUT) -> checkOut(locationA, reminder)
            else -> checkSimple(locationA, reminder)
        }
    }

    private suspend fun checkSimple(locationA: Location, reminder: Reminder) {
        val locationB = Location("point B")
        locationB.latitude = reminder.latitude
        locationB.longitude = reminder.longitude
        val distance = locationA.distanceTo(locationB)
        val roundedDistance = Math.round(distance)
        if (roundedDistance <= getRadius(reminder.radius)) {
            showReminder(reminder)
        } else {
            showNotification(roundedDistance, reminder)
            updateWidget(reminder.widgetId, roundedDistance)
        }
    }

    private fun getRadius(r: Int): Int {
        var radius = r
        if (radius == -1) radius = stockRadius
        return radius
    }

    private suspend fun checkOut(locationA: Location, reminder: Reminder) {
        val locationB = Location("point B")
        locationB.latitude = reminder.latitude
        locationB.longitude = reminder.longitude
        val distance = locationA.distanceTo(locationB)
        val roundedDistance = Math.round(distance)
        if (reminder.isLocked) {
            if (roundedDistance > getRadius(reminder.radius)) {
                showReminder(reminder)
            } else {
                showNotification(roundedDistance, reminder)
                updateWidget(reminder.widgetId, roundedDistance)
            }
        } else {
            if (roundedDistance < getRadius(reminder.radius)) {
                reminder.isLocked = true
                db.reminderDao().insert(reminder)
            }
            updateWidget(reminder.widgetId, roundedDistance)
        }
    }

    private fun updateWidget(prefsKey: String?, distance: Int) {
        if (prefsKey != null) {
            val context = applicationContext
            LeftDistanceWidgetConfigureActivity.saveDistancePref(context, prefsKey, distance)
            SimpleWidgetConfigureActivity.saveDistancePref(context, prefsKey, distance)

            WidgetUtil.updateWidgets(context)
        }
    }

    private suspend fun showReminder(reminder: Reminder) {
        if (reminder.isNotificationShown) return
        reminder.isNotificationShown = true
        db.reminderDao().insert(reminder)
        withUIContext {
            ReminderDialogActivity.getLaunchIntent(applicationContext, reminder.uuId)
        }
    }

    private fun showNotification(roundedDistance: Int, reminder: Reminder) {
        if (!isNotificationEnabled) return
        val builder = NotificationCompat.Builder(applicationContext, Notifier.CHANNEL_SYSTEM)
        builder.setContentText(roundedDistance.toString())
        builder.setContentTitle(reminder.summary)
        builder.setContentText(roundedDistance.toString())
        builder.priority = NotificationCompat.PRIORITY_LOW
        builder.setSmallIcon(R.drawable.ic_directions_white_24dp)
        startForeground(NOTIFICATION_ID, builder.build())
    }

    private fun showDefaultNotification() {
        if (!isNotificationEnabled) return
        val builder = NotificationCompat.Builder(applicationContext, Notifier.CHANNEL_SYSTEM)
        builder.setContentText(getString(R.string.app_name))

        builder.setContentTitle(getString(R.string.location_tracking_service_running))
        builder.setSmallIcon(R.drawable.ic_directions_white_24dp)
        startForeground(NOTIFICATION_ID, builder.build())
    }

    companion object {
        private const val NOTIFICATION_ID = 1245
    }
}