package com.backdoor.moove.core.helper

import android.app.NotificationManager
import android.content.Context
import android.content.Intent

import com.backdoor.moove.R
import com.backdoor.moove.ReminderManagerActivity
import com.backdoor.moove.core.async.DisableAsync
import com.backdoor.moove.core.consts.Constants
import com.backdoor.moove.core.interfaces.ActionCallbacks
import com.backdoor.moove.core.services.GeolocationService
import com.backdoor.moove.core.services.PositionDelayReceiver
import com.backdoor.moove.core.utils.LocationUtil
import com.backdoor.moove.utils.SuperUtil
import com.backdoor.moove.core.widgets.LeftDistanceWidgetConfigureActivity
import com.backdoor.moove.core.widgets.SimpleWidgetConfigureActivity

/**
 * Helper class for interaction with reminders.
 */
class Reminder(var id: Long, var title: String?, var type: String?, var melody: String?, var uuId: String?,
               var place: DoubleArray?, var number: String?, var radius: Int, var startTime: Long,
               var color: Int, val marker: Int, val volume: Int) {
    var group: String? = null

    companion object {

        /**
         * Toggle reminder status.
         *
         * @param id      reminder identifier.
         * @param context application context.
         * @return boolean
         */
        fun toggle(id: Long, context: Context, callbacks: ActionCallbacks): Boolean {
            val db = DataBase(context)
            db.open()
            val c = db.getReminder(id)
            var startTime: Long = 0
            var status = Constants.ENABLE
            if (c != null && c.moveToFirst()) {
                startTime = c.getLong(c.getColumnIndex(DataBase.START_TIME))
                status = c.getInt(c.getColumnIndex(DataBase.STATUS_DB))
            }
            c?.close()
            if (status == Constants.ENABLE) {
                db.close()
                disableReminder(id, context)
                callbacks.showSnackbar(R.string.reminder_disabled)
                return true
            } else {
                if (!LocationUtil.checkLocationEnable(context)) {
                    db.close()
                    LocationUtil.showLocationAlert(context, callbacks)
                    return false
                } else {
                    db.setStatus(id, Constants.ENABLE)
                    db.setReminderStatus(id, Constants.NOT_SHOWN)
                    db.setStatusNotification(id, Constants.NOT_SHOWN)
                    db.setLocationStatus(id, Constants.NOT_LOCKED)
                    db.close()
                    if (startTime > -1) {
                        PositionDelayReceiver().setAlarm(context, id)
                        callbacks.showSnackbar(R.string.reminder_tracking_start_delayed)
                    } else {
                        if (!SuperUtil.isServiceRunning(context, GeolocationService::class.java)) {
                            context.startService(Intent(context, GeolocationService::class.java)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                        }

                        callbacks.showSnackbar(R.string.tracking_start)
                    }
                    return true
                }
            }
        }

        /**
         * Disable reminder.
         *
         * @param id      reminder identifier.
         * @param context application context.
         */
        fun disableReminder(id: Long, context: Context) {
            val db = DataBase(context)
            if (!db.isOpen) {
                db.open()
            }
            val c = db.getReminder(id)
            if (c != null && c.moveToFirst()) {
                val widgetId = c.getString(c.getColumnIndex(DataBase.WIDGET_ID))
                if (widgetId != null) {
                    LeftDistanceWidgetConfigureActivity.saveDistancePref(context, widgetId, -1)
                    SimpleWidgetConfigureActivity.saveDistancePref(context, widgetId, -1)
                    Widget.updateWidgets(context)
                }
            }
            c?.close()
            db.setStatus(id, Constants.DISABLE)
            db.close()
            disable(context, id)
        }

        /**
         * Disable all available reminder notifications.
         *
         * @param context application context.
         * @param id      reminder identifier.
         */
        private fun disable(context: Context, id: Long) {
            val mNotifyMgr = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val i = id.toInt()
            mNotifyMgr.cancel(i)
            PositionDelayReceiver().cancelAlarm(context, i.toLong())
            DisableAsync(context).execute()
        }

        /**
         * Edit reminder.
         *
         * @param id      reminder identifier.
         * @param context application context.
         */
        fun edit(id: Long, context: Context) {
            disable(context, id)
            val intentId = Intent(context, ReminderManagerActivity::class.java)
            intentId.putExtra(Constants.EDIT_ID, id)
            context.startActivity(intentId)
        }

        /**
         * Delete reminder from application.
         *
         * @param id      reminder identifier.
         * @param context application context.
         */
        fun delete(id: Long, context: Context) {
            val db = DataBase(context)
            if (!db.isOpen) {
                db.open()
            }
            db.deleteReminder(id)
            db.close()
            disable(context, id)
        }

        /**
         * Set widget for reminder.
         *
         * @param reminderId reminder identifier.
         * @param prefs      appWidget preferences key.
         * @param context    application context.
         */
        fun setWidget(context: Context, reminderId: Long, prefs: String) {
            val db = DataBase(context)
            if (!db.isOpen) {
                db.open()
            }
            db.setWidgetId(reminderId, prefs)
            db.close()
        }

        /**
         * Remove widget from reminder.
         *
         * @param prefs   appWidget preferences key.
         * @param context application context.
         */
        fun removeWidget(context: Context, prefs: String) {
            val db = DataBase(context)
            if (!db.isOpen) {
                db.open()
            }
            val c = db.getRemindersWithWidget(prefs)
            if (c != null && c.moveToFirst()) {
                do {
                    val id = c.getLong(c.getColumnIndex(DataBase._ID))
                    db.removeWidget(id)
                } while (c.moveToNext())
            }
            c?.close()
            db.close()
        }
    }
}
