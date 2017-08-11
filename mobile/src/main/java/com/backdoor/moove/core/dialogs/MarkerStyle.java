package com.backdoor.moove.core.dialogs;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.backdoor.moove.R;
import com.backdoor.moove.core.consts.Prefs;
import com.backdoor.moove.core.helper.Coloring;
import com.backdoor.moove.core.helper.SharedPrefs;

public class MarkerStyle extends Activity implements View.OnClickListener {
    private RadioButton red, green, blue, yellow, greenLight, blueLight, grey, purple,
            brown, orange, pink, teal, deepPurple, deepOrange, indigo, lime;
    private RadioGroup themeGroup, themeGroup2, themeGroup3, themeGroup4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Coloring cs = new Coloring(MarkerStyle.this);
        setTheme(cs.getDialogStyle());
        setContentView(R.layout.marker_style_layout);
        findViewById(R.id.windowBackground).setBackgroundColor(cs.getBackgroundStyle());
        TextView themeClose = findViewById(R.id.themeClose);
        themeClose.setOnClickListener(this);

        red = findViewById(R.id.redCheck);
        green = findViewById(R.id.greenCheck);
        blue = findViewById(R.id.blueCheck);
        yellow = findViewById(R.id.yellowCheck);
        greenLight = findViewById(R.id.greenLightCheck);
        blueLight = findViewById(R.id.blueLightCheck);
        grey = findViewById(R.id.greyCheck);
        purple = findViewById(R.id.purpleCheck);
        brown = findViewById(R.id.brownCheck);
        orange = findViewById(R.id.orangeCheck);
        pink = findViewById(R.id.pinkCheck);
        teal = findViewById(R.id.tealCheck);
        deepPurple = findViewById(R.id.deep_purple);
        deepOrange = findViewById(R.id.deep_orange);
        indigo = findViewById(R.id.indigo);
        lime = findViewById(R.id.lime);

        themeGroup = findViewById(R.id.themeGroup);
        themeGroup2 = findViewById(R.id.themeGroup2);
        themeGroup3 = findViewById(R.id.themeGroup3);
        themeGroup4 = findViewById(R.id.themeGroup4);

        themeGroup.clearCheck();
        themeGroup2.clearCheck();
        themeGroup3.clearCheck();
        themeGroup4.clearCheck();
        themeGroup.setOnCheckedChangeListener(listener1);
        themeGroup2.setOnCheckedChangeListener(listener2);
        themeGroup3.setOnCheckedChangeListener(listener3);
        themeGroup4.setOnCheckedChangeListener(listener4);

        setUpRadio();
    }

    private RadioGroup.OnCheckedChangeListener listener1 = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (checkedId != -1) {
                themeGroup2.setOnCheckedChangeListener(null);
                themeGroup3.setOnCheckedChangeListener(null);
                themeGroup4.setOnCheckedChangeListener(null);
                themeGroup2.clearCheck();
                themeGroup3.clearCheck();
                themeGroup4.clearCheck();
                themeGroup2.setOnCheckedChangeListener(listener2);
                themeGroup3.setOnCheckedChangeListener(listener3);
                themeGroup4.setOnCheckedChangeListener(listener4);
                themeColorSwitch(group.getCheckedRadioButtonId());
            }
        }
    };
    private RadioGroup.OnCheckedChangeListener listener2 = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (checkedId != -1) {
                themeGroup.setOnCheckedChangeListener(null);
                themeGroup3.setOnCheckedChangeListener(null);
                themeGroup4.setOnCheckedChangeListener(null);
                themeGroup.clearCheck();
                themeGroup3.clearCheck();
                themeGroup4.clearCheck();
                themeGroup.setOnCheckedChangeListener(listener1);
                themeGroup3.setOnCheckedChangeListener(listener3);
                themeGroup4.setOnCheckedChangeListener(listener4);
                themeColorSwitch(group.getCheckedRadioButtonId());
            }
        }
    };
    private RadioGroup.OnCheckedChangeListener listener3 = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (checkedId != -1) {
                themeGroup.setOnCheckedChangeListener(null);
                themeGroup2.setOnCheckedChangeListener(null);
                themeGroup4.setOnCheckedChangeListener(null);
                themeGroup.clearCheck();
                themeGroup2.clearCheck();
                themeGroup4.clearCheck();
                themeGroup.setOnCheckedChangeListener(listener1);
                themeGroup2.setOnCheckedChangeListener(listener2);
                themeGroup4.setOnCheckedChangeListener(listener4);
                themeColorSwitch(group.getCheckedRadioButtonId());
            }
        }
    };
    private RadioGroup.OnCheckedChangeListener listener4 = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (checkedId != -1) {
                themeGroup.setOnCheckedChangeListener(null);
                themeGroup2.setOnCheckedChangeListener(null);
                themeGroup3.setOnCheckedChangeListener(null);
                themeGroup.clearCheck();
                themeGroup2.clearCheck();
                themeGroup3.clearCheck();
                themeGroup.setOnCheckedChangeListener(listener1);
                themeGroup2.setOnCheckedChangeListener(listener2);
                themeGroup3.setOnCheckedChangeListener(listener3);
                themeColorSwitch(group.getCheckedRadioButtonId());
            }
        }
    };

    public void setUpRadio() {
        int loaded = SharedPrefs.getInstance(this).loadInt(Prefs.MARKER_STYLE);
        switch (loaded) {
            case 0:
                red.setChecked(true);
                break;
            case 1:
                green.setChecked(true);
                break;
            case 2:
                blue.setChecked(true);
                break;
            case 3:
                yellow.setChecked(true);
                break;
            case 4:
                greenLight.setChecked(true);
                break;
            case 5:
                blueLight.setChecked(true);
                break;
            case 6:
                grey.setChecked(true);
                break;
            case 7:
                purple.setChecked(true);
                break;
            case 8:
                orange.setChecked(true);
                break;
            case 9:
                pink.setChecked(true);
                break;
            case 10:
                teal.setChecked(true);
                break;
            case 11:
                brown.setChecked(true);
                break;
            case 12:
                deepPurple.setChecked(true);
                break;
            case 13:
                deepOrange.setChecked(true);
                break;
            case 14:
                indigo.setChecked(true);
                break;
            case 15:
                lime.setChecked(true);
                break;
        }
    }

    private void themeColorSwitch(int radio) {
        switch (radio) {
            case R.id.redCheck:
                saveColor(0);
                break;
            case R.id.greenCheck:
                saveColor(1);
                break;
            case R.id.blueCheck:
                saveColor(2);
                break;
            case R.id.yellowCheck:
                saveColor(3);
                break;
            case R.id.greenLightCheck:
                saveColor(4);
                break;
            case R.id.blueLightCheck:
                saveColor(5);
                break;
            case R.id.greyCheck:
                saveColor(6);
                break;
            case R.id.purpleCheck:
                saveColor(7);
                break;
            case R.id.orangeCheck:
                saveColor(8);
                break;
            case R.id.pinkCheck:
                saveColor(9);
                break;
            case R.id.tealCheck:
                saveColor(10);
                break;
            case R.id.brownCheck:
                saveColor(11);
                break;
            case R.id.deep_purple:
                saveColor(12);
                break;
            case R.id.deep_orange:
                saveColor(13);
                break;
            case R.id.indigo:
                saveColor(14);
                break;
            case R.id.lime:
                saveColor(15);
                break;
        }
    }

    void saveColor(int style) {
        SharedPrefs.getInstance(this).saveInt(Prefs.MARKER_STYLE, style);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.themeClose:
                finish();
                break;
        }
    }
}