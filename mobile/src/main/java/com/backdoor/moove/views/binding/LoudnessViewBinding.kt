package com.backdoor.moove.views.binding

import android.view.View
import android.widget.SeekBar
import android.widget.TextView
import com.backdoor.moove.R

class LoudnessViewBinding(view: View) : Binding(view) {
    val hintIcon: View by bindView(R.id.hintIcon)
    val labelView: TextView by bindView(R.id.labelView)
    val sliderView: SeekBar by bindView(R.id.sliderView)
}