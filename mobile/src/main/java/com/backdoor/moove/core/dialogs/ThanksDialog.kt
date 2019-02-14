package com.backdoor.moove.core.dialogs

import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.view.MenuItem
import android.view.View
import android.webkit.WebView

import com.backdoor.moove.R
import com.backdoor.moove.utils.Coloring
import com.backdoor.moove.core.views.FloatingEditText

/**
 * Show all open source libraries used in project.
 */
class ThanksDialog : AppCompatActivity() {

    /**
     * Helper method initialization.
     */
    private val cSetter = Coloring(this@ThanksDialog)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = cSetter.colorPrimaryDark()
        }
        setContentView(R.layout.help_layout)

        val code = intent.getIntExtra("int", 0)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setLogo(R.drawable.ic_security_white_24dp)
        val bar = supportActionBar
        if (bar != null) {
            bar.setDisplayShowTitleEnabled(true)
            bar.title = getString(R.string.open_source_licenses)
            if (code == 1) {
                bar.title = getString(R.string.permissions)
            }
            bar.setDisplayHomeAsUpEnabled(true)
            bar.setHomeButtonEnabled(true)
            bar.setDisplayShowHomeEnabled(true)
        }

        val helpView = findViewById<WebView>(R.id.helpView)
        var url = "file:///android_asset/LICENSE.html"
        if (code == 1) {
            url = "file:///android_asset/permissions.html"
        }
        helpView.loadUrl(url)

        val searchEdit = findViewById<FloatingEditText>(R.id.searchEdit)
        searchEdit.visibility = View.GONE
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }
}