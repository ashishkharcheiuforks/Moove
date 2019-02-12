package com.backdoor.moove.core.async

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.os.AsyncTask

import com.backdoor.moove.core.consts.Constants
import com.backdoor.moove.core.helper.DataBase
import com.backdoor.moove.core.services.GeolocationService
import com.backdoor.moove.core.utils.TimeUtil

class DisableAsync(private val mContext: Context) : AsyncTask<Void, Void, Void>() {

    override fun doInBackground(vararg params: Void): Void? {
        val db = DataBase(mContext)
        db.open()
        val c = db.getReminders(Constants.ENABLE)
        if (c != null && c.moveToFirst()) {
            var i = 0
            do {
                val startTime = c.getInt(c.getColumnIndex(DataBase.START_TIME)).toLong()
                val isShown = c.getInt(c.getColumnIndex(DataBase.STATUS_REMINDER))
                if (startTime == -1) {
                    if (isShown != Constants.SHOWN) {
                        i++
                    }
                } else {
                    if (TimeUtil.isCurrent(startTime)) {
                        if (isShown != Constants.SHOWN) {
                            i++
                        }
                    }
                }
            } while (c.moveToNext())
            if (i == 0) {
                mContext.stopService(Intent(mContext, GeolocationService::class.java))
            }
        } else {
            mContext.stopService(Intent(mContext, GeolocationService::class.java))
        }
        c?.close()
        db.close()
        return null
    }
}
