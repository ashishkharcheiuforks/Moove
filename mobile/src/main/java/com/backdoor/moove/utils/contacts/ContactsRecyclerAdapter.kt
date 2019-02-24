package com.backdoor.moove.utils.contacts

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter

class ContactsRecyclerAdapter : ListAdapter<ContactItem, ContactHolder>(ContactDiffCallback()) {

    var clickListener: ((name: String, number: String) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactHolder {
        return ContactHolder(parent) { performClick(it) }
    }

    private fun performClick(it: Int) {
        val item = getItem(it)
        clickListener?.invoke(item.name, "")
    }

    override fun onBindViewHolder(holder: ContactHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
