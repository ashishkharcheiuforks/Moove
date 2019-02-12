package com.backdoor.moove

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.Button
import android.widget.RelativeLayout

import com.backdoor.moove.core.consts.Prefs
import com.backdoor.moove.core.helper.Permissions
import com.backdoor.moove.core.helper.SharedPrefs
import com.backdoor.moove.core.utils.LocationUtil
import com.google.android.gms.common.ConnectionResult

class StartHelpActivity : AppCompatActivity() {

    private var serviceShow: RelativeLayout? = null
    private var locationShow: RelativeLayout? = null
    private var resultCode: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start_help)

        serviceShow = findViewById(R.id.serviceShow)
        locationShow = findViewById(R.id.locationShow)

        val servicesFix = findViewById<Button>(R.id.servicesFix)
        val permissionsFix = findViewById<Button>(R.id.permissionsFix)

        permissionsFix.setOnClickListener { requestPermission() }
        servicesFix.setOnClickListener { LocationUtil.showPlayDialog(this@StartHelpActivity, resultCode) }
        checkAll()
    }

    private fun requestPermission() {
        Permissions.requestPermission(this@StartHelpActivity, 200, Permissions.ACCESS_COARSE_LOCATION,
                Permissions.ACCESS_FINE_LOCATION)
    }

    private fun checkAll() {
        if (checkDevice() && checkPermissions()) {
            startActivity(Intent(this@StartHelpActivity, MainActivity::class.java))
            val prefs = SharedPrefs.getInstance(this)
            prefs?.saveBoolean(Prefs.FIRST_LOAD, true)
            finish()
        } else {
            if (!checkDevice()) {
                serviceShow!!.visibility = View.VISIBLE
            } else {
                serviceShow!!.visibility = View.GONE
            }
            if (!checkPermissions()) {
                locationShow!!.visibility = View.VISIBLE
            } else {
                locationShow!!.visibility = View.GONE
            }
        }
    }

    private fun checkPermissions(): Boolean {
        return Permissions.checkPermission(this@StartHelpActivity, Permissions.ACCESS_COARSE_LOCATION,
                Permissions.ACCESS_FINE_LOCATION)
    }

    private fun checkDevice(): Boolean {
        resultCode = LocationUtil.checkPlay(this@StartHelpActivity)
        return resultCode == ConnectionResult.SUCCESS
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (grantResults.isEmpty()) return
        if (requestCode == 200) {
            checkAll()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 99) {
            checkAll()
        }
    }
}
