package com.backdoor.moove.core.utils

import android.content.Context
import androidx.core.content.ContextCompat
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator

import com.backdoor.moove.R

object ViewUtils {

    val icon: Int
        get() = R.drawable.ic_notifications_white_24dp

    /**
     * Get color from resource.
     *
     * @param context  application context.
     * @param resource color resource.
     * @return Color
     */
    fun getColor(context: Context?, resource: Int): Int {
        return if (context == null) 0 else ContextCompat.getColor(context, resource)
    }

    fun slideInUp(context: Context, view: View) {
        val animation = AnimationUtils.loadAnimation(context,
                R.anim.slide_up)
        view.startAnimation(animation)
        view.visibility = View.VISIBLE
    }

    fun slideOutDown(context: Context, view: View) {
        val animation = AnimationUtils.loadAnimation(context,
                R.anim.slide_down)
        view.startAnimation(animation)
        view.visibility = View.GONE
    }

    fun fadeInAnimation(view: View) {
        val fadeIn = AlphaAnimation(0f, 1f)
        fadeIn.interpolator = DecelerateInterpolator()
        fadeIn.startOffset = 400
        fadeIn.duration = 400
        view.animation = fadeIn
        view.visibility = View.VISIBLE
    }

    fun fadeOutAnimation(view: View) {
        val fadeOut = AlphaAnimation(1f, 0f)
        fadeOut.interpolator = AccelerateInterpolator() //and this
        fadeOut.duration = 400
        view.animation = fadeOut
        view.visibility = View.GONE
    }

    fun show(view: View) {
        val fadeIn = AlphaAnimation(0f, 1f)
        fadeIn.interpolator = DecelerateInterpolator()
        fadeIn.startOffset = 400
        fadeIn.duration = 400
        view.animation = fadeIn
        view.visibility = View.VISIBLE
    }

    fun showOver(view: View) {
        val fadeIn = AlphaAnimation(0f, 1f)
        fadeIn.interpolator = OvershootInterpolator()
        fadeIn.duration = 300
        view.animation = fadeIn
        view.visibility = View.VISIBLE
    }

    fun hideOver(view: View) {
        val fadeIn = AlphaAnimation(1f, 0f)
        fadeIn.interpolator = OvershootInterpolator()
        fadeIn.duration = 300
        view.animation = fadeIn
        view.visibility = View.GONE
    }

    fun show(context: Context, v: View) {
        val slide = AnimationUtils.loadAnimation(context, R.anim.scale_zoom)
        v.startAnimation(slide)
        v.visibility = View.VISIBLE
    }

    fun hide(context: Context, v: View) {
        val slide = AnimationUtils.loadAnimation(context, R.anim.scale_zoom_out)
        v.startAnimation(slide)
        v.visibility = View.GONE
    }
}
