package com.backdoor.moove

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.backdoor.moove.modern_ui.HomeActivity
import com.backdoor.moove.utils.Prefs
import org.koin.android.ext.android.inject

class SplashScreenActivity : AppCompatActivity() {

    val prefs: Prefs by inject()

    override fun onResume() {
        super.onResume()
        prefs.initPrefs(this)
        prefs.checkPrefs()
        startActivity(Intent(this@SplashScreenActivity, HomeActivity::class.java))
        finish()
    }
}
