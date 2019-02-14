package com.backdoor.moove.utils

import android.content.Context
import androidx.annotation.ColorInt

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
    fun getMarkerRadiusStyle(color: Int): Marker {
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
        return Marker(fillColor, strokeColor)
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

    @ColorInt
    fun accentColor(code: Int): Int {
        val color: Int
        when (code) {
            RED -> color = R.color.redAccent
            PURPLE -> color = R.color.purpleAccent
            LIGHT_GREEN -> color = R.color.greenLightAccent
            GREEN -> color = R.color.greenAccent
            LIGHT_BLUE -> color = R.color.blueLightAccent
            BLUE -> color = R.color.blueAccent
            YELLOW -> color = R.color.yellowAccent
            ORANGE -> color = R.color.orangeAccent
            CYAN -> color = R.color.cyanAccent
            PINK -> color = R.color.pinkAccent
            TEAL -> color = R.color.tealAccent
            AMBER -> color = R.color.amberAccent
            DEEP_PURPLE -> color = R.color.purpleDeepAccent
            DEEP_ORANGE -> color = R.color.orangeDeepAccent
            LIME -> color = R.color.limeAccent
            INDIGO -> color = R.color.indigoAccent
            else -> color = R.color.blueAccent
        }
        return getColor(color)
    }

    @ColorInt
    fun colorsForSlider(): IntArray {
        return intArrayOf(
                getColor(R.color.redAccent),
                getColor(R.color.pinkAccent),
                getColor(R.color.purpleAccent),
                getColor(R.color.purpleDeepAccent),
                getColor(R.color.indigoAccent),
                getColor(R.color.blueAccent),
                getColor(R.color.blueLightAccent),
                getColor(R.color.cyanAccent),
                getColor(R.color.tealAccent),
                getColor(R.color.greenAccent),
                getColor(R.color.greenLightAccent),
                getColor(R.color.limeAccent),
                getColor(R.color.yellowAccent),
                getColor(R.color.amberAccent),
                getColor(R.color.orangeAccent),
                getColor(R.color.orangeDeepAccent)
        )
    }

    data class Marker(val fillColor: Int, val strokeColor: Int)

    companion object {

        const val RED = 0
        const val PINK = 1
        const val PURPLE = 2
        const val DEEP_PURPLE = 3
        const val INDIGO = 4
        const val BLUE = 5
        const val LIGHT_BLUE = 6
        const val CYAN = 7
        const val TEAL = 8
        const val GREEN = 9
        const val LIGHT_GREEN = 10
        const val LIME = 11
        const val YELLOW = 12
        const val AMBER = 13
        const val ORANGE = 14
        const val DEEP_ORANGE = 15

        const val NUM_OF_MARKERS = 16
    }
}
