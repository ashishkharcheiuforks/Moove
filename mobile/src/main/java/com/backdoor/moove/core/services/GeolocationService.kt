package com.backdoor.moove.core.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.location.Location
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

import com.backdoor.moove.R
import com.backdoor.moove.ReminderDialogActivity
import com.backdoor.moove.core.consts.Constants
import com.backdoor.moove.core.consts.Prefs
import com.backdoor.moove.core.helper.DataBase
import com.backdoor.moove.core.helper.Module
import com.backdoor.moove.core.helper.Notifier
import com.backdoor.moove.core.helper.SharedPrefs
import com.backdoor.moove.core.helper.Widget
import com.backdoor.moove.core.location.LocationTracker
import com.backdoor.moove.core.utils.TimeUtil
import com.backdoor.moove.core.utils.ViewUtils
import com.backdoor.moove.core.widgets.LeftDistanceWidgetConfigureActivity
import com.backdoor.moove.core.widgets.SimpleWidgetConfigureActivity

/**
 * Copyright 2016 Nazar Suhovich
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
class GeolocationService : Service() {

    private var mTracker: LocationTracker? = null
    private var isNotificationEnabled: Boolean = false
    private var stockRadius: Int = 0
    private var isWear: Boolean = false

    private val mLocationCallback = { lat, lon ->
        val locationA = Location("point A")
        locationA.latitude = lat
        locationA.longitude = lon
        checkReminders(locationA)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mTracker != null) mTracker!!.removeUpdates()
        stopForeground(true)
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val prefs = SharedPrefs.getInstance(this)
        isWear = prefs!!.loadBoolean(Prefs.WEAR_NOTIFICATION)
        isNotificationEnabled = prefs.loadBoolean(Prefs.TRACKING_NOTIFICATION)
        stockRadius = prefs.loadInt(Prefs.LOCATION_RADIUS)
        mTracker = LocationTracker(applicationContext, mLocationCallback)
        showDefaultNotification()
        return Service.START_STICKY
    }

    private fun checkReminders(locationA: Location) {
        val db = DataBase(applicationContext)
        db.open()
        val c = db.getReminders(Constants.ENABLE)
        if (c != null && c.moveToFirst()) {
            do {
                val lat = c.getDouble(c.getColumnIndex(DataBase.LATITUDE))
                val lon = c.getDouble(c.getColumnIndex(DataBase.LONGITUDE))
                val id = c.getLong(c.getColumnIndex(DataBase._ID))
                val startTime = c.getLong(c.getColumnIndex(DataBase.START_TIME))
                val task = c.getString(c.getColumnIndex(DataBase.SUMMARY))
                val type = c.getString(c.getColumnIndex(DataBase.TYPE))
                val status = c.getInt(c.getColumnIndex(DataBase.STATUS))
                val statusNot = c.getInt(c.getColumnIndex(DataBase.STATUS_NOTIFICATION))
                val statusRem = c.getInt(c.getColumnIndex(DataBase.STATUS_REMINDER))
                var radius = c.getInt(c.getColumnIndex(DataBase.RADIUS))
                val widgetId = c.getString(c.getColumnIndex(DataBase.WIDGET_ID))
                if (radius == -1) {
                    radius = stockRadius
                }
                if (startTime <= 0) {
                    val locationB = Location("point B")
                    locationB.latitude = lat
                    locationB.longitude = lon
                    val distance = locationA.distanceTo(locationB)
                    val roundedDistance = Math.round(distance)
                    if (type.startsWith(Constants.TYPE_LOCATION_OUT)) {
                        if (status == Constants.NOT_LOCKED) {
                            if (roundedDistance < radius) {
                                db.setLocationStatus(id, Constants.LOCKED)
                            }
                        } else {
                            if (roundedDistance > radius) {
                                if (statusRem != Constants.SHOWN) {
                                    showReminder(id, task)
                                }
                            } else {
                                if (isNotificationEnabled) {
                                    showNotification(id, roundedDistance, statusNot, task, isWear)
                                }
                                updateWidget(widgetId, roundedDistance)
                            }
                        }
                    } else {
                        if (roundedDistance <= radius) {
                            if (statusRem != Constants.SHOWN) {
                                showReminder(id, task)
                            }
                        } else {
                            if (isNotificationEnabled) {
                                showNotification(id, roundedDistance, statusNot, task, isWear)
                            }
                            updateWidget(widgetId, roundedDistance)
                        }
                    }
                } else {
                    if (TimeUtil.isCurrent(startTime)) {
                        val locationB = Location("point B")
                        locationB.latitude = lat
                        locationB.longitude = lon
                        val distance = locationA.distanceTo(locationB)
                        val roundedDistance = Math.round(distance)
                        if (type.startsWith(Constants.TYPE_LOCATION_OUT)) {
                            if (status == Constants.NOT_LOCKED) {
                                if (roundedDistance <= radius) {
                                    db.setLocationStatus(id, Constants.LOCKED)
                                }
                            } else {
                                if (roundedDistance > radius) {
                                    if (statusRem != Constants.SHOWN) {
                                        showReminder(id, task)
                                    }
                                } else {
                                    if (isNotificationEnabled) {
                                        showNotification(id, roundedDistance, statusNot, task, isWear)
                                    }
                                    updateWidget(widgetId, roundedDistance)
                                }
                            }
                        } else {
                            if (roundedDistance <= radius) {
                                if (statusRem != Constants.SHOWN) {
                                    showReminder(id, task)
                                }
                            } else {
                                if (isNotificationEnabled) {
                                    showNotification(id, roundedDistance, statusNot, task, isWear)
                                }
                                updateWidget(widgetId, roundedDistance)
                            }
                        }
                    }
                }
            } while (c.moveToNext())
        }
        c?.close()
        db.close()
    }

    private fun updateWidget(prefsKey: String?, distance: Int) {
        if (prefsKey != null) {
            val context = applicationContext
            LeftDistanceWidgetConfigureActivity.saveDistancePref(context, prefsKey, distance)
            SimpleWidgetConfigureActivity.saveDistancePref(context, prefsKey, distance)

            Widget.updateWidgets(context)
        }
    }

    private fun showReminder(id: Long, task: String) {
        val db = DataBase(applicationContext)
        db.open().setReminderStatus(id, Constants.SHOWN)
        db.close()
        val resultIntent = Intent(applicationContext, ReminderDialogActivity::class.java)
        resultIntent.putExtra("taskDialog", task)
        resultIntent.putExtra(Constants.ITEM_ID_INTENT, id)
        resultIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
        startActivity(resultIntent)
    }

    private fun showNotification(id: Long, roundedDistance: Int, shown: Int, task: String, isWear: Boolean) {
        val i = id.toInt()
        val context = applicationContext
        val content = roundedDistance.toString()

        val builder = NotificationCompat.Builder(context, Notifier.CHANNEL_SYSTEM)
        builder.setContentText(content)
        builder.setContentTitle(task)
        builder.setSmallIcon(R.drawable.ic_navigation_white_24dp)
        builder.priority = NotificationCompat.PRIORITY_DEFAULT

        if (Module.isLollipop) {
            builder.color = ViewUtils.getColor(context, R.color.themePrimaryDark)
        }

        if (isWear) {
            if (Module.isJellyBean) {
                builder.setOnlyAlertOnce(true)
                builder.setGroup("LOCATION")
                builder.setGroupSummary(true)
            }
        }

        if (shown != Constants.SHOWN) {
            val db = DataBase(context)
            db.open().setStatusNotification(id, Constants.SHOWN)
            db.close()
        }
        val mNotifyMgr = NotificationManagerCompat.from(context)
        mNotifyMgr.notify(i, builder.build())

        if (isWear) {
            if (Module.isJellyBean) {
                val wearableNotificationBuilder = NotificationCompat.Builder(context, Notifier.CHANNEL_SYSTEM)
                wearableNotificationBuilder.setSmallIcon(R.drawable.ic_navigation_white_24dp)
                wearableNotificationBuilder.setContentTitle(task)
                wearableNotificationBuilder.setContentText(content)
                wearableNotificationBuilder.setOngoing(false)
                if (Module.isLollipop) {
                    wearableNotificationBuilder.color = ViewUtils.getColor(context, R.color.themePrimaryDark)
                }
                wearableNotificationBuilder.setOnlyAlertOnce(true)
                wearableNotificationBuilder.setGroup("LOCATION")
                wearableNotificationBuilder.setGroupSummary(false)
                mNotifyMgr.notify(i, wearableNotificationBuilder.build())
            }
        }
    }

    private fun showDefaultNotification() {
        if (!isNotificationEnabled) return
        val builder = NotificationCompat.Builder(applicationContext, Notifier.CHANNEL_SYSTEM)
        builder.setContentText(getString(R.string.app_name))

        builder.setContentTitle(getString(R.string.location_tracking_service_running))
        builder.setSmallIcon(R.drawable.ic_navigation_white_24dp)
        startForeground(NOTIFICATION_ID, builder.build())
    }

    companion object {

        private val TAG = "GeolocationService"
        val NOTIFICATION_ID = 1245
    }
}
