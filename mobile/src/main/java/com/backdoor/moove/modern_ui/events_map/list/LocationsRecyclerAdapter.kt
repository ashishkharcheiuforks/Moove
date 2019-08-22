package com.backdoor.moove.modern_ui.events_map.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.backdoor.moove.R
import com.backdoor.moove.data.Reminder
import com.backdoor.moove.databinding.ListItemLocationBinding
import com.backdoor.moove.utils.ActionsListener
import com.backdoor.moove.utils.Coloring
import com.backdoor.moove.utils.DrawableHelper
import com.backdoor.moove.utils.ListActions
import org.koin.core.KoinComponent
import org.koin.core.inject

class LocationsRecyclerAdapter : RecyclerView.Adapter<LocationsRecyclerAdapter.ViewHolder>(), KoinComponent {

    private val coloring: Coloring by inject()

    var data: MutableList<Reminder> = mutableListOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    var actionsListener: ActionsListener<Reminder>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int {
        return data.size
    }

    inner class ViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_location, parent, false)) {

        val binding: ListItemLocationBinding = DataBindingUtil.bind(itemView)!!

        init {
            binding.itemCard.setOnClickListener {
                actionsListener?.onAction(it, adapterPosition, data[adapterPosition], ListActions.OPEN)
            }
        }

        fun bind(reminder: Reminder) {
            binding.textView.text = reminder.summary
            val pointer = DrawableHelper.withContext(binding.root.context)
                    .withDrawable(R.drawable.ic_twotone_place_24px)
                    .withColor(coloring.accentColor(reminder.markerColor))
                    .tint()
                    .get()
            binding.markerImage.setImageDrawable(pointer)
        }
    }
}