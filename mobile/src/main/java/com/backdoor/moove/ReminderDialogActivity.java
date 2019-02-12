package com.backdoor.moove;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.speech.tts.TextToSpeech;
import androidx.annotation.Nullable;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.telephony.SmsManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.backdoor.moove.core.async.DisableAsync;
import com.backdoor.moove.core.consts.Configs;
import com.backdoor.moove.core.consts.Constants;
import com.backdoor.moove.core.consts.Language;
import com.backdoor.moove.core.consts.Prefs;
import com.backdoor.moove.core.helper.Coloring;
import com.backdoor.moove.core.helper.Contacts;
import com.backdoor.moove.core.helper.Messages;
import com.backdoor.moove.core.helper.Module;
import com.backdoor.moove.core.helper.Notifier;
import com.backdoor.moove.core.helper.Reminder;
import com.backdoor.moove.core.helper.SharedPrefs;
import com.backdoor.moove.core.helper.Telephony;
import com.backdoor.moove.core.helper.Type;
import com.backdoor.moove.core.interfaces.SendListener;
import com.backdoor.moove.core.services.DeliveredReceiver;
import com.backdoor.moove.core.services.SendReceiver;
import com.backdoor.moove.core.views.RoundImageView;
import com.squareup.picasso.Picasso;

import jp.wasabeef.picasso.transformations.BlurTransformation;

public class ReminderDialogActivity extends Activity implements TextToSpeech.OnInitListener, SendListener,
        View.OnClickListener {

    private static final int MY_DATA_CHECK_CODE = 111;

    private FloatingActionButton buttonCall;
    private TextView remText;

    @Nullable
    private BroadcastReceiver deliveredReceiver, sentReceiver;

    private long id;
    private int color = -1, volume;
    private int isMelody;
    private String melody, number, name, task, reminderType;
    private int currVolume;

    @Nullable
    private Type reminder;
    @Nullable
    private Reminder item;

    @Nullable
    private SharedPrefs mPrefs;
    @Nullable
    private Notifier mNotifier;
    @Nullable
    private TextToSpeech tts;

    @SuppressLint("SetTextI18n")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPrefs = SharedPrefs.getInstance(this);
        Coloring cs = new Coloring(ReminderDialogActivity.this);
        mNotifier = new Notifier(ReminderDialogActivity.this);

        Intent res = getIntent();
        id = res.getLongExtra(Constants.ITEM_ID_INTENT, 0);
        isMelody = res.getIntExtra("int", 0);
        reminder = new Type(this);

        item = reminder.getItem(id);
        if (item != null) {
            task = item.getTitle();
            reminderType = item.getType();
            number = item.getNumber();
            melody = item.getMelody();
            color = item.getColor();
            volume = item.getVolume();
        } else {
            mNotifier.discardNotification(id);
            finish();
        }

        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (am != null) {
            currVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        }
        int prefsVol = mPrefs != null ? mPrefs.loadInt(Prefs.VOLUME) : 25;
        if (volume != -1) {
            prefsVol = volume;
        }
        if (am != null) {
            float volPercent = (float) prefsVol / Configs.MAX_VOLUME;
            int maxVol = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            int streamVol = (int) (maxVol * volPercent);
            am.setStreamVolume(AudioManager.STREAM_MUSIC, streamVol, 0);
        }

        boolean isFull = mPrefs != null && mPrefs.loadBoolean(Prefs.UNLOCK_DEVICE);
        if (isFull) {
            runOnUiThread(() -> getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD));
        }

        boolean isWake = mPrefs != null && mPrefs.loadBoolean(Prefs.WAKE_STATUS);
        if (isWake) {
            PowerManager.WakeLock screenLock = ((PowerManager) getSystemService(POWER_SERVICE)).newWakeLock(
                    PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "TAG");
            screenLock.acquire();
            screenLock.release();
        }

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setTheme(cs.getTransparentStyle());
        setContentView(R.layout.reminder_dialog_layout);

        if (Module.isLollipop()) {
            getWindow().setStatusBarColor(cs.getStatusBarStyle());
        }

        LinearLayout single_container = findViewById(R.id.single_container);
        single_container.setVisibility(View.VISIBLE);

        loadImage();

        FloatingActionButton buttonOk = findViewById(R.id.buttonOk);
        FloatingActionButton buttonEdit = findViewById(R.id.buttonEdit);
        buttonCall = findViewById(R.id.buttonCall);
        FloatingActionButton buttonNotification = findViewById(R.id.buttonNotification);

        RoundImageView contactPhoto = findViewById(R.id.contactPhoto);
        contactPhoto.setVisibility(View.GONE);

        remText = findViewById(R.id.remText);
        remText.setText("");
        String type = getType();
        if (type != null) {
            if (type.contains(Constants.TYPE_CALL)) {
                contactPhoto.setVisibility(View.VISIBLE);
                long conID = Contacts.getContactIDFromNumber(number, ReminderDialogActivity.this);
                Bitmap photo = Contacts.getPhoto(this, conID);
                if (photo != null) {
                    contactPhoto.setImageBitmap(photo);
                } else {
                    contactPhoto.setVisibility(View.GONE);
                }
                name = Contacts.getContactNameFromNumber(number, ReminderDialogActivity.this);
                remText.setText(task + "\n" + name + "\n" + number);
            } else if (type.contains(Constants.TYPE_MESSAGE)) {
                if (!mPrefs.loadBoolean(Prefs.SILENT_SMS)) {
                    remText.setText(task + "\n" + number);
                    buttonCall.setVisibility(View.VISIBLE);
                    buttonCall.setImageResource(R.drawable.ic_send_black_24dp);
                } else {
                    remText.setText(task + "\n" + number);
                    buttonCall.setVisibility(View.GONE);
                }
            } else {
                remText.setText(task);
                buttonCall.setVisibility(View.GONE);
            }
        } else {
            remText.setText(task);
            buttonCall.setVisibility(View.GONE);
        }

        buttonNotification.setOnClickListener(this);
        buttonOk.setOnClickListener(this);
        buttonEdit.setOnClickListener(this);
        buttonCall.setOnClickListener(this);

        boolean silentSMS = mPrefs.loadBoolean(Prefs.SILENT_SMS);
        boolean silentCall = mPrefs.loadBoolean(Prefs.SILENT_CALL);
        if (type != null) {
            if (type.contains(Constants.TYPE_MESSAGE)) {
                if (silentSMS) {
                    sendSMS(number, task);
                } else {
                    showReminder(1);
                }
            } else if (type.contains(Constants.TYPE_CALL)) {
                if (silentCall) {
                    Telephony.makeCall(number, ReminderDialogActivity.this);
                    make();
                    finish();
                } else {
                    showReminder(1);
                }
            } else {
                showReminder(1);
            }
        } else {
            showReminder(1);
        }

        boolean isTTS = mPrefs.loadBoolean(Prefs.TTS);
        if (isTTS) {
            Intent checkTTSIntent = new Intent();
            checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
            try {
                startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);
            } catch (ActivityNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadImage() {
        ImageView bgImage = findViewById(R.id.bgImage);
        bgImage.setVisibility(View.GONE);
        String imagePrefs = mPrefs != null ? mPrefs.loadPrefs(Prefs.REMINDER_IMAGE) : Constants.DEFAULT;
        boolean blur = mPrefs != null && mPrefs.loadBoolean(Prefs.REMINDER_IMAGE_BLUR);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;
        if (imagePrefs.matches(Constants.DEFAULT)) {
            if (blur) {
                Picasso.with(ReminderDialogActivity.this)
                        .load(R.drawable.photo)
                        .resize(width, height)
                        .transform(new BlurTransformation(this, 15, 2))
                        .into(bgImage);
            } else {
                Picasso.with(ReminderDialogActivity.this)
                        .load(R.drawable.photo)
                        .resize(width, height)
                        .into(bgImage);
            }
            bgImage.setVisibility(View.VISIBLE);
        } else if (imagePrefs.matches(Constants.NONE)) {
            bgImage.setVisibility(View.GONE);
        } else {
            if (blur) {
                Picasso.with(ReminderDialogActivity.this)
                        .load(Uri.parse(imagePrefs))
                        .resize(width, height)
                        .transform(new BlurTransformation(this, 15, 2))
                        .into(bgImage);
            } else {
                Picasso.with(ReminderDialogActivity.this)
                        .load(Uri.parse(imagePrefs))
                        .resize(width, height)
                        .into(bgImage);
            }
            bgImage.setVisibility(View.VISIBLE);
        }
    }

    private String getType() {
        if (reminderType != null) {
            return reminderType;
        } else {
            if (item != null) {
                return item.getType();
            } else {
                if (id != 0 && reminder != null) {
                    return reminder.getItem(id).getType();
                } else {
                    return "";
                }
            }
        }
    }

    private void make() {
        Reminder.disableReminder(id, ReminderDialogActivity.this);
        if (mNotifier != null) mNotifier.discardNotification(id);
    }

    private void showReminder(int i) {
        boolean isTTS = mPrefs != null && mPrefs.loadBoolean(Prefs.TTS);
        if (isMelody == 1) {
            i = 0;
        }
        if (!isTTS) {
            if (mNotifier != null) mNotifier.showReminder(task, i, id, melody, color);
        } else {
            if (mNotifier != null) mNotifier.showTTSNotification(task, id, color);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MY_DATA_CHECK_CODE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                tts = new TextToSpeech(this, this);
            } else {
                Intent installTTSIntent = new Intent();
                installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                try {
                    startActivity(installTTSIntent);
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void sendSMS(String phoneNumber, String message) {
        String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";

        PendingIntent sentPI = PendingIntent.getBroadcast(ReminderDialogActivity.this, 0,
                new Intent(SENT), 0);
        PendingIntent deliveredPI = PendingIntent.getBroadcast(ReminderDialogActivity.this,
                0, new Intent(DELIVERED), 0);

        registerReceiver(sentReceiver = new SendReceiver(this), new IntentFilter(SENT));
        registerReceiver(deliveredReceiver = new DeliveredReceiver(), new IntentFilter(DELIVERED));

        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (MotionEvent.ACTION_DOWN == event.getAction() && mNotifier != null) {
            mNotifier.discardMedia();
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (sentReceiver != null) {
            unregisterReceiver(sentReceiver);
        }
        if (deliveredReceiver != null) {
            unregisterReceiver(deliveredReceiver);
        }
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (am != null) am.setStreamVolume(AudioManager.STREAM_MUSIC, currVolume, 0);

        new DisableAsync(this).execute();
    }

    @Override
    public void onBackPressed() {
        if (mNotifier != null) mNotifier.discardMedia();
        Messages.toast(ReminderDialogActivity.this, getString(R.string.select_one_of_item));
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS && tts != null) {
            int result = tts.setLanguage(new Language().getLocale(ReminderDialogActivity.this));
            if (result == TextToSpeech.LANG_MISSING_DATA ||
                    result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("error", "This Language is not supported");
            } else {
                if (task != null && !task.matches("")) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        tts.speak(task, TextToSpeech.QUEUE_FLUSH, null, null);
                    } else {
                        tts.speak(task, TextToSpeech.QUEUE_FLUSH, null);
                    }
                }
            }
        } else {
            Log.e("error", "Initialization Failed!");
        }
    }

    @Override
    public void messageSendResult(boolean isSent) {
        if (isSent) {
            finish();
        } else {
            showReminder(0);
            remText.setText(R.string.error_sending_message);
            buttonCall.setImageResource(R.drawable.ic_cached_black_24dp);
            if (buttonCall.getVisibility() == View.GONE) {
                buttonCall.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonNotification:
                showNotification();
                break;
            case R.id.buttonOk:
                ok();
                break;
            case R.id.buttonEdit:
                make();
                Reminder.edit(id, ReminderDialogActivity.this);
                finish();
                break;
            case R.id.buttonCall:
                makeCall();
                break;
        }
    }

    private void showNotification() {
        make();
        if ((task == null || task.trim().matches("")) &&
                (number != null && !number.trim().matches(""))) {
            if (mNotifier != null) mNotifier.showReminderNotification(name + " " + number, id);
        } else {
            if (mNotifier != null) mNotifier.showReminderNotification(task, id);
        }
        finish();
    }

    private void ok() {
        make();
        finish();
    }

    private void makeCall() {
        String type = getType();
        if (type.contains(Constants.TYPE_MESSAGE)) {
            sendSMS(number, task);
        } else {
            Telephony.makeCall(number, ReminderDialogActivity.this);
        }
        make();
        if (!type.contains(Constants.TYPE_MESSAGE)) {
            finish();
        }
    }
}