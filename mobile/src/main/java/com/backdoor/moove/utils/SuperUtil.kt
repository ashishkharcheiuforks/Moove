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
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import com.backdoor.moove.R
import com.backdoor.moove.services.GeolocationService
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import timber.log.Timber
import java.util.*

object SuperUtil {

    fun launchMarket(context: Context) {
        val uri = Uri.parse("market://details?id=" + context.packageName)
        val goToMarket = Intent(Intent.ACTION_VIEW, uri)
        try {
            context.startActivity(goToMarket)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, context.getString(R.string.could_not_launch_market), Toast.LENGTH_SHORT).show()
        }
    }

    fun startGpsTracking(context: Context) {
        if (!Permissions.checkForeground(context) || isServiceRunning(context, GeolocationService::class.java)) {
            return
        }
        Timber.d("startGpsTracking: ")
        val intent = Intent(context, GeolocationService::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startService(intent)
    }

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
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?
        for (service in manager?.getRunningServices(Integer.MAX_VALUE) ?: listOf()) {
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
}
