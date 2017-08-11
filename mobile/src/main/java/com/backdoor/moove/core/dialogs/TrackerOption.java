package com.backdoor.moove.core.dialogs;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.backdoor.moove.R;
import com.backdoor.moove.core.async.DisableAsync;
import com.backdoor.moove.core.consts.Prefs;
import com.backdoor.moove.core.helper.Coloring;
import com.backdoor.moove.core.helper.SharedPrefs;

public class TrackerOption extends Activity {

    private SeekBar radiusBar, timeBar;
    private TextView radiusValue, timeValue;
    private SharedPrefs sPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Coloring cs = new Coloring(TrackerOption.this);
        setTheme(cs.getDialogStyle());
        setContentView(R.layout.tracker_settings_layout);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        findViewById(R.id.windowBackground).setBackgroundColor(cs.getBackgroundStyle());
        sPrefs = SharedPrefs.getInstance(this);

        radiusValue = findViewById(R.id.radiusValue);
        radiusValue.setText(sPrefs.loadInt(Prefs.TRACK_DISTANCE) + getString(R.string.m));

        radiusBar = findViewById(R.id.radiusBar);
        radiusBar.setMax(499);
        radiusBar.setProgress(sPrefs.loadInt(Prefs.TRACK_DISTANCE) - 1);
        radiusBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                radiusValue.setText((i + 1) + getString(R.string.m));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        timeValue = findViewById(R.id.timeValue);
        timeValue.setText(sPrefs.loadInt(Prefs.TRACK_TIME) + getString(R.string.s));

        timeBar = findViewById(R.id.timeBar);
        timeBar.setMax(119);
        timeBar.setProgress(sPrefs.loadInt(Prefs.TRACK_TIME) - 1);
        timeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                timeValue.setText((i + 1) + getString(R.string.s));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        TextView aboutClose = findViewById(R.id.aboutClose);
        aboutClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sPrefs.saveInt(Prefs.TRACK_DISTANCE, radiusBar.getProgress() + 1);
                sPrefs.saveInt(Prefs.TRACK_TIME, timeBar.getProgress() + 1);
                new DisableAsync(TrackerOption.this).execute();
                finish();
            }
        });
    }
}