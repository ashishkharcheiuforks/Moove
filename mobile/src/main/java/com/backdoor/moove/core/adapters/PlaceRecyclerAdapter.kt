package com.backdoor.moove.core.adapters

import android.content.Context
import android.graphics.Typeface
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

import com.backdoor.moove.R
import com.backdoor.moove.core.consts.Configs
import com.backdoor.moove.core.data.MarkerModel
import com.backdoor.moove.core.data.PlaceDataProvider
import com.backdoor.moove.core.helper.Coloring
import com.backdoor.moove.core.helper.Module
import com.backdoor.moove.core.interfaces.SimpleListener
import com.backdoor.moove.core.utils.AssetsUtil

/**
 * Recycler view adapter for frequently used places.
 */
class PlaceRecyclerAdapter
/**
 * Adapter constructor.
 *
 * @param context  application context.
 * @param provider places data provider.
 */
(context: Context,
 /**
  * Data provider for markers.
  */
 val provider: PlaceDataProvider,
 showMarker: Boolean) : RecyclerView.Adapter<PlaceRecyclerAdapter.ViewHolder>() {

    /**
     * Coloring helper class field.
     */
    private val cs: Coloring

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

    private val showMarker = false

    init {
        this.showMarker = showMarker
        cs = Coloring(context)
        typeface = AssetsUtil.getLightTypeface(context)
        setHasStableIds(true)
    }

    /**
     * View holder for adapter.
     */
    inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v), View.OnClickListener, View.OnLongClickListener {

        var textView: TextView
        var markerImage: ImageView
        var itemCard: CardView

        init {
            textView = v.findViewById(R.id.textView)
            markerImage = v.findViewById(R.id.markerImage)
            textView.typeface = typeface
            itemCard = v.findViewById(R.id.itemCard)
            itemCard.setCardBackgroundColor(cs.cardStyle)
            if (Module.isLollipop) {
                if (showMarker) {
                    itemCard.cardElevation = 0f
                } else {
                    itemCard.cardElevation = Configs.CARD_ELEVATION
                }
            }

            v.setOnClickListener(this)
            v.setOnLongClickListener(this)
        }

        override fun onClick(v: View) {
            if (eventListener != null) {
                eventListener!!.onItemClicked(adapterPosition, textView)
            }
        }

        override fun onLongClick(v: View): Boolean {
            if (eventListener != null) {
                eventListener!!.onItemLongClicked(adapterPosition, textView)
            }
            return true
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // create a new view
        val itemLayoutView = LayoutInflater.from(parent.context)
                .inflate(R.layout.list_item_simple_card, parent, false)
        return ViewHolder(itemLayoutView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = provider.data!![position]
        holder.textView.text = item.title
        if (showMarker) {
            holder.markerImage.visibility = View.VISIBLE
            holder.markerImage.setImageResource(cs.getMarkerStyle(item.icon))
        } else {
            holder.markerImage.visibility = View.GONE
        }
    }

    override fun getItemViewType(position: Int): Int {
        return 0
    }

    override fun getItemId(position: Int): Long {
        return provider.data!![position].id
    }

    override fun getItemCount(): Int {
        return provider.data!!.size
    }
}
