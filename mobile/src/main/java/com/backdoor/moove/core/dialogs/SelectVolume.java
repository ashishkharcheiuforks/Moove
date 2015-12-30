package com.backdoor.moove.core.dialogs;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.backdoor.moove.R;
import com.backdoor.moove.core.consts.Constants;
import com.backdoor.moove.core.consts.Prefs;
import com.backdoor.moove.core.helper.Coloring;
import com.backdoor.moove.core.helper.SharedPrefs;

public class SelectVolume extends Activity {

    private TextView radiusValue;
    private ImageView volumeImage;
    private int volume;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Coloring cs = new Coloring(SelectVolume.this);
        setTheme(cs.getDialogStyle());
        setContentView(R.layout.volume_dialog_layout);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        SharedPrefs sPrefs = new SharedPrefs(SelectVolume.this);

        radiusValue = (TextView) findViewById(R.id.radiusValue);
        volumeImage = (ImageView) findViewById(R.id.volumeImage);

        SeekBar radiusBar = (SeekBar) findViewById(R.id.radiusBar);
        int n = sPrefs.loadInt(Prefs.VOLUME);
        radiusBar.setProgress(n);
        radiusValue.setText(String.valueOf(n));
        setValue(n);
        radiusBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                volume = i;
                radiusValue.setText(String.valueOf(i));
                setValue(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        TextView aboutClose = (TextView) findViewById(R.id.aboutClose);
        aboutClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra(Constants.SELECTED_VOLUME, volume);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    private void setValue(int i) {
        if (i < 7 && i > 0){
            volumeImage.setImageResource(R.drawable.ic_volume_mute_white_24dp);
        } else if (i > 18){
            volumeImage.setImageResource(R.drawable.ic_volume_up_white_24dp);
        } else if (i == 0){
            volumeImage.setImageResource(R.drawable.ic_volume_off_white_24dp);
        } else {
            volumeImage.setImageResource(R.drawable.ic_volume_down_white_24dp);
        }
    }
}