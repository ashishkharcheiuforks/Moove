package com.backdoor.moove.modern_ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.backdoor.moove.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private lateinit var binding: FragmentSettingsBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.prefsGeneral.setOnClickListener {
            findNavController().navigate(SettingsFragmentDirections.actionSettingsFragmentToGeneralSettingsFragment())
        }
        binding.prefsNotifications.setOnClickListener {
            findNavController().navigate(SettingsFragmentDirections.actionSettingsFragmentToNotificationSettingsFragment())
        }
        binding.prefsLocation.setOnClickListener {
            findNavController().navigate(SettingsFragmentDirections.actionSettingsFragmentToLocationSettingsFragment())
        }
        binding.prefsOther.setOnClickListener {
            findNavController().navigate(SettingsFragmentDirections.actionSettingsFragmentToOtherSettingsFragment())
        }
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
    }
}
