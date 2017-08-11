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
import com.backdoor.moove.core.helper.Coloring;
import com.backdoor.moove.core.helper.Messages;
import com.backdoor.moove.core.helper.Notifier;

public class LedColor extends Activity {

    private ListView musicList;
    private NotificationManagerCompat mNotifyMgr;
    private NotificationCompat.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Coloring cs = new Coloring(LedColor.this);
        setTheme(cs.getDialogStyle());
        setContentView(R.layout.music_list_dilog);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        findViewById(R.id.windowBackground).setBackgroundColor(cs.getBackgroundStyle());
        TextView dialogTitle = findViewById(R.id.dialogTitle);
        dialogTitle.setText(getString(R.string.led_color));

        musicList = findViewById(R.id.musicList);
        musicList.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);

        String[] colors = new String[LED.NUM_OF_LEDS];
        for (int i = 0; i < LED.NUM_OF_LEDS; i++) {
            colors[i] = LED.getTitle(this, i);
        }

        final ArrayAdapter<String> adapter = new ArrayAdapter<>(LedColor.this,
                android.R.layout.simple_list_item_single_choice, colors);
        musicList.setAdapter(adapter);

        musicList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (i != -1) {
                    Messages.toast(LedColor.this, getString(R.string.turn_screen_off_to_see_led_light));
                    showLED(LED.getLED(i));
                }
            }
        });

        TextView musicDialogOk = findViewById(R.id.musicDialogOk);
        musicDialogOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = musicList.getCheckedItemPosition();
                if (position != -1) {
                    mNotifyMgr = NotificationManagerCompat.from(LedColor.this);
                    mNotifyMgr.cancel(1);
                    Intent i = new Intent();
                    i.putExtra(Constants.SELECTED_LED_COLOR, position);
                    setResult(RESULT_OK, i);
                    finish();
                } else {
                    Messages.toast(LedColor.this, getString(R.string.select_one_of_item));
                }
            }
        });
    }

    private void showLED(int color) {
        mNotifyMgr = NotificationManagerCompat.from(LedColor.this);
        mNotifyMgr.cancel(1);
        builder = new NotificationCompat.Builder(LedColor.this, Notifier.CHANNEL_SYSTEM);
        builder.setLights(color, 500, 1000);
        mNotifyMgr = NotificationManagerCompat.from(LedColor.this);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mNotifyMgr.notify(1, builder.build());
            }
        }, 3000);
    }
}
