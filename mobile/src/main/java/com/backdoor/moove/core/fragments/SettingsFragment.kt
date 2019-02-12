package com.backdoor.moove.core.fragments

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.ListFragment
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView

import com.backdoor.moove.R

class SettingsFragment : ListFragment() {
    private var mCallback: OnHeadlineSelectedListener? = null

    interface OnHeadlineSelectedListener {
        fun onArticleSelected(position: Int)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val list = arrayOf(getString(R.string.general), getString(R.string.notification), getString(R.string.location), getString(R.string.other))

        val layout = android.R.layout.simple_list_item_activated_1
        listAdapter = ArrayAdapter(activity!!, layout, list)
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        try {
            mCallback = activity as OnHeadlineSelectedListener
        } catch (e: ClassCastException) {
            throw ClassCastException("$activity must implement OnHeadlineSelectedListener")
        }

    }

    override fun onListItemClick(l: ListView, v: View, position: Int, id: Long) {
        mCallback!!.onArticleSelected(position)
        listView.setItemChecked(position, true)
    }
}
