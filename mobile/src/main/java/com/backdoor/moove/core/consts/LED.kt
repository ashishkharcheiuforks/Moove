package com.backdoor.moove.core.consts

import android.content.Context

import com.backdoor.moove.R

/**
 * Copyright 2016 Nazar Suhovich
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
object LED {

    val NUM_OF_LEDS = 17

    val WHITE = -0x1
    val RED = -0xbbcca
    val GREEN = -0xb350b0
    val BLUE = -0xde690d
    val ORANGE = -0x6800
    val YELLOW = -0x14c5
    val AMBER = -0x3ef9
    val PINK = -0x16e19d
    val GREEN_LIGHT = -0x743cb6
    val BLUE_LIGHT = -0xfc560c
    val CYAN = -0xff432c
    val PURPLE = -0x63d850
    val LIME = -0x3223c7
    val INDIGO = -0xc0ae4b
    val DEEP_PURPLE = -0x98c549
    val DEEP_ORANGE = -0xa8de
    val TEAL = -0xff6978

    fun getLED(code: Int): Int {
        val color: Int
        when (code) {
            0 -> color = LED.WHITE
            1 -> color = LED.RED
            2 -> color = LED.GREEN
            3 -> color = LED.BLUE
            4 -> color = LED.ORANGE
            5 -> color = LED.YELLOW
            6 -> color = LED.PINK
            7 -> color = LED.GREEN_LIGHT
            8 -> color = LED.BLUE_LIGHT
            9 -> color = LED.PURPLE
            10 -> color = LED.AMBER
            11 -> color = LED.CYAN
            12 -> color = LED.LIME
            13 -> color = LED.INDIGO
            14 -> color = LED.DEEP_ORANGE
            15 -> color = LED.DEEP_PURPLE
            16 -> color = LED.TEAL
            else -> color = LED.BLUE
        }
        return color
    }

    fun getTitle(context: Context, code: Int): String {
        val color: String
        when (code) {
            0 -> color = context.getString(R.string.white)
            1 -> color = context.getString(R.string.red)
            2 -> color = context.getString(R.string.green)
            3 -> color = context.getString(R.string.blue)
            4 -> color = context.getString(R.string.orange)
            5 -> color = context.getString(R.string.yellow)
            6 -> color = context.getString(R.string.pink)
            7 -> color = context.getString(R.string.light_green)
            8 -> color = context.getString(R.string.light_blue)
            9 -> color = context.getString(R.string.purple)
            10 -> color = context.getString(R.string.amber)
            11 -> color = context.getString(R.string.cyan)
            12 -> color = context.getString(R.string.lime)
            13 -> color = context.getString(R.string.indigo)
            14 -> color = context.getString(R.string.deep_orange)
            15 -> color = context.getString(R.string.deep_purple)
            16 -> color = context.getString(R.string.teal)
            else -> color = context.getString(R.string.blue)
        }
        return color
    }
}
