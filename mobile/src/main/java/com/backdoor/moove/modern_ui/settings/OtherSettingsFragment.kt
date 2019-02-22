package com.backdoor.moove.modern_ui.settings

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.backdoor.moove.R
import com.backdoor.moove.databinding.DialogAboutBinding
import com.backdoor.moove.databinding.FragmentSettingsOtherBinding

class OtherSettingsFragment : Fragment() {

    private lateinit var binding: FragmentSettingsOtherBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentSettingsOtherBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        binding.prefsAbout.setOnClickListener { showAbout() }
        binding.prefsFeedback.setOnClickListener { sendFeedback() }
        binding.prefsOss.setOnClickListener { openOss() }
        binding.prefsRate.setOnClickListener { openRate() }
        binding.prefsShare.setOnClickListener { shareApp() }
    }

    private fun shareApp() {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=" + activity?.packageName)
        context?.startActivity(Intent.createChooser(shareIntent, "Share..."))
    }

    private fun openRate() {
        val uri = Uri.parse("market://details?id=" + context?.packageName)
        val goToMarket = Intent(Intent.ACTION_VIEW, uri)
        try {
            context?.startActivity(goToMarket)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, getString(R.string.app_not_found), Toast.LENGTH_SHORT).show()
        }
    }

    private fun openOss() {
        findNavController().navigate(OtherSettingsFragmentDirections.actionOtherSettingsFragmentToOssSettingsFragment())
    }

    private fun sendFeedback() {
        val emailIntent = Intent(Intent.ACTION_SEND)
        emailIntent.type = "plain/text"
        emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayListOf("feedback.cray@gmail.com"))
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Moove")
        context?.startActivity(Intent.createChooser(emailIntent, "Send mail..."))
    }

    private fun showAbout() {
        val builder = AlertDialog.Builder(context!!, R.style.HomeDarkDialog)
        val b = DialogAboutBinding.inflate(LayoutInflater.from(context))
        val name: String = getString(com.backdoor.moove.R.string.app_name)
        b.appName.text = name.toUpperCase()
        val pInfo: PackageInfo
        try {
            pInfo = context!!.packageManager.getPackageInfo(context!!.packageName, 0)
            b.appVersion.text = pInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        builder.setView(b.root)
        builder.create().show()
    }
}
