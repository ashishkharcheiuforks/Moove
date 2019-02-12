package com.backdoor.moove.core.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.database.Cursor

import com.backdoor.moove.core.async.DisableAsync
import com.backdoor.moove.core.consts.Constants
import com.backdoor.moove.core.helper.DataBase

class JustBootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Thread {
            val db = DataBase(context)
            db.open()
            val c = db.getReminders(Constants.ENABLE)
            if (c != null && c.moveToFirst()) {
                do {
                    val id = c.getLong(c.getColumnIndex(DataBase._ID))
                    val startTime = c.getLong(c.getColumnIndex(DataBase.START_TIME))
                    if (startTime > 0) {
                        PositionDelayReceiver().setAlarm(context, id)
                    }
                } while (c.moveToNext())
            }
            c?.close()
            db.close()
        }.start()

        DisableAsync(context).execute()
    }
}
