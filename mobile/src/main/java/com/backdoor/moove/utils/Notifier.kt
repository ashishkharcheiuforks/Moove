package com.backdoor.moove.utils

import android.annotation.TargetApi
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.backdoor.moove.R

object Notifier {

    val CHANNEL_REMINDER = "moove.channel1"
    val CHANNEL_SYSTEM = "moove.channel2"

    fun hideNotification(context: Context, id: Int) {
        getManager(context)?.cancel(id)
    }

    private fun getManager(context: Context): NotificationManager? {
        return context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
    }

    fun createChannels(context: Context) {
        if (Module.isOreo) {
            getManager(context)?.createNotificationChannel(createReminderChannel(context))
            getManager(context)?.createNotificationChannel(createSystemChannel(context))
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun createSystemChannel(context: Context): NotificationChannel {
        val name = context.getString(R.string.info_channel)
        val descr = context.getString(R.string.channel_for_other_info_notifications)
        val importance = NotificationManager.IMPORTANCE_LOW
        val mChannel = NotificationChannel(CHANNEL_SYSTEM, name, importance)
        mChannel.description = descr
        return mChannel
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun createReminderChannel(context: Context): NotificationChannel {
        val name = context.getString(R.string.reminder_channel)
        val descr = context.getString(R.string.default_reminder_notifications)
        val importance = NotificationManager.IMPORTANCE_HIGH
        val mChannel = NotificationChannel(CHANNEL_REMINDER, name, importance)
        mChannel.description = descr
        return mChannel
    }
}
