package com.backdoor.moove.core.helper

import android.os.Build

/**
 * Helper class for checking type of built application.
 */
object Module {

    /**
     * Check if device runs on JellyBean 2 and above.
     *
     * @return boolean
     */
    val isJellyBean: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2

    /**
     * Check if device runs on Lollipop and above.
     *
     * @return boolean
     */
    val isLollipop: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP

    /**
     * Check if device runs on Marshmallow and above.
     *
     * @return boolean
     */
    val isMarshmallow: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M

    /**
     * Check if device runs on Lollipop and above.
     *
     * @return boolean
     */
    val isKitkat: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT

    val isNougat: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N

    val isO: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
}
