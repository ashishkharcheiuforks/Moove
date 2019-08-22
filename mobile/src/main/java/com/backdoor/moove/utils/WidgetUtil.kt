package com.backdoor.moove.utils

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent

import com.backdoor.moove.widgets.LeftDistanceWidget
import com.backdoor.moove.widgets.SimpleWidget

object WidgetUtil {

    fun updateWidgets(context: Context) {
        var intent = Intent(context, LeftDistanceWidget::class.java)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        val ids = AppWidgetManager.getInstance(context).getAppWidgetIds(ComponentName(context, LeftDistanceWidget::class.java))
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        context.sendBroadcast(intent)

        intent = Intent(context, SimpleWidget::class.java)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        val idx = AppWidgetManager.getInstance(context).getAppWidgetIds(ComponentName(context, SimpleWidget::class.java))
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, idx)
        context.sendBroadcast(intent)
    }
}
