package com.backdoor.moove.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.backdoor.moove.utils.EnableThread
import org.koin.standalone.KoinComponent
import timber.log.Timber

class BootReceiver : BroadcastReceiver(), KoinComponent {

    override fun onReceive(context: Context, intent: Intent) {
        Timber.d("onReceive: ")
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            EnableThread().run()
        }
    }
}