package com.backdoor.moove.utils

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.SeekBar
import android.widget.TextView
import com.backdoor.moove.R
import com.backdoor.moove.core.consts.Constants
import com.backdoor.moove.core.consts.Language
import com.backdoor.moove.core.helper.Module
import com.backdoor.moove.databinding.DialogBottomColorSliderBinding
import com.backdoor.moove.databinding.DialogBottomSeekAndTitleBinding
import com.google.android.gms.maps.GoogleMap
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.util.*

class Dialogues(val prefs: Prefs) {

    private var mSelectedItem = 0

    fun showColorBottomDialog(activity: Activity, current: Int, colors: IntArray, onChange: (Int) -> Unit) {
        val dialog = BottomSheetDialog(activity)
        val b = DialogBottomColorSliderBinding.inflate(LayoutInflater.from(activity))
        b.colorSlider.setColors(colors)
        b.colorSlider.setSelectorColorResource(R.color.whitePrimary)
        b.colorSlider.setSelection(current)
        b.colorSlider.setListener { i, _ ->
            onChange.invoke(i)
        }
        dialog.setContentView(b.root)
        dialog.show()
    }

    fun showRadiusBottomDialog(activity: Activity, current: Int, listener: (Int) -> String) {
        val dialog = BottomSheetDialog(activity)
        val b = DialogBottomSeekAndTitleBinding.inflate(LayoutInflater.from(activity))
        b.seekBar.max = MAX_DEF_RADIUS
        if (b.seekBar.max < current && b.seekBar.max < MAX_RADIUS) {
            b.seekBar.max = (current + (b.seekBar.max * 0.2)).toInt()
        }
        if (current > MAX_RADIUS) {
            b.seekBar.max = MAX_RADIUS
        }
        b.seekBar.max = current * 2
        if (current == 0) {
            b.seekBar.max = MAX_DEF_RADIUS
        }
        b.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                b.titleView.text = listener.invoke(progress)
                val perc = progress.toFloat() / b.seekBar.max.toFloat() * 100f
                if (perc > 95f && b.seekBar.max < MAX_RADIUS) {
                    b.seekBar.max = (b.seekBar.max + (b.seekBar.max * 0.2)).toInt()
                } else if (perc < 10f && b.seekBar.max > 5000) {
                    b.seekBar.max = (b.seekBar.max - (b.seekBar.max * 0.2)).toInt()
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {

            }
        })
        b.seekBar.progress = current
        b.titleView.text = listener.invoke(current)
        dialog.setContentView(b.root)
        dialog.show()
    }

    fun imageDialog(context: Activity, listener: DialogInterface.OnDismissListener) {
        val builder = AlertDialog.Builder(context)
        builder.setCancelable(true)
        builder.setTitle(context.getString(R.string.background_image))
        val types = arrayOf(context.getString(R.string.none), context.getString(R.string.default_string), context.getString(R.string.select_image))

        val adapter = ArrayAdapter(context,
                android.R.layout.simple_list_item_single_choice, types)

        val image = prefs.reminderImage
        mSelectedItem = when {
            image.matches(Constants.NONE.toRegex()) -> 0
            image.matches(Constants.DEFAULT.toRegex()) -> 1
            else -> 2
        }

        builder.setSingleChoiceItems(adapter, mSelectedItem) { _, which ->
            if (which != -1) {
                mSelectedItem = which
            }
        }
        builder.setPositiveButton(context.getString(R.string.ok)) { dialog, _ ->
            dialog.dismiss()
            when (mSelectedItem) {
                0 -> prefs.reminderImage = Constants.NONE
                1 -> prefs.reminderImage = Constants.DEFAULT
                2 -> {
                    var intent = Intent(Intent.ACTION_GET_CONTENT)
                    intent.type = "image/*"
                    if (Module.isKitkat) {
                        intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                        intent.addCategory(Intent.CATEGORY_OPENABLE)
                        intent.type = "image/*"
                    }
                    val chooser = Intent.createChooser(intent, context.getString(R.string.select_image))
                    context.startActivityForResult(chooser, Constants.ACTION_REQUEST_GALLERY)
                }
            }
        }
        val dialog = builder.create()
        dialog.setOnDismissListener(listener)
        dialog.show()
    }

    fun dialogWithSeek(context: Context, progress: Int, max: Int, title: String, onSelect: (Int) -> Unit) {
        val builder = AlertDialog.Builder(context)
        builder.setCancelable(true)
        builder.setTitle(title)
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val layout = inflater.inflate(R.layout.dialog_seekbar, null)
        val textView = layout.findViewById<TextView>(R.id.seekValue)
        val seekBar = layout.findViewById<SeekBar>(R.id.dialogSeek)
        seekBar.max = max
        seekBar.progress = progress
        textView.text = progress.toString()
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                textView.text = progress.toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {

            }
        })
        builder.setView(layout)
        builder.setPositiveButton(context.getString(R.string.ok)) { dialog, _ ->
            onSelect.invoke(seekBar.progress)
            dialog.dismiss()
        }
        builder.create().show()
    }

    fun ledColor(context: Context) {
        val builder = AlertDialog.Builder(context)
        builder.setCancelable(false)
        builder.setTitle(context.getString(R.string.led_color))

        val colors = com.backdoor.moove.utils.LED.getAllNames(context)

        val adapter = ArrayAdapter<String>(context,
                android.R.layout.simple_list_item_single_choice, colors)

        mSelectedItem = prefs.ledColor

        builder.setSingleChoiceItems(adapter, mSelectedItem) { _, which ->
            if (which != -1) {
                mSelectedItem = which
            }
        }
        builder.setPositiveButton(context.getString(R.string.ok)) { dialog, _ ->
            prefs.ledColor = mSelectedItem
            dialog.dismiss()
        }
        builder.create().show()
    }

    /**
     * AlertDialog for selecting language for voice notifications (text to speech).
     *
     * @param context     application context.
     * @param prefsToSave Preference key for results saving.
     */
    fun ttsLocale(context: Context) {
        val builder = AlertDialog.Builder(context)
        builder.setCancelable(false)
        builder.setTitle(R.string.language)
        val names = ArrayList<String>()
        names.add(context.getString(R.string.english))
        names.add(context.getString(R.string.french))
        names.add(context.getString(R.string.german))
        names.add(context.getString(R.string.italian))
        names.add(context.getString(R.string.japanese))
        names.add(context.getString(R.string.korean))
        names.add(context.getString(R.string.polish))
        names.add(context.getString(R.string.russian))
        names.add(context.getString(R.string.spanish))

        val adapter = ArrayAdapter(context,
                android.R.layout.simple_list_item_single_choice, names)

        mSelectedItem = 1
        val locale = prefs.ttsLocale
        if (locale.matches(Language.ENGLISH.toRegex())) mSelectedItem = 0
        if (locale.matches(Language.FRENCH.toRegex())) mSelectedItem = 1
        if (locale.matches(Language.GERMAN.toRegex())) mSelectedItem = 2
        if (locale.matches(Language.ITALIAN.toRegex())) mSelectedItem = 3
        if (locale.matches(Language.JAPANESE.toRegex())) mSelectedItem = 4
        if (locale.matches(Language.KOREAN.toRegex())) mSelectedItem = 5
        if (locale.matches(Language.POLISH.toRegex())) mSelectedItem = 6
        if (locale.matches(Language.RUSSIAN.toRegex())) mSelectedItem = 7
        if (locale.matches(Language.SPANISH.toRegex())) mSelectedItem = 8

        builder.setSingleChoiceItems(adapter, mSelectedItem) { _, which ->
            if (which != -1) {
                mSelectedItem = which
            }
        }
        builder.setPositiveButton(context.getString(R.string.ok)) { dialog, which ->
            var locale1 = Language.ENGLISH
            if (which == 0) locale1 = Language.ENGLISH
            if (which == 1) locale1 = Language.FRENCH
            if (which == 2) locale1 = Language.GERMAN
            if (which == 3) locale1 = Language.ITALIAN
            if (which == 4) locale1 = Language.JAPANESE
            if (which == 5) locale1 = Language.KOREAN
            if (which == 6) locale1 = Language.POLISH
            if (which == 7) locale1 = Language.RUSSIAN
            if (which == 8) locale1 = Language.SPANISH
            prefs.ttsLocale = locale1
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }

    /**
     * AlertDialog for selecting map type.
     *
     * @param context application context.
     */
    fun mapType(context: Context) {
        val builder = AlertDialog.Builder(context)
        builder.setCancelable(true)
        builder.setTitle(context.getString(R.string.map_type))

        val adapter = ArrayAdapter.createFromResource(context, R.array.map_types,
                android.R.layout.simple_list_item_single_choice)

        val type = prefs.mapType
        mSelectedItem = when (type) {
            GoogleMap.MAP_TYPE_NORMAL -> 0
            GoogleMap.MAP_TYPE_SATELLITE -> 1
            GoogleMap.MAP_TYPE_HYBRID -> 2
            GoogleMap.MAP_TYPE_TERRAIN -> 3
            else -> 0
        }

        builder.setSingleChoiceItems(adapter, mSelectedItem) { _, which ->
            if (which != -1) {
                mSelectedItem = which
            }
        }
        builder.setPositiveButton(context.getString(R.string.ok)) { dialog, _ ->
            if (mSelectedItem == 0) {
                prefs.mapType = GoogleMap.MAP_TYPE_NORMAL
            } else if (mSelectedItem == 1) {
                prefs.mapType = GoogleMap.MAP_TYPE_SATELLITE
            } else if (mSelectedItem == 2) {
                prefs.mapType = GoogleMap.MAP_TYPE_HYBRID
            } else {
                prefs.mapType = GoogleMap.MAP_TYPE_TERRAIN
            }
            dialog.dismiss()
        }
        builder.create().show()
    }

    companion object {
        private const val MAX_RADIUS = 100000
        private const val MAX_DEF_RADIUS = 5000
    }
}
