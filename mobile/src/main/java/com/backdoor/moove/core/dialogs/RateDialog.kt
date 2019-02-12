package com.backdoor.moove.core.dialogs

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView

import com.backdoor.moove.R
import com.backdoor.moove.core.consts.Prefs
import com.backdoor.moove.core.helper.Coloring
import com.backdoor.moove.core.helper.Messages
import com.backdoor.moove.core.helper.SharedPrefs

class RateDialog : Activity() {

    private var sharedPrefs: SharedPrefs? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL)
        window.setFlags(WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH, WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH)
        val cs = Coloring(this@RateDialog)
        setTheme(cs.dialogStyle)
        setContentView(R.layout.rate_dialog_layout)
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        findViewById<View>(R.id.windowBackground).setBackgroundColor(cs.backgroundStyle)
        sharedPrefs = SharedPrefs.getInstance(this)

        val buttonRate = findViewById<TextView>(R.id.buttonRate)
        buttonRate.setOnClickListener { v ->
            sharedPrefs!!.saveBoolean(Prefs.RATE_SHOW, true)
            launchMarket()
            finish()
        }

        val rateLater = findViewById<TextView>(R.id.rateLater)
        rateLater.setOnClickListener { v ->
            sharedPrefs!!.saveBoolean(Prefs.RATE_SHOW, false)
            sharedPrefs!!.saveInt(Prefs.APP_RUNS_COUNT, 0)
            finish()
        }

        val rateNever = findViewById<TextView>(R.id.rateNever)
        rateNever.setOnClickListener { v ->
            sharedPrefs!!.saveBoolean(Prefs.RATE_SHOW, true)
            finish()
        }
    }

    private fun launchMarket() {
        val uri = Uri.parse("market://details?id=$packageName")
        val goToMarket = Intent(Intent.ACTION_VIEW, uri)
        try {
            startActivity(goToMarket)
        } catch (e: ActivityNotFoundException) {
            Messages.toast(this, "Couldn't launch market")
        }

    }

    override fun onBackPressed() {


    }
}