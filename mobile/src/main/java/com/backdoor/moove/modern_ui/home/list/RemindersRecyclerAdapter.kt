package com.backdoor.moove.modern_ui.home.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.backdoor.moove.R
import com.backdoor.moove.utils.Contacts
import com.backdoor.moove.utils.ReminderUtils
import com.backdoor.moove.data.Reminder
import com.backdoor.moove.databinding.ListItemReminderBinding
import com.backdoor.moove.utils.*
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject

class RemindersRecyclerAdapter : RecyclerView.Adapter<RemindersRecyclerAdapter.ViewHolder>(), KoinComponent {

    private val prefs: Prefs by inject()

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

    inner class ViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_reminder, parent, false)) {

        val binding: ListItemReminderBinding = DataBindingUtil.bind(itemView)!!

        init {
            binding.itemCard.setOnClickListener {
                actionsListener?.onAction(it, adapterPosition, data[adapterPosition], ListActions.OPEN)
            }
            binding.itemCheck.setOnClickListener {
                actionsListener?.onAction(it, adapterPosition, data[adapterPosition], ListActions.SWITCH)
            }
        }

        fun bind(reminder: Reminder) {
            binding.taskText.text = reminder.summary
            binding.reminderType.text = ReminderUtils.getTypeString(itemView.context, reminder.type)

            if (reminder.type.contains(ReminderUtils.TYPE_CALL) || reminder.type.contains(ReminderUtils.TYPE_MESSAGE)) {
                binding.reminderPhone.text = reminder.phoneNumber
                val name = Contacts.getContactNameFromNumber(reminder.phoneNumber, itemView.context)
                if (name != null) {
                    binding.reminderContactName.text = name
                    binding.reminderContactName.show()
                } else {
                    binding.reminderContactName.hide()
                }
                binding.contactGroup.show()
            } else {
                binding.contactGroup.hide()
            }

            if (reminder.latitude != 0.0 || reminder.longitude != 0.0) {
                binding.taskDate.text = String.format("%.5f", reminder.latitude) + "\n" + String.format("%.5f", reminder.longitude)
            } else {
                binding.taskDate.text = TimeUtils.getFullDateTime(reminder.delayTime, prefs.use24Hour)
            }

            binding.itemCheck.isChecked = reminder.isActive
        }
    }
}