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
 * The configuration screen for the {@link SimpleWidget SimpleWidget} AppWidget.
 */
public class SimpleWidgetConfigureActivity extends Activity implements DialogInterface.OnDismissListener {

    private static final String PREFS_NAME = "com.backdoor.moove.core.widgets.SimpleWidget";
    public static final String PREF_PREFIX_KEY = "appwidget_";
    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private long reminderId;
    private String title;
    TextView mAppWidgetText;

    View.OnClickListener mOnClickListener = v -> {
        final Context context = SimpleWidgetConfigureActivity.this;

        if (!saveReminderPref(context, mAppWidgetId)) {
            return;
        }

        // It is the responsibility of the configuration activity to update the app widget
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        SimpleWidget.updateAppWidget(context, appWidgetManager, mAppWidgetId);

        // Make sure we pass back the original appWidgetId
        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
        setResult(RESULT_OK, resultValue);
        finish();
    };

    View.OnClickListener chooseClick = new View.OnClickListener() {
        public void onClick(View v) {
            final Context context = SimpleWidgetConfigureActivity.this;

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
                    title = model.getTitle();
                }
            });
            builder.setPositiveButton(context.getString(R.string.ok), (dialog, which) -> {
                if (which != -1) {
                    dialog.dismiss();
                    MarkerModel model = list.get(which);
                    reminderId = model.getId();
                    title = model.getTitle();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.setOnDismissListener(SimpleWidgetConfigureActivity.this);
            dialog.show();
        }
    };

    public SimpleWidgetConfigureActivity() {
        super();
    }

    // Write the prefix to the SharedPreferences object for this widget
    public static void saveDistancePref(Context context, String key, int distance) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.putInt(key, distance);
        prefs.apply();
    }

    private boolean saveReminderPref(Context context, int appWidgetId) {
        if (reminderId > 0) {
            saveDistancePref(context, PREF_PREFIX_KEY + appWidgetId, 1);
            Reminder.setWidget(context, reminderId, PREF_PREFIX_KEY + appWidgetId);
            return true;
        } else {
            Snackbar.make(mAppWidgetText, R.string.at_first_select_reminder, Snackbar.LENGTH_SHORT).show();
            return false;
        }
    }

    public static int loadDistancePref(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        int iconValue = prefs.getInt(PREF_PREFIX_KEY + appWidgetId, -1);
        if (iconValue != -1) {
            return iconValue;
        } else {
            return -1;
        }
    }

    static void deletePref(Context context, int appWidgetId) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.remove(PREF_PREFIX_KEY + appWidgetId);
        prefs.apply();
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED);

        setContentView(R.layout.simple_widget_configure);
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

        mAppWidgetText.setText(getString(R.string.no_reminder));
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        mAppWidgetText.setText(title);
    }
}

