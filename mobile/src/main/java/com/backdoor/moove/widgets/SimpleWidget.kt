package com.backdoor.moove.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.backdoor.moove.R
import com.backdoor.moove.SplashScreenActivity
import com.backdoor.moove.data.RoomDb
import com.backdoor.moove.utils.launchDefault
import org.koin.core.KoinComponent
import org.koin.core.inject

class SimpleWidget : AppWidgetProvider(), KoinComponent {

    private val roomDb: RoomDb by inject()

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            SimpleWidgetConfigureActivity.deletePref(context, appWidgetId)
            launchDefault {
                val reminder = roomDb.reminderDao().getByWidgetId(SimpleWidgetConfigureActivity.PREF_PREFIX_KEY + appWidgetId)
                reminder?.let {
                    it.widgetId = ""
                    roomDb.reminderDao().insert(it)
                }
            }
        }
    }

    override fun onEnabled(context: Context) {
    }

    override fun onDisabled(context: Context) {
    }

    companion object {

        internal fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager,
                                     appWidgetId: Int) {

            val distance = SimpleWidgetConfigureActivity.loadDistancePref(context, appWidgetId)

            val views = RemoteViews(context.packageName, R.layout.simple_widget)
            views.setTextViewText(R.id.leftDistance, if (distance <= 0)
                context.getString(R.string.off)
            else
                String.format(context.getString(R.string.distance_m), distance.toString()))

            val configIntent = Intent(context, SplashScreenActivity::class.java)
            val configPendingIntent = PendingIntent.getActivity(context, 0, configIntent, 0)
            views.setOnClickPendingIntent(R.id.widgetBg, configPendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}

