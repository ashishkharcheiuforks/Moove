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
import androidx.lifecycle.ViewModelProviders
import com.backdoor.moove.R
import com.backdoor.moove.data.Reminder
import com.backdoor.moove.databinding.ActivitySimpleWidgetBinding
import com.backdoor.moove.modern_ui.home.HomeViewModel

class SimpleWidgetConfigureActivity : AppCompatActivity() {

    private val viewModel: HomeViewModel by lazy {
        ViewModelProviders.of(this).get(HomeViewModel::class.java)
    }

    private lateinit var binding: ActivitySimpleWidgetBinding

    private var mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private var mReminder: Reminder? = null
    private var mSelectItem = 0
    private val data: MutableList<Reminder> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(Activity.RESULT_CANCELED)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_simple_widget)
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
            binding.appwidgetText.text = mReminder?.summary
        }
    }

    private fun addClick() {
        if (!saveReminderPref(this, mAppWidgetId)) {
            return
        }

        val appWidgetManager = AppWidgetManager.getInstance(this)
        SimpleWidget.updateAppWidget(this, appWidgetManager, mAppWidgetId)

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
        saveDistancePref(context, PREF_PREFIX_KEY + appWidgetId, 1)
        reminder.widgetId = PREF_PREFIX_KEY + appWidgetId
        viewModel.save(reminder)
        return true
    }

    companion object {

        private const val PREFS_NAME = "com.backdoor.moove.widgets.SimpleWidget"
        const val PREF_PREFIX_KEY = "appwidget_"

        fun saveDistancePref(context: Context, key: String, distance: Int) {
            val prefs = context.getSharedPreferences(PREFS_NAME, 0).edit()
            prefs.putInt(key, distance)
            prefs.apply()
        }

        fun loadDistancePref(context: Context, appWidgetId: Int): Int {
            val prefs = context.getSharedPreferences(PREFS_NAME, 0)
            val iconValue = prefs.getInt(PREF_PREFIX_KEY + appWidgetId, -1)
            return if (iconValue != -1) {
                iconValue
            } else {
                -1
            }
        }

        internal fun deletePref(context: Context, appWidgetId: Int) {
            val prefs = context.getSharedPreferences(PREFS_NAME, 0).edit()
            prefs.remove(PREF_PREFIX_KEY + appWidgetId)
            prefs.apply()
        }
    }
}

