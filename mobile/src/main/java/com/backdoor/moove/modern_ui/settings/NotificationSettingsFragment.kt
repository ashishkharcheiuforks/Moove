package com.backdoor.moove.modern_ui.settings

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.backdoor.moove.R
import com.backdoor.moove.databinding.DialogWithSeekAndTitleBinding
import com.backdoor.moove.databinding.FragmentSettingsNotificationBinding
import com.backdoor.moove.utils.*
import com.backdoor.moove.utils.file_explorer.FileExplorerActivity
import org.koin.android.ext.android.inject
import java.io.File
import java.util.*

class NotificationSettingsFragment : Fragment() {

    val prefs: Prefs by inject()
    val language: Language by inject()
    val dialogues: Dialogues by inject()

    private lateinit var binding: FragmentSettingsNotificationBinding
    private var mItemSelect: Int = 0
    private val localeAdapter: ArrayAdapter<String>
        get() = ArrayAdapter(context!!, android.R.layout.simple_list_item_single_choice, language.getLocaleNames(context!!))

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentSettingsNotificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        binding.prefsBgImage.setOnClickListener {
            dialogues.imageDialog(activity!!) {}
        }
        initBlurPrefs()
        initVibratePrefs()
        initInfiniteVibratePrefs()
        initSoundInSilentModePrefs()
        initInfiniteSoundPrefs()
        initMelodyPrefs()
        initLoudnessPrefs()
        initTtsPrefs()
        initTtsLocalePrefs()
        initUnlockPrefs()
        initAutoSmsPrefs()
        initAutoCallPrefs()
        initLedPrefs()
        initLedColorPrefs()
        Permissions.ensurePermissions(activity!!, PERM_SD, Permissions.READ_EXTERNAL)
    }

    private fun changeBlurPrefs() {
        val isChecked = binding.prefsImageBlur.isChecked
        binding.prefsImageBlur.isChecked = !isChecked
        prefs.blurImage = !isChecked
    }

    private fun initBlurPrefs() {
        binding.prefsImageBlur.setOnClickListener { changeBlurPrefs() }
        binding.prefsImageBlur.isChecked = prefs.blurImage
    }

    private fun showLedColorDialog() {
        val builder = AlertDialog.Builder(context!!, R.style.HomeDarkDialog)
        builder.setTitle(getString(R.string.led_color))
        val colors = LED.getAllNames(context!!)
        val adapter = ArrayAdapter(context!!, android.R.layout.simple_list_item_single_choice, colors)
        mItemSelect = prefs.ledColor
        builder.setSingleChoiceItems(adapter, mItemSelect) { _, which -> mItemSelect = which }
        builder.setPositiveButton(getString(R.string.ok)) { dialog, _ ->
            prefs.ledColor = mItemSelect
            showLedColor()
            dialog.dismiss()
        }
        builder.setNegativeButton(R.string.cancel) { dialog, _ ->
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.setOnCancelListener { mItemSelect = 0 }
        dialog.setOnDismissListener { mItemSelect = 0 }
        dialog.show()
    }

    private fun showLedColor() {
        binding.prefsLedColor.setDetailText(LED.getTitle(context!!, prefs.ledColor))
    }

    private fun initLedColorPrefs() {
        binding.prefsLedColor.setOnClickListener { showLedColorDialog() }
        binding.prefsLedColor.setDependentView(binding.prefsLed)
        showLedColor()
    }

    private fun changeLedPrefs() {
        val isChecked = binding.prefsLed.isChecked
        binding.prefsLed.isChecked = !isChecked
        prefs.ledEnabled = !isChecked
    }

    private fun initLedPrefs() {
        binding.prefsLed.setOnClickListener { changeLedPrefs() }
        binding.prefsLed.isChecked = prefs.ledEnabled
    }

    private fun changeAutoCallPrefs() {
        val isChecked = binding.prefsAutoCall.isChecked
        if (!isChecked) {
            if (Permissions.ensurePermissions(activity!!, PERM_AUTO_CALL, Permissions.CALL_PHONE)) {
                binding.prefsAutoCall.isChecked = !isChecked
                prefs.silentCall = !isChecked
            } else {
                binding.prefsAutoCall.isChecked = isChecked
                prefs.silentCall = isChecked
            }
        } else {
            binding.prefsAutoCall.isChecked = !isChecked
            prefs.silentCall = !isChecked
        }
    }

    private fun initAutoCallPrefs() {
        binding.prefsAutoCall.setOnClickListener { changeAutoCallPrefs() }
        binding.prefsAutoCall.isChecked = prefs.autoPlace
    }

    private fun changeAutoSmsPrefs() {
        val isChecked = binding.prefsAutoSms.isChecked
        if (!isChecked) {
            if (Permissions.ensurePermissions(activity!!, PERM_AUTO_SMS, Permissions.SEND_SMS)) {
                binding.prefsAutoSms.isChecked = !isChecked
                prefs.silentSms = !isChecked
            } else {
                binding.prefsAutoSms.isChecked = isChecked
                prefs.silentSms = isChecked
            }
        } else {
            binding.prefsAutoSms.isChecked = !isChecked
            prefs.silentSms = !isChecked
        }
    }

    private fun initAutoSmsPrefs() {
        binding.prefsAutoSms.setOnClickListener { changeAutoSmsPrefs() }
        binding.prefsAutoSms.isChecked = prefs.silentSms
    }

    private fun changeUnlockPrefs() {
        val isChecked = binding.prefsUnlock.isChecked
        binding.prefsUnlock.isChecked = !isChecked
        prefs.unlockScreen = !isChecked
    }

    private fun initUnlockPrefs() {
        binding.prefsUnlock.setOnClickListener { changeUnlockPrefs() }
        binding.prefsUnlock.isChecked = prefs.unlockScreen
    }

    private fun showTtsLocaleDialog() {
        val builder = AlertDialog.Builder(context!!, R.style.HomeDarkDialog)
        builder.setTitle(getString(R.string.language))
        val locale = prefs.ttsLocale
        mItemSelect = language.getLocalePosition(locale)
        builder.setSingleChoiceItems(localeAdapter, mItemSelect) { _, which -> mItemSelect = which }
        builder.setPositiveButton(getString(R.string.ok)) { dialog, _ ->
            saveTtsLocalePrefs()
            dialog.dismiss()
        }
        builder.setNegativeButton(R.string.cancel) { dialog, _ ->
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.setOnCancelListener { mItemSelect = 0 }
        dialog.setOnDismissListener { mItemSelect = 0 }
        dialog.show()
    }

    private fun showTtsLocale() {
        val locale = prefs.ttsLocale
        val i = language.getLocalePosition(locale)
        binding.prefsTtsLanguage.setDetailText(language.getLocaleNames(context!!)[i])
    }

    private fun saveTtsLocalePrefs() {
        prefs.ttsLocale = language.getLocaleByPosition(mItemSelect)
        showTtsLocale()
    }

    private fun initTtsLocalePrefs() {
        binding.prefsTtsLanguage.setOnClickListener { showTtsLocaleDialog() }
        binding.prefsTtsLanguage.setDependentView(binding.prefsTts)
        showTtsLocale()
    }

    private fun changeTtsPrefs() {
        val isChecked = binding.prefsTts.isChecked
        binding.prefsTts.isChecked = !isChecked
        prefs.ttsEnabled = !isChecked
    }

    private fun initTtsPrefs() {
        binding.prefsTts.setOnClickListener { changeTtsPrefs() }
        binding.prefsTts.isChecked = prefs.ttsEnabled
    }

    private fun openNotificationsSettings() {
        if (Module.isNougat) {
            val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
            activity?.startActivityForResult(intent, 1248)
        }
    }

    private fun showLoudnessDialog() {
        if (!SuperUtil.hasVolumePermission(context!!)) {
            openNotificationsSettings()
            return
        }
        val builder = AlertDialog.Builder(context!!, R.style.HomeDarkDialog)
        builder.setTitle(R.string.volume)
        val b = DialogWithSeekAndTitleBinding.inflate(layoutInflater)
        b.seekBar.max = 25
        b.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                b.titleView.text = progress.toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {

            }
        })
        val loudness = prefs.loudness
        b.seekBar.progress = loudness
        b.titleView.text = loudness.toString()
        builder.setView(b.root)
        builder.setPositiveButton(R.string.ok) { _, _ ->
            prefs.loudness = b.seekBar.progress
            showLoudness()
        }
        builder.setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
        val dialog = builder.create()
        dialog.show()
        Dialogues.setFullWidthDialog(dialog, activity!!)
    }

    private fun initLoudnessPrefs() {
        binding.prefsVolume.setOnClickListener { showLoudnessDialog() }
        showLoudness()
    }

    private fun showLoudness() {
        binding.prefsVolume.setDetailText(String.format(Locale.getDefault(), getString(R.string.volume) + " %d",
                prefs.loudness))
    }

    private fun initMelodyPrefs() {
        binding.prefsMelody.setOnClickListener { showSoundDialog() }
        showMelody()
    }

    private fun showMelody() {
        val filePath = prefs.melody
        if (filePath == "" || filePath.matches(Module.DEFAULT.toRegex())) {
            binding.prefsMelody.setDetailText(resources.getString(R.string.default_string))
        } else if (!filePath.matches("".toRegex())) {
            val sound = File(filePath)
            val fileName = sound.name
            val pos = fileName.lastIndexOf(".")
            val fileNameS = fileName.substring(0, pos)
            binding.prefsMelody.setDetailText(fileNameS)
        } else {
            binding.prefsMelody.setDetailText(resources.getString(R.string.default_string))
        }
    }

    private fun showSoundDialog() {
        val builder = AlertDialog.Builder(context!!, R.style.HomeDarkDialog)
        builder.setCancelable(true)
        builder.setTitle(getString(R.string.melody))
        val types = arrayOf(getString(R.string.default_string), getString(R.string.select_file))
        val adapter = ArrayAdapter(context!!, android.R.layout.simple_list_item_single_choice, types)
        mItemSelect = if (prefs.melody == "" || prefs.melody.matches(Module.DEFAULT.toRegex())) {
            0
        } else {
            1
        }
        builder.setSingleChoiceItems(adapter, mItemSelect) { _, which -> mItemSelect = which }
        builder.setPositiveButton(getString(R.string.ok)) { dialog, _ ->
            if (mItemSelect == 0) {
                prefs.melody = Module.DEFAULT
                showMelody()
            } else {
                dialog.dismiss()
                startActivityForResult(Intent(context, FileExplorerActivity::class.java), MELODY_CODE)
            }
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.setOnCancelListener { mItemSelect = 0 }
        dialog.setOnDismissListener { mItemSelect = 0 }
        dialog.show()
    }

    private fun changeInfiniteSoundPrefs() {
        val isChecked = binding.prefsMelodyRepeat.isChecked
        binding.prefsMelodyRepeat.isChecked = !isChecked
        prefs.repeatMelody = !isChecked
    }

    private fun initInfiniteSoundPrefs() {
        binding.prefsMelodyRepeat.setOnClickListener { changeInfiniteSoundPrefs() }
        binding.prefsMelodyRepeat.isChecked = prefs.repeatMelody
    }

    private fun changeSoundPrefs() {
        val isChecked = binding.prefsSilent.isChecked
        binding.prefsSilent.isChecked = !isChecked
        prefs.soundInSilent = !isChecked
        if (!SuperUtil.checkNotificationPermission(activity!!)) {
            SuperUtil.askNotificationPermission(activity!!)
        } else {
            Permissions.ensurePermissions(activity!!, PERM_BT, Permissions.BLUETOOTH)
        }
    }

    private fun initSoundInSilentModePrefs() {
        binding.prefsSilent.setOnClickListener { changeSoundPrefs() }
        binding.prefsSilent.isChecked = prefs.soundInSilent
    }

    private fun changeInfiniteVibratePrefs() {
        val isChecked = binding.prefsVibrateInfinite.isChecked
        binding.prefsVibrateInfinite.isChecked = !isChecked
        prefs.infiniteVibration = !isChecked
    }

    private fun initInfiniteVibratePrefs() {
        binding.prefsVibrateInfinite.setOnClickListener { changeInfiniteVibratePrefs() }
        binding.prefsVibrateInfinite.isChecked = prefs.infiniteVibration
        binding.prefsVibrateInfinite.setDependentView(binding.prefsVibrate)
    }

    private fun changeVibratePrefs() {
        val isChecked = binding.prefsVibrate.isChecked
        binding.prefsVibrate.isChecked = !isChecked
        prefs.vibrate = !isChecked
    }

    private fun initVibratePrefs() {
        binding.prefsVibrate.setOnClickListener { changeVibratePrefs() }
        binding.prefsVibrate.isChecked = prefs.vibrate
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            MELODY_CODE -> if (resultCode == Activity.RESULT_OK) {
                val filePath = data?.getStringExtra(Module.FILE_PICKED)
                if (filePath != null) {
                    val file = File(filePath)
                    if (file.exists()) {
                        prefs.melody = file.toString()
                    }
                }
                showMelody()
            }
            Module.ACTION_REQUEST_GALLERY -> if (resultCode == Activity.RESULT_OK) {
                val selectedImage = data?.data
                if (selectedImage != null) {
                    prefs.reminderImage = selectedImage.toString()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (Permissions.isAllGranted(grantResults)) {
            when (requestCode) {
                PERM_AUTO_CALL -> changeAutoCallPrefs()
                PERM_AUTO_SMS -> changeAutoSmsPrefs()
            }
        }
    }

    companion object {

        private const val MELODY_CODE = 125
        private const val PERM_BT = 1425
        private const val PERM_SD = 1426
        private const val PERM_AUTO_CALL = 1427
        private const val PERM_AUTO_SMS = 1428
    }
}
