package com.backdoor.moove.modern_ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.backdoor.moove.databinding.FragmentSettingsOssBinding

class OssSettingsFragment : Fragment() {

    private lateinit var binding: FragmentSettingsOssBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding =  FragmentSettingsOssBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        val url = "file:///android_asset/LICENSE.html"
        binding.webView.loadUrl(url)
    }
}
