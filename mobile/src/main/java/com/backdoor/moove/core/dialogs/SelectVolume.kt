package com.backdoor.moove.core.dialogs

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView

import com.backdoor.moove.R
import com.backdoor.moove.core.consts.Constants
import com.backdoor.moove.core.consts.Prefs
import com.backdoor.moove.core.helper.Coloring
import com.backdoor.moove.core.helper.SharedPrefs

class SelectVolume : Activity() {

    private var radiusValue: TextView? = null
    private var volumeImage: ImageView? = null
    private var volume: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val cs = Coloring(this@SelectVolume)
        setTheme(cs.dialogStyle)
        setContentView(R.layout.volume_dialog_layout)
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        findViewById<View>(R.id.windowBackground).setBackgroundColor(cs.backgroundStyle)

        radiusValue = findViewById(R.id.radiusValue)
        volumeImage = findViewById(R.id.volumeImage)

        val radiusBar = findViewById<SeekBar>(R.id.radiusBar)
        val n = SharedPrefs.getInstance(this)!!.loadInt(Prefs.VOLUME)
        radiusBar.progress = n
        radiusValue!!.text = n.toString()
        setValue(n)
        radiusBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                volume = i
                radiusValue!!.text = i.toString()
                setValue(i)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {

            }
        })

        val aboutClose = findViewById<TextView>(R.id.aboutClose)
        aboutClose.setOnClickListener { v ->
            val intent = Intent()
            intent.putExtra(Constants.SELECTED_VOLUME, volume)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }

    private fun setValue(i: Int) {
        if (i < 7 && i > 0) {
            volumeImage!!.setImageResource(R.drawable.ic_volume_mute_white_24dp)
        } else if (i > 18) {
            volumeImage!!.setImageResource(R.drawable.ic_volume_up_white_24dp)
        } else if (i == 0) {
            volumeImage!!.setImageResource(R.drawable.ic_volume_off_white_24dp)
        } else {
            volumeImage!!.setImageResource(R.drawable.ic_volume_down_white_24dp)
        }
    }
}