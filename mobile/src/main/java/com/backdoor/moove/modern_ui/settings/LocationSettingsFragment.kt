package com.backdoor.moove.modern_ui.settings

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.backdoor.moove.R
import com.backdoor.moove.databinding.FragmentSettingsLocationBinding
import com.backdoor.moove.databinding.TrackerSettingsLayoutBinding
import com.backdoor.moove.utils.Coloring
import com.backdoor.moove.utils.Dialogues
import com.backdoor.moove.utils.DrawableHelper
import com.backdoor.moove.utils.Prefs
import com.google.android.gms.maps.GoogleMap
import org.koin.android.ext.android.inject

class LocationSettingsFragment : Fragment() {

    val dialogues: Dialogues by inject()
    val prefs: Prefs by inject()
    val coloring: Coloring by inject()
    private var mItemSelect: Int = 0

    private lateinit var binding: FragmentSettingsLocationBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding =  FragmentSettingsLocationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        initMapTypePrefs()
        initMarkerStylePrefs()
        initRadiusPrefs()
        binding.prefsTracking.setOnClickListener { showTrackerOptionsDialog() }
        binding.prefsDistance.setOnClickListener { changeNotificationPrefs() }
        binding.prefsAutoPlace.setOnClickListener { changeAutoPlace() }

        binding.prefsDistance.isChecked = prefs.isDistanceNotificationEnabled
        binding.prefsAutoPlace.isChecked = prefs.autoPlace

        binding.prefsPlaces.setOnClickListener {
            findNavController().navigate(LocationSettingsFragmentDirections.actionLocationSettingsFragmentToPlacesFragment())
        }
    }

    private fun initMarkerStylePrefs() {
        binding.prefsMarkerStyle.setOnClickListener { showStyleDialog() }
        showMarkerStyle()
    }

    private fun showStyleDialog() {
        dialogues.showColorBottomDialog(activity!!, prefs.markerStyle,
                coloring.colorsForSlider()) {
            prefs.markerStyle = it
            showMarkerStyle()
        }
    }

    private fun showMarkerStyle() {
        val pointer = DrawableHelper.withContext(context!!)
                .withDrawable(R.drawable.ic_twotone_place_24px)
                .withColor(coloring.accentColor(prefs.markerStyle))
                .tint()
                .get()
        binding.prefsMarkerStyle.setViewDrawable(pointer)
    }

    private fun initMapTypePrefs() {
        binding.prefsMapType.setOnClickListener { showMapTypeDialog() }
        showMapType()
    }

    private fun showMapType() {
        val types = resources.getStringArray(R.array.map_types)
        binding.prefsMapType.setDetailText(types[getPosition(prefs.mapType)])
    }

    private fun initRadiusPrefs() {
        binding.prefsRadius.setOnClickListener { showRadiusPickerDialog() }
        showRadius()
    }

    private fun showTrackerOptionsDialog() {
        val builder = AlertDialog.Builder(context!!, R.style.HomeDarkDialog)
        builder.setTitle(R.string.tracking_preferences)
        val b = TrackerSettingsLayoutBinding.inflate(layoutInflater)
        val time = prefs.trackTime - 1
        b.timeBar.progress = time
        b.timeBar.max = 119
        b.timeBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, bool: Boolean) {
                b.timeValue.text = (i + 1).toString() + getString(R.string.s)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {

            }
        })
        b.timeValue.text = (time + 1).toString() + getString(R.string.s)
        builder.setView(b.root)
        builder.setPositiveButton(R.string.ok) { _, _ ->
            prefs.trackTime = b.timeBar.progress + 1
        }
        builder.setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
        builder.create().show()
    }

    private fun showMapTypeDialog() {
        val builder = AlertDialog.Builder(context!!, R.style.HomeDarkDialog)
        builder.setCancelable(true)
        builder.setTitle(getString(R.string.map_type))
        val types = arrayOf(getString(R.string.normal), getString(R.string.satellite), getString(R.string.terrain), getString(R.string.hybrid))
        val adapter = ArrayAdapter(context!!, android.R.layout.simple_list_item_single_choice, types)
        val type = prefs.mapType
        mItemSelect = getPosition(type)
        builder.setSingleChoiceItems(adapter, mItemSelect) { _, which -> mItemSelect = which }
        builder.setPositiveButton(getString(R.string.ok)) { dialogInterface, _ ->
            prefs.mapType = mItemSelect + 1
            showMapType()
            dialogInterface.dismiss()
        }
        val dialog = builder.create()
        dialog.setOnCancelListener { mItemSelect = 0 }
        dialog.setOnDismissListener { mItemSelect = 0 }
        dialog.show()
    }

    private fun getPosition(type: Int): Int {
        return when (type) {
            GoogleMap.MAP_TYPE_SATELLITE -> 1
            GoogleMap.MAP_TYPE_TERRAIN -> 2
            GoogleMap.MAP_TYPE_HYBRID -> 3
            else -> 0
        }
    }

    private fun changeNotificationPrefs() {
        val isChecked = !binding.prefsDistance.isChecked
        binding.prefsDistance.isChecked = isChecked
        prefs.isDistanceNotificationEnabled = isChecked
    }

    private fun showRadius() {
        binding.prefsRadius.setDetailText(prefs.radius.toString() + getString(R.string.m))
    }

    private fun showRadiusPickerDialog() {
        val radius = prefs.radius
        dialogues.showRadiusBottomDialog(activity!!, radius) {
            prefs.radius = it
            showRadius()
            it.toString() + getString(R.string.m)
        }
    }

    private fun changeAutoPlace() {
        val isChecked = !binding.prefsAutoPlace.isChecked
        binding.prefsAutoPlace.isChecked = isChecked
        prefs.autoPlace = isChecked
    }
}
