package com.backdoor.moove.core.dialogs

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView

import com.backdoor.moove.R
import com.backdoor.moove.core.consts.Constants
import com.backdoor.moove.core.consts.LED
import com.backdoor.moove.core.helper.Coloring
import com.backdoor.moove.core.helper.Messages
import com.backdoor.moove.core.helper.Notifier

class LedColor : Activity() {

    private var musicList: ListView? = null
    private var mNotifyMgr: NotificationManagerCompat? = null
    private var builder: NotificationCompat.Builder? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val cs = Coloring(this@LedColor)
        setTheme(cs.dialogStyle)
        setContentView(R.layout.music_list_dilog)
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        findViewById<View>(R.id.windowBackground).setBackgroundColor(cs.backgroundStyle)
        val dialogTitle = findViewById<TextView>(R.id.dialogTitle)
        dialogTitle.text = getString(R.string.led_color)

        musicList = findViewById(R.id.musicList)
        musicList!!.choiceMode = AbsListView.CHOICE_MODE_SINGLE

        val colors = arrayOfNulls<String>(LED.NUM_OF_LEDS)
        for (i in 0 until LED.NUM_OF_LEDS) {
            colors[i] = LED.getTitle(this, i)
        }

        val adapter = ArrayAdapter<String>(this@LedColor,
                android.R.layout.simple_list_item_single_choice, colors)
        musicList!!.adapter = adapter

        musicList!!.setOnItemClickListener { adapterView, view, i, l ->
            if (i != -1) {
                Messages.toast(this@LedColor, getString(R.string.turn_screen_off_to_see_led_light))
                showLED(LED.getLED(i))
            }
        }

        val musicDialogOk = findViewById<TextView>(R.id.musicDialogOk)
        musicDialogOk.setOnClickListener { v ->
            val position = musicList!!.checkedItemPosition
            if (position != -1) {
                mNotifyMgr = NotificationManagerCompat.from(this@LedColor)
                mNotifyMgr!!.cancel(1)
                val i = Intent()
                i.putExtra(Constants.SELECTED_LED_COLOR, position)
                setResult(Activity.RESULT_OK, i)
                finish()
            } else {
                Messages.toast(this@LedColor, getString(R.string.select_one_of_item))
            }
        }
    }

    private fun showLED(color: Int) {
        mNotifyMgr = NotificationManagerCompat.from(this@LedColor)
        mNotifyMgr!!.cancel(1)
        builder = NotificationCompat.Builder(this@LedColor, Notifier.CHANNEL_SYSTEM)
        builder!!.setLights(color, 500, 1000)
        mNotifyMgr = NotificationManagerCompat.from(this@LedColor)
        Handler().postDelayed({ mNotifyMgr!!.notify(1, builder!!.build()) }, 3000)
    }
}
