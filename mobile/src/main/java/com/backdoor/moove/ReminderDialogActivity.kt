package com.backdoor.moove

import android.annotation.SuppressLint
import android.app.Activity
import android.app.PendingIntent
import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.speech.tts.TextToSpeech
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.telephony.SmsManager
import android.util.DisplayMetrics
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

import com.backdoor.moove.core.async.DisableAsync
import com.backdoor.moove.core.consts.Configs
import com.backdoor.moove.core.consts.Constants
import com.backdoor.moove.core.consts.Language
import com.backdoor.moove.core.consts.Prefs
import com.backdoor.moove.core.helper.Coloring
import com.backdoor.moove.core.helper.Contacts
import com.backdoor.moove.core.helper.Messages
import com.backdoor.moove.core.helper.Module
import com.backdoor.moove.core.helper.Notifier
import com.backdoor.moove.core.helper.Reminder
import com.backdoor.moove.core.helper.SharedPrefs
import com.backdoor.moove.core.helper.Telephony
import com.backdoor.moove.core.helper.Type
import com.backdoor.moove.core.interfaces.SendListener
import com.backdoor.moove.core.services.DeliveredReceiver
import com.backdoor.moove.core.services.SendReceiver
import com.backdoor.moove.core.views.RoundImageView
import com.squareup.picasso.Picasso

import jp.wasabeef.picasso.transformations.BlurTransformation

class ReminderDialogActivity : Activity(), TextToSpeech.OnInitListener, SendListener, View.OnClickListener {

    private var buttonCall: FloatingActionButton? = null
    private var remText: TextView? = null

    private var deliveredReceiver: BroadcastReceiver? = null
    private var sentReceiver: BroadcastReceiver? = null

    private var id: Long = 0
    private var color = -1
    private var volume: Int = 0
    private var isMelody: Int = 0
    private var melody: String? = null
    private var number: String? = null
    private var name: String? = null
    private var task: String? = null
    private var reminderType: String? = null
    private var currVolume: Int = 0

    private var reminder: Type? = null
    private var item: Reminder? = null

    private var mPrefs: SharedPrefs? = null
    private var mNotifier: Notifier? = null
    private var tts: TextToSpeech? = null

    private val type: String?
        get() = if (reminderType != null) {
            reminderType
        } else {
            if (item != null) {
                item!!.type
            } else {
                if (id != 0L && reminder != null) {
                    reminder!!.getItem(id)!!.type
                } else {
                    ""
                }
            }
        }

    @SuppressLint("SetTextI18n")
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mPrefs = SharedPrefs.getInstance(this)
        val cs = Coloring(this@ReminderDialogActivity)
        mNotifier = Notifier(this@ReminderDialogActivity)

        val res = intent
        id = res.getLongExtra(Constants.ITEM_ID_INTENT, 0)
        isMelody = res.getIntExtra("int", 0)
        reminder = Type(this)

        item = reminder!!.getItem(id)
        if (item != null) {
            task = item!!.title
            reminderType = item!!.type
            number = item!!.number
            melody = item!!.melody
            color = item!!.color
            volume = item!!.volume
        } else {
            mNotifier!!.discardNotification(id)
            finish()
        }

        val am = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (am != null) {
            currVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC)
        }
        var prefsVol = if (mPrefs != null) mPrefs!!.loadInt(Prefs.VOLUME) else 25
        if (volume != -1) {
            prefsVol = volume
        }
        if (am != null) {
            val volPercent = prefsVol.toFloat() / Configs.MAX_VOLUME
            val maxVol = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val streamVol = (maxVol * volPercent).toInt()
            am.setStreamVolume(AudioManager.STREAM_MUSIC, streamVol, 0)
        }

        val isFull = mPrefs != null && mPrefs!!.loadBoolean(Prefs.UNLOCK_DEVICE)
        if (isFull) {
            runOnUiThread {
                window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                        or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)
            }
        }

        val isWake = mPrefs != null && mPrefs!!.loadBoolean(Prefs.WAKE_STATUS)
        if (isWake) {
            val screenLock = (getSystemService(Context.POWER_SERVICE) as PowerManager).newWakeLock(
                    PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP, "TAG")
            screenLock.acquire()
            screenLock.release()
        }

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setTheme(cs.transparentStyle)
        setContentView(R.layout.reminder_dialog_layout)

        if (Module.isLollipop) {
            window.statusBarColor = cs.statusBarStyle
        }

        val single_container = findViewById<LinearLayout>(R.id.single_container)
        single_container.visibility = View.VISIBLE

        loadImage()

        val buttonOk = findViewById<FloatingActionButton>(R.id.buttonOk)
        val buttonEdit = findViewById<FloatingActionButton>(R.id.buttonEdit)
        buttonCall = findViewById(R.id.buttonCall)
        val buttonNotification = findViewById<FloatingActionButton>(R.id.buttonNotification)

        val contactPhoto = findViewById<RoundImageView>(R.id.contactPhoto)
        contactPhoto.visibility = View.GONE

        remText = findViewById(R.id.remText)
        remText!!.text = ""
        val type = type
        if (type != null) {
            if (type.contains(Constants.TYPE_CALL)) {
                contactPhoto.visibility = View.VISIBLE
                val conID = Contacts.getContactIDFromNumber(number, this@ReminderDialogActivity).toLong()
                val photo = Contacts.getPhoto(this, conID)
                if (photo != null) {
                    contactPhoto.setImageBitmap(photo)
                } else {
                    contactPhoto.visibility = View.GONE
                }
                name = Contacts.getContactNameFromNumber(number, this@ReminderDialogActivity)
                remText!!.text = task + "\n" + name + "\n" + number
            } else if (type.contains(Constants.TYPE_MESSAGE)) {
                if (!mPrefs!!.loadBoolean(Prefs.SILENT_SMS)) {
                    remText!!.text = task + "\n" + number
                    buttonCall!!.visibility = View.VISIBLE
                    buttonCall!!.setImageResource(R.drawable.ic_send_black_24dp)
                } else {
                    remText!!.text = task + "\n" + number
                    buttonCall!!.visibility = View.GONE
                }
            } else {
                remText!!.text = task
                buttonCall!!.visibility = View.GONE
            }
        } else {
            remText!!.text = task
            buttonCall!!.visibility = View.GONE
        }

        buttonNotification.setOnClickListener(this)
        buttonOk.setOnClickListener(this)
        buttonEdit.setOnClickListener(this)
        buttonCall!!.setOnClickListener(this)

        val silentSMS = mPrefs!!.loadBoolean(Prefs.SILENT_SMS)
        val silentCall = mPrefs!!.loadBoolean(Prefs.SILENT_CALL)
        if (type != null) {
            if (type.contains(Constants.TYPE_MESSAGE)) {
                if (silentSMS) {
                    sendSMS(number, task)
                } else {
                    showReminder(1)
                }
            } else if (type.contains(Constants.TYPE_CALL)) {
                if (silentCall) {
                    Telephony.makeCall(number, this@ReminderDialogActivity)
                    make()
                    finish()
                } else {
                    showReminder(1)
                }
            } else {
                showReminder(1)
            }
        } else {
            showReminder(1)
        }

        val isTTS = mPrefs!!.loadBoolean(Prefs.TTS)
        if (isTTS) {
            val checkTTSIntent = Intent()
            checkTTSIntent.action = TextToSpeech.Engine.ACTION_CHECK_TTS_DATA
            try {
                startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE)
            } catch (e: ActivityNotFoundException) {
                e.printStackTrace()
            }

        }
    }

    private fun loadImage() {
        val bgImage = findViewById<ImageView>(R.id.bgImage)
        bgImage.visibility = View.GONE
        val imagePrefs = if (mPrefs != null) mPrefs!!.loadPrefs(Prefs.REMINDER_IMAGE) else Constants.DEFAULT
        val blur = mPrefs != null && mPrefs!!.loadBoolean(Prefs.REMINDER_IMAGE_BLUR)

        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        val width = metrics.widthPixels
        val height = metrics.heightPixels
        if (imagePrefs!!.matches(Constants.DEFAULT.toRegex())) {
            if (blur) {
                Picasso.with(this@ReminderDialogActivity)
                        .load(R.drawable.photo)
                        .resize(width, height)
                        .transform(BlurTransformation(this, 15, 2))
                        .into(bgImage)
            } else {
                Picasso.with(this@ReminderDialogActivity)
                        .load(R.drawable.photo)
                        .resize(width, height)
                        .into(bgImage)
            }
            bgImage.visibility = View.VISIBLE
        } else if (imagePrefs.matches(Constants.NONE.toRegex())) {
            bgImage.visibility = View.GONE
        } else {
            if (blur) {
                Picasso.with(this@ReminderDialogActivity)
                        .load(Uri.parse(imagePrefs))
                        .resize(width, height)
                        .transform(BlurTransformation(this, 15, 2))
                        .into(bgImage)
            } else {
                Picasso.with(this@ReminderDialogActivity)
                        .load(Uri.parse(imagePrefs))
                        .resize(width, height)
                        .into(bgImage)
            }
            bgImage.visibility = View.VISIBLE
        }
    }

    private fun make() {
        Reminder.disableReminder(id, this@ReminderDialogActivity)
        if (mNotifier != null) mNotifier!!.discardNotification(id)
    }

    private fun showReminder(i: Int) {
        var i = i
        val isTTS = mPrefs != null && mPrefs!!.loadBoolean(Prefs.TTS)
        if (isMelody == 1) {
            i = 0
        }
        if (!isTTS) {
            if (mNotifier != null) mNotifier!!.showReminder(task, i, id, melody, color)
        } else {
            if (mNotifier != null) mNotifier!!.showTTSNotification(task, id, color)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (requestCode == MY_DATA_CHECK_CODE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                tts = TextToSpeech(this, this)
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

    private fun sendSMS(phoneNumber: String?, message: String?) {
        val SENT = "SMS_SENT"
        val DELIVERED = "SMS_DELIVERED"

        val sentPI = PendingIntent.getBroadcast(this@ReminderDialogActivity, 0,
                Intent(SENT), 0)
        val deliveredPI = PendingIntent.getBroadcast(this@ReminderDialogActivity,
                0, Intent(DELIVERED), 0)

        registerReceiver(sentReceiver = SendReceiver(this), IntentFilter(SENT))
        registerReceiver(deliveredReceiver = DeliveredReceiver(), IntentFilter(DELIVERED))

        val sms = SmsManager.getDefault()
        sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (MotionEvent.ACTION_DOWN == event.action && mNotifier != null) {
            mNotifier!!.discardMedia()
        }
        return super.onTouchEvent(event)
    }

    override fun onDestroy() {
        super.onDestroy()

        if (sentReceiver != null) {
            unregisterReceiver(sentReceiver)
        }
        if (deliveredReceiver != null) {
            unregisterReceiver(deliveredReceiver)
        }
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)

        if (tts != null) {
            tts!!.stop()
            tts!!.shutdown()
        }
        val am = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        am?.setStreamVolume(AudioManager.STREAM_MUSIC, currVolume, 0)

        DisableAsync(this).execute()
    }

    override fun onBackPressed() {
        if (mNotifier != null) mNotifier!!.discardMedia()
        Messages.toast(this@ReminderDialogActivity, getString(R.string.select_one_of_item))
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS && tts != null) {
            val result = tts!!.setLanguage(Language().getLocale(this@ReminderDialogActivity))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("error", "This Language is not supported")
            } else {
                if (task != null && !task!!.matches("".toRegex())) {
                    try {
                        Thread.sleep(1000)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        tts!!.speak(task, TextToSpeech.QUEUE_FLUSH, null, null)
                    } else {
                        tts!!.speak(task, TextToSpeech.QUEUE_FLUSH, null)
                    }
                }
            }
        } else {
            Log.e("error", "Initialization Failed!")
        }
    }

    override fun messageSendResult(isSent: Boolean) {
        if (isSent) {
            finish()
        } else {
            showReminder(0)
            remText!!.setText(R.string.error_sending_message)
            buttonCall!!.setImageResource(R.drawable.ic_cached_black_24dp)
            if (buttonCall!!.visibility == View.GONE) {
                buttonCall!!.visibility = View.VISIBLE
            }
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.buttonNotification -> showNotification()
            R.id.buttonOk -> ok()
            R.id.buttonEdit -> {
                make()
                Reminder.edit(id, this@ReminderDialogActivity)
                finish()
            }
            R.id.buttonCall -> makeCall()
        }
    }

    private fun showNotification() {
        make()
        if ((task == null || task!!.trim { it <= ' ' }.matches("".toRegex())) && number != null && !number!!.trim { it <= ' ' }.matches("".toRegex())) {
            if (mNotifier != null) mNotifier!!.showReminderNotification("$name $number", id)
        } else {
            if (mNotifier != null) mNotifier!!.showReminderNotification(task, id)
        }
        finish()
    }

    private fun ok() {
        make()
        finish()
    }

    private fun makeCall() {
        val type = type
        if (type!!.contains(Constants.TYPE_MESSAGE)) {
            sendSMS(number, task)
        } else {
            Telephony.makeCall(number, this@ReminderDialogActivity)
        }
        make()
        if (!type.contains(Constants.TYPE_MESSAGE)) {
            finish()
        }
    }

    companion object {

        private val MY_DATA_CHECK_CODE = 111
    }
}