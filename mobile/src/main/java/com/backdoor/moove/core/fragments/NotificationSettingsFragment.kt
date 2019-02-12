package com.backdoor.moove.core.fragments


import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import com.backdoor.moove.R
import com.backdoor.moove.core.consts.Configs
import com.backdoor.moove.core.consts.Constants
import com.backdoor.moove.core.consts.Prefs
import com.backdoor.moove.core.helper.Dialogues
import com.backdoor.moove.core.helper.Module
import com.backdoor.moove.core.helper.Permissions
import com.backdoor.moove.core.helper.SharedPrefs
import com.backdoor.moove.core.views.PrefsView

import java.io.File

class NotificationSettingsFragment : Fragment(), View.OnClickListener, DialogInterface.OnDismissListener {

    private var sPrefs: SharedPrefs? = null
    private var ab: ActionBar? = null
    private var locale: TextView? = null

    private var blurPrefs: PrefsView? = null
    private var vibrationOptionPrefs: PrefsView? = null
    private var infiniteVibrateOptionPrefs: PrefsView? = null
    private var soundOptionPrefs: PrefsView? = null
    private var infiniteSoundOptionPrefs: PrefsView? = null
    private var ttsPrefs: PrefsView? = null
    private var wakeScreenOptionPrefs: PrefsView? = null
    private var unlockScreenPrefs: PrefsView? = null
    private var silentSMSOptionPrefs: PrefsView? = null
    private var ledPrefs: PrefsView? = null
    private var chooseSoundPrefs: PrefsView? = null
    private var chooseLedColorPrefs: PrefsView? = null
    private var silentCallOptionPrefs: PrefsView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.settings_notification, container, false)
        ab = (activity as AppCompatActivity).supportActionBar
        if (ab != null) {
            ab!!.setTitle(R.string.notification)
        }
        sPrefs = SharedPrefs.getInstance(activity)
        if (Module.isLollipop) {
            rootView.findViewById<View>(R.id.imageCard).elevation = Configs.CARD_ELEVATION
            rootView.findViewById<View>(R.id.soundCard).elevation = Configs.CARD_ELEVATION
            rootView.findViewById<View>(R.id.systemCard).elevation = Configs.CARD_ELEVATION
        }
        val selectImage = rootView.findViewById<TextView>(R.id.selectImage)
        selectImage.setOnClickListener { v ->
            Dialogues.imageDialog(activity) { dialogInterface ->

            }
        }

        blurPrefs = rootView.findViewById(R.id.blurPrefs)
        blurPrefs!!.setOnClickListener(this)
        blurPrefs!!.isChecked = sPrefs!!.loadBoolean(Prefs.REMINDER_IMAGE_BLUR)
        blurPrefs!!.isEnabled = true

        vibrationOptionPrefs = rootView.findViewById(R.id.vibrationOptionPrefs)
        vibrationOptionPrefs!!.setOnClickListener(this)
        vibrationOptionPrefs!!.isChecked = sPrefs!!.loadBoolean(Prefs.VIBRATION_STATUS)

        infiniteVibrateOptionPrefs = rootView.findViewById(R.id.infiniteVibrateOptionPrefs)
        infiniteVibrateOptionPrefs!!.setOnClickListener(this)
        infiniteVibrateOptionPrefs!!.isChecked = sPrefs!!.loadBoolean(Prefs.INFINITE_VIBRATION)

        soundOptionPrefs = rootView.findViewById(R.id.soundOptionPrefs)
        soundOptionPrefs!!.setOnClickListener(this)
        soundOptionPrefs!!.isChecked = sPrefs!!.loadBoolean(Prefs.SILENT_SOUND)

        infiniteSoundOptionPrefs = rootView.findViewById(R.id.infiniteSoundOptionPrefs)
        infiniteSoundOptionPrefs!!.setOnClickListener(this)
        infiniteSoundOptionPrefs!!.isChecked = sPrefs!!.loadBoolean(Prefs.INFINITE_SOUND)

        ttsPrefs = rootView.findViewById(R.id.ttsPrefs)
        ttsPrefs!!.setOnClickListener(this)
        ttsPrefs!!.isChecked = sPrefs!!.loadBoolean(Prefs.TTS)

        wakeScreenOptionPrefs = rootView.findViewById(R.id.wakeScreenOptionPrefs)
        wakeScreenOptionPrefs!!.setOnClickListener(this)
        wakeScreenOptionPrefs!!.isChecked = sPrefs!!.loadBoolean(Prefs.WAKE_STATUS)

        unlockScreenPrefs = rootView.findViewById(R.id.unlockScreenPrefs)
        unlockScreenPrefs!!.setOnClickListener(this)
        unlockScreenPrefs!!.isChecked = sPrefs!!.loadBoolean(Prefs.UNLOCK_DEVICE)

        silentSMSOptionPrefs = rootView.findViewById(R.id.silentSMSOptionPrefs)
        silentSMSOptionPrefs!!.setOnClickListener(this)
        silentSMSOptionPrefs!!.isChecked = sPrefs!!.loadBoolean(Prefs.SILENT_SMS)

        silentCallOptionPrefs = rootView.findViewById(R.id.silentCallOptionPrefs)
        silentCallOptionPrefs!!.setOnClickListener(this)
        silentCallOptionPrefs!!.isChecked = sPrefs!!.loadBoolean(Prefs.SILENT_CALL)

        chooseSoundPrefs = rootView.findViewById(R.id.chooseSoundPrefs)
        chooseSoundPrefs!!.setOnClickListener(this)

        showMelody()

        val volume = rootView.findViewById<TextView>(R.id.volume)
        volume.setOnClickListener(this)

        locale = rootView.findViewById(R.id.locale)
        locale!!.setOnClickListener { v -> Dialogues.ttsLocale(activity, Prefs.TTS_LOCALE) }

        ledPrefs = rootView.findViewById(R.id.ledPrefs)
        chooseLedColorPrefs = rootView.findViewById(R.id.chooseLedColorPrefs)

        checkVibrate()

        checkTTS()

        ledPrefs!!.setOnClickListener(this)
        ledPrefs!!.visibility = View.VISIBLE
        ledPrefs!!.isChecked = sPrefs!!.loadBoolean(Prefs.LED_STATUS)

        chooseLedColorPrefs!!.visibility = View.VISIBLE
        chooseLedColorPrefs!!.setOnClickListener { view -> Dialogues.ledColor(activity) }

        checkEnabling()

        return rootView
    }

    private fun checkTTS() {
        if (ttsPrefs!!.isChecked) {
            locale!!.isEnabled = true
        } else {
            locale!!.isEnabled = false
        }
    }

    private fun checkVibrate() {
        if (vibrationOptionPrefs!!.isChecked) {
            infiniteVibrateOptionPrefs!!.isEnabled = true
        } else {
            infiniteVibrateOptionPrefs!!.isEnabled = false
        }
    }

    private fun showMelody() {
        if (sPrefs!!.loadBoolean(Prefs.CUSTOM_SOUND)) {
            if (sPrefs!!.isString(Prefs.CUSTOM_SOUND_FILE)) {
                val path = sPrefs!!.loadPrefs(Prefs.CUSTOM_SOUND_FILE)
                if (!path!!.matches("".toRegex())) {
                    val sound = File(path)
                    if (sound.exists()) {
                        val fileName = sound.name
                        val pos = fileName.lastIndexOf(".")
                        val fileNameS = fileName.substring(0, pos)
                        chooseSoundPrefs!!.setDetailText(fileNameS)
                    } else {
                        chooseSoundPrefs!!.setDetailText(activity!!.getString(R.string.default_string))
                    }
                } else {
                    chooseSoundPrefs!!.setDetailText(activity!!.getString(R.string.default_string))
                }
            }
        } else {
            chooseSoundPrefs!!.setDetailText(resources.getString(R.string.default_string))
        }
    }

    private fun vibrationChange() {
        if (vibrationOptionPrefs!!.isChecked) {
            sPrefs!!.saveBoolean(Prefs.VIBRATION_STATUS, false)
            vibrationOptionPrefs!!.isChecked = false
        } else {
            sPrefs!!.saveBoolean(Prefs.VIBRATION_STATUS, true)
            vibrationOptionPrefs!!.isChecked = true
        }
        checkVibrate()
    }

    private fun infiniteVibrationChange() {
        if (infiniteVibrateOptionPrefs!!.isChecked) {
            sPrefs!!.saveBoolean(Prefs.INFINITE_VIBRATION, false)
            infiniteVibrateOptionPrefs!!.isChecked = false
        } else {
            sPrefs!!.saveBoolean(Prefs.INFINITE_VIBRATION, true)
            infiniteVibrateOptionPrefs!!.isChecked = true
        }
    }

    private fun ttsChange() {
        if (ttsPrefs!!.isChecked) {
            sPrefs!!.saveBoolean(Prefs.TTS, false)
            ttsPrefs!!.isChecked = false
        } else {
            sPrefs!!.saveBoolean(Prefs.TTS, true)
            ttsPrefs!!.isChecked = true
            Dialogues.ttsLocale(activity, Prefs.TTS_LOCALE)
        }
        checkTTS()
    }

    private fun blurChange() {
        if (blurPrefs!!.isChecked) {
            sPrefs!!.saveBoolean(Prefs.REMINDER_IMAGE_BLUR, false)
            blurPrefs!!.isChecked = false
        } else {
            sPrefs!!.saveBoolean(Prefs.REMINDER_IMAGE_BLUR, true)
            blurPrefs!!.isChecked = true
        }
    }

    private fun soundChange() {
        if (soundOptionPrefs!!.isChecked) {
            sPrefs!!.saveBoolean(Prefs.SILENT_SOUND, false)
            soundOptionPrefs!!.isChecked = false
        } else {
            sPrefs!!.saveBoolean(Prefs.SILENT_SOUND, true)
            soundOptionPrefs!!.isChecked = true
        }
    }

    private fun infiniteSoundChange() {
        if (infiniteSoundOptionPrefs!!.isChecked) {
            sPrefs!!.saveBoolean(Prefs.INFINITE_SOUND, false)
            infiniteSoundOptionPrefs!!.isChecked = false
        } else {
            sPrefs!!.saveBoolean(Prefs.INFINITE_SOUND, true)
            infiniteSoundOptionPrefs!!.isChecked = true
        }
    }

    private fun checkEnabling() {
        if (ledPrefs!!.isChecked) {
            chooseLedColorPrefs!!.isEnabled = true
        } else {
            chooseLedColorPrefs!!.isEnabled = false
        }
    }

    private fun ledChange() {
        if (ledPrefs!!.isChecked) {
            sPrefs!!.saveBoolean(Prefs.LED_STATUS, false)
            ledPrefs!!.isChecked = false
        } else {
            sPrefs!!.saveBoolean(Prefs.LED_STATUS, true)
            ledPrefs!!.isChecked = true
        }
        checkEnabling()
    }

    private fun silentSMSChange() {
        if (silentSMSOptionPrefs!!.isChecked) {
            sPrefs!!.saveBoolean(Prefs.SILENT_SMS, false)
            silentSMSOptionPrefs!!.isChecked = false
        } else {
            sPrefs!!.saveBoolean(Prefs.SILENT_SMS, true)
            silentSMSOptionPrefs!!.isChecked = true
        }
    }

    private fun silentCallChange() {
        if (silentCallOptionPrefs!!.isChecked) {
            sPrefs!!.saveBoolean(Prefs.SILENT_CALL, false)
            silentCallOptionPrefs!!.isChecked = false
        } else {
            sPrefs!!.saveBoolean(Prefs.SILENT_CALL, true)
            silentCallOptionPrefs!!.isChecked = true
        }
    }

    private fun wakeChange() {
        if (wakeScreenOptionPrefs!!.isChecked) {
            sPrefs!!.saveBoolean(Prefs.WAKE_STATUS, false)
            wakeScreenOptionPrefs!!.isChecked = false
        } else {
            sPrefs!!.saveBoolean(Prefs.WAKE_STATUS, true)
            wakeScreenOptionPrefs!!.isChecked = true
        }
    }

    private fun unlockChange() {
        if (unlockScreenPrefs!!.isChecked) {
            sPrefs!!.saveBoolean(Prefs.UNLOCK_DEVICE, false)
            unlockScreenPrefs!!.isChecked = false
        } else {
            sPrefs!!.saveBoolean(Prefs.UNLOCK_DEVICE, true)
            unlockScreenPrefs!!.isChecked = true
        }
    }

    override fun onDetach() {
        super.onDetach()
        ab = (activity as AppCompatActivity).supportActionBar
        if (ab != null) {
            ab!!.setTitle(R.string.settings)
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.blurPrefs -> blurChange()
            R.id.vibrationOptionPrefs -> vibrationChange()
            R.id.soundOptionPrefs -> soundChange()
            R.id.infiniteSoundOptionPrefs -> infiniteSoundChange()
            R.id.chooseSoundPrefs -> if (Permissions.checkPermission(activity, Permissions.READ_EXTERNAL)) {
                Dialogues.melodyType(activity, Prefs.CUSTOM_SOUND, 201)
            } else {
                Permissions.requestPermission(activity, 103,
                        Permissions.READ_EXTERNAL, Permissions.WRITE_EXTERNAL)
            }
            R.id.infiniteVibrateOptionPrefs -> infiniteVibrationChange()
            R.id.silentSMSOptionPrefs -> silentSMSChange()
            R.id.wakeScreenOptionPrefs -> wakeChange()
            R.id.unlockScreenPrefs -> unlockChange()
            R.id.volume -> Dialogues.dialogWithSeek(activity, 25, Prefs.VOLUME, getString(R.string.volume), this)
            R.id.ttsPrefs -> ttsChange()
            R.id.ledPrefs -> ledChange()
            R.id.silentCallOptionPrefs -> silentCallChange()
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        showMelody()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        showMelody()
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (grantResults.size == 0) return
        when (requestCode) {
            101 -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                var intent = Intent(Intent.ACTION_GET_CONTENT)
                if (Module.isKitkat) {
                    intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
                    intent.addCategory(Intent.CATEGORY_OPENABLE)
                }
                intent.type = "image/*"
                val chooser = Intent.createChooser(intent, activity!!.getString(R.string.select_image))
                activity!!.startActivityForResult(chooser, Constants.ACTION_REQUEST_GALLERY)
            } else {
                Permissions.showInfo(activity, Permissions.READ_CALENDAR)
            }
        }
    }
}
