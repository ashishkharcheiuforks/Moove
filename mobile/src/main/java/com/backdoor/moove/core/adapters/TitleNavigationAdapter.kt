package com.backdoor.moove.core.adapters

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView

import com.backdoor.moove.R
import com.backdoor.moove.core.data.SpinnerItem
import com.backdoor.moove.utils.Coloring

import java.util.ArrayList

class TitleNavigationAdapter(private val context: Context,
                             private val spinnerNavItem: ArrayList<SpinnerItem>) : BaseAdapter() {

    private var imgIcon: ImageView? = null
    private var txtTitle: TextView? = null
    private val cs: Coloring

    init {
        cs = Coloring(context)
    }

    override fun getCount(): Int {
        return spinnerNavItem.size
    }

    override fun getItem(index: Int): Any {
        return spinnerNavItem[index]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        if (convertView == null) {
            val mInflater = context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = mInflater.inflate(R.layout.list_item_navigation, null)
        }

        imgIcon = convertView!!.findViewById(R.id.imgIcon)
        txtTitle = convertView.findViewById(R.id.txtTitle)

        imgIcon!!.setImageResource(spinnerNavItem[position].icon)
        imgIcon!!.visibility = View.GONE
        txtTitle!!.text = spinnerNavItem[position].title
        txtTitle!!.setTextColor(context.resources.getColor(R.color.whitePrimary))
        return convertView
    }


    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        if (convertView == null) {
            val mInflater = context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = mInflater.inflate(R.layout.list_item_navigation, null)
        }

        val itemBg = convertView!!.findViewById<RelativeLayout>(R.id.itemBg)
        itemBg.setBackgroundColor(cs.spinnerStyle)

        imgIcon = convertView.findViewById(R.id.imgIcon)
        txtTitle = convertView.findViewById(R.id.txtTitle)

        imgIcon!!.setImageResource(spinnerNavItem[position].icon)
        txtTitle!!.setTextColor(context.resources.getColor(R.color.whitePrimary))
        txtTitle!!.text = spinnerNavItem[position].title
        return convertView
    }
}