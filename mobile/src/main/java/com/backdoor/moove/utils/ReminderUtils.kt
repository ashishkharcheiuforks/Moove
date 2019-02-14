package com.backdoor.moove.utils

import android.content.Context
import com.backdoor.moove.R

object ReminderUtils {

    const val TYPE_LOCATION = "location"
    const val TYPE_LOCATION_OUT = "out_location"
    const val TYPE_LOCATION_OUT_CALL = "out_location_call"
    const val TYPE_LOCATION_OUT_MESSAGE = "out_location_message"
    const val TYPE_LOCATION_CALL = "location_call"
    const val TYPE_LOCATION_MESSAGE = "location_message"
    const val TYPE_MESSAGE = "message"
    const val TYPE_CALL = "call"

    fun getTypeString(context: Context, type: String): String {
        val res: String
        res = if (type.matches(TYPE_LOCATION_CALL.toRegex()) || type.matches(TYPE_LOCATION_OUT_CALL.toRegex())) {
            val init = context.getString(R.string.make_call)
            init + " (" + getType(context, type) + ")"
        } else if (type.contains(TYPE_MESSAGE)) {
            val init = context.getString(R.string.send_message)
            init + " (" + getType(context, type) + ")"
        } else {
            val init = context.getString(R.string.reminder)
            init + " (" + getType(context, type) + ")"
        }
        return res
    }

    fun getType(context: Context, type: String): String {
        val res: String
        res = if (type.startsWith(TYPE_LOCATION)) {
            context.getString(R.string.location)
        } else {
            context.getString(R.string.place_out)
        }
        return res
    }
}
