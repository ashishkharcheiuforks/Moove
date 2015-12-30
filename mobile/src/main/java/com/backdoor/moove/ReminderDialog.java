package com.backdoor.moove;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.speech.tts.TextToSpeech;
import android.telephony.SmsManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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
import com.backdoor.moove.core.utils.ViewUtils;
import com.backdoor.moove.core.views.RoundImageView;
import com.backdoor.moove.core.views.TextDrawable;
import com.melnykov.fab.FloatingActionButton;
import com.squareup.picasso.Picasso;

import jp.wasabeef.picasso.transformations.BlurTransformation;

public class ReminderDialog extends Activity implements TextToSpeech.OnInitListener, SendListener {

    private static final int MY_DATA_CHECK_CODE = 111;

    private FloatingActionButton buttonCall;
    private TextView remText;

    private BroadcastReceiver deliveredReceiver, sentReceiver;

    private long id;
    private int color = -1;
    private int isMelody;
    private String melody, number, name, task, reminderType;
    private int currVolume;

    private Type reminder;
    private Reminder item;

    private SharedPrefs sPrefs;
    private Coloring cs = new Coloring(ReminderDialog.this);
    private Notifier notifier = new Notifier(ReminderDialog.this);
    private TextToSpeech tts;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sPrefs = new SharedPrefs(ReminderDialog.this);

        AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        currVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        int prefsVol = sPrefs.loadInt(Prefs.VOLUME);
        float volPercent = (float) prefsVol / Configs.MAX_VOLUME;
        int maxVol = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int streamVol = (int) (maxVol * volPercent);
        am.setStreamVolume(AudioManager.STREAM_MUSIC, streamVol, 0);

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
        } else {
            notifier.discardNotification(id);
            finish();
        }

        boolean isFull = sPrefs.loadBoolean(Prefs.UNLOCK_DEVICE);
        if (isFull) {
            runOnUiThread(new Runnable() {
                public void run() {
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                            | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                            | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
                }
            });
        }

        boolean isWake = sPrefs.loadBoolean(Prefs.WAKE_STATUS);
        if (isWake) {
            PowerManager.WakeLock screenLock = ((PowerManager)getSystemService(POWER_SERVICE)).newWakeLock(
                    PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "TAG");
            screenLock.acquire();
            screenLock.release();
        }

        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setTheme(cs.getTransparentStyle());
        setContentView(R.layout.reminder_dialog_layout);

        if (Module.isLollipop()) {
            getWindow().setStatusBarColor(cs.getStatusBarStyle());
        }

        LinearLayout single_container = (LinearLayout) findViewById(R.id.single_container);
        single_container.setVisibility(View.VISIBLE);

        loadImage();

        FloatingActionButton buttonOk = (FloatingActionButton) findViewById(R.id.buttonOk);
        FloatingActionButton buttonEdit = (FloatingActionButton) findViewById(R.id.buttonEdit);
        buttonCall = (FloatingActionButton) findViewById(R.id.buttonCall);
        FloatingActionButton buttonNotification = (FloatingActionButton) findViewById(R.id.buttonNotification);
        colorify(buttonOk, buttonCall, buttonNotification, buttonEdit);
        buttonOk.setImageDrawable(ViewUtils.getDrawable(this, R.drawable.ic_done_black_24dp));
        buttonEdit.setImageDrawable(ViewUtils.getDrawable(this, R.drawable.ic_create_black_24dp));
        buttonCall.setImageDrawable(ViewUtils.getDrawable(this, R.drawable.ic_call_black_24dp));
        buttonNotification.setImageDrawable(ViewUtils.getDrawable(this, R.drawable.ic_favorite_black_24dp));

        RoundImageView contactPhoto = (RoundImageView) findViewById(R.id.contactPhoto);
        contactPhoto.setVisibility(View.GONE);

        remText = (TextView) findViewById(R.id.remText);
        remText.setText("");
        String type = getType();
        if (type != null) {
            if (type.matches(Constants.TYPE_LOCATION_CALL) ||
                    type.matches(Constants.TYPE_LOCATION_OUT_CALL)) {
                contactPhoto.setVisibility(View.VISIBLE);
                long conID = Contacts.getContactIDFromNumber(number, ReminderDialog.this);
                Bitmap photo = Contacts.getPhoto(this, conID);
                if (photo != null) {
                    contactPhoto.setImageBitmap(photo);
                } else {
                    contactPhoto.setVisibility(View.GONE);
                }
                name = Contacts.getContactNameFromNumber(number, ReminderDialog.this);
                remText.setText(task + "\n" + name + "\n" + number);
            } else if (type.matches(Constants.TYPE_LOCATION_MESSAGE) ||
                    type.matches(Constants.TYPE_LOCATION_OUT_MESSAGE)) {
                if (!sPrefs.loadBoolean(Prefs.SILENT_SMS)) {
                    remText.setText(task + "\n" + number);
                    buttonCall.setVisibility(View.VISIBLE);
                    buttonCall.setImageDrawable(ViewUtils.getDrawable(this, R.drawable.ic_send_white_24dp));
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

        buttonNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                make();
                update(1);
                if ((task == null || task.trim().matches("")) &&
                        (number != null && !number.trim().matches(""))) {
                    notifier.showReminderNotification(name + " " + number, id);
                } else {
                    notifier.showReminderNotification(task, id);
                }
                finish();
            }
        });

        buttonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                make();
                update(1);
                finish();
            }
        });

        buttonEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                make();
                update(1);
                Reminder.edit(id, ReminderDialog.this);
                finish();
            }
        });

        buttonCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String type = getType();
                if (type.matches(Constants.TYPE_LOCATION_MESSAGE) ||
                        type.matches(Constants.TYPE_LOCATION_OUT_MESSAGE)){
                    sendSMS(number, task);
                } else {
                    Telephony.makeCall(number, ReminderDialog.this);
                }
                make();
                update(1);
                if (!type.contains(Constants.TYPE_MESSAGE)){
                    finish();
                }
            }
        });

        boolean silentSMS = sPrefs.loadBoolean(Prefs.SILENT_SMS);
        if (type != null) {
            if (type.matches(Constants.TYPE_MESSAGE) || type.matches(Constants.TYPE_LOCATION_MESSAGE) ||
                    type.matches(Constants.TYPE_LOCATION_OUT_MESSAGE)) {
                if (silentSMS) {
                    sendSMS(number, task);
                } else {
                    showReminder(1);
                }
            } else {
                showReminder(1);
            }
        } else {
            showReminder(1);
        }

        boolean isTTS = sPrefs.loadBoolean(Prefs.TTS);
        if (isTTS) {
            Intent checkTTSIntent = new Intent();
            checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
            try {
                startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);
            } catch (ActivityNotFoundException e){
                e.printStackTrace();
            }
        }
    }

    private void loadImage() {
        ImageView bgImage = (ImageView) findViewById(R.id.bgImage);
        bgImage.setVisibility(View.GONE);
        String imagePrefs = sPrefs.loadPrefs(Prefs.REMINDER_IMAGE);
        boolean blur = sPrefs.loadBoolean(Prefs.REMINDER_IMAGE_BLUR);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;
        if (imagePrefs.matches(Constants.DEFAULT)){
            if (blur) {
                Picasso.with(ReminderDialog.this)
                        .load(R.drawable.photo)
                        .resize(width, height)
                        .transform(new BlurTransformation(this, 15, 2))
                        .into(bgImage);
            } else {
                Picasso.with(ReminderDialog.this)
                        .load(R.drawable.photo)
                        .resize(width, height)
                        .into(bgImage);
            }
            bgImage.setVisibility(View.VISIBLE);
        } else if (imagePrefs.matches(Constants.NONE)){
            bgImage.setVisibility(View.GONE);
        } else {
            if (blur) {
                Picasso.with(ReminderDialog.this)
                        .load(Uri.parse(imagePrefs))
                        .resize(width, height)
                        .transform(new BlurTransformation(this, 15, 2))
                        .into(bgImage);
            } else {
                Picasso.with(ReminderDialog.this)
                        .load(Uri.parse(imagePrefs))
                        .resize(width, height)
                        .into(bgImage);
            }
            bgImage.setVisibility(View.VISIBLE);
        }
    }

    private String getType(){
        if (reminderType != null) {
            return reminderType;
        } else {
            if (item != null){
                return item.getType();
            } else {
                if (id != 0){
                    return reminder.getItem(id).getType();
                } else {
                    return "";
                }
            }
        }
    }

    private void update(int i){
        if (i == 1) {
            removeFlags();
        }
        notifier.discardNotification(id);
    }

    private void make(){
        Reminder.disableReminder(id, ReminderDialog.this);
    }

    private void setTextDrawable(FloatingActionButton button, String text){
        TextDrawable drawable = TextDrawable.builder()
                .beginConfig()
                .textColor(Color.DKGRAY)
                .useFont(Typeface.DEFAULT)
                .fontSize(30) /* size in px */
                .bold()
                .toUpperCase()
                .endConfig()
                .buildRound(text, Color.TRANSPARENT);
        button.setImageDrawable(drawable);
    }

    private void colorify(FloatingActionButton... fab){
        for (FloatingActionButton button : fab){
            button.setColorNormal(getResources().getColor(R.color.whitePrimary));
            button.setColorPressed(getResources().getColor(R.color.material_divider));
        }
    }

    private void showReminder(int i){
        sPrefs = new SharedPrefs(ReminderDialog.this);
        String type = getType();
        boolean isTTS = sPrefs.loadBoolean(Prefs.TTS);
        if (isMelody == 1) {
            i = 0;
        }
        if (!isTTS) {
            notifier.showReminder(task, i, id, melody, color);
        } else {
            notifier.showTTSNotification(task, id, color);
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
                } catch (ActivityNotFoundException e){
                    e.printStackTrace();
                }
            }
        }
    }

    public void openLink(String number) {
        Telephony.openLink(number, this);
        notifier.discardNotification(id);
        make();
        finish();
    }

    public void openApplication(String number) {
        Telephony.openApp(number, this);
        notifier.discardNotification(id);
        make();
        finish();
    }

    private void sendSMS(String phoneNumber, String message) {
        String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";

        PendingIntent sentPI = PendingIntent.getBroadcast(ReminderDialog.this, 0,
                new Intent(SENT), 0);
        PendingIntent deliveredPI = PendingIntent.getBroadcast(ReminderDialog.this,
                0, new Intent(DELIVERED), 0);
        
        registerReceiver(sentReceiver = new SendReceiver(this), new IntentFilter(SENT));
        registerReceiver(deliveredReceiver = new DeliveredReceiver(), new IntentFilter(DELIVERED));

        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);
    }

    public void removeFlags(){
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (MotionEvent.ACTION_DOWN == event.getAction()){
            notifier.discardMedia();
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onDestroy() {
        if (sentReceiver != null) {
            unregisterReceiver(sentReceiver);
        }
        if (deliveredReceiver != null) {
            unregisterReceiver(deliveredReceiver);
        }
        removeFlags();
        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        am.setStreamVolume(AudioManager.STREAM_MUSIC, currVolume, 0);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        notifier.discardMedia();
        Messages.toast(ReminderDialog.this, getString(R.string.select_one_of_item));
    }

    @Override
    public void onInit(int status) {
        sPrefs = new SharedPrefs(ReminderDialog.this);
        if(status == TextToSpeech.SUCCESS){
            int result = tts.setLanguage(new Language().getLocale(ReminderDialog.this));
            if(result == TextToSpeech.LANG_MISSING_DATA ||
                    result == TextToSpeech.LANG_NOT_SUPPORTED){
                Log.e("error", "This Language is not supported");
            } else{
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
            buttonCall.setImageDrawable(ViewUtils.getDrawable(ReminderDialog.this, R.drawable.ic_cached_white_24dp));
            if (buttonCall.getVisibility() == View.GONE) {
                buttonCall.setVisibility(View.VISIBLE);
            }
        }
    }
}