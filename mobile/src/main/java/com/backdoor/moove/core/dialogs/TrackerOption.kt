package com.backdoor.moove.core.dialogs

import android.app.Activity
import android.os.Bundle
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView

import com.backdoor.moove.R
import com.backdoor.moove.core.async.DisableAsync
import com.backdoor.moove.core.consts.Prefs
import com.backdoor.moove.core.helper.Coloring
import com.backdoor.moove.core.helper.SharedPrefs

class TrackerOption : Activity() {

    private var radiusBar: SeekBar? = null
    private var timeBar: SeekBar? = null
    private var radiusValue: TextView? = null
    private var timeValue: TextView? = null
    private var sPrefs: SharedPrefs? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val cs = Coloring(this@TrackerOption)
        setTheme(cs.dialogStyle)
        setContentView(R.layout.tracker_settings_layout)
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        findViewById<View>(R.id.windowBackground).setBackgroundColor(cs.backgroundStyle)
        sPrefs = SharedPrefs.getInstance(this)

        radiusValue = findViewById(R.id.radiusValue)
        radiusValue!!.text = sPrefs!!.loadInt(Prefs.TRACK_DISTANCE).toString() + getString(R.string.m)

        radiusBar = findViewById(R.id.radiusBar)
        radiusBar!!.max = 499
        radiusBar!!.progress = sPrefs!!.loadInt(Prefs.TRACK_DISTANCE) - 1
        radiusBar!!.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                radiusValue!!.text = (i + 1).toString() + getString(R.string.m)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {

            }
        })

        timeValue = findViewById(R.id.timeValue)
        timeValue!!.text = sPrefs!!.loadInt(Prefs.TRACK_TIME).toString() + getString(R.string.s)

        timeBar = findViewById(R.id.timeBar)
        timeBar!!.max = 119
        timeBar!!.progress = sPrefs!!.loadInt(Prefs.TRACK_TIME) - 1
        timeBar!!.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                timeValue!!.text = (i + 1).toString() + getString(R.string.s)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {

            }
        })

        val aboutClose = findViewById<TextView>(R.id.aboutClose)
        aboutClose.setOnClickListener { v ->
            sPrefs!!.saveInt(Prefs.TRACK_DISTANCE, radiusBar!!.progress + 1)
            sPrefs!!.saveInt(Prefs.TRACK_TIME, timeBar!!.progress + 1)
            DisableAsync(this@TrackerOption).execute()
            finish()
        }
    }
}