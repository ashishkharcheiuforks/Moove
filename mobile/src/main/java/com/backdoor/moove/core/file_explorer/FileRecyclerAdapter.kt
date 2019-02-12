package com.backdoor.moove.core.file_explorer

import android.content.Context
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView

import com.backdoor.moove.R
import com.backdoor.moove.databinding.ListItemFileLayoutBinding

import java.util.ArrayList

/**
 * Copyright 2016 Nazar Suhovich
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

class FileRecyclerAdapter(private val mContext: Context, dataItemList: List<FileDataItem>, private val mListener: RecyclerClickListener?, private val mCallback: FilterCallback?) : RecyclerView.Adapter<FileRecyclerAdapter.ContactViewHolder>() {
    private val mDataList: MutableList<FileDataItem>?

    init {
        this.mDataList = ArrayList(dataItemList)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val inflater = LayoutInflater.from(mContext)
        return ContactViewHolder(ListItemFileLayoutBinding.inflate(inflater, parent, false).root)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val item = mDataList!![position]
        holder.binding!!.item = item
    }

    override fun getItemCount(): Int {
        return mDataList?.size ?: 0
    }

    inner class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var binding: ListItemFileLayoutBinding? = null

        init {
            binding = DataBindingUtil.bind(itemView)
            binding!!.setClick { view ->
                mListener?.onItemClick(adapterPosition)
            }
        }
    }

    fun filter(q: String, list: List<FileDataItem>) {
        val res = filter(list, q)
        animateTo(res)
        mCallback?.filter(res.size)
    }

    private fun filter(mData: List<FileDataItem>?, q: String): List<FileDataItem> {
        var mData = mData
        var q = q
        q = q.toLowerCase()
        if (mData == null) mData = ArrayList()
        var filteredModelList: MutableList<FileDataItem> = ArrayList()
        if (q.matches("".toRegex())) {
            filteredModelList = ArrayList(mData)
        } else {
            filteredModelList.addAll(getFiltered(mData, q))
        }
        return filteredModelList
    }

    private fun getFiltered(models: List<FileDataItem>, query: String): List<FileDataItem> {
        val list = ArrayList<FileDataItem>()
        for (model in models) {
            val text = model.fileName.toLowerCase()
            if (text.contains(query)) {
                list.add(model)
            }
        }
        return list
    }

    fun getItem(position: Int): FileDataItem {
        return mDataList!![position]
    }

    fun removeItem(position: Int): FileDataItem {
        val model = mDataList!!.removeAt(position)
        notifyItemRemoved(position)
        return model
    }

    fun addItem(position: Int, model: FileDataItem) {
        mDataList!!.add(position, model)
        notifyItemInserted(position)
    }

    fun moveItem(fromPosition: Int, toPosition: Int) {
        val model = mDataList!!.removeAt(fromPosition)
        mDataList.add(toPosition, model)
        notifyItemMoved(fromPosition, toPosition)
    }

    fun animateTo(models: List<FileDataItem>) {
        applyAndAnimateRemovals(models)
        applyAndAnimateAdditions(models)
        applyAndAnimateMovedItems(models)
    }

    private fun applyAndAnimateRemovals(newModels: List<FileDataItem>) {
        for (i in mDataList!!.indices.reversed()) {
            val model = mDataList[i]
            if (!newModels.contains(model)) {
                removeItem(i)
            }
        }
    }

    private fun applyAndAnimateAdditions(newModels: List<FileDataItem>) {
        var i = 0
        val count = newModels.size
        while (i < count) {
            val model = newModels[i]
            if (!mDataList!!.contains(model)) {
                addItem(i, model)
            }
            i++
        }
    }

    private fun applyAndAnimateMovedItems(newModels: List<FileDataItem>) {
        for (toPosition in newModels.indices.reversed()) {
            val model = newModels[toPosition]
            val fromPosition = mDataList!!.indexOf(model)
            if (fromPosition >= 0 && fromPosition != toPosition) {
                moveItem(fromPosition, toPosition)
            }
        }
    }

    companion object {

        @BindingAdapter("loadImage")
        fun loadImage(imageView: ImageView, v: String) {
            imageView.setImageResource(getFileIcon(v))
        }

        private fun getFileIcon(file: String): Int {
            return if (isMelody(file)) {
                R.drawable.ic_music_note_white_24dp
            } else if (isPicture(file)) {
                R.drawable.ic_image_white_24dp
            } else if (isMovie(file)) {
                R.drawable.ic_movie_white_24dp
            } else if (isGif(file)) {
                R.drawable.ic_gif_white_24dp
            } else if (isArchive(file)) {
                R.drawable.ic_storage_white_24dp
            } else if (isAndroid(file)) {
                R.drawable.ic_android_white_24dp
            } else if (!file.contains(".")) {
                R.drawable.ic_folder_white_24dp
            } else {
                R.drawable.ic_insert_drive_file_white_24dp
            }
        }

        private fun isPicture(file: String): Boolean {
            return file.contains(".jpg") || file.contains(".jpeg") || file.contains(".png")
        }

        private fun isArchive(file: String): Boolean {
            return file.contains(".zip") || file.contains(".rar") || file.contains(".tar.gz")
        }

        private fun isMovie(file: String): Boolean {
            return file.contains(".mov") || file.contains(".3gp") || file.contains(".avi") ||
                    file.contains(".mkv") || file.contains(".vob") || file.contains(".divx") ||
                    file.contains(".mp4") || file.contains(".flv")
        }

        private fun isGif(file: String): Boolean {
            return file.contains(".gif")
        }

        private fun isAndroid(file: String): Boolean {
            return file.contains(".apk")
        }

        private fun isMelody(file: String): Boolean {
            return file.contains(".mp3") || file.contains(".ogg") || file.contains(".m4a") || file.contains(".flac")
        }
    }
}
