package com.backdoor.moove.core.views

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import android.widget.DatePicker
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.TimePicker

import com.backdoor.moove.R
import com.backdoor.moove.core.consts.Prefs
import com.backdoor.moove.core.helper.SharedPrefs
import com.backdoor.moove.core.utils.AssetsUtil
import com.backdoor.moove.core.utils.TimeUtil

import java.util.Calendar

/**
 * Copyright 2015 Nazar Suhovich
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
class DateTimeView : RelativeLayout, DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    private var date: TextView? = null
    private var time: TextView? = null
    private var mills: Long = 0
    private var mContext: Context? = null
    private var listener: OnSelectListener? = null

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        View.inflate(context, R.layout.date_time_view_layout, this)
        descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS
        date = findViewById(R.id.dateField)
        time = findViewById(R.id.timeField)

        val medium = AssetsUtil.getMediumTypeface(context)
        date!!.typeface = medium
        time!!.typeface = medium

        date!!.setOnClickListener { v -> dateDialog() }
        time!!.setOnClickListener { v -> timeDialog() }

        this.mContext = context
        updateDateTime(0)
    }

    /**
     * Set DateTime listener.
     *
     * @param listener OnSelectListener.
     */
    fun setListener(listener: OnSelectListener) {
        this.listener = listener
    }

    /**
     * Set date time to view.
     *
     * @param mills DateTime in mills.
     */
    fun setDateTime(mills: Long) {
        this.mills = mills
        updateDateTime(mills)
    }

    /**
     * Update views for DateTime.
     *
     * @param mills DateTime in mills.
     */
    private fun updateDateTime(mills: Long) {
        updateTime(mills)
        updateDate(mills)
    }

    /**
     * Update date view.
     *
     * @param mills date in mills.
     */
    private fun updateDate(mills: Long) {
        val cal = Calendar.getInstance()
        cal.timeInMillis = mills
        if (mills == 0L) {
            cal.timeInMillis = System.currentTimeMillis()
        }
        date!!.text = TimeUtil.getDate(cal.time)
    }

    /**
     * Update time view.
     *
     * @param mills time in mills.
     */
    private fun updateTime(mills: Long) {
        val cal = Calendar.getInstance()
        cal.timeInMillis = mills
        if (mills == 0L) {
            cal.timeInMillis = System.currentTimeMillis()
        }
        time!!.text = TimeUtil.getTime(cal.time, SharedPrefs.getInstance(mContext)!!.loadBoolean(Prefs.IS_24_TIME_FORMAT))
    }

    /**
     * Show date picker dialog.
     */
    private fun dateDialog() {
        val cal = Calendar.getInstance()
        cal.timeInMillis = mills
        if (mills == 0L) {
            cal.timeInMillis = System.currentTimeMillis()
        }
        val myYear = cal.get(Calendar.YEAR)
        val myMonth = cal.get(Calendar.MONTH)
        val myDay = cal.get(Calendar.DAY_OF_MONTH)
        DatePickerDialog(mContext!!, this, myYear, myMonth, myDay).show()
    }

    /**
     * Show time picker dialog.
     */
    private fun timeDialog() {
        val cal = Calendar.getInstance()
        cal.timeInMillis = mills
        if (mills == 0L) {
            cal.timeInMillis = System.currentTimeMillis()
        }
        val myHour = cal.get(Calendar.HOUR_OF_DAY)
        val myMinute = cal.get(Calendar.MINUTE)
        TimePickerDialog(mContext, this, myHour, myMinute,
                SharedPrefs.getInstance(mContext)!!.loadBoolean(Prefs.IS_24_TIME_FORMAT)).show()
    }

    override fun onDateSet(view: DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int) {
        val cal = Calendar.getInstance()
        cal.set(year, monthOfYear, dayOfMonth)
        if (listener != null) {
            listener!!.onDateSelect(cal.timeInMillis, dayOfMonth, monthOfYear, year)
        }
        updateDate(cal.timeInMillis)
    }

    override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, hourOfDay)
        cal.set(Calendar.MINUTE, minute)
        if (listener != null) {
            listener!!.onTimeSelect(cal.timeInMillis, hourOfDay, minute)
        }
        updateTime(cal.timeInMillis)
    }

    interface OnSelectListener {
        fun onDateSelect(mills: Long, day: Int, month: Int, year: Int)

        fun onTimeSelect(mills: Long, hour: Int, minute: Int)
    }
}
