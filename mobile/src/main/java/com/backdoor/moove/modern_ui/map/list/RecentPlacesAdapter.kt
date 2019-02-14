package com.backdoor.moove.modern_ui.map.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.backdoor.moove.R
import com.backdoor.moove.utils.Coloring
import com.backdoor.moove.data.Place
import com.backdoor.moove.databinding.ListItemMapPlaceBinding
import com.backdoor.moove.utils.ActionsListener
import com.backdoor.moove.utils.DrawableHelper
import com.backdoor.moove.utils.ListActions
import com.backdoor.moove.utils.TimeUtils
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject

class RecentPlacesAdapter : RecyclerView.Adapter<RecentPlacesAdapter.ViewHolder>(), KoinComponent {

    val coloring by inject<Coloring>()

    private val mData = mutableListOf<Place>()
    var actionsListener: ActionsListener<Place>? = null

    var data: List<Place>
        get() = mData
        set(list) {
            this.mData.clear()
            this.mData.addAll(list)
            notifyDataSetChanged()
        }

    override fun getItemCount(): Int {
        return mData.size
    }

    inner class ViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_map_place, parent, false)) {

        private val binding: ListItemMapPlaceBinding = DataBindingUtil.bind(itemView)!!

        fun bind(item: Place) {
            binding.textView.text = item.name

            val dmy = TimeUtils.getPlaceDateTimeFromGmt(item.createdAt)
            binding.dayView.text = dmy.day
            binding.monthYearView.text = "${dmy.month}\n${dmy.year}"

            DrawableHelper.withContext(itemView.context)
                    .withDrawable(R.drawable.ic_twotone_place_24px)
                    .withColor(coloring.accentColor(item.markerColor))
                    .tint()
                    .applyTo(binding.markerImage)
        }

        init {
            binding.itemCard.setOnClickListener { view ->
                actionsListener?.onAction(view, adapterPosition, getItem(adapterPosition), ListActions.OPEN)
            }
        }
    }

    fun getItem(position: Int): Place {
        return mData[position]
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(parent)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}