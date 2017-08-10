package com.backdoor.moove.core.consts;

import android.content.Context;

import com.backdoor.moove.R;

/**
 * Copyright 2016 Nazar Suhovich
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class LED {

    public static final int NUM_OF_LEDS = 17;

    public static final int WHITE = 0xffffffff;
    public static final int RED = 0xfff44336;
    public static final int GREEN = 0xff4caf50;
    public static final int BLUE = 0xff2196f3;
    public static final int ORANGE = 0xffff9800;
    public static final int YELLOW = 0xffffeb3b;
    public static final int AMBER = 0xffffc107;
    public static final int PINK = 0xffe91e63;
    public static final int GREEN_LIGHT = 0xff8bc34a;
    public static final int BLUE_LIGHT = 0xff03a9f4;
    public static final int CYAN = 0xff00bcd4;
    public static final int PURPLE = 0xff9c27b0;
    public static final int LIME = 0xffcddc39;
    public static final int INDIGO = 0xff3f51b5;
    public static final int DEEP_PURPLE = 0xff673ab7;
    public static final int DEEP_ORANGE = 0xffff5722;
    public static final int TEAL = 0xff009688;

    public static int getLED(int code) {
        int color;
        switch (code) {
            case 0:
                color = LED.WHITE;
                break;
            case 1:
                color = LED.RED;
                break;
            case 2:
                color = LED.GREEN;
                break;
            case 3:
                color = LED.BLUE;
                break;
            case 4:
                color = LED.ORANGE;
                break;
            case 5:
                color = LED.YELLOW;
                break;
            case 6:
                color = LED.PINK;
                break;
            case 7:
                color = LED.GREEN_LIGHT;
                break;
            case 8:
                color = LED.BLUE_LIGHT;
                break;
            case 9:
                color = LED.PURPLE;
                break;
            case 10:
                color = LED.AMBER;
                break;
            case 11:
                color = LED.CYAN;
                break;
            case 12:
                color = LED.LIME;
                break;
            case 13:
                color = LED.INDIGO;
                break;
            case 14:
                color = LED.DEEP_ORANGE;
                break;
            case 15:
                color = LED.DEEP_PURPLE;
                break;
            case 16:
                color = LED.TEAL;
                break;
            default:
                color = LED.BLUE;
                break;
        }
        return color;
    }

    public static String getTitle(Context context, int code) {
        String color;
        switch (code) {
            case 0:
                color = context.getString(R.string.white);
                break;
            case 1:
                color = context.getString(R.string.red);
                break;
            case 2:
                color = context.getString(R.string.green);
                break;
            case 3:
                color = context.getString(R.string.blue);
                break;
            case 4:
                color = context.getString(R.string.orange);
                break;
            case 5:
                color = context.getString(R.string.yellow);
                break;
            case 6:
                color = context.getString(R.string.pink);
                break;
            case 7:
                color = context.getString(R.string.light_green);
                break;
            case 8:
                color = context.getString(R.string.light_blue);
                break;
            case 9:
                color = context.getString(R.string.purple);
                break;
            case 10:
                color = context.getString(R.string.amber);
                break;
            case 11:
                color = context.getString(R.string.cyan);
                break;
            case 12:
                color = context.getString(R.string.lime);
                break;
            case 13:
                color = context.getString(R.string.indigo);
                break;
            case 14:
                color = context.getString(R.string.deep_orange);
                break;
            case 15:
                color = context.getString(R.string.deep_purple);
                break;
            case 16:
                color = context.getString(R.string.teal);
                break;
            default:
                color = context.getString(R.string.blue);
                break;
        }
        return color;
    }
}
