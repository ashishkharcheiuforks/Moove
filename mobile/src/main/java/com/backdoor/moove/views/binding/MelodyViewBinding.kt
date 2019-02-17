package com.backdoor.moove.views.binding

import android.view.View
import android.widget.TextView
import com.backdoor.moove.R

class MelodyViewBinding(view: View) : Binding(view) {
    val removeButton: View by bindView(R.id.removeButton)
    val text: TextView by bindView(R.id.text)
    val hintIcon: View by bindView(R.id.hintIcon)
}