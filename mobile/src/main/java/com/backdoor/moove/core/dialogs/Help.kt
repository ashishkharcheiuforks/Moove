package com.backdoor.moove.core.dialogs

import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.webkit.WebView

import com.backdoor.moove.R
import com.backdoor.moove.core.helper.Coloring
import com.backdoor.moove.core.views.FloatingEditText

import java.util.Locale

class Help : AppCompatActivity() {

    private var helpView: WebView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val cSetter = Coloring(this@Help)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = cSetter.colorPrimaryDark()
        }
        setContentView(R.layout.help_layout)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setLogo(R.drawable.ic_help_white_24dp)
        supportActionBar!!.setDisplayShowTitleEnabled(true)
        supportActionBar!!.title = getString(R.string.help)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeButtonEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        helpView = findViewById(R.id.helpView)
        val localeCheck = Locale.getDefault().toString().toLowerCase()
        val url: String
        if (localeCheck.startsWith("uk")) {
            url = "file:///android_asset/web_page/index.html"
        } else if (localeCheck.startsWith("ru")) {
            url = "file:///android_asset/web_page/index_ru.html"
        } else {
            url = "file:///android_asset/web_page/index_en.html"
        }

        helpView!!.loadUrl(url)

        val searchEdit = findViewById<FloatingEditText>(R.id.searchEdit)
        searchEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                helpView!!.findAll(s.toString())
            }

            override fun afterTextChanged(s: Editable) {

            }
        })
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
