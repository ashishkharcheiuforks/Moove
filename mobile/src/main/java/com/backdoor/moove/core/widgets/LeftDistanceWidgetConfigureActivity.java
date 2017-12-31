package com.backdoor.moove.core.widgets;

import android.app.Activity;
import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.backdoor.moove.R;
import com.backdoor.moove.core.data.MarkerModel;
import com.backdoor.moove.core.data.ReminderDataProvider;
import com.backdoor.moove.core.helper.Reminder;

import java.util.ArrayList;

/**
 * The configuration screen for the {@link LeftDistanceWidget LeftDistanceWidget} AppWidget.
 */
public class LeftDistanceWidgetConfigureActivity extends Activity implements DialogInterface.OnDismissListener {

    private static final String PREFS_NAME = "com.backdoor.moove.core.widgets.LeftDistanceWidget";
    private static final String PREF_PREFIX_KEY = "appwidget_";
    private static final String PREF_ICON_KEY = "appwidget_icon_";
    public static final String PREF_DISTANCE_KEY = "appwidget_distance_";
    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private long reminderId;
    TextView mAppWidgetText;
    View.OnClickListener mOnClickListener = v -> {
        final Context context = LeftDistanceWidgetConfigureActivity.this;

        if (!saveReminderPref(context, mAppWidgetId)) {
            return;
        }

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        LeftDistanceWidget.updateAppWidget(context, appWidgetManager, mAppWidgetId);

        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
        setResult(RESULT_OK, resultValue);
        finish();
    };

    View.OnClickListener chooseClick = new View.OnClickListener() {
        public void onClick(View v) {
            final Context context = LeftDistanceWidgetConfigureActivity.this;

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setCancelable(true);
            builder.setTitle(context.getString(R.string.choose_reminder));
            final ArrayList<MarkerModel> list = ReminderDataProvider.getListData(context);
            ArrayList<String> titles = new ArrayList<>();
            for (MarkerModel item : list) {
                titles.add(item.getTitle());
            }

            final ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                    android.R.layout.simple_list_item_single_choice, titles);

            builder.setSingleChoiceItems(adapter, -1, (dialog, which) -> {
                if (which != -1) {
                    dialog.dismiss();
                    MarkerModel model = list.get(which);
                    reminderId = model.getId();
                    String title = model.getTitle();
                    saveTitlePref(context, mAppWidgetId, title);
                    saveIconPref(context, mAppWidgetId, model.getIcon());
                }
            });
            builder.setPositiveButton(context.getString(R.string.ok), (dialog, which) -> {
                if (which != -1) {
                    dialog.dismiss();
                    MarkerModel model = list.get(which);
                    reminderId = model.getId();
                    String title = model.getTitle();
                    saveTitlePref(context, mAppWidgetId, title);
                    saveIconPref(context, mAppWidgetId, model.getIcon());
                }
            });
            AlertDialog dialog = builder.create();
            dialog.setOnDismissListener(LeftDistanceWidgetConfigureActivity.this);
            dialog.show();
        }
    };

    public LeftDistanceWidgetConfigureActivity() {
        super();
    }

    // Write the prefix to the SharedPreferences object for this widget
    static void saveTitlePref(Context context, int appWidgetId, String text) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.putString(PREF_PREFIX_KEY + appWidgetId, text);
        prefs.apply();
    }

    static void saveIconPref(Context context, int appWidgetId, int icon) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.putInt(PREF_ICON_KEY + appWidgetId, icon);
        prefs.apply();
    }

    public static void saveDistancePref(Context context, String prefsKey, int distance) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.putInt(prefsKey, distance);
        prefs.apply();
    }

    private boolean saveReminderPref(Context context, int appWidgetId) {
        if (reminderId > 0) {
            saveDistancePref(context, PREF_DISTANCE_KEY + appWidgetId, 1);
            Reminder.setWidget(context, reminderId, PREF_DISTANCE_KEY + appWidgetId);
            return true;
        } else {
            Snackbar.make(mAppWidgetText, R.string.at_first_select_reminder, Snackbar.LENGTH_SHORT).show();
            return false;
        }
    }

    // Read the prefix from the SharedPreferences object for this widget.
    // If there is no preference saved, get the default from a resource
    static String loadTitlePref(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        try {
            String titleValue = prefs.getString(PREF_PREFIX_KEY + appWidgetId, null);
            if (titleValue != null) {
                return titleValue;
            } else {
                return context.getString(R.string.no_reminder);
            }
        } catch (ClassCastException e) {
            return context.getString(R.string.no_reminder);
        }
    }

    static int loadIConPref(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        int iconValue = prefs.getInt(PREF_ICON_KEY + appWidgetId, -1);
        if (iconValue != -1) {
            return iconValue;
        } else {
            return 0;
        }
    }

    public static int loadDistancePref(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        int iconValue = prefs.getInt(PREF_DISTANCE_KEY + appWidgetId, -1);
        if (iconValue != -1) {
            return iconValue;
        } else {
            return 0;
        }
    }

    static void deletePref(Context context, int appWidgetId) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.remove(PREF_PREFIX_KEY + appWidgetId);
        prefs.remove(PREF_ICON_KEY + appWidgetId);
        prefs.remove(PREF_DISTANCE_KEY + appWidgetId);
        prefs.apply();
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED);

        setContentView(R.layout.left_distance_widget_configure);
        mAppWidgetText = findViewById(R.id.appwidget_text);
        findViewById(R.id.add_button).setOnClickListener(mOnClickListener);
        findViewById(R.id.selectButton).setOnClickListener(chooseClick);

        // Find the widget id from the intent.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }

        mAppWidgetText.setText(loadTitlePref(LeftDistanceWidgetConfigureActivity.this, mAppWidgetId));
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        mAppWidgetText.setText(loadTitlePref(this, mAppWidgetId));
    }
}

