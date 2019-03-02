package com.backdoor.moove.utils.contacts

import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.backdoor.moove.R
import com.backdoor.moove.databinding.ListItemContactBinding
import com.backdoor.moove.utils.BitmapUtils
import com.backdoor.moove.utils.ViewUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition

class ContactHolder(parent: ViewGroup, callback: ((Int) -> Unit)?)
    : RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_contact, parent, false)) {

    val binding: ListItemContactBinding = DataBindingUtil.bind(itemView)!!

    fun bind(contactItem: ContactItem) {
        binding.itemName.text = contactItem.name
        loadImage(binding.itemImage, contactItem)
    }

    init {
        binding.clickView.setOnClickListener { callback?.invoke(adapterPosition) }
    }

    private fun loadImage(imageView: ImageView, contactItem: ContactItem) {
        if (contactItem.uri == null) {
            loadNameIcon(contactItem, imageView)
            return
        }
        Glide.with(imageView)
                .load(Uri.parse(contactItem.uri))
                .apply(RequestOptions.centerCropTransform())
                .apply(RequestOptions.overrideOf(100, 100))
                .into(object : SimpleTarget<Drawable>() {
                    override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                        imageView.setImageDrawable(resource)
                    }

                    override fun onLoadFailed(errorDrawable: Drawable?) {
                        super.onLoadFailed(errorDrawable)
                        loadNameIcon(contactItem, imageView)
                    }
                })
    }

    private fun loadNameIcon(contactItem: ContactItem, imageView: ImageView) {
        BitmapUtils.imageFromName(contactItem.name) {
            if (it != null) {
                imageView.setImageDrawable(it)
            } else {
                imageView.setImageDrawable(ViewUtils.tintIcon(imageView.context, R.drawable.ic_twotone_person_24px, true))
            }
        }
    }
}