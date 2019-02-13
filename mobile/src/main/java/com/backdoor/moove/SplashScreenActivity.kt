package com.backdoor.moove

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.backdoor.moove.modern_ui.HomeActivity
import com.backdoor.moove.utils.Prefs

class SplashScreenActivity : AppCompatActivity() {

    override fun onResume() {
        super.onResume()
        Prefs.getInstance(this).initPrefs(this)
        Prefs.getInstance(this).checkPrefs()
        startActivity(Intent(this@SplashScreenActivity, HomeActivity::class.java))
        finish()
    }
}
