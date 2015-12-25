package com.backdoor.moove.core.helper;

import android.os.Build;

/**
 * Helper class for checking type of built application.
 */
public class Module {

    /**
     * Check if device runs on Lollipop and above.
     * @return boolean
     */
    public static boolean isLollipop(){
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    /**
     * Check if device runs on Marshmallow and above.
     * @return boolean
     */
    public static boolean isMarshmallow(){
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    /**
     * Check if device runs on Lollipop and above.
     * @return boolean
     */
    public static boolean isKitkat(){
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    }
}
