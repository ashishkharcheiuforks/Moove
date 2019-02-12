package com.backdoor.moove.core.widgets

import android.app.Activity
import android.app.AlertDialog
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import android.view.View
import android.widget.ArrayAdapter
import android.widget.TextView

import com.backdoor.moove.R
import com.backdoor.moove.core.data.MarkerModel
import com.backdoor.moove.core.data.ReminderDataProvider
import com.backdoor.moove.core.helper.Reminder

import java.util.ArrayList

/**
 * The configuration screen for the [LeftDistanceWidget] AppWidget.
 */
class LeftDistanceWidgetConfigureActivity : Activity(), DialogInterface.OnDismissListener {
    internal var mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private var reminderId: Long = 0
    internal var mAppWidgetText: TextView
    internal var mOnClickListener = { v ->
        val context = this@LeftDistanceWidgetConfigureActivity

        if (!saveReminderPref(context, mAppWidgetId)) {
            return
        }

        val appWidgetManager = AppWidgetManager.getInstance(context)
        LeftDistanceWidget.updateAppWidget(context, appWidgetManager, mAppWidgetId)

        val resultValue = Intent()
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId)
        setResult(Activity.RESULT_OK, resultValue)
        finish()
    }

    internal var chooseClick: View.OnClickListener = View.OnClickListener {
        val context = this@LeftDistanceWidgetConfigureActivity

        val builder = AlertDialog.Builder(context)
        builder.setCancelable(true)
        builder.setTitle(context.getString(R.string.choose_reminder))
        val list = ReminderDataProvider.getListData(context)
        val titles = ArrayList<String>()
        for (item in list) {
            titles.add(item.title)
        }

        val adapter = ArrayAdapter(context,
                android.R.layout.simple_list_item_single_choice, titles)

        builder.setSingleChoiceItems(adapter, -1) { dialog, which ->
            if (which != -1) {
                dialog.dismiss()
                val model = list[which]
                reminderId = model.id
                val title = model.title
                saveTitlePref(context, mAppWidgetId, title)
                saveIconPref(context, mAppWidgetId, model.icon)
            }
        }
        builder.setPositiveButton(context.getString(R.string.ok)) { dialog, which ->
            if (which != -1) {
                dialog.dismiss()
                val model = list[which]
                reminderId = model.id
                val title = model.title
                saveTitlePref(context, mAppWidgetId, title)
                saveIconPref(context, mAppWidgetId, model.icon)
            }
        }
        val dialog = builder.create()
        dialog.setOnDismissListener(this@LeftDistanceWidgetConfigureActivity)
        dialog.show()
    }

    private fun saveReminderPref(context: Context, appWidgetId: Int): Boolean {
        if (reminderId > 0) {
            saveDistancePref(context, PREF_DISTANCE_KEY + appWidgetId, 1)
            Reminder.setWidget(context, reminderId, PREF_DISTANCE_KEY + appWidgetId)
            return true
        } else {
            Snackbar.make(mAppWidgetText, R.string.at_first_select_reminder, Snackbar.LENGTH_SHORT).show()
            return false
        }
    }

    public override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(Activity.RESULT_CANCELED)

        setContentView(R.layout.left_distance_widget_configure)
        mAppWidgetText = findViewById(R.id.appwidget_text)
        findViewById<View>(R.id.add_button).setOnClickListener(mOnClickListener)
        findViewById<View>(R.id.selectButton).setOnClickListener(chooseClick)

        // Find the widget id from the intent.
        val intent = intent
        val extras = intent.extras
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        mAppWidgetText.text = loadTitlePref(this@LeftDistanceWidgetConfigureActivity, mAppWidgetId)
    }

    override fun onDismiss(dialog: DialogInterface) {
        mAppWidgetText.text = loadTitlePref(this, mAppWidgetId)
    }

    companion object {

        private val PREFS_NAME = "com.backdoor.moove.core.widgets.LeftDistanceWidget"
        private val PREF_PREFIX_KEY = "appwidget_"
        private val PREF_ICON_KEY = "appwidget_icon_"
        val PREF_DISTANCE_KEY = "appwidget_distance_"

        // Write the prefix to the SharedPreferences object for this widget
        internal fun saveTitlePref(context: Context, appWidgetId: Int, text: String?) {
            val prefs = context.getSharedPreferences(PREFS_NAME, 0).edit()
            prefs.putString(PREF_PREFIX_KEY + appWidgetId, text)
            prefs.apply()
        }

        internal fun saveIconPref(context: Context, appWidgetId: Int, icon: Int) {
            val prefs = context.getSharedPreferences(PREFS_NAME, 0).edit()
            prefs.putInt(PREF_ICON_KEY + appWidgetId, icon)
            prefs.apply()
        }

        fun saveDistancePref(context: Context, prefsKey: String, distance: Int) {
            val prefs = context.getSharedPreferences(PREFS_NAME, 0).edit()
            prefs.putInt(prefsKey, distance)
            prefs.apply()
        }

        // Read the prefix from the SharedPreferences object for this widget.
        // If there is no preference saved, get the default from a resource
        internal fun loadTitlePref(context: Context, appWidgetId: Int): String {
            val prefs = context.getSharedPreferences(PREFS_NAME, 0)
            try {
                val titleValue = prefs.getString(PREF_PREFIX_KEY + appWidgetId, null)
                return titleValue ?: context.getString(R.string.no_reminder)
            } catch (e: ClassCastException) {
                return context.getString(R.string.no_reminder)
            }

        }

        internal fun loadIConPref(context: Context, appWidgetId: Int): Int {
            val prefs = context.getSharedPreferences(PREFS_NAME, 0)
            val iconValue = prefs.getInt(PREF_ICON_KEY + appWidgetId, -1)
            return if (iconValue != -1) {
                iconValue
            } else {
                0
            }
        }

        fun loadDistancePref(context: Context, appWidgetId: Int): Int {
            val prefs = context.getSharedPreferences(PREFS_NAME, 0)
            val iconValue = prefs.getInt(PREF_DISTANCE_KEY + appWidgetId, -1)
            return if (iconValue != -1) {
                iconValue
            } else {
                0
            }
        }

        internal fun deletePref(context: Context, appWidgetId: Int) {
            val prefs = context.getSharedPreferences(PREFS_NAME, 0).edit()
            prefs.remove(PREF_PREFIX_KEY + appWidgetId)
            prefs.remove(PREF_ICON_KEY + appWidgetId)
            prefs.remove(PREF_DISTANCE_KEY + appWidgetId)
            prefs.apply()
        }
    }
}

