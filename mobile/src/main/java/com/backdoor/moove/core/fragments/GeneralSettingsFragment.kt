package com.backdoor.moove.core.fragments


import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.backdoor.moove.R
import com.backdoor.moove.core.consts.Configs
import com.backdoor.moove.core.consts.Prefs
import com.backdoor.moove.core.helper.Module
import com.backdoor.moove.core.helper.SharedPrefs
import com.backdoor.moove.core.views.PrefsView

class GeneralSettingsFragment : Fragment(), View.OnClickListener {

    private var sPrefs: SharedPrefs? = null
    private var ab: ActionBar? = null

    private var use24TimePrefs: PrefsView? = null
    private var wearEnablePrefs: PrefsView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val rootView = inflater.inflate(R.layout.settings_general, container, false)

        ab = (activity as AppCompatActivity).supportActionBar
        if (ab != null) {
            ab!!.setTitle(R.string.general)
        }

        activity!!.intent.action = "General attached"
        sPrefs = SharedPrefs.getInstance(activity)

        if (Module.isLollipop) {
            rootView.findViewById<View>(R.id.generalCard).elevation = Configs.CARD_ELEVATION
        }

        use24TimePrefs = rootView.findViewById(R.id.use24TimePrefs)
        use24TimePrefs!!.isChecked = sPrefs!!.loadBoolean(Prefs.IS_24_TIME_FORMAT)
        use24TimePrefs!!.setOnClickListener(this)

        wearEnablePrefs = rootView.findViewById(R.id.wearEnablePrefs)
        wearEnablePrefs!!.isChecked = sPrefs!!.loadBoolean(Prefs.WEAR_NOTIFICATION)
        wearEnablePrefs!!.setOnClickListener(this)

        return rootView
    }

    private fun _24Change() {
        if (use24TimePrefs!!.isChecked) {
            sPrefs!!.saveBoolean(Prefs.IS_24_TIME_FORMAT, false)
            use24TimePrefs!!.isChecked = false
        } else {
            sPrefs!!.saveBoolean(Prefs.IS_24_TIME_FORMAT, true)
            use24TimePrefs!!.isChecked = true
        }
    }

    private fun wearChange() {
        if (wearEnablePrefs!!.isChecked) {
            sPrefs!!.saveBoolean(Prefs.WEAR_NOTIFICATION, false)
            wearEnablePrefs!!.isChecked = false
        } else {
            sPrefs!!.saveBoolean(Prefs.WEAR_NOTIFICATION, true)
            wearEnablePrefs!!.isChecked = true
        }
    }

    override fun onDetach() {
        super.onDetach()
        ab = (activity as AppCompatActivity).supportActionBar
        if (ab != null) {
            ab!!.setTitle(R.string.settings)
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.use24TimePrefs -> _24Change()
            R.id.wearEnablePrefs -> wearChange()
        }
    }
}
