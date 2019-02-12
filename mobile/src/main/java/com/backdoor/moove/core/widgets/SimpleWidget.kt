package com.backdoor.moove.core.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews

import com.backdoor.moove.MainActivity
import com.backdoor.moove.R
import com.backdoor.moove.core.consts.Constants
import com.backdoor.moove.core.helper.Reminder


/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in [SimpleWidgetConfigureActivity]
 */
class SimpleWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        // When the user deletes the widget, delete the preference associated with it.
        for (appWidgetId in appWidgetIds) {
            SimpleWidgetConfigureActivity.deletePref(context, appWidgetId)
            Reminder.removeWidget(context, SimpleWidgetConfigureActivity.PREF_PREFIX_KEY + appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    companion object {

        internal fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager,
                                     appWidgetId: Int) {

            val distance = SimpleWidgetConfigureActivity.loadDistancePref(context, appWidgetId)

            Log.d(Constants.LOG_TAG, "distance $distance")
            val views = RemoteViews(context.packageName, R.layout.simple_widget)
            views.setTextViewText(R.id.leftDistance, if (distance <= 0)
                context.getString(R.string.off)
            else
                String.format(context.getString(R.string.distance_m), distance.toString()))

            val configIntent = Intent(context, MainActivity::class.java)
            val configPendingIntent = PendingIntent.getActivity(context, 0, configIntent, 0)
            views.setOnClickPendingIntent(R.id.widgetBg, configPendingIntent)

            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}

