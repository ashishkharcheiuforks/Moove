package com.backdoor.moove.core.dialogs;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.backdoor.moove.R;
import com.backdoor.moove.core.consts.Constants;
import com.backdoor.moove.core.consts.Prefs;
import com.backdoor.moove.core.helper.Coloring;
import com.backdoor.moove.core.helper.SharedPrefs;

public class TargetRadius extends Activity {

    private SeekBar radiusBar;
    private TextView radiusValue;
    private int progressInt, i;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Coloring cs = new Coloring(TargetRadius.this);
        setTheme(cs.getDialogStyle());
        setContentView(R.layout.radius_dialog_layout);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        findViewById(R.id.windowBackground).setBackgroundColor(cs.getBackgroundStyle());
        Intent intent = getIntent();
        i = intent.getIntExtra("item", 0);
        radiusValue = findViewById(R.id.radiusValue);
        progressInt = SharedPrefs.getInstance(this).loadInt(Prefs.LOCATION_RADIUS);
        radiusValue.setText(progressInt + getString(R.string.m));

        radiusBar = findViewById(R.id.radiusBar);
        radiusBar.setProgress(progressInt);
        radiusBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                progressInt = i;
                radiusValue.setText(progressInt + getString(R.string.m));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        Button plusButton = findViewById(R.id.plusButton);
        plusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                radiusBar.setProgress(progressInt + 1);
            }
        });

        Button minusButton = findViewById(R.id.minusButton);
        minusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                radiusBar.setProgress(progressInt - 1);
            }
        });

        CheckBox transportCheck = findViewById(R.id.transportCheck);
        transportCheck.setVisibility(View.VISIBLE);
        if (progressInt > 2000) {
            transportCheck.setChecked(true);
        }
        if (transportCheck.isChecked()) {
            radiusBar.setMax(5000);
            radiusBar.setProgress(progressInt);
        } else {
            radiusBar.setMax(2000);
            radiusBar.setProgress(progressInt);
        }

        transportCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    radiusBar.setMax(5000);
                    radiusBar.setProgress(progressInt);
                } else {
                    radiusBar.setMax(2000);
                    radiusBar.setProgress(progressInt);
                }
            }
        });

        TextView aboutClose = findViewById(R.id.aboutClose);
        aboutClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (i == 0) {
                    SharedPrefs.getInstance(TargetRadius.this).saveInt(Prefs.LOCATION_RADIUS, radiusBar.getProgress());
                    finish();
                } else {
                    Intent intent = new Intent();
                    intent.putExtra(Constants.SELECTED_RADIUS, radiusBar.getProgress());
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        });
    }
}