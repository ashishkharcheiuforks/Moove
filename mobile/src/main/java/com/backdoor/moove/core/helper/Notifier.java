package com.backdoor.moove.core.helper;

import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.backdoor.moove.R;
import com.backdoor.moove.ReminderDialog;
import com.backdoor.moove.core.consts.Prefs;
import com.backdoor.moove.core.utils.ViewUtils;

import java.io.File;
import java.io.IOException;

/**
 * Helper class for status bar notifications.
 */
public class Notifier {

    public static final String CHANNEL_REMINDER = "moove.channel1";
    public static final String CHANNEL_SYSTEM = "moove.channel2";

    private Context mContext;
    private NotificationManagerCompat mNotifyMgr;
    private NotificationCompat.Builder builder;
    private int NOT_ID = 0;
    private SharedPrefs sPrefs;
    private Sound sound;

    public Notifier(Context context) {
        this.mContext = context;
        this.sPrefs = SharedPrefs.getInstance(context);
        sound = new Sound(context);
    }

    public static void createChannels(Context context) {
        if (Module.isO()) {
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            manager.createNotificationChannel(createReminderChannel(context));
            manager.createNotificationChannel(createSystemChannel(context));
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    private static NotificationChannel createSystemChannel(Context context) {
        String name = context.getString(R.string.info_channel);
        String descr = context.getString(R.string.channel_for_other_info_notifications);
        int importance = NotificationManager.IMPORTANCE_LOW;
        NotificationChannel mChannel = new NotificationChannel(CHANNEL_SYSTEM, name, importance);
        mChannel.setDescription(descr);
        return mChannel;
    }

    @TargetApi(Build.VERSION_CODES.O)
    private static NotificationChannel createReminderChannel(Context context) {
        String name = context.getString(R.string.reminder_channel);
        String descr = context.getString(R.string.default_reminder_notifications);
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel mChannel = new NotificationChannel(CHANNEL_REMINDER, name, importance);
        mChannel.setDescription(descr);
        return mChannel;
    }

    /**
     * Status bar notification to use when enabled tts.
     *
     * @param task   task string.
     * @param itemId reminder identifier.
     * @param color  LED lights color.
     */
    public void showTTSNotification(final String task, long itemId, int color) {
        builder = new NotificationCompat.Builder(mContext, CHANNEL_REMINDER);
        builder.setContentTitle(task);
        Intent notificationIntent = new Intent(mContext, ReminderDialog.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        PendingIntent intent = PendingIntent.getActivity(mContext, (int) itemId, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(intent);
        builder.setAutoCancel(false);
        builder.setPriority(NotificationCompat.PRIORITY_MAX);
        builder.setOngoing(true);
        String app = mContext.getString(R.string.app_name);
        builder.setContentText(app);
        builder.setSmallIcon(ViewUtils.getIcon());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setColor(ViewUtils.getColor(mContext, R.color.bluePrimary));
        }

        AudioManager am = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        if (am.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
            try {
                AssetFileDescriptor afd = mContext.getAssets().openFd("sounds/beep.mp3");
                sound.playAlarm(afd, false);
            } catch (IOException e) {
                e.printStackTrace();
                sound.playAlarm(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), false);
            }
        } else {
            if (sPrefs.loadBoolean(Prefs.SILENT_SOUND)) {
                try {
                    AssetFileDescriptor afd = mContext.getAssets().openFd("sounds/beep.mp3");
                    sound.playAlarm(afd, false);
                } catch (IOException e) {
                    e.printStackTrace();
                    sound.playAlarm(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), false);
                }
            }
        }

        boolean isV = sPrefs.loadBoolean(Prefs.VIBRATION_STATUS);
        if (isV) {
            long[] pattern;
            if (sPrefs.loadBoolean(Prefs.INFINITE_VIBRATION)) {
                pattern = new long[]{150, 86400000};
            } else {
                pattern = new long[]{150, 400, 100, 450, 200, 500, 300, 500};
            }
            builder.setVibrate(pattern);
        }

        if (sPrefs.loadBoolean(Prefs.LED_STATUS)) {
            if (color != 0) {
                builder.setLights(color, 500, 1000);
            } else {
                builder.setLights(sPrefs.loadInt(Prefs.LED_COLOR), 500, 1000);
            }
        }

        boolean isWear = sPrefs.loadBoolean(Prefs.WEAR_NOTIFICATION);
        if (isWear) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                builder.setOnlyAlertOnce(true);
                builder.setGroup("GROUP");
                builder.setGroupSummary(true);
            }
        }

        mNotifyMgr = NotificationManagerCompat.from(mContext);
        Integer it = (int) (long) itemId;
        mNotifyMgr.notify(it, builder.build());

        if (isWear) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                final NotificationCompat.Builder wearableNotificationBuilder = new NotificationCompat.Builder(mContext, CHANNEL_REMINDER);
                wearableNotificationBuilder.setSmallIcon(R.mipmap.ic_launcher);
                wearableNotificationBuilder.setContentTitle(task);
                wearableNotificationBuilder.setContentText(app);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    wearableNotificationBuilder.setColor(ViewUtils.getColor(mContext, R.color.bluePrimary));
                }
                wearableNotificationBuilder.setOngoing(false);
                wearableNotificationBuilder.setOnlyAlertOnce(true);
                wearableNotificationBuilder.setGroup("GROUP");
                wearableNotificationBuilder.setGroupSummary(false);
                mNotifyMgr.notify(10100, wearableNotificationBuilder.build());
            }
        }
    }

    /**
     * Standard status bar notification for reminder.
     *
     * @param task   reminder task.
     * @param i      flag for enabling sounds (1 - enabled).
     * @param itemId reminder identifier.
     * @param melody reminder custom melody file.
     * @param color  LED lights color.
     */
    public void showReminder(final String task, int i, long itemId, String melody, int color) {
        Uri soundUri;
        if (melody != null && !melody.matches("")) {
            File sound = new File(melody);
            soundUri = Uri.fromFile(sound);
        } else {
            if (sPrefs.loadBoolean(Prefs.CUSTOM_SOUND)) {
                String path = sPrefs.loadPrefs(Prefs.CUSTOM_SOUND_FILE);
                if (path != null) {
                    File sound = new File(path);
                    soundUri = Uri.fromFile(sound);
                } else {
                    soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                }
            } else {
                soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            }
        }

        Intent notificationIntent = new Intent(mContext, ReminderDialog.class);
        notificationIntent.putExtra("int", 1);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        PendingIntent intent = PendingIntent.getActivity(mContext, (int) itemId, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        builder = new NotificationCompat.Builder(mContext, CHANNEL_REMINDER);
        builder.setContentTitle(task);
        builder.setContentIntent(intent);
        builder.setAutoCancel(false);
        builder.setPriority(NotificationCompat.PRIORITY_MAX);
        builder.setOngoing(true);
        String app = mContext.getString(R.string.app_name);
        builder.setContentText(app);
        builder.setSmallIcon(ViewUtils.getIcon());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setColor(ViewUtils.getColor(mContext, R.color.bluePrimary));
        }

        if (i == 1) {
            AudioManager am = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
            if (am.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
                sound.playAlarm(soundUri, sPrefs.loadBoolean(Prefs.INFINITE_SOUND));
            } else {
                if (sPrefs.loadBoolean(Prefs.SILENT_SOUND)) {
                    sound.playAlarm(soundUri, sPrefs.loadBoolean(Prefs.INFINITE_SOUND));
                }
            }
        }

        boolean isV = sPrefs.loadBoolean(Prefs.VIBRATION_STATUS);
        if (isV) {
            long[] pattern;
            if (sPrefs.loadBoolean(Prefs.INFINITE_VIBRATION)) {
                pattern = new long[]{150, 86400000};
            } else {
                pattern = new long[]{150, 400, 100, 450, 200, 500, 300, 500};
            }
            builder.setVibrate(pattern);
        }
        if (sPrefs.loadBoolean(Prefs.LED_STATUS)) {
            if (color != 0) {
                builder.setLights(color, 500, 1000);
            } else {
                builder.setLights(sPrefs.loadInt(Prefs.LED_COLOR), 500, 1000);
            }
        }

        boolean isWear = sPrefs.loadBoolean(Prefs.WEAR_NOTIFICATION);
        if (isWear) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                builder.setOnlyAlertOnce(true);
                builder.setGroup("GROUP");
                builder.setGroupSummary(true);
            }
        }

        mNotifyMgr = NotificationManagerCompat.from(mContext);
        Integer it = (int) (long) itemId;
        mNotifyMgr.notify(it, builder.build());

        if (isWear) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                final NotificationCompat.Builder wearableNotificationBuilder = new NotificationCompat.Builder(mContext, CHANNEL_REMINDER);
                wearableNotificationBuilder.setSmallIcon(R.mipmap.ic_launcher);
                wearableNotificationBuilder.setContentTitle(task);
                wearableNotificationBuilder.setContentText(app);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    wearableNotificationBuilder.setColor(ViewUtils.getColor(mContext, R.color.bluePrimary));
                }
                wearableNotificationBuilder.setOngoing(false);
                wearableNotificationBuilder.setOnlyAlertOnce(true);
                wearableNotificationBuilder.setGroup("GROUP");
                wearableNotificationBuilder.setGroupSummary(false);
                mNotifyMgr.notify(10100, wearableNotificationBuilder.build());
            }
        }
    }

    /**
     * Simple status bar notification for reminders.
     *
     * @param content notification title.
     * @param id      reminder identifier.
     */
    public void showReminderNotification(String content, long id) {
        builder = new NotificationCompat.Builder(mContext, CHANNEL_REMINDER);
        builder.setContentTitle(content);
        String app = mContext.getString(R.string.app_name);
        builder.setContentText(app);
        builder.setSmallIcon(R.drawable.ic_notifications_white_24dp);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setColor(ViewUtils.getColor(mContext, R.color.bluePrimary));
        }

        boolean isWear = sPrefs.loadBoolean(Prefs.WEAR_NOTIFICATION);
        if (isWear) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                builder.setOnlyAlertOnce(true);
                builder.setGroup("GROUP");
                builder.setGroupSummary(true);
            }
        }

        mNotifyMgr = NotificationManagerCompat.from(mContext);
        Integer it = (int) (long) id;
        mNotifyMgr.notify(it, builder.build());

        if (isWear) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                final NotificationCompat.Builder wearableNotificationBuilder = new NotificationCompat.Builder(mContext, CHANNEL_REMINDER);
                wearableNotificationBuilder.setSmallIcon(R.mipmap.ic_launcher);
                wearableNotificationBuilder.setContentTitle(content);
                wearableNotificationBuilder.setContentText(app);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    wearableNotificationBuilder.setColor(ViewUtils.getColor(mContext, R.color.bluePrimary));
                }
                wearableNotificationBuilder.setOngoing(false);
                wearableNotificationBuilder.setOnlyAlertOnce(true);
                wearableNotificationBuilder.setGroup("GROUP");
                wearableNotificationBuilder.setGroupSummary(false);
                mNotifyMgr.notify(it + 10, wearableNotificationBuilder.build());
            }
        }
    }

    public void discardNotification() {
        discardMedia();
        mNotifyMgr = NotificationManagerCompat.from(mContext);
        mNotifyMgr.cancel(NOT_ID);
    }

    public void discardStatusNotification(long id) {
        Integer i = (int) (long) id;
        mNotifyMgr = NotificationManagerCompat.from(mContext);
        mNotifyMgr.cancel(i);
    }

    public void discardNotification(long id) {
        discardMedia();
        Integer i = (int) (long) id;
        mNotifyMgr = NotificationManagerCompat.from(mContext);
        mNotifyMgr.cancel(i);
    }

    /**
     * Stops playing notification sound.
     */
    public void discardMedia() {
        sound.stop();
    }
}
