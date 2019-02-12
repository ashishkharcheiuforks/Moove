package com.backdoor.moove.core.fragments

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

import com.backdoor.moove.R
import com.backdoor.moove.core.consts.Configs
import com.backdoor.moove.core.dialogs.ChangeDialog
import com.backdoor.moove.core.dialogs.Help
import com.backdoor.moove.core.dialogs.ThanksDialog
import com.backdoor.moove.core.helper.Module
import com.backdoor.moove.core.helper.Permissions
import com.backdoor.moove.databinding.DialogAboutLayoutBinding

import java.util.ArrayList

class OtherSettingsFragment : Fragment() {

    private var ab: ActionBar? = null
    private val mDataList = ArrayList<Item>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.settings_other, container, false)
        ab = (activity as AppCompatActivity).supportActionBar
        if (ab != null) {
            ab!!.setTitle(R.string.other)
        }
        if (Module.isLollipop) {
            rootView.findViewById<View>(R.id.otherCard).elevation = Configs.CARD_ELEVATION
        }
        val about = rootView.findViewById<TextView>(R.id.about)
        about.setOnClickListener { v -> showAboutDialog() }

        val changes = rootView.findViewById<TextView>(R.id.changes)
        changes.setOnClickListener { v ->
            activity!!.applicationContext
                    .startActivity(Intent(activity!!.applicationContext,
                            ChangeDialog::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }

        val rateApp = rootView.findViewById<TextView>(R.id.rateApp)
        rateApp.setOnClickListener { v -> launchMarket(activity!!) }

        val thanks = rootView.findViewById<TextView>(R.id.thanks)
        thanks.setOnClickListener { v ->
            activity!!.applicationContext
                    .startActivity(Intent(activity!!.applicationContext,
                            ThanksDialog::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }

        val help = rootView.findViewById<TextView>(R.id.help)
        help.setOnClickListener { v ->
            activity!!.applicationContext
                    .startActivity(Intent(activity!!.applicationContext,
                            Help::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }

        val menuFeedback = rootView.findViewById<TextView>(R.id.menuFeedback)
        menuFeedback.setOnClickListener { v ->
            val emailIntent = Intent(Intent.ACTION_SEND)
            emailIntent.type = "plain/text"
            emailIntent.putExtra(Intent.EXTRA_EMAIL,
                    arrayOf("feedback.cray@gmail.com"))
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Moove")
            activity!!.startActivity(Intent.createChooser(emailIntent, "Send mail..."))
        }

        val menuShare = rootView.findViewById<TextView>(R.id.menuShare)
        menuShare.setOnClickListener { v ->
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_TEXT,
                    "https://play.google.com/store/apps/details?id=" + activity!!.packageName)
            activity!!.startActivity(Intent.createChooser(shareIntent, "Share..."))
        }

        val permissionBlock = rootView.findViewById<LinearLayout>(R.id.permissionBlock)
        if (Module.isMarshmallow) {
            permissionBlock.visibility = View.VISIBLE
        } else {
            permissionBlock.visibility = View.GONE
        }

        val permissionExplain = rootView.findViewById<TextView>(R.id.permissionExplain)
        permissionExplain.setOnClickListener { v ->
            activity!!.applicationContext
                    .startActivity(Intent(activity!!.applicationContext, ThanksDialog::class.java)
                            .putExtra("int", 1)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }

        val permissionEnable = rootView.findViewById<TextView>(R.id.permissionEnable)
        permissionEnable.setOnClickListener { v -> showPermissionDialog() }

        return rootView
    }

    private fun launchMarket(context: Context) {
        val uri = Uri.parse("market://details?id=" + context.packageName)
        val goToMarket = Intent(Intent.ACTION_VIEW, uri)
        try {
            context.startActivity(goToMarket)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, context.getString(R.string.could_not_launch_market), Toast.LENGTH_SHORT).show()
        }

    }

    override fun onDetach() {
        super.onDetach()
        ab = (activity as AppCompatActivity).supportActionBar
        if (ab != null) {
            ab!!.setTitle(R.string.settings)
        }
    }

    private fun showAboutDialog() {
        val builder = AlertDialog.Builder(context)
        val binding = DialogAboutLayoutBinding.inflate(LayoutInflater.from(context))
        val pInfo: PackageInfo
        try {
            pInfo = context!!.packageManager.getPackageInfo(context!!.packageName, 0)
            binding.appVersion.text = pInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        builder.setView(binding.root)
        builder.create().show()
    }

    private fun showPermissionDialog() {
        if (!loadDataToList()) return
        val builder = AlertDialog.Builder(context)
        builder.setTitle(R.string.allow_permission)
        builder.setSingleChoiceItems(object : ArrayAdapter<Item>(context!!, android.R.layout.simple_list_item_1) {
            override fun getCount(): Int {
                return mDataList.size
            }

            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                var convertView = convertView
                if (convertView == null) {
                    convertView = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false)
                }
                val tvName = convertView!!.findViewById<TextView>(android.R.id.text1)
                tvName.text = mDataList[position].title
                return convertView
            }
        }, -1) { dialogInterface, i ->
            dialogInterface.dismiss()
            requestPermission(i)
        }
        builder.create().show()
    }

    private fun loadDataToList(): Boolean {
        mDataList.clear()
        if (!Permissions.checkPermission(activity, Permissions.ACCESS_COARSE_LOCATION)) {
            mDataList.add(Item(getString(R.string.course_location), Permissions.ACCESS_COARSE_LOCATION))
        }
        if (!Permissions.checkPermission(activity, Permissions.ACCESS_FINE_LOCATION)) {
            mDataList.add(Item(getString(R.string.fine_location), Permissions.ACCESS_FINE_LOCATION))
        }
        if (!Permissions.checkPermission(activity, Permissions.CALL_PHONE)) {
            mDataList.add(Item(getString(R.string.call_phone), Permissions.CALL_PHONE))
        }
        if (!Permissions.checkPermission(activity, Permissions.GET_ACCOUNTS)) {
            mDataList.add(Item(getString(R.string.get_accounts), Permissions.GET_ACCOUNTS))
        }
        if (!Permissions.checkPermission(activity, Permissions.READ_PHONE_STATE)) {
            mDataList.add(Item(getString(R.string.read_phone_state), Permissions.READ_PHONE_STATE))
        }
        if (!Permissions.checkPermission(activity, Permissions.READ_CONTACTS)) {
            mDataList.add(Item(getString(R.string.read_contacts), Permissions.READ_CONTACTS))
        }
        if (!Permissions.checkPermission(activity, Permissions.READ_EXTERNAL)) {
            mDataList.add(Item(getString(R.string.read_external_storage), Permissions.READ_EXTERNAL))
        }
        if (!Permissions.checkPermission(activity, Permissions.WRITE_EXTERNAL)) {
            mDataList.add(Item(getString(R.string.write_external_storage), Permissions.WRITE_EXTERNAL))
        }
        if (!Permissions.checkPermission(activity, Permissions.SEND_SMS)) {
            mDataList.add(Item(getString(R.string.send_sms), Permissions.SEND_SMS))
        }
        if (mDataList.size == 0) {
            Toast.makeText(context, R.string.all_permissions_are_enabled, Toast.LENGTH_SHORT).show()
            return false
        } else {
            return true
        }
    }

    private fun requestPermission(position: Int) {
        requestPermissions(arrayOf(mDataList[position].permission), 155)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (grantResults.size == 0) return
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            showPermissionDialog()
        }
    }

    private inner class Item internal constructor(val title: String, val permission: String)
}
