package com.backdoor.moove.core.adapters

import android.content.Context
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.ToggleButton

import com.backdoor.moove.R
import com.backdoor.moove.core.consts.Configs
import com.backdoor.moove.core.consts.Constants
import com.backdoor.moove.core.consts.Prefs
import com.backdoor.moove.core.data.ReminderModel
import com.backdoor.moove.core.helper.Coloring
import com.backdoor.moove.core.helper.Contacts
import com.backdoor.moove.core.helper.Module
import com.backdoor.moove.core.helper.SharedPrefs
import com.backdoor.moove.core.interfaces.RecyclerListener
import com.backdoor.moove.core.utils.ReminderUtils
import com.backdoor.moove.core.utils.TimeUtil

import java.util.ArrayList

class RemindersRecyclerAdapter(private val mContext: Context, private val provider: ArrayList<ReminderModel>) : RecyclerView.Adapter<RemindersRecyclerAdapter.ViewHolder>() {
    private val cs: Coloring
    var eventListener: RecyclerListener? = null
    private val is24: Boolean

    init {
        cs = Coloring(mContext)
        is24 = SharedPrefs.getInstance(mContext)!!.loadBoolean(Prefs.IS_24_TIME_FORMAT)
        setHasStableIds(true)
    }

    inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v), View.OnClickListener, View.OnLongClickListener {

        var taskTitle: TextView
        var taskDate: TextView
        var reminder_type: TextView
        var reminder_phone: TextView
        var reminder_contact_name: TextView
        var listHeader: TextView
        var check: ToggleButton
        var itemCard: CardView

        var reminderContainer: RelativeLayout
        var contactGroup: LinearLayout

        init {
            reminderContainer = v.findViewById(R.id.reminderContainer)
            contactGroup = v.findViewById(R.id.contactGroup)
            listHeader = v.findViewById(R.id.listHeader)
            check = v.findViewById(R.id.itemCheck)
            taskDate = v.findViewById(R.id.taskDate)
            taskDate.text = ""
            reminder_type = v.findViewById(R.id.reminder_type)
            reminder_type.text = ""
            reminder_phone = v.findViewById(R.id.reminder_phone)
            reminder_phone.text = ""
            reminder_contact_name = v.findViewById(R.id.reminder_contact_name)
            reminder_contact_name.text = ""

            taskTitle = v.findViewById(R.id.taskText)
            taskTitle.text = ""
            itemCard = v.findViewById(R.id.itemCard)
            itemCard.setCardBackgroundColor(cs.cardStyle)
            if (Module.isLollipop) {
                itemCard.cardElevation = Configs.CARD_ELEVATION
            }

            v.setOnClickListener(this)
            v.setOnLongClickListener(this)
            check.setOnClickListener { v1 ->
                if (eventListener != null) {
                    eventListener!!.onItemSwitched(adapterPosition, check)
                }
            }
        }

        override fun onClick(v: View) {
            val view = itemCard
            if (eventListener != null) {
                eventListener!!.onItemClicked(adapterPosition, view)
            }
        }

        override fun onLongClick(v: View): Boolean {
            if (eventListener != null) {
                eventListener!!.onItemLongClicked(adapterPosition, itemCard)
            }
            return true
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // create a new view
        val itemLayoutView = LayoutInflater.from(parent.context)
                .inflate(R.layout.list_item_card, parent, false)

        // create ViewHolder
        return ViewHolder(itemLayoutView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = provider[position]
        val title = item.title
        val type = item.type
        val number = item.number
        val due = item.startTime
        val lat = item.place[0]
        val lon = item.place[1]
        val isDone = item.statusDb

        holder.reminderContainer.visibility = View.VISIBLE

        holder.taskTitle.text = ""
        holder.reminder_contact_name.text = ""
        holder.taskDate.text = ""
        holder.reminder_type.text = ""
        holder.reminder_phone.text = ""

        holder.taskTitle.text = title
        holder.reminder_type.text = ReminderUtils.getTypeString(mContext, type)

        if (type.contains(Constants.TYPE_CALL) || type.contains(Constants.TYPE_MESSAGE)) {
            holder.reminder_phone.text = number
            val name = Contacts.getContactNameFromNumber(number, mContext)
            if (name != null) {
                holder.reminder_contact_name.text = name
            } else {
                holder.reminder_contact_name.text = ""
            }
            holder.contactGroup.visibility = View.VISIBLE
        } else {
            holder.contactGroup.visibility = View.GONE
        }

        if (lat != 0.0 || lon != 0.0) {
            holder.taskDate.text = String.format("%.5f", lat) + "\n" + String.format("%.5f", lon)
        } else {
            holder.taskDate.text = TimeUtil.getFullDateTime(due, is24)
        }

        if (isDone == Constants.DISABLE) {
            holder.check.isChecked = false
        } else {
            holder.check.isChecked = true
        }
    }

    override fun getItemViewType(position: Int): Int {
        return 0
    }

    override fun getItemId(position: Int): Long {
        return provider[position].id
    }

    override fun getItemCount(): Int {
        return provider.size
    }
}