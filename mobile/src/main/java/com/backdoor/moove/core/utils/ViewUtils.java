package com.backdoor.moove.core.utils;

import android.content.Context;
import androidx.core.content.ContextCompat;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import com.backdoor.moove.R;

public class ViewUtils {

    /**
     * Get color from resource.
     *
     * @param context  application context.
     * @param resource color resource.
     * @return Color
     */
    public static int getColor(Context context, int resource) {
        if (context == null) return 0;
        return ContextCompat.getColor(context, resource);
    }

    public static void slideInUp(Context context, View view) {
        Animation animation = AnimationUtils.loadAnimation(context,
                R.anim.slide_up);
        view.startAnimation(animation);
        view.setVisibility(View.VISIBLE);
    }

    public static void slideOutDown(Context context, View view) {
        Animation animation = AnimationUtils.loadAnimation(context,
                R.anim.slide_down);
        view.startAnimation(animation);
        view.setVisibility(View.GONE);
    }

    public static void fadeInAnimation(View view) {
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new DecelerateInterpolator());
        fadeIn.setStartOffset(400);
        fadeIn.setDuration(400);
        view.setAnimation(fadeIn);
        view.setVisibility(View.VISIBLE);
    }

    public static void fadeOutAnimation(View view) {
        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new AccelerateInterpolator()); //and this
        fadeOut.setDuration(400);
        view.setAnimation(fadeOut);
        view.setVisibility(View.GONE);
    }

    public static void show(View view) {
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new DecelerateInterpolator());
        fadeIn.setStartOffset(400);
        fadeIn.setDuration(400);
        view.setAnimation(fadeIn);
        view.setVisibility(View.VISIBLE);
    }

    public static void showOver(View view) {
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new OvershootInterpolator());
        fadeIn.setDuration(300);
        view.setAnimation(fadeIn);
        view.setVisibility(View.VISIBLE);
    }

    public static void hideOver(View view) {
        Animation fadeIn = new AlphaAnimation(1, 0);
        fadeIn.setInterpolator(new OvershootInterpolator());
        fadeIn.setDuration(300);
        view.setAnimation(fadeIn);
        view.setVisibility(View.GONE);
    }

    public static void show(Context context, View v) {
        Animation slide = AnimationUtils.loadAnimation(context, R.anim.scale_zoom);
        v.startAnimation(slide);
        v.setVisibility(View.VISIBLE);
    }

    public static void hide(Context context, View v) {
        Animation slide = AnimationUtils.loadAnimation(context, R.anim.scale_zoom_out);
        v.startAnimation(slide);
        v.setVisibility(View.GONE);
    }

    public static int getIcon() {
        return R.drawable.ic_notifications_white_24dp;
    }
}
