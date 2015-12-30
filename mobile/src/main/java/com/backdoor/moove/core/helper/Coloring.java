package com.backdoor.moove.core.helper;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.backdoor.moove.R;
import com.backdoor.moove.core.consts.Prefs;
import com.backdoor.moove.core.utils.ViewUtils;

public class Coloring {

    public static final int NUM_OF_MARKERS = 16;
    private Context mContext;
    private SharedPrefs sPrefs;

    public Coloring(Context context){
        this.mContext = context;
    }

    /**
     * Get color from resource.
     * @param color resource.
     * @return Color
     */
    private int getColor(int color){
        return ViewUtils.getColor(mContext, color);
    }

    /**
     * Get current theme primary color.
     * @return Color
     */
    public int colorPrimary(){
        return getColor(R.color.themePrimary);
    }

    /**
     * Get accent color.
     * @return Color
     */
    public int colorAccent(){
        return getColor(R.color.greenPrimary);
    }

    /**
     * Get theme for application based on user choice.
     * @return Theme resource
     */
    public int getStyle(){
        return R.style.HomeDark;
    }

    /**
     * Get drawable from resource.
     * @param i resource.
     * @return Drawable
     */
    private Drawable getDrawable(int i){
        return ViewUtils.getDrawable(mContext, i);
    }

    /**
     * Get status bar color based on current application theme.
     * @return Color
     */
    public int colorPrimaryDark(){
        return getColor(R.color.themePrimaryDark);
    }

    /**
     * Get style for spinner based on current theme.
     * @return Color
     */
    public int getSpinnerStyle(){
        return getColor(R.color.themePrimaryDark);
    }

    /**
     * Get theme for dialog styled activity based on current application theme.
     * @return Theme resource
     */
    public int getDialogStyle(){
        return R.style.HomeDarkDialog;
    }

    /**
     * Get theme for fullscreen activities.
     * @return Theme resource
     */
    public int getFullscreenStyle(){
        return R.style.HomeDarkFullscreen;
    }

    /**
     * Get theme for translucent activities.
     * @return Theme resource
     */
    public int getTransparentStyle(){
        return R.style.HomeDarkTranslucent;
    }

    /**
     * Get window background color based on current theme.
     * @return Color
     */
    public int getBackgroundStyle(){
        return getColor(R.color.themeBackground);
    }

    /**
     * Get status bar color for reminder window based on current theme.
     * @return Color
     */
    public int getStatusBarStyle(){
        return colorPrimaryDark();
    }

    /**
     * Get background color for CardView based on current theme.
     * @return Color
     */
    public int getCardStyle(){
        return getColor(R.color.themePrimaryDark);
    }

    /**
     * Get fill amd stroke color for drawing circle around marker on Google Map.
     * @return color resources array
     */
    public int[] getMarkerRadiusStyle(){
        sPrefs = new SharedPrefs(mContext);
        int color = sPrefs.loadInt(Prefs.MARKER_STYLE);
        return getMarkerRadiusStyle(color);
    }

    /**
     * Get fill amd stroke color by marker color, for drawing circle around marker on Google Map.
     * @param color marker color.
     * @return  color resources array
     */
    public int[] getMarkerRadiusStyle(int color){
        int fillColor;
        int strokeColor;
        switch (color) {
            case 0:
                fillColor = R.color.red50;
                strokeColor = R.color.redPrimaryDark;
                break;
            case 1:
                fillColor = R.color.green50;
                strokeColor = R.color.greenPrimaryDark;
                break;
            case 2:
                fillColor = R.color.blue50;
                strokeColor = R.color.bluePrimaryDark;
                break;
            case 3:
                fillColor = R.color.yellow50;
                strokeColor = R.color.yellowPrimaryDark;
                break;
            case 4:
                fillColor = R.color.greenLight50;
                strokeColor = R.color.greenLightPrimaryDark;
                break;
            case 5:
                fillColor = R.color.blueLight50;
                strokeColor = R.color.blueLightPrimaryDark;
                break;
            case 6:
                fillColor = R.color.cyan50;
                strokeColor = R.color.cyanPrimaryDark;
                break;
            case 7:
                fillColor = R.color.purple50;
                strokeColor = R.color.purplePrimaryDark;
                break;
            case 8:
                fillColor = R.color.orange50;
                strokeColor = R.color.orangePrimaryDark;
                break;
            case 9:
                fillColor = R.color.pink50;
                strokeColor = R.color.pinkPrimaryDark;
                break;
            case 10:
                fillColor = R.color.teal50;
                strokeColor = R.color.tealPrimaryDark;
                break;
            case 11:
                fillColor = R.color.amber50;
                strokeColor = R.color.amberPrimaryDark;
                break;
            case 12:
                fillColor = R.color.purpleDeep50;
                strokeColor = R.color.purpleDeepPrimaryDark;
                break;
            case 13:
                fillColor = R.color.orangeDeep50;
                strokeColor = R.color.orangeDeepPrimaryDark;
                break;
            case 14:
                fillColor = R.color.indigo50;
                strokeColor = R.color.indigoPrimaryDark;
                break;
            case 15:
                fillColor = R.color.lime50;
                strokeColor = R.color.limePrimaryDark;
                break;
            default:
                fillColor = R.color.blue50;
                strokeColor = R.color.bluePrimaryDark;
                break;
        }
        return new int[]{fillColor, strokeColor};
    }

    /**
     * Get marker icon, based on user settings.
     * @return Drawable resource
     */
    public int getMarkerStyle(){
        sPrefs = new SharedPrefs(mContext);
        int loaded = sPrefs.loadInt(Prefs.MARKER_STYLE);
        return getMarkerStyle(loaded);
    }

    /**
     * Get marker icon by code.
     * @param code code of marker icon.
     * @return Drawable resource
     */
    public int getMarkerStyle(int code){
        int color;
        switch (code) {
            case 0:
                color = R.drawable.marker_red;
                break;
            case 1:
                color = R.drawable.marker_green;
                break;
            case 2:
                color = R.drawable.marker_blue;
                break;
            case 3:
                color = R.drawable.marker_yellow;
                break;
            case 4:
                color = R.drawable.marker_green_light;
                break;
            case 5:
                color = R.drawable.marker_blue_light;
                break;
            case 6:
                color = R.drawable.marker_cyan;
                break;
            case 7:
                color = R.drawable.marker_violet;
                break;
            case 8:
                color = R.drawable.marker_orange;
                break;
            case 9:
                color = R.drawable.marker_pink;
                break;
            case 10:
                color = R.drawable.marker_teal;
                break;
            case 11:
                color = R.drawable.marker_amber;
                break;
            case 12:
                color = R.drawable.marker_deep_purple;
                break;
            case 13:
                color = R.drawable.marker_deep_orange;
                break;
            case 14:
                color = R.drawable.marker_indigo;
                break;
            case 15:
                color = R.drawable.marker_lime;
                break;
            default:
                color = R.drawable.marker_blue;
                break;
        }
        return color;
    }
}
