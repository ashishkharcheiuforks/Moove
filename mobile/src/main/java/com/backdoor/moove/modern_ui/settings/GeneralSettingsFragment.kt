package com.backdoor.moove.modern_ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.backdoor.moove.databinding.FragmentSettingsGeneralBinding
import com.backdoor.moove.utils.Prefs
import org.koin.android.ext.android.inject

class GeneralSettingsFragment : Fragment() {

    private val prefs: Prefs by inject()
    private lateinit var binding: FragmentSettingsGeneralBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentSettingsGeneralBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        binding.prefs24Hour.setOnClickListener { change24Hour() }
        binding.prefsWear.setOnClickListener { changeWear() }

        binding.prefsWear.isChecked = prefs.wearNotification
        binding.prefs24Hour.isChecked = prefs.use24Hour
    }

    private fun change24Hour() {
        val state = !binding.prefs24Hour.isChecked
        prefs.use24Hour = state
        binding.prefs24Hour.isChecked = state
    }

    private fun changeWear() {
        val state = !binding.prefsWear.isChecked
        prefs.wearNotification = state
        binding.prefsWear.isChecked = state
    }
}
