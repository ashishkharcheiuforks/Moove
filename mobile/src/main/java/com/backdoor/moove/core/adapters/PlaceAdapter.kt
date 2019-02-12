package com.backdoor.moove.core.adapters

import android.content.Context
import android.graphics.Typeface
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import com.backdoor.moove.R
import com.backdoor.moove.core.interfaces.SimpleListener
import com.backdoor.moove.core.utils.AssetsUtil

import java.util.ArrayList

/**
 * Recycler view adapter for frequently used places.
 */
class PlaceAdapter
/**
 * Adapter constructor.
 *
 * @param context application context.
 * @param array   places data provider.
 */
(context: Context, array: ArrayList<String>) : RecyclerView.Adapter<PlaceAdapter.ViewHolder>() {

    private val array = ArrayList<String>()

    /**
     * Font typeface for text view's.
     */
    private val typeface: Typeface

    /**
     * Action listener for adapter.
     */
    /**
     * Get current action listener.
     *
     * @return Action listener.
     */
    /**
     * Set action listener for adapter.
     *
     * @param eventListener action listener.
     */
    var eventListener: SimpleListener? = null

    init {
        this.array = array
        typeface = AssetsUtil.getLightTypeface(context)
        setHasStableIds(true)
    }

    /**
     * View holder for adapter.
     */
    inner class ViewHolder
    /**
     * View holder constructor.
     *
     * @param v view.
     */
    (v: View) : RecyclerView.ViewHolder(v), View.OnClickListener {

        /**
         * Place title.
         */
        var textView: TextView

        init {
            textView = v.findViewById(R.id.text1)
            textView.typeface = typeface

            v.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            if (eventListener != null) {
                eventListener!!.onItemClicked(adapterPosition, textView)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // create a new view
        val itemLayoutView = LayoutInflater.from(parent.context)
                .inflate(R.layout.simple_taxt_item, parent, false)
        return ViewHolder(itemLayoutView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textView.text = array[position]
    }

    override fun getItemViewType(position: Int): Int {
        return 0
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemCount(): Int {
        return array.size
    }
}
