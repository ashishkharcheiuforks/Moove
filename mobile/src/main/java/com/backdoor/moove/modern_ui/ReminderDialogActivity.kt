package com.backdoor.moove.modern_ui

import android.app.Activity
import android.app.PendingIntent
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.os.PowerManager
import android.speech.tts.TextToSpeech
import android.text.TextUtils
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.backdoor.moove.R
import com.backdoor.moove.data.Reminder
import com.backdoor.moove.databinding.ActivityReminderDialogBinding
import com.backdoor.moove.modern_ui.create.CreateReminderViewModel
import com.backdoor.moove.utils.*
import com.squareup.picasso.Picasso
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.lang.Exception
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

class ReminderDialogActivity : AppCompatActivity() {

    private val soundStackHolder: SoundStackHolder by inject()
    private val language: Language by inject()
    private val locationEvent: LocationEvent by inject()
    private val prefs: Prefs by inject()

    private lateinit var binding: ActivityReminderDialogBinding
    private lateinit var viewModel: CreateReminderViewModel
    private var mId = ""

    private var tts: TextToSpeech? = null
    private var mWakeLock: PowerManager.WakeLock? = null

    private var mReminder: Reminder? = null
    private var isMockedTest = false
    private var isReminderShowed = false
    private var isScreenResumed: Boolean = false

    private val isAutoCallEnabled: Boolean
        get() {
            return prefs.silentCall
        }

    private val isTtsEnabled: Boolean
        get() {
            val isTTS = prefs.ttsEnabled
            Timber.d("isTtsEnabled: $isTTS")
            return isTTS
        }

    private val melody: String
        get() = if (mReminder == null) "" else mReminder?.melody ?: ""

    private val summary: String
        get() = if (mReminder == null) "" else mReminder?.summary ?: ""

    private val uuId: String
        get() = if (mReminder == null) "" else mReminder?.uuId ?: ""

    private val id: Int
        get() = if (mReminder == null) 0 else mReminder?.uniqueId ?: 2121

    private val ledColor: Int
        get() {
            val reminder = mReminder ?: return 0
            return if (reminder.ledColor != -1) {
                    LED.getLED(reminder.ledColor)
                } else {
                    LED.getLED(prefs.ledColor)
                }
        }

    private val isUnlockDevice: Boolean
        get() {
            return prefs.unlockScreen
        }

    private val maxVolume: Int
        get() {
            val reminder = mReminder ?: return 25
            return if (reminder.volume != -1) {
                reminder.volume
            } else {
                prefs.loudness
            }
        }

    private var mTextToSpeechListener: TextToSpeech.OnInitListener = TextToSpeech.OnInitListener { status ->
        if (status == TextToSpeech.SUCCESS && tts != null) {
            val result = tts?.setLanguage(ttsLocale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Timber.d("This Language is not supported")
            } else {
                if (!TextUtils.isEmpty(summary)) {
                    try {
                        Thread.sleep(1000)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }

                    tts?.speak(summary, TextToSpeech.QUEUE_FLUSH, null, null)
                }
            }
        } else {
            Timber.d("Initialization Failed!")
        }
    }

    private val sound: Sound?
        get() = soundStackHolder.sound

    private val ttsLocale: Locale?
        get() {
            return language.getLocale(false)
        }

    private val soundUri: Uri
        get() {
            if (!TextUtils.isEmpty(melody) && !Sound.isDefaultMelody(melody)) {
                val uri = UriUtil.getUri(this, melody)
                if (uri != null) return uri
            } else {
                val defMelody = prefs.melody
                if (!TextUtils.isEmpty(defMelody) && !Sound.isDefaultMelody(defMelody)) {
                    val sound = File(defMelody)
                    if (sound.exists()) {
                        val uri = UriUtil.getUri(this, sound)
                        if (uri != null) return uri
                    }
                }
            }
            return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        }

    private val isRateDialogShowed: Boolean
        get() {
            var count = prefs.appRuns
            count++
            prefs.appRuns = count
            return count == 10 || prefs.rateShowed
        }

    private val mReminderObserver: Observer<in Reminder> = Observer {
        if (it != null) {
            showReminder(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val current = instanceCount.incrementAndGet()
        Timber.d("onCreate: $current, ${TimeUtils.getFullDateTime(System.currentTimeMillis(), true)}")

        isScreenResumed = intent.getBooleanExtra(Module.INTENT_NOTIFICATION, false)
        mId = intent.getStringExtra(Module.INTENT_ID) ?: ""

        binding = DataBindingUtil.setContentView(this, R.layout.activity_reminder_dialog)

        binding.contactBlock.visibility = View.INVISIBLE
        if (prefs.reminderImage != Module.NONE) {
            binding.bgImage.visibility = View.VISIBLE
            if (prefs.reminderImage == Module.DEFAULT) {
                Picasso.get()
                        .load(R.drawable.photo)
                        .resize(1080, 1080)
                        .centerCrop()
                        .into(binding.bgImage)
            } else {
                val imageFile = File(prefs.reminderImage)
                if (Permissions.checkPermission(this, Permissions.READ_EXTERNAL) && imageFile.exists()) {
                    Picasso.get()
                            .load(imageFile)
                            .resize(1080, 1080)
                            .centerCrop()
                            .into(binding.bgImage)
                } else {
                    Picasso.get()
                            .load(R.drawable.photo)
                            .resize(1080, 1080)
                            .centerCrop()
                            .into(binding.bgImage)
                }
            }
        } else {
            binding.bgImage.visibility = View.INVISIBLE
        }

        initButtons()

        if (savedInstanceState != null) {
            isScreenResumed = savedInstanceState.getBoolean(ARG_IS_ROTATED, false)
        }

        viewModel = ViewModelProviders.of(this, CreateReminderViewModel.Factory(mId)).get(CreateReminderViewModel::class.java)
        viewModel.loadedReminder.observe(this, mReminderObserver)

    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(ARG_IS_ROTATED, true)
        super.onSaveInstanceState(outState)
    }

    private fun initButtons() {
        binding.buttonNotification.setOnClickListener { favourite() }
        binding.buttonOk.setOnClickListener { ok() }
        binding.buttonEdit.setOnClickListener { editReminder() }
        binding.buttonCall.setOnClickListener { call() }
        binding.buttonSend.setOnClickListener { sendSMS() }
    }

    override fun onResume() {
        super.onResume()
        if (isRateDialogShowed) {
            showRateDialog()
        }
    }

    private fun showRateDialog() {
        val builder = AlertDialog.Builder(this, R.style.HomeDarkDialog)
        builder.setTitle(R.string.rate)
        builder.setMessage(R.string.do_you_want_ro_rate_this_application)
        builder.setPositiveButton(R.string.rate) { dialogInterface, _ ->
            dialogInterface.dismiss()
            SuperUtil.launchMarket(this)
        }
        builder.setNegativeButton(R.string.never) { dialogInterface, _ -> dialogInterface.dismiss() }
        builder.setNeutralButton(R.string.later) { dialogInterface, _ ->
            dialogInterface.dismiss()
            prefs.appRuns = 0
        }
        builder.create().show()
    }

    private fun showReminder(reminder: Reminder) {
        if (isReminderShowed) return
        isReminderShowed = true
        this.mReminder = reminder

        val contactPhoto = binding.contactPhoto
        contactPhoto.borderColor = ContextCompat.getColor(this, R.color.secondary)
        contactPhoto.hide()

        binding.remText.text = reminder.summary
        when {
            reminder.type.contains(ReminderUtils.TYPE_CALL) -> {
                contactPhoto.show()
                val conID = Contacts.getContactIDFromNumber(reminder.phoneNumber, this)
                val name = Contacts.getContactNameFromNumber(reminder.phoneNumber, this)

                val photo = Contacts.getPhoto(conID)
                if (photo != null) {
                    contactPhoto.setImageURI(photo)
                } else {
                    BitmapUtils.imageFromName(name ?: reminder.summary) {
                        contactPhoto.setImageDrawable(it)
                    }
                }

                binding.contactName.text = name
                binding.contactNumber.text = reminder.phoneNumber
                binding.contactBlock.show()
                binding.buttonCall.show()
                binding.buttonSend.hide()
            }
            reminder.type.contains(ReminderUtils.TYPE_MESSAGE) -> {
                contactPhoto.show()
                val conID = Contacts.getContactIDFromNumber(reminder.phoneNumber, this)
                val name = Contacts.getContactNameFromNumber(reminder.phoneNumber, this)

                val photo = Contacts.getPhoto(conID)
                if (photo != null) {
                    contactPhoto.setImageURI(photo)
                } else {
                    BitmapUtils.imageFromName(name ?: reminder.summary) {
                        contactPhoto.setImageDrawable(it)
                    }
                }

                binding.contactName.text = name
                binding.contactNumber.text = reminder.phoneNumber
                binding.contactBlock.show()
                binding.buttonCall.hide()
                binding.buttonSend.show()
            }
            else -> {
                binding.contactBlock.visibility = View.INVISIBLE
                binding.buttonCall.hide()
                binding.buttonSend.hide()
            }
        }

        init()

        if (reminder.type.contains(ReminderUtils.TYPE_CALL) && isAutoCallEnabled) {
            call()
        } else {
            showNotification()
            if (isTtsEnabled) {
                startTts()
            }
        }
    }

    private fun sendSMS() {
        val reminder = mReminder ?: return
        if (TextUtils.isEmpty(summary)) return
        TelephonyUtil.sendSms(this, reminder.phoneNumber, summary)
    }

    private fun showNotification() {
        if (isMockedTest || isReminderShowed) return
        if (!isTtsEnabled) {
            showReminderNotification(this)
        } else {
            showTTSNotification(this)
        }
    }

    private fun editReminder() {
        doActions({ it.stop() }, {
            val intent = Intent(ACTION_VIEW, Uri.parse("mooveapp://reminder/${it.uuId}"))
            try {
                startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            finish()
        })
    }

    private fun call() {
        doActions({ it.stop() }, {
            makeCall()
            finish()
        })
    }

    private fun makeCall() {
        val reminder = mReminder ?: return
        if (Permissions.ensurePermissions(this, CALL_PERM, Permissions.CALL_PHONE)) {
            TelephonyUtil.makeCall(reminder.phoneNumber, this)
        }
    }

    private fun favourite() {
        doActions({ it.stop() }, {
            showFavouriteNotification()
            finish()
        })
    }

    private fun ok() {
        doActions({ it.stop() }, { finish() })
    }

    private fun init() {
        setUpScreenOptions()
        soundStackHolder.setMaxVolume(maxVolume)
    }

    override fun onBackPressed() {
        discardMedia()
        Toast.makeText(this, getString(R.string.select_one_of_item), Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        val left = instanceCount.decrementAndGet()
        Timber.d("onDestroy: left screens -> $left")
        viewModel.loadedReminder.removeObserver(mReminderObserver)
        removeFlags()
    }

    override fun onPause() {
        super.onPause()
        soundStackHolder.cancelIncreaseSound()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (MotionEvent.ACTION_DOWN == event.action) {
            discardMedia()
        }
        return super.onTouchEvent(event)
    }

    private fun canUnlockScreen(): Boolean {
        return isUnlockDevice
    }

    private fun setUpScreenOptions() {
        Timber.d("setUpScreenOptions: ${canUnlockScreen()}")
        if (canUnlockScreen()) {
            SuperUtil.turnScreenOn(this, window)
            SuperUtil.unlockOn(this, window)
        }
        mWakeLock = SuperUtil.wakeDevice(this)
    }

    private fun removeFlags() {
        if (canUnlockScreen()) {
            SuperUtil.unlockOff(this, window)
            SuperUtil.turnScreenOff(this, window, mWakeLock)
        }

        if (tts != null) {
            tts?.stop()
            tts?.shutdown()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == MY_DATA_CHECK_CODE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                tts = TextToSpeech(this, mTextToSpeechListener)
            } else {
                val installTTSIntent = Intent()
                installTTSIntent.action = TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA
                try {
                    startActivity(installTTSIntent)
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }
            }
        }
    }


    private fun startTts() {
        val checkTTSIntent = Intent()
        checkTTSIntent.action = TextToSpeech.Engine.ACTION_CHECK_TTS_DATA
        try {
            startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE)
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
        }
    }

    private fun discardNotification(id: Int) {
        Timber.d("discardNotification: $id")
        discardMedia()
        Notifier.getManager(this)?.cancel(id)
    }

    private fun discardMedia() {
        sound?.stop(true)
    }

    private fun showWearNotification(secondaryText: String) {
        Timber.d("showWearNotification: $secondaryText")
        val wearableNotificationBuilder = NotificationCompat.Builder(this, Notifier.CHANNEL_REMINDER)
        wearableNotificationBuilder.setSmallIcon(R.drawable.ic_twotone_notifications_white)
        wearableNotificationBuilder.setContentTitle(summary)
        wearableNotificationBuilder.setContentText(secondaryText)
        wearableNotificationBuilder.color = ContextCompat.getColor(this, R.color.secondary)
        wearableNotificationBuilder.setOngoing(false)
        wearableNotificationBuilder.setOnlyAlertOnce(true)
        wearableNotificationBuilder.setGroup("GROUP")
        wearableNotificationBuilder.setGroupSummary(false)
        Notifier.getManager(this)?.notify(id, wearableNotificationBuilder.build())
    }

    private fun showFavouriteNotification() {
        val builder = NotificationCompat.Builder(this, Notifier.CHANNEL_SYSTEM)
        builder.setContentTitle(summary)
        val appName: String = getString(R.string.app_name)
        builder.setContentText(appName)
        builder.setSmallIcon(R.drawable.ic_twotone_notifications_white)
        builder.color = ContextCompat.getColor(this, R.color.bluePrimary)
        val isWear = prefs.wearNotification
        if (isWear) {
            builder.setOnlyAlertOnce(true)
            builder.setGroup("GROUP")
            builder.setGroupSummary(true)
        }
        Notifier.getManager(this)?.notify(id, builder.build())
        if (isWear) {
            showWearNotification(appName)
        }
    }

    private fun showReminderNotification(activity: Activity) {
        if (isScreenResumed) {
            return
        }
        Timber.d("showReminderNotification: $uuId")
        val notificationIntent = Intent(this, activity.javaClass)
        notificationIntent.putExtra(Module.INTENT_ID, uuId)
        notificationIntent.putExtra(Module.INTENT_NOTIFICATION, true)
        notificationIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
        val intent = PendingIntent.getActivity(this, id, notificationIntent, 0)
        val builder: NotificationCompat.Builder
        if (isScreenResumed) {
            builder = NotificationCompat.Builder(this, Notifier.CHANNEL_SYSTEM)
            builder.priority = NotificationCompat.PRIORITY_LOW
        } else {
            builder = NotificationCompat.Builder(this, Notifier.CHANNEL_REMINDER)
            builder.priority = NotificationCompat.PRIORITY_HIGH
            if ((!SuperUtil.isDoNotDisturbEnabled(this) ||
                            (SuperUtil.checkNotificationPermission(this) && prefs.soundInSilent))) {
                val soundUri = soundUri
                Timber.d("showReminderNotification: $soundUri")
                sound?.playAlarm(soundUri, prefs.repeatMelody)
            }
            if (prefs.vibrate) {
                val pattern: LongArray = if (prefs.infiniteVibration) {
                    longArrayOf(150, 86400000)
                } else {
                    longArrayOf(150, 400, 100, 450, 200, 500, 300, 500)
                }
                builder.setVibrate(pattern)
            }
        }
        builder.setContentTitle(summary)
        builder.setContentIntent(intent)
        builder.setAutoCancel(false)
        builder.priority = NotificationCompat.PRIORITY_MAX
        builder.setOngoing(true)
        val appName = getString(R.string.app_name)
        if (prefs.ledEnabled) {
            builder.setLights(ledColor, 500, 1000)
        }
        builder.setContentText(appName)
        builder.setSmallIcon(R.drawable.ic_twotone_notifications_white)
        builder.color = ContextCompat.getColor(this, R.color.bluePrimary)
        val isWear = prefs.wearNotification
        if (isWear) {
            builder.setOnlyAlertOnce(true)
            builder.setGroup("GROUP")
            builder.setGroupSummary(true)
        }
        Notifier.getManager(this)?.notify(id, builder.build())
        if (isWear) {
            showWearNotification(appName)
        }
    }

    private fun showTTSNotification(activityClass: Activity) {
        if (isScreenResumed) {
            return
        }
        Timber.d("showTTSNotification: ")
        val builder: NotificationCompat.Builder
        if (isScreenResumed) {
            builder = NotificationCompat.Builder(this, Notifier.CHANNEL_SYSTEM)
            builder.priority = NotificationCompat.PRIORITY_LOW
        } else {
            builder = NotificationCompat.Builder(this, Notifier.CHANNEL_REMINDER)
            builder.priority = NotificationCompat.PRIORITY_HIGH
            if ((!SuperUtil.isDoNotDisturbEnabled(this) ||
                            (SuperUtil.checkNotificationPermission(this) && prefs.soundInSilent))) {
                playDefaultMelody()
            }
            if (prefs.vibrate) {
                val pattern: LongArray = if (prefs.infiniteVibration) {
                    longArrayOf(150, 86400000)
                } else {
                    longArrayOf(150, 400, 100, 450, 200, 500, 300, 500)
                }
                builder.setVibrate(pattern)
            }
        }
        builder.setContentTitle(summary)

        val notificationIntent = Intent(this, activityClass.javaClass)
        notificationIntent.putExtra(Module.INTENT_ID, uuId)
        notificationIntent.putExtra(Module.INTENT_NOTIFICATION, true)
        notificationIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
        val intent = PendingIntent.getActivity(this, id, notificationIntent, 0)
        builder.setContentIntent(intent)
        builder.setAutoCancel(false)
        builder.setOngoing(true)
        val appName = getString(R.string.app_name)
        if (prefs.ledEnabled) {
            builder.setLights(ledColor, 500, 1000)
        }
        builder.setContentText(appName)
        builder.setSmallIcon(R.drawable.ic_twotone_notifications_white)
        builder.color = ContextCompat.getColor(this, R.color.bluePrimary)
        val isWear = prefs.wearNotification
        if (isWear) {
            builder.setOnlyAlertOnce(true)
            builder.setGroup("GROUP")
            builder.setGroupSummary(true)
        }
        Notifier.getManager(this)?.notify(id, builder.build())
        if (isWear) {
            showWearNotification(appName)
        }
    }

    private fun playDefaultMelody() {
        if (sound == null) return
        Timber.d("playDefaultMelody: ")
        try {
            val afd = assets.openFd("sounds/beep.mp3")
            sound?.playAlarm(afd)
        } catch (e: IOException) {
            e.printStackTrace()
            sound?.playAlarm(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), false)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CALL_PERM -> if (Permissions.isAllGranted(grantResults)) {
                makeCall()
            }
        }
    }

    private fun doActions(onControl: (LocationEvent) -> Unit, onEnd: (Reminder) -> Unit) {
        isReminderShowed = true
        viewModel.loadedReminder.removeObserver(mReminderObserver)
        val reminder = mReminder
        if (reminder == null) {
            removeFlags()
            finish()
            return
        }
        val control = locationEvent
        launchDefault {
            onControl.invoke(control)
            withUIContext {
                discardNotification(reminder.uniqueId)
                removeFlags()
                onEnd.invoke(reminder)
            }
        }
    }

    companion object {

        private const val CALL_PERM = 612
        private const val MY_DATA_CHECK_CODE = 111
        private const val ARG_IS_ROTATED = "arg_rotated"

        private val instanceCount = AtomicInteger(0)

        fun getLaunchIntent(context: Context, uuid: String) {
            val resultIntent = Intent(context, ReminderDialogActivity::class.java)
            resultIntent.putExtra(Module.INTENT_ID, uuid)
            resultIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
            context.startActivity(resultIntent)
        }
    }
}