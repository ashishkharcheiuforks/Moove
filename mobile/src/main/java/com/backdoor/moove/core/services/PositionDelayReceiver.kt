package com.backdoor.moove.core.services

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.database.Cursor

import com.backdoor.moove.core.helper.DataBase
import com.backdoor.moove.core.utils.SuperUtil

import java.util.Calendar

class PositionDelayReceiver : BroadcastReceiver() {

    private var alarmMgr: AlarmManager? = null
    private var alarmIntent: PendingIntent? = null

    override fun onReceive(context: Context, intent: Intent) {
        if (!SuperUtil.isServiceRunning(context, GeolocationService::class.java)) {
            context.startService(Intent(context, GeolocationService::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }

    fun setAlarm(context: Context, id: Long) {
        val db = DataBase(context)
        db.open()
        val c = db.getReminder(id)

        val i = id.toInt()
        var startTime: Long = 0
        if (c != null && c.moveToNext()) {
            startTime = c.getLong(c.getColumnIndex(DataBase.START_TIME))
        }
        c?.close()
        val intent = Intent(context, PositionDelayReceiver::class.java)
        alarmIntent = PendingIntent.getBroadcast(context, i, intent, 0)
        alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = startTime
        alarmMgr!!.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, alarmIntent)
        db.close()
    }

    fun cancelAlarm(context: Context, id: Long) {
        val i = id.toInt()
        val intent = Intent(context, PositionDelayReceiver::class.java)
        alarmIntent = PendingIntent.getBroadcast(context, i, intent, 0)
        alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (alarmMgr != null) {
            alarmMgr!!.cancel(alarmIntent)
        }
    }
}