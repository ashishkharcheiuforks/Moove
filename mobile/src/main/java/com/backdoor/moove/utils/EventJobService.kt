package com.backdoor.moove.utils

import com.backdoor.moove.data.Reminder
import com.evernote.android.job.Job
import com.evernote.android.job.JobManager
import com.evernote.android.job.JobRequest
import com.evernote.android.job.util.support.PersistableBundleCompat
import timber.log.Timber

class EventJobService : Job() {

    override fun onRunJob(params: Params): Result {
        val bundle = params.extras
        if (bundle.getBoolean(ARG_LOCATION, false)) {
            SuperUtil.startGpsTracking(context)
        }
        return Result.SUCCESS
    }

    companion object {
        private const val ARG_LOCATION = "arg_location"

        fun enablePositionDelay(reminder: Reminder): Boolean {
            val due = TimeUtils.getDateTimeFromGmt(reminder.delayTime)
            val mills = due - System.currentTimeMillis()
            if (due == 0L || mills <= 0) {
                return false
            }
            Timber.d("enablePositionDelay: $reminder")
            val bundle = PersistableBundleCompat()
            bundle.putBoolean(ARG_LOCATION, true)
            JobRequest.Builder(reminder.uuId)
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
            return JobManager.instance().getAllJobsForTag(id).isNotEmpty()
        }

        fun cancelReminder(tag: String) {
            Timber.i("cancelReminder: $tag")
            JobManager.instance().cancelAllForTag(tag)
        }
    }
}