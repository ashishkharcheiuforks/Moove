package com.backdoor.moove.core.helper

import android.content.Context

import com.backdoor.moove.R
import com.backdoor.moove.core.utils.ViewUtils

class Coloring(private val mContext: Context) {

    /**
     * Get theme for application based on user choice.
     *
     * @return Theme resource
     */
    val style: Int
        get() = R.style.HomeDark

    /**
     * Get style for spinner based on current theme.
     *
     * @return Color
     */
    val spinnerStyle: Int
        get() = getColor(R.color.themePrimaryDark)

    /**
     * Get theme for dialog styled activity based on current application theme.
     *
     * @return Theme resource
     */
    val dialogStyle: Int
        get() = R.style.HomeDarkDialog

    /**
     * Get theme for translucent activities.
     *
     * @return Theme resource
     */
    val transparentStyle: Int
        get() = R.style.HomeDarkTranslucent

    /**
     * Get window background color based on current theme.
     *
     * @return Color
     */
    val backgroundStyle: Int
        get() = getColor(R.color.themeBackground)

    /**
     * Get status bar color for reminder window based on current theme.
     *
     * @return Color
     */
    val statusBarStyle: Int
        get() = colorPrimaryDark()

    /**
     * Get background color for CardView based on current theme.
     *
     * @return Color
     */
    val cardStyle: Int
        get() = getColor(R.color.themePrimaryDark)

    /**
     * Get color from resource.
     *
     * @param color resource.
     * @return Color
     */
    fun getColor(color: Int): Int {
        return ViewUtils.getColor(mContext, color)
    }

    /**
     * Get current theme primary color.
     *
     * @return Color
     */
    fun colorPrimary(): Int {
        return getColor(R.color.themePrimary)
    }

    /**
     * Get accent color.
     *
     * @return Color
     */
    fun colorAccent(): Int {
        return getColor(R.color.greenPrimary)
    }

    /**
     * Get status bar color based on current application theme.
     *
     * @return Color
     */
    fun colorPrimaryDark(): Int {
        return getColor(R.color.themePrimaryDark)
    }

    /**
     * Get fill amd stroke color by marker color, for drawing circle around marker on Google Map.
     *
     * @param color marker color.
     * @return color resources array
     */
    fun getMarkerRadiusStyle(color: Int): IntArray {
        val fillColor: Int
        val strokeColor: Int
        when (color) {
            0 -> {
                fillColor = R.color.red50
                strokeColor = R.color.redPrimaryDark
            }
            1 -> {
                fillColor = R.color.green50
                strokeColor = R.color.greenPrimaryDark
            }
            2 -> {
                fillColor = R.color.blue50
                strokeColor = R.color.bluePrimaryDark
            }
            3 -> {
                fillColor = R.color.yellow50
                strokeColor = R.color.yellowPrimaryDark
            }
            4 -> {
                fillColor = R.color.greenLight50
                strokeColor = R.color.greenLightPrimaryDark
            }
            5 -> {
                fillColor = R.color.blueLight50
                strokeColor = R.color.blueLightPrimaryDark
            }
            6 -> {
                fillColor = R.color.cyan50
                strokeColor = R.color.cyanPrimaryDark
            }
            7 -> {
                fillColor = R.color.purple50
                strokeColor = R.color.purplePrimaryDark
            }
            8 -> {
                fillColor = R.color.orange50
                strokeColor = R.color.orangePrimaryDark
            }
            9 -> {
                fillColor = R.color.pink50
                strokeColor = R.color.pinkPrimaryDark
            }
            10 -> {
                fillColor = R.color.teal50
                strokeColor = R.color.tealPrimaryDark
            }
            11 -> {
                fillColor = R.color.amber50
                strokeColor = R.color.amberPrimaryDark
            }
            12 -> {
                fillColor = R.color.purpleDeep50
                strokeColor = R.color.purpleDeepPrimaryDark
            }
            13 -> {
                fillColor = R.color.orangeDeep50
                strokeColor = R.color.orangeDeepPrimaryDark
            }
            14 -> {
                fillColor = R.color.indigo50
                strokeColor = R.color.indigoPrimaryDark
            }
            15 -> {
                fillColor = R.color.lime50
                strokeColor = R.color.limePrimaryDark
            }
            else -> {
                fillColor = R.color.blue50
                strokeColor = R.color.bluePrimaryDark
            }
        }
        return intArrayOf(fillColor, strokeColor)
    }

    /**
     * Get marker icon by code.
     *
     * @param code code of marker icon.
     * @return Drawable resource
     */
    fun getMarkerStyle(code: Int): Int {
        val color: Int
        when (code) {
            0 -> color = R.drawable.marker_red
            1 -> color = R.drawable.marker_green
            2 -> color = R.drawable.marker_blue
            3 -> color = R.drawable.marker_yellow
            4 -> color = R.drawable.marker_green_light
            5 -> color = R.drawable.marker_blue_light
            6 -> color = R.drawable.marker_cyan
            7 -> color = R.drawable.marker_violet
            8 -> color = R.drawable.marker_orange
            9 -> color = R.drawable.marker_pink
            10 -> color = R.drawable.marker_teal
            11 -> color = R.drawable.marker_amber
            12 -> color = R.drawable.marker_deep_purple
            13 -> color = R.drawable.marker_deep_orange
            14 -> color = R.drawable.marker_indigo
            15 -> color = R.drawable.marker_lime
            else -> color = R.drawable.marker_blue
        }
        return color
    }

    companion object {

        val NUM_OF_MARKERS = 16
    }
}
