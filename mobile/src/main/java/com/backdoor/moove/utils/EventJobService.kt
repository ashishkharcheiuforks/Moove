package com.backdoor.moove.utils

import android.content.Context
import com.backdoor.moove.data.RoomDb
import com.evernote.android.job.Job
import com.evernote.android.job.JobManager
import com.evernote.android.job.JobRequest
import com.evernote.android.job.util.support.PersistableBundleCompat
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import timber.log.Timber

class EventJobService : Job(), KoinComponent {

    val prefs: Prefs by inject()

    override fun onRunJob(params: Job.Params): Job.Result {
        val bundle = params.extras
        if (bundle.getBoolean(ARG_LOCATION, false)) {
            SuperUtil.startGpsTracking(context)
        }
        return Job.Result.SUCCESS
    }

    companion object {
        private const val ARG_LOCATION = "arg_location"

        fun enableDelay(time: Int, id: String) {
            val min = TimeCount.MINUTE
            val millis = min * time
            enableDelay(millis, id)
        }

        fun enableDelay(millis: Long, id: String) {
            if (millis <= 0) {
                return
            }
            JobRequest.Builder(id)
                    .setExact(millis)
                    .setRequiresCharging(false)
                    .setRequiresDeviceIdle(false)
                    .setRequiresBatteryNotLow(false)
                    .setRequiresStorageNotLow(false)
                    .setUpdateCurrent(true)
                    .build()
                    .schedule()
        }

        fun enablePositionDelay(context: Context, id: String): Boolean {
            val item = RoomDb.getInMemoryDatabase(context).reminderDao().getById(id) ?: return false
            val due = TimeUtils.getDateTimeFromGmt(item.delayTime)
            val mills = due - System.currentTimeMillis()
            if (due == 0L || mills <= 0) {
                return false
            }
            val bundle = PersistableBundleCompat()
            bundle.putBoolean(ARG_LOCATION, true)
            JobRequest.Builder(item.uuId)
                    .setExact(mills)
                    .setRequiresCharging(false)
                    .setRequiresDeviceIdle(false)
                    .setRequiresBatteryNotLow(false)
                    .setRequiresStorageNotLow(false)
                    .setUpdateCurrent(true)
                    .setExtras(bundle)
                    .build()
                    .schedule()
            return true
        }

        fun isEnabledReminder(id: String): Boolean {
            return !JobManager.instance().getAllJobsForTag(id).isEmpty()
        }

        fun cancelReminder(tag: String) {
            Timber.i("cancelReminder: $tag")
            JobManager.instance().cancelAllForTag(tag)
        }
    }
}