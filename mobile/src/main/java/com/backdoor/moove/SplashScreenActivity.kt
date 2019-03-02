package com.backdoor.moove

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.backdoor.moove.modern_ui.HomeActivity
import com.backdoor.moove.utils.EnableThread
import com.backdoor.moove.utils.Prefs
import org.koin.android.ext.android.inject

class SplashScreenActivity : AppCompatActivity() {

    private val prefs: Prefs by inject()

    override fun onResume() {
        super.onResume()
        EnableThread().run()
        prefs.initPrefs(this)
        prefs.checkPrefs()
        startActivity(Intent(this@SplashScreenActivity, HomeActivity::class.java))
        finish()
    }
}
