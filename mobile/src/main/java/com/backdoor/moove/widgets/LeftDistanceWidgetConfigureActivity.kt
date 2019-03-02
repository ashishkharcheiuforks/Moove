package com.backdoor.moove.widgets

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.backdoor.moove.R
import com.backdoor.moove.data.Reminder
import com.backdoor.moove.databinding.ActivityLeftDistanceWidgetBinding
import com.backdoor.moove.modern_ui.home.HomeViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class LeftDistanceWidgetConfigureActivity : AppCompatActivity() {

    private val viewModel: HomeViewModel by viewModel()

    private lateinit var binding: ActivityLeftDistanceWidgetBinding

    private var mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private var mReminder: Reminder? = null
    private var mSelectItem = 0
    private val data: MutableList<Reminder> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(Activity.RESULT_CANCELED)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_left_distance_widget)
        binding.addButton.setOnClickListener { addClick() }
        binding.selectButton.setOnClickListener { selectClick() }

        val intent = intent
        val extras = intent.extras
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        }

        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        binding.appwidgetText.text = getString(R.string.no_reminder)

        viewModel.reminders.observe(this, Observer {
            if (it != null) {
                data.clear()
                data.addAll(it)
            }
        })
    }

    private fun selectClick() {
        val builder = AlertDialog.Builder(this, R.style.HomeDarkDialog)
        builder.setCancelable(true)
        builder.setTitle(getString(R.string.choose_reminder))
        val list = data
        val titles = list.map { it.summary }

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_single_choice, titles)

        builder.setSingleChoiceItems(adapter, -1) { _, which ->
            if (which != -1) {
                mSelectItem = which
            }
        }
        builder.setPositiveButton(getString(R.string.ok)) { dialog, _ ->
            dialog.dismiss()
            showSelected(mSelectItem)
        }
        builder.create().show()
    }

    private fun showSelected(selectItem: Int) {
        if (selectItem >= 0 && selectItem < data.size) {
            mReminder = data[selectItem]
            val title = mReminder?.summary ?: ""
            binding.appwidgetText.text = title

            saveTitlePref(this, mAppWidgetId, title)
            saveIconPref(this, mAppWidgetId, mReminder?.markerColor ?: 0)
        }
    }

    private fun addClick() {
        if (!saveReminderPref(this, mAppWidgetId)) {
            return
        }

        val appWidgetManager = AppWidgetManager.getInstance(this)
        LeftDistanceWidget.updateAppWidget(this, appWidgetManager, mAppWidgetId)

        val resultValue = Intent()
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId)
        setResult(Activity.RESULT_OK, resultValue)
        finish()
    }

    private fun saveReminderPref(context: Context, appWidgetId: Int): Boolean {
        val reminder = mReminder
        if (reminder == null) {
            Toast.makeText(this,  R.string.at_first_select_reminder, Toast.LENGTH_SHORT).show()
            return false
        }
        saveDistancePref(context, PREF_DISTANCE_KEY + appWidgetId, 1)
        reminder.widgetId = PREF_DISTANCE_KEY + appWidgetId
        viewModel.save(reminder)
        return true
    }

    companion object {

        private const val PREFS_NAME = "com.backdoor.moove.widgets.LeftDistanceWidget"
        private const val PREF_PREFIX_KEY = "appwidget_"
        private const val PREF_ICON_KEY = "appwidget_icon_"
        const val PREF_DISTANCE_KEY = "appwidget_distance_"

        private fun saveTitlePref(context: Context, appWidgetId: Int, text: String?) {
            val prefs = context.getSharedPreferences(PREFS_NAME, 0).edit()
            prefs.putString(PREF_PREFIX_KEY + appWidgetId, text)
            prefs.apply()
        }

        private fun saveIconPref(context: Context, appWidgetId: Int, icon: Int) {
            val prefs = context.getSharedPreferences(PREFS_NAME, 0).edit()
            prefs.putInt(PREF_ICON_KEY + appWidgetId, icon)
            prefs.apply()
        }

        fun saveDistancePref(context: Context, prefsKey: String, distance: Int) {
            val prefs = context.getSharedPreferences(PREFS_NAME, 0).edit()
            prefs.putInt(prefsKey, distance)
            prefs.apply()
        }

        internal fun loadTitlePref(context: Context, appWidgetId: Int): String {
            val prefs = context.getSharedPreferences(PREFS_NAME, 0)
            try {
                val titleValue = prefs.getString(PREF_PREFIX_KEY + appWidgetId, null)
                return titleValue ?: context.getString(R.string.no_reminder)
            } catch (e: ClassCastException) {
                return context.getString(R.string.no_reminder)
            }

        }

        internal fun loadIconPref(context: Context, appWidgetId: Int): Int {
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

