package com.backdoor.moove.core.helper

import android.content.Context
import com.google.android.material.snackbar.Snackbar
import android.view.View
import android.widget.Toast

/**
 * Helper method for showing toast or snackbar messages.
 */
object Messages {

    /**
     * Show toast message.
     * @param context application context.
     * @param message message string.
     */
    fun toast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    /**
     * Show toast message.
     * @param context application context.
     * @param resId message string resource.
     */
    fun toast(context: Context, resId: Int) {
        Toast.makeText(context, context.getString(resId), Toast.LENGTH_SHORT).show()
    }

    /**
     * Show message in snackbar.
     * @param v snackbar container view.
     * @param message message string.
     */
    fun snackbar(v: View, message: String) {
        Snackbar.make(v, message, Snackbar.LENGTH_SHORT).show()
    }

    /**
     * Show message in snackbar.
     * @param v snackbar container view.
     * @param resId message string resource.
     */
    fun snackbar(v: View, resId: Int) {
        Snackbar.make(v, resId, Snackbar.LENGTH_SHORT).show()
    }
}
