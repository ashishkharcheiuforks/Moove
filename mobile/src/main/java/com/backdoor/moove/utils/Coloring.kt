package com.backdoor.moove.utils

import android.content.Context
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat

import com.backdoor.moove.R

class Coloring(private val mContext: Context) {

    val style: Int
        get() = R.style.HomeDark

    fun getColor(color: Int): Int {
        return ContextCompat.getColor(mContext, color)
    }

    fun getMarkerRadiusStyle(color: Int): Marker {
        val fillColor: Int
        val strokeColor: Int
        when (color) {
            RED -> {
                fillColor = R.color.red50
                strokeColor = R.color.redPrimaryDark
            }
            GREEN -> {
                fillColor = R.color.green50
                strokeColor = R.color.greenPrimaryDark
            }
            BLUE -> {
                fillColor = R.color.blue50
                strokeColor = R.color.bluePrimaryDark
            }
            YELLOW -> {
                fillColor = R.color.yellow50
                strokeColor = R.color.yellowPrimaryDark
            }
            LIGHT_GREEN -> {
                fillColor = R.color.greenLight50
                strokeColor = R.color.greenLightPrimaryDark
            }
            LIGHT_BLUE -> {
                fillColor = R.color.blueLight50
                strokeColor = R.color.blueLightPrimaryDark
            }
            CYAN -> {
                fillColor = R.color.cyan50
                strokeColor = R.color.cyanPrimaryDark
            }
            PURPLE -> {
                fillColor = R.color.purple50
                strokeColor = R.color.purplePrimaryDark
            }
            ORANGE -> {
                fillColor = R.color.orange50
                strokeColor = R.color.orangePrimaryDark
            }
            PINK -> {
                fillColor = R.color.pink50
                strokeColor = R.color.pinkPrimaryDark
            }
            TEAL -> {
                fillColor = R.color.teal50
                strokeColor = R.color.tealPrimaryDark
            }
            AMBER -> {
                fillColor = R.color.amber50
                strokeColor = R.color.amberPrimaryDark
            }
            DEEP_PURPLE -> {
                fillColor = R.color.purpleDeep50
                strokeColor = R.color.purpleDeepPrimaryDark
            }
            DEEP_ORANGE -> {
                fillColor = R.color.orangeDeep50
                strokeColor = R.color.orangeDeepPrimaryDark
            }
            INDIGO -> {
                fillColor = R.color.indigo50
                strokeColor = R.color.indigoPrimaryDark
            }
            LIME -> {
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

    @ColorInt
    fun accentColor(code: Int): Int {
        val color: Int
        when (code) {
            RED -> color = R.color.redAccent
            PINK -> color = R.color.pinkAccent
            PURPLE -> color = R.color.purpleAccent
            DEEP_PURPLE -> color = R.color.purpleDeepAccent
            INDIGO -> color = R.color.indigoAccent
            BLUE -> color = R.color.blueAccent
            LIGHT_BLUE -> color = R.color.blueLightAccent
            CYAN -> color = R.color.cyanAccent
            TEAL -> color = R.color.tealAccent
            GREEN -> color = R.color.greenAccent
            LIGHT_GREEN -> color = R.color.greenLightAccent
            LIME -> color = R.color.limeAccent
            YELLOW -> color = R.color.yellowAccent
            AMBER -> color = R.color.amberAccent
            ORANGE -> color = R.color.orangeAccent
            DEEP_ORANGE -> color = R.color.orangeDeepAccent
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
    }
}
