package com.backdoor.moove.modern_ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.backdoor.moove.R

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val navHost = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
        navHost?.let { navFragment ->
            navFragment.childFragmentManager.primaryNavigationFragment?.onActivityResult(requestCode, resultCode, data)
        }
    }
}
