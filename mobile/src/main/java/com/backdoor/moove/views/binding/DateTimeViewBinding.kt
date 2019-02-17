package com.backdoor.moove.views.binding

import android.view.View
import android.widget.TextView
import com.backdoor.moove.R

class DateTimeViewBinding(view: View) : Binding(view) {
    val dateField: TextView by bindView(R.id.dateField)
    val timeField: TextView by bindView(R.id.timeField)
}