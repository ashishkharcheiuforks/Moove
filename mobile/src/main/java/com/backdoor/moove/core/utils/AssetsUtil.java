package com.backdoor.moove.core.utils;

import android.content.Context;
import android.graphics.Typeface;
import androidx.annotation.NonNull;

public class AssetsUtil {
    public AssetsUtil() {
    }

    @NonNull
    public static Typeface getLightTypeface(Context context) {
        return Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Light.ttf");
    }

    @NonNull
    public static Typeface getMediumTypeface(Context context) {
        return Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Medium.ttf");
    }
}
