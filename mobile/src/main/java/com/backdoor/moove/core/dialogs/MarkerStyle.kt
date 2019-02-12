package com.backdoor.moove.core.dialogs

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView

import com.backdoor.moove.R
import com.backdoor.moove.core.consts.Prefs
import com.backdoor.moove.core.helper.Coloring
import com.backdoor.moove.core.helper.SharedPrefs

class MarkerStyle : Activity(), View.OnClickListener {
    private var red: RadioButton? = null
    private var green: RadioButton? = null
    private var blue: RadioButton? = null
    private var yellow: RadioButton? = null
    private var greenLight: RadioButton? = null
    private var blueLight: RadioButton? = null
    private var grey: RadioButton? = null
    private var purple: RadioButton? = null
    private var brown: RadioButton? = null
    private var orange: RadioButton? = null
    private var pink: RadioButton? = null
    private var teal: RadioButton? = null
    private var deepPurple: RadioButton? = null
    private var deepOrange: RadioButton? = null
    private var indigo: RadioButton? = null
    private var lime: RadioButton? = null
    private var themeGroup: RadioGroup? = null
    private var themeGroup2: RadioGroup? = null
    private var themeGroup3: RadioGroup? = null
    private var themeGroup4: RadioGroup? = null

    private val listener1 = RadioGroup.OnCheckedChangeListener { group, checkedId ->
        if (checkedId != -1) {
            themeGroup2!!.setOnCheckedChangeListener(null)
            themeGroup3!!.setOnCheckedChangeListener(null)
            themeGroup4!!.setOnCheckedChangeListener(null)
            themeGroup2!!.clearCheck()
            themeGroup3!!.clearCheck()
            themeGroup4!!.clearCheck()
            themeGroup2!!.setOnCheckedChangeListener(listener2)
            themeGroup3!!.setOnCheckedChangeListener(listener3)
            themeGroup4!!.setOnCheckedChangeListener(listener4)
            themeColorSwitch(group.checkedRadioButtonId)
        }
    }
    private val listener2 = RadioGroup.OnCheckedChangeListener { group, checkedId ->
        if (checkedId != -1) {
            themeGroup!!.setOnCheckedChangeListener(null)
            themeGroup3!!.setOnCheckedChangeListener(null)
            themeGroup4!!.setOnCheckedChangeListener(null)
            themeGroup!!.clearCheck()
            themeGroup3!!.clearCheck()
            themeGroup4!!.clearCheck()
            themeGroup!!.setOnCheckedChangeListener(listener1)
            themeGroup3!!.setOnCheckedChangeListener(listener3)
            themeGroup4!!.setOnCheckedChangeListener(listener4)
            themeColorSwitch(group.checkedRadioButtonId)
        }
    }
    private val listener3 = RadioGroup.OnCheckedChangeListener { group, checkedId ->
        if (checkedId != -1) {
            themeGroup!!.setOnCheckedChangeListener(null)
            themeGroup2!!.setOnCheckedChangeListener(null)
            themeGroup4!!.setOnCheckedChangeListener(null)
            themeGroup!!.clearCheck()
            themeGroup2!!.clearCheck()
            themeGroup4!!.clearCheck()
            themeGroup!!.setOnCheckedChangeListener(listener1)
            themeGroup2!!.setOnCheckedChangeListener(listener2)
            themeGroup4!!.setOnCheckedChangeListener(listener4)
            themeColorSwitch(group.checkedRadioButtonId)
        }
    }
    private val listener4 = RadioGroup.OnCheckedChangeListener { group, checkedId ->
        if (checkedId != -1) {
            themeGroup!!.setOnCheckedChangeListener(null)
            themeGroup2!!.setOnCheckedChangeListener(null)
            themeGroup3!!.setOnCheckedChangeListener(null)
            themeGroup!!.clearCheck()
            themeGroup2!!.clearCheck()
            themeGroup3!!.clearCheck()
            themeGroup!!.setOnCheckedChangeListener(listener1)
            themeGroup2!!.setOnCheckedChangeListener(listener2)
            themeGroup3!!.setOnCheckedChangeListener(listener3)
            themeColorSwitch(group.checkedRadioButtonId)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val cs = Coloring(this@MarkerStyle)
        setTheme(cs.dialogStyle)
        setContentView(R.layout.marker_style_layout)
        findViewById<View>(R.id.windowBackground).setBackgroundColor(cs.backgroundStyle)
        val themeClose = findViewById<TextView>(R.id.themeClose)
        themeClose.setOnClickListener(this)

        red = findViewById(R.id.redCheck)
        green = findViewById(R.id.greenCheck)
        blue = findViewById(R.id.blueCheck)
        yellow = findViewById(R.id.yellowCheck)
        greenLight = findViewById(R.id.greenLightCheck)
        blueLight = findViewById(R.id.blueLightCheck)
        grey = findViewById(R.id.greyCheck)
        purple = findViewById(R.id.purpleCheck)
        brown = findViewById(R.id.brownCheck)
        orange = findViewById(R.id.orangeCheck)
        pink = findViewById(R.id.pinkCheck)
        teal = findViewById(R.id.tealCheck)
        deepPurple = findViewById(R.id.deep_purple)
        deepOrange = findViewById(R.id.deep_orange)
        indigo = findViewById(R.id.indigo)
        lime = findViewById(R.id.lime)

        themeGroup = findViewById(R.id.themeGroup)
        themeGroup2 = findViewById(R.id.themeGroup2)
        themeGroup3 = findViewById(R.id.themeGroup3)
        themeGroup4 = findViewById(R.id.themeGroup4)

        themeGroup!!.clearCheck()
        themeGroup2!!.clearCheck()
        themeGroup3!!.clearCheck()
        themeGroup4!!.clearCheck()
        themeGroup!!.setOnCheckedChangeListener(listener1)
        themeGroup2!!.setOnCheckedChangeListener(listener2)
        themeGroup3!!.setOnCheckedChangeListener(listener3)
        themeGroup4!!.setOnCheckedChangeListener(listener4)

        setUpRadio()
    }

    fun setUpRadio() {
        val loaded = SharedPrefs.getInstance(this)!!.loadInt(Prefs.MARKER_STYLE)
        when (loaded) {
            0 -> red!!.isChecked = true
            1 -> green!!.isChecked = true
            2 -> blue!!.isChecked = true
            3 -> yellow!!.isChecked = true
            4 -> greenLight!!.isChecked = true
            5 -> blueLight!!.isChecked = true
            6 -> grey!!.isChecked = true
            7 -> purple!!.isChecked = true
            8 -> orange!!.isChecked = true
            9 -> pink!!.isChecked = true
            10 -> teal!!.isChecked = true
            11 -> brown!!.isChecked = true
            12 -> deepPurple!!.isChecked = true
            13 -> deepOrange!!.isChecked = true
            14 -> indigo!!.isChecked = true
            15 -> lime!!.isChecked = true
        }
    }

    private fun themeColorSwitch(radio: Int) {
        when (radio) {
            R.id.redCheck -> saveColor(0)
            R.id.greenCheck -> saveColor(1)
            R.id.blueCheck -> saveColor(2)
            R.id.yellowCheck -> saveColor(3)
            R.id.greenLightCheck -> saveColor(4)
            R.id.blueLightCheck -> saveColor(5)
            R.id.greyCheck -> saveColor(6)
            R.id.purpleCheck -> saveColor(7)
            R.id.orangeCheck -> saveColor(8)
            R.id.pinkCheck -> saveColor(9)
            R.id.tealCheck -> saveColor(10)
            R.id.brownCheck -> saveColor(11)
            R.id.deep_purple -> saveColor(12)
            R.id.deep_orange -> saveColor(13)
            R.id.indigo -> saveColor(14)
            R.id.lime -> saveColor(15)
        }
    }

    internal fun saveColor(style: Int) {
        SharedPrefs.getInstance(this)!!.saveInt(Prefs.MARKER_STYLE, style)
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.themeClose -> finish()
        }
    }
}