package com.backdoor.moove.widgets

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.widget.RemoteViews
import com.backdoor.moove.R
import com.backdoor.moove.utils.Coloring
import com.backdoor.moove.utils.DrawableHelper
import com.backdoor.moove.utils.Prefs

class LeftDistanceWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            LeftDistanceWidgetConfigureActivity.deletePref(context, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
    }

    override fun onDisabled(context: Context) {
    }

    companion object {

        internal fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager,
                                     appWidgetId: Int) {

            val widgetText = LeftDistanceWidgetConfigureActivity.loadTitlePref(context, appWidgetId)
            val icon = LeftDistanceWidgetConfigureActivity.loadIConPref(context, appWidgetId)
            val distance = LeftDistanceWidgetConfigureActivity.loadDistancePref(context, appWidgetId)

            // Construct the RemoteViews object
            val views = RemoteViews(context.packageName, R.layout.left_distance_widget)
            views.setTextViewText(R.id.appwidget_text, widgetText)
            views.setTextViewText(R.id.leftDistance, if (distance <= 0) {
                context.getString(R.string.off)
            } else {
                String.format(context.getString(R.string.distance_m), distance.toString())
            })

            val coloring = Coloring(context)
            val prefs = Prefs(context)
            val pointer = DrawableHelper.withContext(context)
                    .withDrawable(R.drawable.ic_twotone_place_24px)
                    .withColor(coloring.accentColor(prefs.markerStyle))
                    .tint()
                    .get()

            views.setImageViewBitmap(R.id.markerImage, toBitmap(pointer))

//            val configIntent = Intent(context, MainActivity::class.java)
//            val configPendingIntent = PendingIntent.getActivity(context, 0, configIntent, 0)
//            views.setOnClickPendingIntent(R.id.widgetBg, configPendingIntent)
//
//            // Instruct the widget manager to update the widget
//            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun toBitmap(drawable: Drawable): Bitmap {
            val bitmap = if (drawable.intrinsicWidth <= 0 || drawable.intrinsicHeight <= 0) {
                Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
            } else {
                Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
            }
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            return bitmap
        }
    }
}

