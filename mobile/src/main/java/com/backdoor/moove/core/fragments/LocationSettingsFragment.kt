package com.backdoor.moove.core.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import com.backdoor.moove.PlacesListActivity
import com.backdoor.moove.R
import com.backdoor.moove.core.consts.Configs
import com.backdoor.moove.core.consts.Prefs
import com.backdoor.moove.core.dialogs.MarkerStyle
import com.backdoor.moove.core.dialogs.TargetRadius
import com.backdoor.moove.core.dialogs.TrackerOption
import com.backdoor.moove.core.helper.Dialogues
import com.backdoor.moove.core.helper.Module
import com.backdoor.moove.core.helper.SharedPrefs
import com.backdoor.moove.core.views.PrefsView

class LocationSettingsFragment : Fragment(), View.OnClickListener {

    private var sPrefs: SharedPrefs? = null
    private var ab: ActionBar? = null

    private var notificationOptionPrefs: PrefsView? = null
    private var radiusPrefs: PrefsView? = null
    private var autoFill: PrefsView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val rootView = inflater.inflate(R.layout.settings_location, container, false)
        sPrefs = SharedPrefs.getInstance(activity)

        ab = (activity as AppCompatActivity).supportActionBar
        if (ab != null) {
            ab!!.setTitle(R.string.location)
        }

        if (Module.isLollipop) {
            rootView.findViewById<View>(R.id.locationCard).elevation = Configs.CARD_ELEVATION
        }

        val mapType = rootView.findViewById<TextView>(R.id.mapType)
        mapType.setOnClickListener(this)

        notificationOptionPrefs = rootView.findViewById(R.id.notificationOptionPrefs)
        notificationOptionPrefs!!.isChecked = sPrefs!!.loadBoolean(Prefs.TRACKING_NOTIFICATION)
        notificationOptionPrefs!!.setOnClickListener(this)

        autoFill = rootView.findViewById(R.id.autoFill)
        autoFill!!.isChecked = sPrefs!!.loadBoolean(Prefs.PLACES_AUTO)
        autoFill!!.setOnClickListener(this)

        radiusPrefs = rootView.findViewById(R.id.radiusPrefs)
        radiusPrefs!!.setOnClickListener(this)

        val places = rootView.findViewById<TextView>(R.id.places)
        places.setOnClickListener(this)

        val tracker = rootView.findViewById<TextView>(R.id.tracker)
        tracker.setOnClickListener(this)

        val markerStyle = rootView.findViewById<TextView>(R.id.markerStyle)
        markerStyle.setOnClickListener(this)

        return rootView
    }

    private fun notificationChange() {
        if (notificationOptionPrefs!!.isChecked) {
            sPrefs!!.saveBoolean(Prefs.TRACKING_NOTIFICATION, false)
            notificationOptionPrefs!!.isChecked = false
        } else {
            sPrefs!!.saveBoolean(Prefs.TRACKING_NOTIFICATION, true)
            notificationOptionPrefs!!.isChecked = true
        }
    }

    private fun placesChange() {
        if (autoFill!!.isChecked) {
            sPrefs!!.saveBoolean(Prefs.PLACES_AUTO, false)
            autoFill!!.isChecked = false
        } else {
            sPrefs!!.saveBoolean(Prefs.PLACES_AUTO, true)
            autoFill!!.isChecked = true
        }
    }

    override fun onResume() {
        super.onResume()
        radiusPrefs!!.setValueText(sPrefs!!.loadInt(Prefs.LOCATION_RADIUS).toString() + getString(R.string.m))
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
            R.id.mapType -> Dialogues.mapType(activity)
            R.id.notificationOptionPrefs -> notificationChange()
            R.id.radiusPrefs -> activity!!.applicationContext
                    .startActivity(Intent(activity!!.applicationContext,
                            TargetRadius::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            R.id.places -> activity!!.applicationContext
                    .startActivity(Intent(activity!!.applicationContext,
                            PlacesListActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            R.id.tracker -> activity!!.applicationContext
                    .startActivity(Intent(activity!!.applicationContext,
                            TrackerOption::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            R.id.markerStyle -> activity!!.applicationContext
                    .startActivity(Intent(activity!!.applicationContext,
                            MarkerStyle::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            R.id.autoFill -> placesChange()
        }
    }
}
