package com.backdoor.moove.core.views

import android.content.Context
import android.location.Address
import androidx.appcompat.widget.AppCompatAutoCompleteTextView
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView

import com.backdoor.moove.core.async.GeocoderTask

import java.util.ArrayList

/**
 * Copyright 2016 Nazar Suhovich
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

class AddressAutoCompleteView : AppCompatAutoCompleteTextView {

    private var mContext: Context? = null
    private var foundPlaces: List<Address> = ArrayList()

    private var task: GeocoderTask? = null
    private var mAdapter: AddressAdapter? = null
    private var mListener: AddressSelectedListener? = null
    private var isEnabled = true

    private val mExecutionCallback = GeocoderTask.GeocoderListener { addresses ->
        Log.d(TAG, "onAddressReceived: $addresses")
        foundPlaces = addresses
        mAdapter = AddressAdapter(context, android.R.layout.simple_list_item_2, addresses)
        setAdapter<AddressAdapter>(mAdapter)
        mAdapter!!.notifyDataSetChanged()
    }

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context)
    }

    private fun init(context: Context) {
        this.mContext = context

        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                if (isEnabled) performTypeValue(charSequence.toString())
            }

            override fun afterTextChanged(editable: Editable) {

            }
        })
        setSingleLine(true)
        imeOptions = EditorInfo.IME_ACTION_SEARCH
        setOnEditorActionListener { textView, actionId, event ->
            if (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER || actionId == EditorInfo.IME_ACTION_SEARCH) {
                performTypeValue(text.toString().trim { it <= ' ' })
                return@setOnEditorActionListener true
            }
            false
        }
        setOnItemClickListener { parent, view, position, id ->
            if (mAdapter != null && mListener != null) {
                mListener!!.onAddressSelected(foundPlaces[position])
            }
        }
    }

    fun setTextWithoutSuggestions(s: String) {
        isEnabled = false
        setText(s)
        isEnabled = true
    }

    fun setListener(listener: AddressSelectedListener?) {
        this.mListener = listener
    }

    fun getAddress(position: Int): Address {
        return foundPlaces[position]
    }

    private fun performTypeValue(s: String) {
        if (task != null && !task!!.isCancelled) {
            task!!.cancel(true)
        }
        task = GeocoderTask(mContext, mExecutionCallback)
        task!!.execute(s)
    }

    override fun setOnItemClickListener(l: AdapterView.OnItemClickListener) {

    }

    private inner class AddressAdapter internal constructor(context: Context, resource: Int, objects: List<Address>) : ArrayAdapter<Address>(context, resource, objects) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var v = convertView
            if (v == null) {
                v = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_2, null, false)
            }
            val tv1 = v!!.findViewById<TextView>(android.R.id.text1)
            val tv2 = v.findViewById<TextView>(android.R.id.text2)
            val address = getItem(position) ?: return v
            if (address.getAddressLine(0) != null) {
                tv1.text = address.getAddressLine(0)
                tv2.text = formName(address)
            } else {
                tv1.text = formName(address)
                tv2.text = ""
            }
            return v
        }

        private fun formName(address: Address?): String {
            if (address == null) return ""
            val sb = StringBuilder()
            sb.append(address.featureName)
            if (address.adminArea != null) {
                sb.append(", ").append(address.adminArea)
            }
            if (address.countryName != null) {
                sb.append(", ").append(address.countryName)
            }
            return sb.toString()
        }
    }

    interface AddressSelectedListener {
        fun onAddressSelected(address: Address?)
    }

    companion object {

        private val TAG = "AddressAutoCompleteView"
    }
}
