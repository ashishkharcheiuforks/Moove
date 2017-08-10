package com.backdoor.moove.core.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.Transformation;

import com.backdoor.moove.R;
import com.backdoor.moove.core.helper.Module;

public class ViewUtils {

    /**
     * Get drawable from resource.
     *
     * @param context  application context.
     * @param resource drawable resource.
     * @return Drawable
     */
    public static Drawable getDrawable(Context context, int resource) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            return context.getResources().getDrawable(resource, null);
        } else {
            return context.getResources().getDrawable(resource);
        }
    }

    /**
     * Get color from resource.
     *
     * @param context  application context.
     * @param resource color resource.
     * @return Color
     */
    public static int getColor(Context context, int resource) {
        if (Module.isMarshmallow()) return context.getResources().getColor(resource, null);
        return context.getResources().getColor(resource);
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

    public static void expand(final View v) {
        v.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        final int targetHeight = v.getMeasuredHeight();
        v.getLayoutParams().height = 0;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().height = interpolatedTime == 1
                        ? ViewGroup.LayoutParams.WRAP_CONTENT
                        : (int) (targetHeight * interpolatedTime);
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };
        // 1dp/ms
        a.setDuration((int) (targetHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }

    public static void collapse(final View v) {
        final int initialHeight = v.getMeasuredHeight();
        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (interpolatedTime == 1) {
                    v.setVisibility(View.GONE);
                } else {
                    v.getLayoutParams().height = initialHeight - (int) (initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };
        // 1dp/ms
        a.setDuration((int) (initialHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }

    public static int getIcon() {
        return R.drawable.ic_notifications_white_24dp;
    }
}
