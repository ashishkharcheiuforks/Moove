package com.backdoor.moove.core.helper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.io.File;

/**
 * Helper class to interact with calls, messages and emails.
 */
public class Telephony {

    public Telephony() {
    }

    /**
     * Start calling to contact.
     *
     * @param number  number to call.
     * @param context application context.
     */
    @SuppressLint("MissingPermission")
    public static void makeCall(String number, Context context) {
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + number));
        context.startActivity(callIntent);
    }
}
