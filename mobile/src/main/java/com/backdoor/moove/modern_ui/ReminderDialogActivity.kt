package com.backdoor.moove.modern_ui

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity

class ReminderDialogActivity : AppCompatActivity() {

    companion object {

        private const val ITEM_ID = "arg_id"

        fun getLaunchIntent(context: Context, uuid: String) {
            val resultIntent = Intent(context, ReminderDialogActivity::class.java)
            resultIntent.putExtra(ITEM_ID, uuid)
            resultIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
            context.startActivity(resultIntent)
        }
    }
}