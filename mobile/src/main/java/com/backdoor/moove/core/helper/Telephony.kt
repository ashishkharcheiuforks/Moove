package com.backdoor.moove.core.helper

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri

import java.io.File

/**
 * Helper class to interact with calls, messages and emails.
 */
class Telephony {
    companion object {

        /**
         * Start calling to contact.
         *
         * @param number  number to call.
         * @param context application context.
         */
        @SuppressLint("MissingPermission")
        fun makeCall(number: String, context: Context) {
            val callIntent = Intent(Intent.ACTION_CALL)
            callIntent.data = Uri.parse("tel:$number")
            context.startActivity(callIntent)
        }
    }
}
