package com.backdoor.moove.core.helper

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.SeekBar
import android.widget.TextView

import com.backdoor.moove.R
import com.backdoor.moove.core.consts.Constants
import com.backdoor.moove.core.consts.LED
import com.backdoor.moove.core.consts.Language
import com.backdoor.moove.core.consts.Prefs
import com.backdoor.moove.core.file_explorer.FileExplorerActivity
import com.google.android.gms.maps.GoogleMap

import java.util.ArrayList

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
object Dialogues {

    /**
     * AlertDialog for selecting application screen orientation.
     *
     * @param context  application context.
     * @param listener listener for Dialog.
     */
    fun imageDialog(context: Activity, listener: DialogInterface.OnDismissListener) {
        val builder = AlertDialog.Builder(context)
        builder.setCancelable(true)
        builder.setTitle(context.getString(R.string.background_image))
        val types = arrayOf(context.getString(R.string.none), context.getString(R.string.default_string), context.getString(R.string.select_image))

        val prefs = SharedPrefs.getInstance(context)

        val adapter = ArrayAdapter(context,
                android.R.layout.simple_list_item_single_choice, types)

        val image = if (prefs != null) prefs.loadPrefs(Prefs.REMINDER_IMAGE) else ""
        val selection: Int
        if (image!!.matches(Constants.NONE.toRegex())) {
            selection = 0
        } else if (image.matches(Constants.DEFAULT.toRegex())) {
            selection = 1
        } else {
            selection = 2
        }

        builder.setSingleChoiceItems(adapter, selection) { dialog, which ->
            if (which != -1) {
                dialog.dismiss()
                val prefs1 = SharedPrefs.getInstance(context)
                if (which == 0) {
                    prefs1?.savePrefs(Prefs.REMINDER_IMAGE, Constants.NONE)
                } else if (which == 1) {
                    prefs1?.savePrefs(Prefs.REMINDER_IMAGE, Constants.DEFAULT)
                } else if (which == 2) {
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
        builder.setPositiveButton(context.getString(R.string.ok)) { dialog, which ->
            dialog.dismiss()
            val prefs12 = SharedPrefs.getInstance(context)
            if (which == 0) {
                prefs12?.savePrefs(Prefs.REMINDER_IMAGE, Constants.NONE)
            } else if (which == 1) {
                prefs12?.savePrefs(Prefs.REMINDER_IMAGE, Constants.DEFAULT)
            } else if (which == 2) {
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
        val dialog = builder.create()
        dialog.setOnDismissListener(listener)
        dialog.show()
    }

    /**
     * Create and AlertDialog with customizable seekbar.
     *
     * @param context  Application context.
     * @param max      seekbar maximum.
     * @param prefs    Preference key for saving result.
     * @param title    title for Dialog.
     * @param listener Dialog action listener.
     */
    fun dialogWithSeek(context: Context, max: Int, prefs: String, title: String,
                       listener: DialogInterface.OnDismissListener) {
        val builder = AlertDialog.Builder(context)
        builder.setCancelable(true)
        builder.setTitle(title)
        val sharedPrefs = SharedPrefs.getInstance(context)
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val layout = inflater.inflate(R.layout.dialog_seekbar, null)
        val textView = layout.findViewById<TextView>(R.id.seekValue)
        val seekBar = layout.findViewById<SeekBar>(R.id.dialogSeek)
        seekBar.max = max
        val progress = sharedPrefs?.loadInt(prefs) ?: 0
        seekBar.progress = progress
        textView.text = progress.toString()
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                textView.text = progress.toString()
                sharedPrefs?.saveInt(prefs, progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {

            }
        })
        builder.setView(layout)
        builder.setPositiveButton(context.getString(R.string.ok)) { dialog, which -> dialog.dismiss() }
        val dialog = builder.create()
        dialog.setOnDismissListener(listener)
        dialog.show()
    }

    /**
     * AlertDialog for selecting type of melody - system or custom file.
     *
     * @param context     application context.
     * @param prefsToSave Preference key to save result.
     */
    fun melodyType(context: Activity, prefsToSave: String,
                   requestCode: Int) {
        val builder = AlertDialog.Builder(context)
        builder.setCancelable(true)
        builder.setTitle(context.getString(R.string.melody))
        val types = arrayOf(context.getString(R.string.default_string), context.getString(R.string.select_file))

        val adapter = ArrayAdapter(context,
                android.R.layout.simple_list_item_single_choice, types)

        val prefs = SharedPrefs.getInstance(context)
        val position: Int
        if (prefs != null && !prefs.loadBoolean(prefsToSave)) {
            position = 0
        } else {
            position = 1
        }

        builder.setSingleChoiceItems(adapter, position) { dialog, which ->
            if (which != -1) {
                val prefs1 = SharedPrefs.getInstance(context)
                if (which == 0) {
                    prefs1?.saveBoolean(prefsToSave, false)
                } else {
                    prefs1?.saveBoolean(prefsToSave, true)
                    dialog.dismiss()
                    context.startActivityForResult(Intent(context, FileExplorerActivity::class.java), requestCode)
                }
            }
        }
        builder.setPositiveButton(context.getString(R.string.ok)) { dialog, which ->
            val prefs12 = SharedPrefs.getInstance(context)
            if (prefs12 != null && !prefs12.loadBoolean(prefsToSave)) {
                dialog.dismiss()
            }
        }
        val dialog = builder.create()
        dialog.show()
    }

    /**
     * AlertDialog for selecting LED indicator color for events.
     *
     * @param context application context.
     */
    fun ledColor(context: Context) {
        val builder = AlertDialog.Builder(context)
        builder.setCancelable(false)
        builder.setTitle(context.getString(R.string.led_color))

        val colors = arrayOfNulls<String>(LED.NUM_OF_LEDS)
        for (i in 0 until LED.NUM_OF_LEDS) {
            colors[i] = LED.getTitle(context, i)
        }

        val adapter = ArrayAdapter<String>(context,
                android.R.layout.simple_list_item_single_choice, colors)

        val prefs = SharedPrefs.getInstance(context)
        val position = prefs?.loadInt(Prefs.LED_COLOR) ?: 0

        builder.setSingleChoiceItems(adapter, position) { dialog, which ->
            if (which != -1) {
                val prefs1 = SharedPrefs.getInstance(context)
                prefs1?.saveInt(Prefs.LED_COLOR, which)
            }
        }
        builder.setPositiveButton(context.getString(R.string.ok)) { dialog, which -> dialog.dismiss() }
        val dialog = builder.create()
        dialog.show()
    }

    /**
     * AlertDialog for selecting language for voice notifications (text to speech).
     *
     * @param context     application context.
     * @param prefsToSave Preference key for results saving.
     */
    fun ttsLocale(context: Context, prefsToSave: String) {
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

        val prefs = SharedPrefs.getInstance(context)
        var position = 1
        val locale = if (prefs != null) prefs.loadPrefs(prefsToSave) else Language.ENGLISH
        if (locale!!.matches(Language.ENGLISH.toRegex())) position = 0
        if (locale.matches(Language.FRENCH.toRegex())) position = 1
        if (locale.matches(Language.GERMAN.toRegex())) position = 2
        if (locale.matches(Language.ITALIAN.toRegex())) position = 3
        if (locale.matches(Language.JAPANESE.toRegex())) position = 4
        if (locale.matches(Language.KOREAN.toRegex())) position = 5
        if (locale.matches(Language.POLISH.toRegex())) position = 6
        if (locale.matches(Language.RUSSIAN.toRegex())) position = 7
        if (locale.matches(Language.SPANISH.toRegex())) position = 8

        builder.setSingleChoiceItems(adapter, position) { dialog, which ->
            if (which != -1) {
                val prefs1 = SharedPrefs.getInstance(context)
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
                prefs1?.savePrefs(prefsToSave, locale1)
            }
        }
        builder.setPositiveButton(context.getString(R.string.ok)) { dialog, which -> dialog.dismiss() }
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

        val prefs = SharedPrefs.getInstance(context)
        val type = prefs?.loadInt(Prefs.MAP_TYPE) ?: GoogleMap.MAP_TYPE_NORMAL
        val position: Int
        if (type == GoogleMap.MAP_TYPE_NORMAL) {
            position = 0
        } else if (type == GoogleMap.MAP_TYPE_SATELLITE) {
            position = 1
        } else if (type == GoogleMap.MAP_TYPE_HYBRID) {
            position = 2
        } else if (type == GoogleMap.MAP_TYPE_TERRAIN) {
            position = 3
        } else {
            position = 0
        }

        builder.setSingleChoiceItems(adapter, position) { dialog, which ->
            if (which != -1) {
                val prefs1 = SharedPrefs.getInstance(context)
                if (prefs1 == null) return@builder.setSingleChoiceItems
                if (which == 0) {
                    prefs1!!.saveInt(Prefs.MAP_TYPE, GoogleMap.MAP_TYPE_NORMAL)
                } else if (which == 1) {
                    prefs1!!.saveInt(Prefs.MAP_TYPE, GoogleMap.MAP_TYPE_SATELLITE)
                } else if (which == 2) {
                    prefs1!!.saveInt(Prefs.MAP_TYPE, GoogleMap.MAP_TYPE_HYBRID)
                } else {
                    prefs1!!.saveInt(Prefs.MAP_TYPE, GoogleMap.MAP_TYPE_TERRAIN)
                }
            }
        }
        builder.setPositiveButton(context.getString(R.string.ok)) { dialog, which -> dialog.dismiss() }
        val dialog = builder.create()
        dialog.show()
    }
}
