package com.backdoor.moove.core.utils

import android.content.Context
import android.graphics.Typeface

class AssetsUtil {
    companion object {

        fun getLightTypeface(context: Context): Typeface {
            return Typeface.createFromAsset(context.assets, "fonts/Roboto-Light.ttf")
        }

        fun getMediumTypeface(context: Context): Typeface {
            return Typeface.createFromAsset(context.assets, "fonts/Roboto-Medium.ttf")
        }
    }
}
