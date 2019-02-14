package com.backdoor.moove.core.dialogs

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.SeekBar
import android.widget.TextView

import com.backdoor.moove.R
import com.backdoor.moove.core.consts.Constants
import com.backdoor.moove.core.consts.Prefs
import com.backdoor.moove.utils.Coloring
import com.backdoor.moove.core.helper.SharedPrefs

class TargetRadius : Activity() {

    private var radiusBar: SeekBar? = null
    private var radiusValue: TextView? = null
    private var progressInt: Int = 0
    private var i: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val cs = Coloring(this@TargetRadius)
        setTheme(cs.dialogStyle)
        setContentView(R.layout.radius_dialog_layout)
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        findViewById<View>(R.id.windowBackground).setBackgroundColor(cs.backgroundStyle)
        val intent = intent
        i = intent.getIntExtra("item", 0)
        radiusValue = findViewById(R.id.radiusValue)
        progressInt = SharedPrefs.getInstance(this)!!.loadInt(Prefs.LOCATION_RADIUS)
        radiusValue!!.text = progressInt.toString() + getString(R.string.m)

        radiusBar = findViewById(R.id.radiusBar)
        radiusBar!!.progress = progressInt
        radiusBar!!.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                progressInt = i
                radiusValue!!.text = progressInt.toString() + getString(R.string.m)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {

            }
        })

        val plusButton = findViewById<Button>(R.id.plusButton)
        plusButton.setOnClickListener { view -> radiusBar!!.progress = progressInt + 1 }

        val minusButton = findViewById<Button>(R.id.minusButton)
        minusButton.setOnClickListener { v -> radiusBar!!.progress = progressInt - 1 }

        val transportCheck = findViewById<CheckBox>(R.id.transportCheck)
        transportCheck.visibility = View.VISIBLE
        if (progressInt > 2000) {
            transportCheck.isChecked = true
        }
        if (transportCheck.isChecked) {
            radiusBar!!.max = 5000
            radiusBar!!.progress = progressInt
        } else {
            radiusBar!!.max = 2000
            radiusBar!!.progress = progressInt
        }

        transportCheck.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                radiusBar!!.max = 5000
                radiusBar!!.progress = progressInt
            } else {
                radiusBar!!.max = 2000
                radiusBar!!.progress = progressInt
            }
        }

        val aboutClose = findViewById<TextView>(R.id.aboutClose)
        aboutClose.setOnClickListener { v ->
            if (i == 0) {
                SharedPrefs.getInstance(this@TargetRadius)!!.saveInt(Prefs.LOCATION_RADIUS, radiusBar!!.progress)
                finish()
            } else {
                val intent1 = Intent()
                intent1.putExtra(Constants.SELECTED_RADIUS, radiusBar!!.progress)
                setResult(Activity.RESULT_OK, intent1)
                finish()
            }
        }
    }
}