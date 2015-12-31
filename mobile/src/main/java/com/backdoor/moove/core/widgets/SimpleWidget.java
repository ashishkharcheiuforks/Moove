package com.backdoor.moove.core.widgets;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.util.Log;
import android.widget.RemoteViews;

import com.backdoor.moove.R;
import com.backdoor.moove.core.consts.Constants;
import com.backdoor.moove.core.helper.Reminder;


/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link SimpleWidgetConfigureActivity SimpleWidgetConfigureActivity}
 */
public class SimpleWidget extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        int distance = SimpleWidgetConfigureActivity.loadDistancePref(context, appWidgetId);

        Log.d(Constants.LOG_TAG, "distance " + distance);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.simple_widget);
        views.setTextViewText(R.id.leftDistance, String.format(context.getString(R.string.distance_m), distance));

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // When the user deletes the widget, delete the preference associated with it.
        for (int appWidgetId : appWidgetIds) {
            SimpleWidgetConfigureActivity.deletePref(context, appWidgetId);
            Reminder.removeWidget(context, SimpleWidgetConfigureActivity.PREF_PREFIX_KEY + appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

