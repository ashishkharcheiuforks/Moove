package com.backdoor.moove.core.dialogs;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.backdoor.moove.R;
import com.backdoor.moove.core.consts.Constants;
import com.backdoor.moove.core.consts.LED;
import com.backdoor.moove.core.consts.Prefs;
import com.backdoor.moove.core.helper.ColorSetter;
import com.backdoor.moove.core.helper.Messages;
import com.backdoor.moove.core.helper.SharedPrefs;

public class LedColor extends Activity{

    private SharedPrefs sPrefs;
    private ListView musicList;
    private TextView musicDialogOk;
    private NotificationManagerCompat mNotifyMgr;
    private NotificationCompat.Builder builder;
    private int id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ColorSetter cs = new ColorSetter(LedColor.this);
        setTheme(cs.getDialogStyle());
        setContentView(R.layout.music_list_dilog);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        final Intent intent = getIntent();
        id = intent.getIntExtra(Constants.ITEM_ID_INTENT, 0);

        TextView dialogTitle = (TextView) findViewById(R.id.dialogTitle);
        dialogTitle.setText(getString(R.string.led_color));

        musicList = (ListView) findViewById(R.id.musicList);
        musicList.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);

        String[] colors = new String[]{getString(R.string.white),
                getString(R.string.red),
                getString(R.string.green),
                getString(R.string.blue),
                getString(R.string.orange),
                getString(R.string.yellow),
                getString(R.string.pink),
                getString(R.string.light_green),
                getString(R.string.light_blue)};

        final ArrayAdapter<String> adapter = new ArrayAdapter<>(LedColor.this,
                android.R.layout.simple_list_item_single_choice, colors);
        musicList.setAdapter(adapter);

        sPrefs = new SharedPrefs(LedColor.this);

        musicList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (i != -1) {
                    Messages.toast(LedColor.this, getString(R.string.turn_screen_off_to_see_led_light));
                    builder = new NotificationCompat.Builder(LedColor.this);
                    if (i == 0){
                        showLED(LED.WHITE);
                    } else if (i == 1){
                        showLED(LED.RED);
                    } else if (i == 2){
                        showLED(LED.GREEN);
                    } else if (i == 3){
                        showLED(LED.BLUE);
                    } else if (i == 4){
                        showLED(LED.ORANGE);
                    } else if (i == 5){
                        showLED(LED.YELLOW);
                    } else if (i == 6){
                        showLED(LED.PINK);
                    } else if (i == 7){
                        showLED(LED.GREEN_LIGHT);
                    } else if (i == 8){
                        showLED(LED.BLUE_LIGHT);
                    } else {
                        showLED(LED.BLUE);
                    }
                }
            }
        });

        musicDialogOk = (TextView) findViewById(R.id.musicDialogOk);
        musicDialogOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectedPosition = musicList.getCheckedItemPosition();
                if (selectedPosition != -1) {
                    if (id != 4) {
                        sPrefs = new SharedPrefs(LedColor.this);
                        String prefs = Prefs.LED_COLOR;
                        if (selectedPosition == 0) {
                            sPrefs.saveInt(prefs, LED.WHITE);
                        } else if (selectedPosition == 1) {
                            sPrefs.saveInt(prefs, LED.RED);
                        } else if (selectedPosition == 2) {
                            sPrefs.saveInt(prefs, LED.GREEN);
                        } else if (selectedPosition == 3) {
                            sPrefs.saveInt(prefs, LED.BLUE);
                        } else if (selectedPosition == 4) {
                            sPrefs.saveInt(prefs, LED.ORANGE);
                        } else if (selectedPosition == 5) {
                            sPrefs.saveInt(prefs, LED.YELLOW);
                        } else if (selectedPosition == 6) {
                            sPrefs.saveInt(prefs, LED.PINK);
                        } else if (selectedPosition == 7) {
                            sPrefs.saveInt(prefs, LED.GREEN_LIGHT);
                        } else if (selectedPosition == 8) {
                            sPrefs.saveInt(prefs, LED.BLUE_LIGHT);
                        } else {
                            sPrefs.saveInt(prefs, LED.BLUE);
                        }
                    } else {
                        Intent i = new Intent();
                        i.putExtra(Constants.SELECTED_LED_COLOR, selectedPosition);
                        setResult(RESULT_OK, i);
                    }
                    mNotifyMgr = NotificationManagerCompat.from(LedColor.this);
                    mNotifyMgr.cancel(1);
                    finish();
                } else {
                    Messages.toast(LedColor.this, getString(R.string.select_one_of_item));
                }
            }
        });
    }

    private void showLED(int color){
        musicDialogOk.setEnabled(false);
        mNotifyMgr = NotificationManagerCompat.from(LedColor.this);
        mNotifyMgr.cancel(1);
        builder.setLights(color, 500, 1000);
        mNotifyMgr = NotificationManagerCompat.from(LedColor.this);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mNotifyMgr.notify(1, builder.build());
                musicDialogOk.setEnabled(true);
            }
        }, 3000);
    }
}
