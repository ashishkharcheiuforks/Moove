package com.backdoor.moove.core.helper

import android.annotation.TargetApi
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.res.AssetFileDescriptor
import android.media.AudioManager
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

import com.backdoor.moove.R
import com.backdoor.moove.ReminderDialogActivity
import com.backdoor.moove.core.consts.Prefs
import com.backdoor.moove.core.utils.ViewUtils

import java.io.File
import java.io.IOException

/**
 * Helper class for status bar notifications.
 */
class Notifier(private val mContext: Context) {
    private var mNotifyMgr: NotificationManagerCompat? = null
    private var builder: NotificationCompat.Builder? = null
    private val sPrefs: SharedPrefs?
    private val sound: Sound

    init {
        this.sPrefs = SharedPrefs.getInstance(mContext)
        sound = Sound(mContext)
    }

    /**
     * Status bar notification to use when enabled tts.
     *
     * @param task   task string.
     * @param itemId reminder identifier.
     * @param color  LED lights color.
     */
    fun showTTSNotification(task: String, itemId: Long, color: Int) {
        builder = NotificationCompat.Builder(mContext, CHANNEL_REMINDER)
        builder!!.setContentTitle(task)
        val notificationIntent = Intent(mContext, ReminderDialogActivity::class.java)
        notificationIntent.flags = (Intent.FLAG_ACTIVITY_CLEAR_TOP
                or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        val intent = PendingIntent.getActivity(mContext, itemId.toInt(), notificationIntent, 0)
        builder!!.setContentIntent(intent)
        builder!!.setAutoCancel(false)
        builder!!.priority = NotificationCompat.PRIORITY_MAX
        builder!!.setOngoing(true)
        val app = mContext.getString(R.string.app_name)
        builder!!.setContentText(app)
        builder!!.setSmallIcon(ViewUtils.icon)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder!!.color = ViewUtils.getColor(mContext, R.color.bluePrimary)
        }

        val am = mContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (am.ringerMode == AudioManager.RINGER_MODE_NORMAL) {
            try {
                val afd = mContext.assets.openFd("sounds/beep.mp3")
                sound.playAlarm(afd, false)
            } catch (e: IOException) {
                e.printStackTrace()
                sound.playAlarm(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), false)
            }

        } else {
            if (sPrefs!!.loadBoolean(Prefs.SILENT_SOUND)) {
                try {
                    val afd = mContext.assets.openFd("sounds/beep.mp3")
                    sound.playAlarm(afd, false)
                } catch (e: IOException) {
                    e.printStackTrace()
                    sound.playAlarm(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), false)
                }

            }
        }

        val isV = sPrefs!!.loadBoolean(Prefs.VIBRATION_STATUS)
        if (isV) {
            val pattern: LongArray
            if (sPrefs.loadBoolean(Prefs.INFINITE_VIBRATION)) {
                pattern = longArrayOf(150, 86400000)
            } else {
                pattern = longArrayOf(150, 400, 100, 450, 200, 500, 300, 500)
            }
            builder!!.setVibrate(pattern)
        }

        if (sPrefs.loadBoolean(Prefs.LED_STATUS)) {
            if (color != 0) {
                builder!!.setLights(color, 500, 1000)
            } else {
                builder!!.setLights(sPrefs.loadInt(Prefs.LED_COLOR), 500, 1000)
            }
        }

        val isWear = sPrefs.loadBoolean(Prefs.WEAR_NOTIFICATION)
        if (isWear) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                builder!!.setOnlyAlertOnce(true)
                builder!!.setGroup("GROUP")
                builder!!.setGroupSummary(true)
            }
        }

        mNotifyMgr = NotificationManagerCompat.from(mContext)
        val it = itemId.toInt()
        mNotifyMgr!!.notify(it, builder!!.build())

        if (isWear) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                val wearableNotificationBuilder = NotificationCompat.Builder(mContext, CHANNEL_REMINDER)
                wearableNotificationBuilder.setSmallIcon(R.mipmap.ic_launcher)
                wearableNotificationBuilder.setContentTitle(task)
                wearableNotificationBuilder.setContentText(app)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    wearableNotificationBuilder.color = ViewUtils.getColor(mContext, R.color.bluePrimary)
                }
                wearableNotificationBuilder.setOngoing(false)
                wearableNotificationBuilder.setOnlyAlertOnce(true)
                wearableNotificationBuilder.setGroup("GROUP")
                wearableNotificationBuilder.setGroupSummary(false)
                mNotifyMgr!!.notify(10100, wearableNotificationBuilder.build())
            }
        }
    }

    /**
     * Standard status bar notification for reminder.
     *
     * @param task   reminder task.
     * @param i      flag for enabling sounds (1 - enabled).
     * @param itemId reminder identifier.
     * @param melody reminder custom melody file.
     * @param color  LED lights color.
     */
    fun showReminder(task: String, i: Int, itemId: Long, melody: String?, color: Int) {
        val soundUri: Uri
        if (melody != null && !melody.matches("".toRegex())) {
            val sound = File(melody)
            soundUri = Uri.fromFile(sound)
        } else {
            if (sPrefs!!.loadBoolean(Prefs.CUSTOM_SOUND)) {
                val path = sPrefs.loadPrefs(Prefs.CUSTOM_SOUND_FILE)
                if (path != null) {
                    val sound = File(path)
                    soundUri = Uri.fromFile(sound)
                } else {
                    soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                }
            } else {
                soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            }
        }

        val notificationIntent = Intent(mContext, ReminderDialogActivity::class.java)
        notificationIntent.putExtra("int", 1)
        notificationIntent.flags = (Intent.FLAG_ACTIVITY_CLEAR_TOP
                or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        val intent = PendingIntent.getActivity(mContext, itemId.toInt(), notificationIntent, 0)

        builder = NotificationCompat.Builder(mContext, CHANNEL_REMINDER)
        builder!!.setContentTitle(task)
        builder!!.setContentIntent(intent)
        builder!!.setAutoCancel(false)
        builder!!.priority = NotificationCompat.PRIORITY_MAX
        builder!!.setOngoing(true)
        val app = mContext.getString(R.string.app_name)
        builder!!.setContentText(app)
        builder!!.setSmallIcon(ViewUtils.icon)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder!!.color = ViewUtils.getColor(mContext, R.color.bluePrimary)
        }

        if (i == 1) {
            val am = mContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            if (am.ringerMode == AudioManager.RINGER_MODE_NORMAL) {
                sound.playAlarm(soundUri, sPrefs!!.loadBoolean(Prefs.INFINITE_SOUND))
            } else {
                if (sPrefs!!.loadBoolean(Prefs.SILENT_SOUND)) {
                    sound.playAlarm(soundUri, sPrefs.loadBoolean(Prefs.INFINITE_SOUND))
                }
            }
        }

        val isV = sPrefs!!.loadBoolean(Prefs.VIBRATION_STATUS)
        if (isV) {
            val pattern: LongArray
            if (sPrefs.loadBoolean(Prefs.INFINITE_VIBRATION)) {
                pattern = longArrayOf(150, 86400000)
            } else {
                pattern = longArrayOf(150, 400, 100, 450, 200, 500, 300, 500)
            }
            builder!!.setVibrate(pattern)
        }
        if (sPrefs.loadBoolean(Prefs.LED_STATUS)) {
            if (color != 0) {
                builder!!.setLights(color, 500, 1000)
            } else {
                builder!!.setLights(sPrefs.loadInt(Prefs.LED_COLOR), 500, 1000)
            }
        }

        val isWear = sPrefs.loadBoolean(Prefs.WEAR_NOTIFICATION)
        if (isWear) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                builder!!.setOnlyAlertOnce(true)
                builder!!.setGroup("GROUP")
                builder!!.setGroupSummary(true)
            }
        }

        mNotifyMgr = NotificationManagerCompat.from(mContext)
        val it = itemId.toInt()
        mNotifyMgr!!.notify(it, builder!!.build())

        if (isWear) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                val wearableNotificationBuilder = NotificationCompat.Builder(mContext, CHANNEL_REMINDER)
                wearableNotificationBuilder.setSmallIcon(R.mipmap.ic_launcher)
                wearableNotificationBuilder.setContentTitle(task)
                wearableNotificationBuilder.setContentText(app)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    wearableNotificationBuilder.color = ViewUtils.getColor(mContext, R.color.bluePrimary)
                }
                wearableNotificationBuilder.setOngoing(false)
                wearableNotificationBuilder.setOnlyAlertOnce(true)
                wearableNotificationBuilder.setGroup("GROUP")
                wearableNotificationBuilder.setGroupSummary(false)
                mNotifyMgr!!.notify(10100, wearableNotificationBuilder.build())
            }
        }
    }

    /**
     * Simple status bar notification for reminders.
     *
     * @param content notification title.
     * @param id      reminder identifier.
     */
    fun showReminderNotification(content: String, id: Long) {
        builder = NotificationCompat.Builder(mContext, CHANNEL_REMINDER)
        builder!!.setContentTitle(content)
        val app = mContext.getString(R.string.app_name)
        builder!!.setContentText(app)
        builder!!.setSmallIcon(R.drawable.ic_notifications_white_24dp)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder!!.color = ViewUtils.getColor(mContext, R.color.bluePrimary)
        }

        val isWear = sPrefs!!.loadBoolean(Prefs.WEAR_NOTIFICATION)
        if (isWear) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                builder!!.setOnlyAlertOnce(true)
                builder!!.setGroup("GROUP")
                builder!!.setGroupSummary(true)
            }
        }

        mNotifyMgr = NotificationManagerCompat.from(mContext)
        val it = id.toInt()
        mNotifyMgr!!.notify(it, builder!!.build())

        if (isWear) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                val wearableNotificationBuilder = NotificationCompat.Builder(mContext, CHANNEL_REMINDER)
                wearableNotificationBuilder.setSmallIcon(R.mipmap.ic_launcher)
                wearableNotificationBuilder.setContentTitle(content)
                wearableNotificationBuilder.setContentText(app)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    wearableNotificationBuilder.color = ViewUtils.getColor(mContext, R.color.bluePrimary)
                }
                wearableNotificationBuilder.setOngoing(false)
                wearableNotificationBuilder.setOnlyAlertOnce(true)
                wearableNotificationBuilder.setGroup("GROUP")
                wearableNotificationBuilder.setGroupSummary(false)
                mNotifyMgr!!.notify(it + 10, wearableNotificationBuilder.build())
            }
        }
    }

    fun discardNotification(id: Long) {
        discardMedia()
        val i = id.toInt()
        mNotifyMgr = NotificationManagerCompat.from(mContext)
        mNotifyMgr!!.cancel(i)
    }

    /**
     * Stops playing notification sound.
     */
    fun discardMedia() {
        sound.stop()
    }

    companion object {

        val CHANNEL_REMINDER = "moove.channel1"
        val CHANNEL_SYSTEM = "moove.channel2"

        fun createChannels(context: Context) {
            if (Module.isO) {
                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        ?: return
                manager.createNotificationChannel(createReminderChannel(context))
                manager.createNotificationChannel(createSystemChannel(context))
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
}
