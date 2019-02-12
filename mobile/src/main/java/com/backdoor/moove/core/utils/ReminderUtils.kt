package com.backdoor.moove.core.utils

import android.content.Context

import com.backdoor.moove.R
import com.backdoor.moove.core.consts.Constants

class ReminderUtils {
    companion object {

        /**
         * Generate human readable string for reminder type.
         *
         * @param context application context.
         * @param type    reminder type.
         * @return reminder type.
         */
        fun getTypeString(context: Context, type: String): String {
            val res: String
            if (type.matches(Constants.TYPE_LOCATION_CALL.toRegex()) || type.matches(Constants.TYPE_LOCATION_OUT_CALL.toRegex())) {
                val init = context.getString(R.string.make_call)
                res = init + " (" + getType(context, type) + ")"
            } else if (type.contains(Constants.TYPE_MESSAGE)) {
                val init = context.getString(R.string.send_message)
                res = init + " (" + getType(context, type) + ")"
            } else {
                val init = context.getString(R.string.reminder)
                res = init + " (" + getType(context, type) + ")"
            }
            return res
        }

        /**
         * Get human readable string for reminder type.
         *
         * @param context application context.
         * @param type    reminder type.
         * @return reminder type.
         */
        fun getType(context: Context, type: String): String {
            val res: String
            if (type.startsWith(Constants.TYPE_LOCATION)) {
                res = context.getString(R.string.location)
            } else {
                res = context.getString(R.string.place_out)
            }
            return res
        }
    }
}
