package com.backdoor.moove.core.helper;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.Toast;

/**
 * Helper method for showing toast or snackbar messages.
 */
public class Messages {

    /**
     * Show toast message.
     * @param context application context.
     * @param message message string.
     */
    public static void toast(Context context, String message){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Show toast message.
     * @param context application context.
     * @param resId message string resource.
     */
    public static void toast(Context context, int resId){
        Toast.makeText(context, context.getString(resId), Toast.LENGTH_SHORT).show();
    }

    /**
     * Show message in snackbar.
     * @param v snackbar container view.
     * @param message message string.
     */
    public static void snackbar(View v, String message){
        Snackbar.make(v, message, Snackbar.LENGTH_SHORT).show();
    }

    /**
     * Show message in snackbar.
     * @param v snackbar container view.
     * @param resId message string resource.
     */
    public static void snackbar(View v, int resId){
        Snackbar.make(v, resId, Snackbar.LENGTH_SHORT).show();
    }
}
