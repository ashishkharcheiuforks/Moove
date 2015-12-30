package com.backdoor.moove;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.backdoor.moove.core.consts.Constants;
import com.backdoor.moove.core.consts.Language;
import com.backdoor.moove.core.consts.Prefs;
import com.backdoor.moove.core.helper.SharedPrefs;
import com.google.android.gms.maps.GoogleMap;

import java.io.File;

/**
 * Application splash screen for checking preferences.
 */
public class SplashScreen extends Activity{

    /**
     * Save initial argument on first application run.
     */
    private void initPrefs() {
        File settingsUI = new File("/data/data/" + getPackageName() +
                "/shared_prefs/" + SharedPrefs.MOOVE_PREFS + ".xml");
        if(!settingsUI.exists()){
            SharedPreferences appUISettings =
                    getSharedPreferences(SharedPrefs.MOOVE_PREFS, Context.MODE_PRIVATE);
            SharedPreferences.Editor uiEd = appUISettings.edit();
            uiEd.putInt(Prefs.MAP_TYPE, GoogleMap.MAP_TYPE_NORMAL);
            uiEd.putString(Prefs.REMINDER_IMAGE, Constants.DEFAULT);
            uiEd.putInt(Prefs.LED_COLOR, 4);
            uiEd.putInt(Prefs.LOCATION_RADIUS, 25);
            uiEd.putInt(Prefs.TRACK_DISTANCE, 1);
            uiEd.putInt(Prefs.TRACK_TIME, 1);
            uiEd.putInt(Prefs.VOLUME, 25);
            uiEd.putInt(Prefs.MARKER_STYLE, 11);

            uiEd.putString(Prefs.TTS_LOCALE, Language.ENGLISH);

            uiEd.putInt(Prefs.APP_RUNS_COUNT, 0);
            uiEd.putBoolean(Prefs.TRACKING_NOTIFICATION, true);
            uiEd.putBoolean(Prefs.RATE_SHOW, false);
            uiEd.putBoolean(Prefs.INFINITE_VIBRATION, false);
            uiEd.putBoolean(Prefs.IS_24_TIME_FORMAT, true);
            uiEd.putBoolean(Prefs.UNLOCK_DEVICE, false);
            uiEd.putBoolean(Prefs.TTS, false);
            uiEd.putBoolean(Prefs.REMINDER_IMAGE_BLUR, true);
            uiEd.putBoolean(Prefs.CUSTOM_SOUND, false);
            uiEd.putBoolean(Prefs.PLACES_AUTO, true);
            uiEd.commit();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        initPrefs();

        checkPrefs();

        /*sPrefs = new SharedPrefs(SplashScreen.this);
        if (!sPrefs.loadBoolean(Prefs.FIRST_LOAD)) {
            startActivity(new Intent(SplashScreen.this, StartHelp.class));
        } else {
            startActivity(new Intent(SplashScreen.this, MainActivity.class));
        }*/

        startActivity(new Intent(SplashScreen.this, MainActivity.class));
        finish();
    }

    /**
     * Check if preference exist. If no save default.
     */
    private void checkPrefs(){
        SharedPrefs sPrefs = new SharedPrefs(SplashScreen.this);
        if (!sPrefs.isString(Prefs.TTS_LOCALE)){
            sPrefs.savePrefs(Prefs.TTS_LOCALE, Language.ENGLISH);
        }
        if (!sPrefs.isString(Prefs.REMINDER_IMAGE)){
            sPrefs.savePrefs(Prefs.REMINDER_IMAGE, Constants.DEFAULT);
        }
        if (!sPrefs.isString(Prefs.TRACK_DISTANCE)){
            sPrefs.saveInt(Prefs.TRACK_DISTANCE, 1);
        }
        if (!sPrefs.isString(Prefs.TRACK_TIME)){
            sPrefs.saveInt(Prefs.TRACK_TIME, 1);
        }
        if (!sPrefs.isString(Prefs.APP_RUNS_COUNT)){
            sPrefs.saveInt(Prefs.APP_RUNS_COUNT, 0);
        }
        if (!sPrefs.isString(Prefs.VOLUME)){
            sPrefs.saveInt(Prefs.VOLUME, 25);
        }
        if (!sPrefs.isString(Prefs.RATE_SHOW)){
            sPrefs.saveBoolean(Prefs.RATE_SHOW, false);
        }
        if (!sPrefs.isString(Prefs.REMINDER_IMAGE_BLUR)){
            sPrefs.saveBoolean(Prefs.REMINDER_IMAGE_BLUR, false);
        }
        if (!sPrefs.isString(Prefs.TTS)){
            sPrefs.saveBoolean(Prefs.TTS, false);
        }
        if (!sPrefs.isString(Prefs.SILENT_SMS)){
            sPrefs.saveBoolean(Prefs.SILENT_SMS, false);
        }
        if (!sPrefs.isString(Prefs.WEAR_NOTIFICATION)){
            sPrefs.saveBoolean(Prefs.WEAR_NOTIFICATION, false);
        }
        if (!sPrefs.isString(Prefs.PLACES_AUTO)){
            sPrefs.saveBoolean(Prefs.PLACES_AUTO, true);
        }
        if (!sPrefs.isString(Prefs.INFINITE_VIBRATION)){
            sPrefs.saveBoolean(Prefs.INFINITE_VIBRATION, false);
        }
        if (!sPrefs.isString(Prefs.IS_24_TIME_FORMAT)){
            sPrefs.saveBoolean(Prefs.IS_24_TIME_FORMAT, true);
        }
        if (!sPrefs.isString(Prefs.UNLOCK_DEVICE)){
            sPrefs.saveBoolean(Prefs.UNLOCK_DEVICE, false);
        }

        if (!sPrefs.isString(Prefs.LED_STATUS)) {
            sPrefs.saveBoolean(Prefs.LED_STATUS, false);
        }
        if (!sPrefs.isString(Prefs.LED_COLOR)) {
            sPrefs.saveInt(Prefs.LED_COLOR, 4);
        }
    }
}
