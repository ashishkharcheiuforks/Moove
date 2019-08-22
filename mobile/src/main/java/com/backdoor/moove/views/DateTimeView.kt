package com.backdoor.moove.views

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.LinearLayout
import android.widget.TimePicker
import com.backdoor.moove.R
import com.backdoor.moove.utils.Prefs
import com.backdoor.moove.utils.TimeUtils
import com.backdoor.moove.utils.hide
import com.backdoor.moove.utils.show
import com.backdoor.moove.views.binding.DateTimeViewBinding
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.*

class DateTimeView : LinearLayout, DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener, KoinComponent {

    private lateinit var binding: DateTimeViewBinding
    private var mHour: Int = 0
    private var mMinute: Int = 0
    private var mYear: Int = 0
    private var mMonth: Int = 0
    private var mDay: Int = 0
    private var isSingleMode = false
    private var mListener: OnSelectListener? = null
    var onDateChangeListener: OnDateChangeListener? = null

    private val mDateClick = OnClickListener{ selectDate() }

    val prefs: Prefs by inject()

    var dateTime: Long
        get() {
            val calendar = Calendar.getInstance()
            calendar.set(mYear, mMonth, mDay, mHour, mMinute, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            return calendar.timeInMillis
        }
        set(dateTime) = updateDateTime(dateTime)

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(context)
    }

    fun setEventListener(listener: OnSelectListener) {
        mListener = listener
    }

    private fun init(context: Context) {
        orientation = LinearLayout.VERTICAL
        View.inflate(context, R.layout.view_date_time, this)
        binding = DateTimeViewBinding(this)

        descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS
        val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)
        layoutParams = params

        binding.dateField.setOnClickListener(mDateClick)
        binding.timeField.setOnClickListener { selectTime() }
        updateDateTime(0)
    }

    override fun setOnClickListener(l: OnClickListener?) {
        if (isSingleMode) binding.dateField.setOnClickListener(l)
    }

    override fun setOnLongClickListener(l: View.OnLongClickListener?) {
        binding.dateField.setOnLongClickListener(l)
        binding.timeField.setOnLongClickListener(l)
    }

    fun setSingleText(text: String?) {
        isSingleMode = text != null
        if (!isSingleMode) {
            binding.timeField.show()
            binding.dateField.setOnClickListener(mDateClick)
            updateDateTime(0)
        } else {
            binding.dateField.text = text
            binding.dateField.setOnClickListener(null)
            binding.timeField.hide()
        }
    }

    fun setDateTime(dateTime: String) {
        val mills = TimeUtils.getDateTimeFromGmt(dateTime)
        updateDateTime(mills)
    }

    private fun updateDateTime(mills: Long) {
        var milliseconds = mills
        if (milliseconds == 0L) {
            milliseconds = System.currentTimeMillis()
        }
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = milliseconds
        mYear = calendar.get(Calendar.YEAR)
        mMonth = calendar.get(Calendar.MONTH)
        mDay = calendar.get(Calendar.DAY_OF_MONTH)
        mHour = calendar.get(Calendar.HOUR_OF_DAY)
        mMinute = calendar.get(Calendar.MINUTE)
        updateTime(milliseconds)
        updateDate(milliseconds)
    }

    private fun updateDate(mills: Long) {
        val cal = Calendar.getInstance()
        cal.timeInMillis = mills
        binding.dateField.text = TimeUtils.getDate(cal.time)
        mListener?.onDateSelect(mills, mDay, mMonth, mYear)
        onDateChangeListener?.onChanged(dateTime)
    }

    private fun updateTime(mills: Long) {
        val cal = Calendar.getInstance()
        cal.timeInMillis = mills
        binding.timeField.text = TimeUtils.getTime(cal.time, prefs.use24Hour)
        mListener?.onTimeSelect(mills, mHour, mMinute)
        onDateChangeListener?.onChanged(dateTime)
    }

    private fun selectDate() {
        TimeUtils.showDatePicker(context, mYear, mMonth, mDay, this)
    }

    private fun selectTime() {
        TimeUtils.showTimePicker(context, prefs.use24Hour, mHour, mMinute, this)
    }

    override fun onDateSet(view: DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int) {
        this.mYear = year
        this.mMonth = monthOfYear
        this.mDay = dayOfMonth
        val cal = Calendar.getInstance()
        cal.set(year, monthOfYear, dayOfMonth)
        updateDate(cal.timeInMillis)
    }

    override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {
        this.mHour = hourOfDay
        this.mMinute = minute
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, hourOfDay)
        cal.set(Calendar.MINUTE, minute)
        updateTime(cal.timeInMillis)
    }

    interface OnSelectListener {
        fun onDateSelect(mills: Long, day: Int, month: Int, year: Int)

        fun onTimeSelect(mills: Long, hour: Int, minute: Int)
    }

    interface OnDateChangeListener {
        fun onChanged(mills: Long)
    }
}
