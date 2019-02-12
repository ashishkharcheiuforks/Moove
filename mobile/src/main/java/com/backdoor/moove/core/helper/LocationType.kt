package com.backdoor.moove.core.helper

import android.content.Context
import android.content.Intent

import com.backdoor.moove.core.services.GeolocationService
import com.backdoor.moove.core.services.PositionDelayReceiver
import com.backdoor.moove.core.utils.SuperUtil

class LocationType(private val mContext: Context, type: String) : Type(mContext) {

    init {
        type = type
    }

    override fun save(item: Reminder): Long {
        val id = super.save(item)
        startTracking(id, item)
        return id
    }

    override fun save(id: Long, item: Reminder) {
        super.save(id, item)
        startTracking(id, item)
    }

    private fun startTracking(id: Long, item: Reminder) {
        if (item.startTime != -1) {
            PositionDelayReceiver().setAlarm(mContext, id)
        } else {
            if (!SuperUtil.isServiceRunning(mContext, GeolocationService::class.java)) {
                mContext.startService(Intent(mContext, GeolocationService::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }
        }
    }
}
