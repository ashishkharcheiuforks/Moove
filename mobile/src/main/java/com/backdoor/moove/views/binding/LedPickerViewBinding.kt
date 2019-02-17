package com.backdoor.moove.views.binding

import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import com.backdoor.moove.R

class LedPickerViewBinding(view: View) : Binding(view) {
    val hintIcon: View by bindView(R.id.hintIcon)
    val ledGroup: RadioGroup by bindView(R.id.ledGroup)
    val ledRed: RadioButton by bindView(R.id.ledRed)
    val ledGreen: RadioButton by bindView(R.id.ledGreen)
    val ledBlue: RadioButton by bindView(R.id.ledBlue)
    val ledYellow: RadioButton by bindView(R.id.ledYellow)
    val ledPink: RadioButton by bindView(R.id.ledPink)
    val ledOrange: RadioButton by bindView(R.id.ledOrange)
    val ledTeal: RadioButton by bindView(R.id.ledTeal)
}