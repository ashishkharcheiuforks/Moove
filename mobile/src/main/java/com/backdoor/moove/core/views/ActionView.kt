package com.backdoor.moove.core.views

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RadioButton

import com.backdoor.moove.R
import com.backdoor.moove.core.consts.Constants
import com.backdoor.moove.utils.SuperUtil
import com.backdoor.moove.core.utils.ViewUtils

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
class ActionView : LinearLayout {

    private var activity: Activity? = null
    private var actionCheck: CheckBox? = null
    private var actionBlock: LinearLayout? = null
    private var callAction: RadioButton? = null
    private var messageAction: RadioButton? = null
    private var selectNumber: ImageButton? = null
    private var numberView: EditText? = null
    private var imm: InputMethodManager? = null

    private var listener: OnActionListener? = null

    var type: Int
        get() = if (hasAction()) {
            if (callAction!!.isChecked) {
                TYPE_CALL
            } else {
                TYPE_MESSAGE
            }
        } else {
            0
        }
        set(type) = if (type == TYPE_CALL) {
            callAction!!.isChecked = true
        } else {
            messageAction!!.isChecked = true
        }

    var number: String
        get() = numberView!!.text.toString().trim { it <= ' ' }
        set(number) = numberView!!.setText(number)

    /**
     * Select contact button click listener.
     */
    private val contactClick = OnClickListener { SuperUtil.selectContact(activity, Constants.REQUEST_CODE_CONTACTS) }

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
        View.inflate(context, R.layout.action_view_layout, this)
        orientation = LinearLayout.VERTICAL

        actionBlock = findViewById(R.id.actionBlock)
        actionBlock!!.visibility = View.GONE

        actionCheck = findViewById(R.id.actionCheck)
        actionCheck!!.setOnCheckedChangeListener { compoundButton, b ->
            if (b) {
                ViewUtils.showOver(actionBlock!!)
                selectNumber = findViewById(R.id.selectNumber)
                selectNumber!!.setOnClickListener(contactClick)
                selectNumber!!.setImageResource(R.drawable.ic_person_add_white_24dp)

                numberView = findViewById(R.id.numberView)
                numberView!!.isFocusableInTouchMode = true
                numberView!!.setOnFocusChangeListener { v, hasFocus ->
                    imm = activity!!.getSystemService(
                            Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    if (!hasFocus) {
                        imm!!.hideSoftInputFromWindow(numberView!!.windowToken, 0)
                    } else {
                        imm!!.showSoftInput(numberView, 0)
                    }
                }
                numberView!!.setOnClickListener { v ->
                    imm = activity!!.getSystemService(
                            Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    if (!imm!!.isActive(numberView)) {
                        imm!!.showSoftInput(numberView, 0)
                    }
                }

                callAction = findViewById(R.id.callAction)
                callAction!!.isChecked = true
                messageAction = findViewById(R.id.messageAction)
                messageAction!!.setOnCheckedChangeListener { compoundButton1, b1 ->
                    if (listener != null) {
                        listener!!.onTypeChange(b1)
                    }
                }
            } else {
                ViewUtils.hideOver(actionBlock!!)
            }
            if (listener != null) {
                listener!!.onActionChange(b)
            }
        }

        if (actionCheck!!.isChecked) {
            ViewUtils.showOver(actionBlock!!)
        }
    }

    fun setListener(listener: OnActionListener) {
        this.listener = listener
    }

    fun hasAction(): Boolean {
        return actionCheck!!.isChecked
    }

    fun setAction(action: Boolean) {
        actionCheck!!.isChecked = action
    }

    fun showError() {
        numberView!!.error = activity!!.getString(R.string.empty_field)
    }

    fun setActivity(activity: Activity) {
        this.activity = activity
    }

    interface OnActionListener {
        fun onActionChange(b: Boolean)

        fun onTypeChange(type: Boolean)
    }

    companion object {

        val TYPE_CALL = 1
        val TYPE_MESSAGE = 2
    }
}
