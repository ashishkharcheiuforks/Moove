package com.backdoor.moove.core.helper;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;

import com.backdoor.moove.R;
import com.backdoor.moove.core.consts.Prefs;
import com.backdoor.moove.core.utils.ViewUtils;

public class ColorSetter {

    private Context mContext;
    private SharedPrefs sPrefs;

    public ColorSetter(Context context){
        this.mContext = context;
    }

    /**
     * Method to get typeface by style code;
     * @param style code of style
     * @return typeface
     */
    public Typeface getTypeface(int style){
        Typeface typeface;
        if (style == 0) {
            typeface = Typeface.createFromAsset(mContext.getAssets(), "fonts/Roboto-Black.ttf");
        } else if (style == 1) {
            typeface = Typeface.createFromAsset(mContext.getAssets(), "fonts/Roboto-BlackItalic.ttf");
        } else if (style == 2) {
            typeface = Typeface.createFromAsset(mContext.getAssets(), "fonts/Roboto-Bold.ttf");
        } else if (style == 3) {
            typeface = Typeface.createFromAsset(mContext.getAssets(), "fonts/Roboto-BoldItalic.ttf");
        } else if (style == 4) {
            typeface = Typeface.createFromAsset(mContext.getAssets(), "fonts/Roboto-Italic.ttf");
        } else if (style == 5) {
            typeface = Typeface.createFromAsset(mContext.getAssets(), "fonts/Roboto-Light.ttf");
        } else if (style == 6) {
            typeface = Typeface.createFromAsset(mContext.getAssets(), "fonts/Roboto-LightItalic.ttf");
        } else if (style == 7) {
            typeface = Typeface.createFromAsset(mContext.getAssets(), "fonts/Roboto-Medium.ttf");
        } else if (style == 8) {
            typeface = Typeface.createFromAsset(mContext.getAssets(), "fonts/Roboto-MediumItalic.ttf");
        } else if (style == 9) {
            typeface = Typeface.createFromAsset(mContext.getAssets(), "fonts/Roboto-Regular.ttf");
        } else if (style == 10) {
            typeface = Typeface.createFromAsset(mContext.getAssets(), "fonts/Roboto-Thin.ttf");
        } else if (style == 11) {
            typeface = Typeface.createFromAsset(mContext.getAssets(), "fonts/Roboto-ThinItalic.ttf");
        } else {
            typeface = Typeface.createFromAsset(mContext.getAssets(), "fonts/Roboto-Light.ttf");
        }
        return typeface;
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
        return getColor(R.color.material_grey);
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
        return getColor(R.color.material_grey);
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
        return getColor(R.color.material_grey);
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
        if (color == 1) {
            fillColor = R.color.redA100;
            strokeColor = R.color.redPrimaryDark;
        } else if (color == 2) {
            fillColor = R.color.greenA100;
            strokeColor = R.color.greenPrimaryDark;
        } else if (color == 3) {
            fillColor = R.color.blueA100;
            strokeColor = R.color.bluePrimaryDark;
        } else if (color == 4) {
            fillColor = R.color.yellowA100;
            strokeColor = R.color.yellowPrimaryDark;
        } else if (color == 5) {
            fillColor = R.color.greenLightA100;
            strokeColor = R.color.greenLightPrimaryDark;
        } else if (color == 6) {
            fillColor = R.color.blueLightA100;
            strokeColor = R.color.blueLightPrimaryDark;
        } else if (color == 7) {
            fillColor = R.color.cyanA100;
            strokeColor = R.color.cyanPrimaryDark;
        } else if (color == 8) {
            fillColor = R.color.purpleA100;
            strokeColor = R.color.purplePrimaryDark;
        } else if (color == 9) {
            fillColor = R.color.amberA100;
            strokeColor = R.color.amberPrimaryDark;
        } else if (color == 10) {
            fillColor = R.color.orangeA100;
            strokeColor = R.color.orangePrimaryDark;
        } else if (color == 11) {
            fillColor = R.color.pinkA100;
            strokeColor = R.color.pinkPrimaryDark;
        } else if (color == 12) {
            fillColor = R.color.tealA100;
            strokeColor = R.color.tealPrimaryDark;
        } else if (color == 13) {
            fillColor = R.color.purpleDeepA100;
            strokeColor = R.color.purpleDeepPrimaryDark;
        } else if (color == 14) {
            fillColor = R.color.orangeDeepA100;
            strokeColor = R.color.orangeDeepPrimaryDark;
        } else if (color == 15) {
            fillColor = R.color.indigoA100;
            strokeColor = R.color.indigoPrimaryDark;
        } else if (color == 16) {
            fillColor = R.color.limeA100;
            strokeColor = R.color.limePrimaryDark;
        } else {
            fillColor = R.color.blueA100;
            strokeColor = R.color.bluePrimaryDark;
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
        if (code == 1) {
            color = R.drawable.marker_red;
        } else if (code == 2) {
            color = R.drawable.marker_green;
        } else if (code == 3) {
            color = R.drawable.marker_blue;
        } else if (code == 4) {
            color = R.drawable.marker_yellow;
        } else if (code == 5) {
            color = R.drawable.marker_green_light;
        } else if (code == 6) {
            color = R.drawable.marker_blue_light;
        } else if (code == 7) {
            color = R.drawable.marker_grey;
        } else if (code == 8) {
            color = R.drawable.marker_violet;
        } else if (code == 9) {
            color = R.drawable.marker_brown;
        } else if (code == 10) {
            color = R.drawable.marker_orange;
        } else if (code == 11) {
            color = R.drawable.marker_pink;
        } else if (code == 12) {
            color = R.drawable.marker_teal;
        } else if (code == 13) {
            color = R.drawable.marker_deep_purple;
        } else if (code == 14) {
            color = R.drawable.marker_deep_orange;
        } else if (code == 15) {
            color = R.drawable.marker_indigo;
        } else if (code == 16) {
            color = R.drawable.marker_lime;
        } else {
            color = R.drawable.marker_blue;
        }
        return color;
    }
}
