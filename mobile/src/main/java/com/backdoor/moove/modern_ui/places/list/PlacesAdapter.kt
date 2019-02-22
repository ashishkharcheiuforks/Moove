package com.backdoor.moove.modern_ui.places.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.backdoor.moove.R
import com.backdoor.moove.data.Place
import com.backdoor.moove.databinding.ListItemPlaceBinding
import com.backdoor.moove.utils.ActionsListener
import com.backdoor.moove.utils.Coloring
import com.backdoor.moove.utils.DrawableHelper
import com.backdoor.moove.utils.ListActions
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject

class PlacesAdapter(private val showMarker: Boolean) : RecyclerView.Adapter<PlacesAdapter.ViewHolder>(), KoinComponent {

    val coloring: Coloring by inject()

    var data: MutableList<Place> = mutableListOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    var actionsListener: ActionsListener<Place>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int {
        return data.size
    }

    inner class ViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_place, parent, false)) {

        val binding: ListItemPlaceBinding = DataBindingUtil.bind(itemView)!!

        init {
            binding.itemCard.setOnClickListener {
                actionsListener?.onAction(it, adapterPosition, data[adapterPosition], ListActions.OPEN)
            }
            binding.itemCard.setOnLongClickListener {
                actionsListener?.onAction(it, adapterPosition, data[adapterPosition], ListActions.MORE)
                return@setOnLongClickListener true
            }
        }

        fun bind(place: Place) {
            binding.textView.text = place.name
            if (showMarker) {
                val pointer = DrawableHelper.withContext(binding.root.context)
                        .withDrawable(R.drawable.ic_twotone_place_24px)
                        .withColor(coloring.accentColor(place.markerColor))
                        .tint()
                        .get()
                binding.markerImage.visibility = View.VISIBLE
                binding.markerImage.setImageDrawable(pointer)
            } else {
                binding.markerImage.visibility = View.GONE
            }
        }
    }
}
