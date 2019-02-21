package com.backdoor.moove.modern_ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.backdoor.moove.databinding.FragmentSettingsOtherBinding

class OtherSettingsFragment : Fragment() {

    private lateinit var binding: FragmentSettingsOtherBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentSettingsOtherBinding.inflate(inflater, container, false)
        return binding.root
    }
}
