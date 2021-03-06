package com.backdoor.moove.views.binding

import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.SwitchCompat
import com.backdoor.moove.R

class PrefsViewBinding(view: View) : Binding(view) {
    val dividerTop: View by bindView(R.id.dividerTop)
    val dividerBottom: View by bindView(R.id.dividerBottom)
    val iconView: AppCompatImageView by bindView(R.id.iconView)
    val itemsContainer: View by bindView(R.id.itemsContainer)
    val viewContainer: View by bindView(R.id.viewContainer)
    val prefsPrimaryText: TextView by bindView(R.id.prefsPrimaryText)
    val prefsSecondaryText: TextView by bindView(R.id.prefsSecondaryText)
    val prefsCheck: CheckBox by bindView(R.id.prefsCheck)
    val prefsSwitch: SwitchCompat by bindView(R.id.prefsSwitch)
    val prefsValue: TextView by bindView(R.id.prefsValue)
    val prefsView: View by bindView(R.id.prefsView)
}