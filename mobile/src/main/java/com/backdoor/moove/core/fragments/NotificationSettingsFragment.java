package com.backdoor.moove.core.fragments;


import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.backdoor.moove.R;
import com.backdoor.moove.core.consts.Configs;
import com.backdoor.moove.core.consts.Constants;
import com.backdoor.moove.core.consts.Prefs;
import com.backdoor.moove.core.helper.Dialogues;
import com.backdoor.moove.core.helper.Module;
import com.backdoor.moove.core.helper.Permissions;
import com.backdoor.moove.core.helper.SharedPrefs;
import com.backdoor.moove.core.views.PrefsView;

import java.io.File;

public class NotificationSettingsFragment extends Fragment implements View.OnClickListener,
        DialogInterface.OnDismissListener {

    private SharedPrefs sPrefs;
    private ActionBar ab;
    private TextView locale;

    private PrefsView blurPrefs, vibrationOptionPrefs, infiniteVibrateOptionPrefs,
            soundOptionPrefs, infiniteSoundOptionPrefs, ttsPrefs, wakeScreenOptionPrefs,
            unlockScreenPrefs, silentSMSOptionPrefs, ledPrefs, chooseSoundPrefs,
            chooseLedColorPrefs, silentCallOptionPrefs;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.settings_notification, container, false);
        ab = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (ab != null) {
            ab.setTitle(R.string.notification);
        }
        sPrefs = SharedPrefs.getInstance(getActivity());
        if (Module.isLollipop()) {
            rootView.findViewById(R.id.imageCard).setElevation(Configs.CARD_ELEVATION);
            rootView.findViewById(R.id.soundCard).setElevation(Configs.CARD_ELEVATION);
            rootView.findViewById(R.id.systemCard).setElevation(Configs.CARD_ELEVATION);
        }
        TextView selectImage = rootView.findViewById(R.id.selectImage);
        selectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialogues.imageDialog(getActivity(), new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {

                    }
                });
            }
        });

        blurPrefs = rootView.findViewById(R.id.blurPrefs);
        blurPrefs.setOnClickListener(this);
        blurPrefs.setChecked(sPrefs.loadBoolean(Prefs.REMINDER_IMAGE_BLUR));
        blurPrefs.setEnabled(true);

        vibrationOptionPrefs = rootView.findViewById(R.id.vibrationOptionPrefs);
        vibrationOptionPrefs.setOnClickListener(this);
        vibrationOptionPrefs.setChecked(sPrefs.loadBoolean(Prefs.VIBRATION_STATUS));

        infiniteVibrateOptionPrefs = rootView.findViewById(R.id.infiniteVibrateOptionPrefs);
        infiniteVibrateOptionPrefs.setOnClickListener(this);
        infiniteVibrateOptionPrefs.setChecked(sPrefs.loadBoolean(Prefs.INFINITE_VIBRATION));

        soundOptionPrefs = rootView.findViewById(R.id.soundOptionPrefs);
        soundOptionPrefs.setOnClickListener(this);
        soundOptionPrefs.setChecked(sPrefs.loadBoolean(Prefs.SILENT_SOUND));

        infiniteSoundOptionPrefs = rootView.findViewById(R.id.infiniteSoundOptionPrefs);
        infiniteSoundOptionPrefs.setOnClickListener(this);
        infiniteSoundOptionPrefs.setChecked(sPrefs.loadBoolean(Prefs.INFINITE_SOUND));

        ttsPrefs = rootView.findViewById(R.id.ttsPrefs);
        ttsPrefs.setOnClickListener(this);
        ttsPrefs.setChecked(sPrefs.loadBoolean(Prefs.TTS));

        wakeScreenOptionPrefs = rootView.findViewById(R.id.wakeScreenOptionPrefs);
        wakeScreenOptionPrefs.setOnClickListener(this);
        wakeScreenOptionPrefs.setChecked(sPrefs.loadBoolean(Prefs.WAKE_STATUS));

        unlockScreenPrefs = rootView.findViewById(R.id.unlockScreenPrefs);
        unlockScreenPrefs.setOnClickListener(this);
        unlockScreenPrefs.setChecked(sPrefs.loadBoolean(Prefs.UNLOCK_DEVICE));

        silentSMSOptionPrefs = rootView.findViewById(R.id.silentSMSOptionPrefs);
        silentSMSOptionPrefs.setOnClickListener(this);
        silentSMSOptionPrefs.setChecked(sPrefs.loadBoolean(Prefs.SILENT_SMS));

        silentCallOptionPrefs = rootView.findViewById(R.id.silentCallOptionPrefs);
        silentCallOptionPrefs.setOnClickListener(this);
        silentCallOptionPrefs.setChecked(sPrefs.loadBoolean(Prefs.SILENT_CALL));

        chooseSoundPrefs = rootView.findViewById(R.id.chooseSoundPrefs);
        chooseSoundPrefs.setOnClickListener(this);

        showMelody();

        TextView volume = rootView.findViewById(R.id.volume);
        volume.setOnClickListener(this);

        locale = rootView.findViewById(R.id.locale);
        locale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialogues.ttsLocale(getActivity(), Prefs.TTS_LOCALE);
            }
        });

        ledPrefs = rootView.findViewById(R.id.ledPrefs);
        chooseLedColorPrefs = rootView.findViewById(R.id.chooseLedColorPrefs);

        checkVibrate();

        checkTTS();

        ledPrefs.setOnClickListener(this);
        ledPrefs.setVisibility(View.VISIBLE);
        ledPrefs.setChecked(sPrefs.loadBoolean(Prefs.LED_STATUS));

        chooseLedColorPrefs.setVisibility(View.VISIBLE);
        chooseLedColorPrefs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dialogues.ledColor(getActivity());
            }
        });

        checkEnabling();

        return rootView;
    }

    private void checkTTS() {
        if (ttsPrefs.isChecked()) {
            locale.setEnabled(true);
        } else {
            locale.setEnabled(false);
        }
    }

    private void checkVibrate() {
        if (vibrationOptionPrefs.isChecked()) {
            infiniteVibrateOptionPrefs.setEnabled(true);
        } else {
            infiniteVibrateOptionPrefs.setEnabled(false);
        }
    }

    private void showMelody() {
        if (sPrefs.loadBoolean(Prefs.CUSTOM_SOUND)) {
            if (sPrefs.isString(Prefs.CUSTOM_SOUND_FILE)) {
                String path = sPrefs.loadPrefs(Prefs.CUSTOM_SOUND_FILE);
                if (!path.matches("")) {
                    File sound = new File(path);
                    if (sound.exists()) {
                        String fileName = sound.getName();
                        int pos = fileName.lastIndexOf(".");
                        String fileNameS = fileName.substring(0, pos);
                        chooseSoundPrefs.setDetailText(fileNameS);
                    } else {
                        chooseSoundPrefs.setDetailText(getActivity().getString(R.string.default_string));
                    }
                } else {
                    chooseSoundPrefs.setDetailText(getActivity().getString(R.string.default_string));
                }
            }
        } else {
            chooseSoundPrefs.setDetailText(getResources().getString(R.string.default_string));
        }
    }

    private void vibrationChange() {
        if (vibrationOptionPrefs.isChecked()) {
            sPrefs.saveBoolean(Prefs.VIBRATION_STATUS, false);
            vibrationOptionPrefs.setChecked(false);
        } else {
            sPrefs.saveBoolean(Prefs.VIBRATION_STATUS, true);
            vibrationOptionPrefs.setChecked(true);
        }
        checkVibrate();
    }

    private void infiniteVibrationChange() {
        if (infiniteVibrateOptionPrefs.isChecked()) {
            sPrefs.saveBoolean(Prefs.INFINITE_VIBRATION, false);
            infiniteVibrateOptionPrefs.setChecked(false);
        } else {
            sPrefs.saveBoolean(Prefs.INFINITE_VIBRATION, true);
            infiniteVibrateOptionPrefs.setChecked(true);
        }
    }

    private void ttsChange() {
        if (ttsPrefs.isChecked()) {
            sPrefs.saveBoolean(Prefs.TTS, false);
            ttsPrefs.setChecked(false);
        } else {
            sPrefs.saveBoolean(Prefs.TTS, true);
            ttsPrefs.setChecked(true);
            Dialogues.ttsLocale(getActivity(), Prefs.TTS_LOCALE);
        }
        checkTTS();
    }

    private void blurChange() {
        if (blurPrefs.isChecked()) {
            sPrefs.saveBoolean(Prefs.REMINDER_IMAGE_BLUR, false);
            blurPrefs.setChecked(false);
        } else {
            sPrefs.saveBoolean(Prefs.REMINDER_IMAGE_BLUR, true);
            blurPrefs.setChecked(true);
        }
    }

    private void soundChange() {
        if (soundOptionPrefs.isChecked()) {
            sPrefs.saveBoolean(Prefs.SILENT_SOUND, false);
            soundOptionPrefs.setChecked(false);
        } else {
            sPrefs.saveBoolean(Prefs.SILENT_SOUND, true);
            soundOptionPrefs.setChecked(true);
        }
    }

    private void infiniteSoundChange() {
        if (infiniteSoundOptionPrefs.isChecked()) {
            sPrefs.saveBoolean(Prefs.INFINITE_SOUND, false);
            infiniteSoundOptionPrefs.setChecked(false);
        } else {
            sPrefs.saveBoolean(Prefs.INFINITE_SOUND, true);
            infiniteSoundOptionPrefs.setChecked(true);
        }
    }

    private void checkEnabling() {
        if (ledPrefs.isChecked()) {
            chooseLedColorPrefs.setEnabled(true);
        } else {
            chooseLedColorPrefs.setEnabled(false);
        }
    }

    private void ledChange() {
        if (ledPrefs.isChecked()) {
            sPrefs.saveBoolean(Prefs.LED_STATUS, false);
            ledPrefs.setChecked(false);
        } else {
            sPrefs.saveBoolean(Prefs.LED_STATUS, true);
            ledPrefs.setChecked(true);
        }
        checkEnabling();
    }

    private void silentSMSChange() {
        if (silentSMSOptionPrefs.isChecked()) {
            sPrefs.saveBoolean(Prefs.SILENT_SMS, false);
            silentSMSOptionPrefs.setChecked(false);
        } else {
            sPrefs.saveBoolean(Prefs.SILENT_SMS, true);
            silentSMSOptionPrefs.setChecked(true);
        }
    }

    private void silentCallChange() {
        if (silentCallOptionPrefs.isChecked()) {
            sPrefs.saveBoolean(Prefs.SILENT_CALL, false);
            silentCallOptionPrefs.setChecked(false);
        } else {
            sPrefs.saveBoolean(Prefs.SILENT_CALL, true);
            silentCallOptionPrefs.setChecked(true);
        }
    }

    private void wakeChange() {
        if (wakeScreenOptionPrefs.isChecked()) {
            sPrefs.saveBoolean(Prefs.WAKE_STATUS, false);
            wakeScreenOptionPrefs.setChecked(false);
        } else {
            sPrefs.saveBoolean(Prefs.WAKE_STATUS, true);
            wakeScreenOptionPrefs.setChecked(true);
        }
    }

    private void unlockChange() {
        if (unlockScreenPrefs.isChecked()) {
            sPrefs.saveBoolean(Prefs.UNLOCK_DEVICE, false);
            unlockScreenPrefs.setChecked(false);
        } else {
            sPrefs.saveBoolean(Prefs.UNLOCK_DEVICE, true);
            unlockScreenPrefs.setChecked(true);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        ab = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (ab != null) {
            ab.setTitle(R.string.settings);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.blurPrefs:
                blurChange();
                break;
            case R.id.vibrationOptionPrefs:
                vibrationChange();
                break;
            case R.id.soundOptionPrefs:
                soundChange();
                break;
            case R.id.infiniteSoundOptionPrefs:
                infiniteSoundChange();
                break;
            case R.id.chooseSoundPrefs:
                if (Permissions.checkPermission(getActivity(), Permissions.READ_EXTERNAL)) {
                    Dialogues.melodyType(getActivity(), Prefs.CUSTOM_SOUND, 201);
                } else {
                    Permissions.requestPermission(getActivity(), 103,
                            Permissions.READ_EXTERNAL, Permissions.WRITE_EXTERNAL);
                }
                break;
            case R.id.infiniteVibrateOptionPrefs:
                infiniteVibrationChange();
                break;
            case R.id.silentSMSOptionPrefs:
                silentSMSChange();
                break;
            case R.id.wakeScreenOptionPrefs:
                wakeChange();
                break;
            case R.id.unlockScreenPrefs:
                unlockChange();
                break;
            case R.id.volume:
                Dialogues.dialogWithSeek(getActivity(), 25, Prefs.VOLUME, getString(R.string.volume), this);
                break;
            case R.id.ttsPrefs:
                ttsChange();
                break;
            case R.id.ledPrefs:
                ledChange();
                break;
            case R.id.silentCallOptionPrefs:
                silentCallChange();
                break;
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        showMelody();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        showMelody();
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 101:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    if (Module.isKitkat()) {
                        intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                        intent.addCategory(Intent.CATEGORY_OPENABLE);
                    }
                    intent.setType("image/*");
                    Intent chooser = Intent.createChooser(intent, getActivity().getString(R.string.select_image));
                    getActivity().startActivityForResult(chooser, Constants.ACTION_REQUEST_GALLERY);
                } else {
                    Permissions.showInfo(getActivity(), Permissions.READ_CALENDAR);
                }
                break;
        }
    }
}
