package com.backdoor.moove.core.dialogs

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient

import com.backdoor.moove.R

class ChangeDialog : Activity() {

    private var alertDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val alert = AlertDialog.Builder(this)
        alert.setTitle(getString(R.string.changes))

        val wv = WebView(this)
        val url = "file:///android_asset/change_log.html"
        wv.loadUrl(url)
        wv.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                view.loadUrl(url)
                return true
            }
        }

        alert.setView(wv)
        alert.setCancelable(false)
        alert.setNegativeButton(getString(R.string.ok)) { dialog, id ->
            dialog.dismiss()
            finish()
        }
        alertDialog = alert.create()
        alertDialog!!.show()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (alertDialog != null && alertDialog!!.isShowing) {
            alertDialog!!.dismiss()
        }
        finish()
    }
}