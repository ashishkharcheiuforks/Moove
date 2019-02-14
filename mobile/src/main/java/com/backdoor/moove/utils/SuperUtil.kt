package com.backdoor.moove.utils

import android.app.*
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.media.AudioManager
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import android.speech.RecognizerIntent
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import com.backdoor.moove.R
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import timber.log.Timber
import java.util.*

/**
 * Copyright 2015 Nazar Suhovich
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
object SuperUtil {

    fun isGooglePlayServicesAvailable(a: Context): Boolean {
        val resultCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(a)
        return resultCode == ConnectionResult.SUCCESS
    }

    fun checkGooglePlayServicesAvailability(a: Activity): Boolean {
        val googleAPI = GoogleApiAvailability.getInstance()
        val result = googleAPI.isGooglePlayServicesAvailable(a)
        Timber.d("checkGooglePlayServicesAvailability: $result")
        return if (result != ConnectionResult.SUCCESS) {
            if (googleAPI.isUserResolvableError(result)) {
                googleAPI.getErrorDialog(a, result, 69).show()
            }
            false
        } else {
            true
        }
    }

    fun isDoNotDisturbEnabled(context: Context): Boolean {
        if (!Module.isMarshmallow) {
            return false
        }
        val mNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return if (mNotificationManager.currentInterruptionFilter == NotificationManager.INTERRUPTION_FILTER_ALARMS || mNotificationManager.currentInterruptionFilter == NotificationManager.INTERRUPTION_FILTER_NONE) {
            Timber.d("isDoNotDisturbEnabled: true")
            true
        } else {
            Timber.d("isDoNotDisturbEnabled: false")
            false
        }
    }

    fun checkNotificationPermission(activity: Context): Boolean {
        val notificationManager = activity.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return !(Module.isMarshmallow && !notificationManager.isNotificationPolicyAccessGranted)
    }

    fun askNotificationPermission(activity: Activity) {
        if (Module.isMarshmallow) {
            val builder = AlertDialog.Builder(activity, R.style.HomeDarkDialog)
            builder.setMessage(R.string.for_correct_work_of_application)
            builder.setPositiveButton(R.string.grant) { dialog, _ ->
                dialog.dismiss()
                val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                try {
                    activity.startActivity(intent)
                } catch (ignored: ActivityNotFoundException) {
                }
            }
            builder.setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
            builder.create().show()
        }
    }

    fun checkLocationEnable(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGPSEnabled = locationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetworkEnabled = locationManager
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        return !(!isGPSEnabled && !isNetworkEnabled)
    }

    fun wakeDevice(activity: Activity, id: String = UUID.randomUUID().toString()): PowerManager.WakeLock {
        val screenLock = (activity.getSystemService(Context.POWER_SERVICE) as PowerManager)
                .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "reminder:ReminderAPPTAG:$id")
        screenLock.acquire(10*60*1000L /*10 minutes*/)
        return screenLock
    }

    @Suppress("DEPRECATION")
    fun unlockOff(activity: Activity, window: Window) {
        Timber.d("unlockOff: ")
        if (Module.isOreoMr1) {
            activity.setShowWhenLocked(false)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)
        }
    }

    @Suppress("DEPRECATION")
    fun unlockOn(activity: Activity, window: Window) {
        Timber.d("unlockOn: ")
        if (Module.isOreo) {
            val keyguardManager = activity.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager?
            keyguardManager?.requestDismissKeyguard(activity, null)
        } else {
            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)
        }
    }

    @Suppress("DEPRECATION")
    fun turnScreenOff(activity: Activity, window: Window, wakeLock: PowerManager.WakeLock? = null) {
        Timber.d("turnScreenOff: ")
        if (wakeLock?.isHeld == true) {
            wakeLock.release()
        }
        if (Module.isOreoMr1) {
            activity.setShowWhenLocked(false)
            activity.setTurnScreenOn(false)
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    or WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                    or WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON)
        }
        unlockOff(activity, window)
    }

    @Suppress("DEPRECATION")
    fun turnScreenOn(activity: Activity, window: Window) {
        Timber.d("turnScreenOn: ")
        if (Module.isOreoMr1) {
            activity.setTurnScreenOn(true)
            activity.setShowWhenLocked(true)
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    or WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON)
        } else {
            window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                    or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    or WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON)
        }
        unlockOn(activity, window)
    }

    fun hasVolumePermission(context: Context): Boolean {
        if (Module.isNougat) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
            return notificationManager != null && notificationManager.isNotificationPolicyAccessGranted
        }
        return true
    }

    fun stopService(context: Context, clazz: Class<*>) {
        context.stopService(Intent(context, clazz))
    }

    fun isHeadsetUsing(context: Context): Boolean {
        val manager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager?
        return manager != null && (manager.isBluetoothA2dpOn || manager.isWiredHeadsetOn)
    }

    fun isServiceRunning(context: Context, serviceClass: Class<*>): Boolean {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    fun showMore(context: Context) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse("market://search?q=pub:Nazar Suhovich")
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, R.string.could_not_launch_market, Toast.LENGTH_LONG).show()
        }

    }

    /**
     * Start voice listener for recognition.
     *
     * @param activity    activity.
     * @param requestCode result request code.
     */
    fun startVoiceRecognitionActivity(activity: Activity, requestCode: Int) {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, activity.getString(R.string.say_something))
        try {
            activity.startActivityForResult(intent, requestCode)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(activity, activity.getString(R.string.no_recognizer_found), Toast.LENGTH_SHORT).show()
        }

    }
}
