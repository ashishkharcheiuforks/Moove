package com.backdoor.moove.utils.file_explorer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.backdoor.moove.R
import com.backdoor.moove.databinding.ListItemFileBinding
import com.backdoor.moove.utils.ActionsListener
import com.backdoor.moove.utils.ListActions
import com.backdoor.moove.utils.UriUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import timber.log.Timber
import java.io.File

class FileRecyclerAdapter : ListAdapter<FileItem, FileRecyclerAdapter.ContactViewHolder>(FileDiffCallback()) {

    var clickListener: ActionsListener<FileItem>? = null

    override fun submitList(list: List<FileItem>?) {
        super.submitList(list)
        notifyDataSetChanged()
    }

    fun getFileItem(position: Int): FileItem {
        return super.getItem(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        return ContactViewHolder(parent)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ContactViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_file, parent, false)) {

        private val binding: ListItemFileBinding = DataBindingUtil.bind(itemView)!!

        fun bind(fileItem: FileItem) {
            if (fileItem.isUp) {
                binding.itemName.text = itemView.context.getString(R.string.up)
            } else {
                binding.itemName.text = fileItem.fileName
            }
            loadImage(fileItem)
        }

        init {
            binding.clickView.setOnClickListener {
                clickListener?.onAction(it, adapterPosition, getFileItem(adapterPosition), ListActions.OPEN)
            }
            binding.clickView.setOnLongClickListener {
                clickListener?.onAction(it, adapterPosition, getFileItem(adapterPosition), ListActions.MORE)
                return@setOnLongClickListener true
            }
        }

        private fun loadImage(item: FileItem) {
            binding.itemImage.visibility = View.VISIBLE
            binding.itemPhoto.visibility = View.GONE
            if (item.filePath != "") {
                binding.itemImage.setImageResource(getFileIcon(File(item.filePath)))
            } else {
                binding.itemImage.setImageResource(item.icon)
            }
            if (item.filePath != "" && isPicture(item.filePath)) {
                binding.itemImage.visibility = View.GONE
                binding.itemPhoto.visibility = View.VISIBLE
                Glide.with(binding.itemPhoto.context)
                        .load(UriUtil.getUri(binding.itemPhoto.context, item.filePath))
                        .apply(RequestOptions.centerCropTransform())
                        .apply(RequestOptions.overrideOf(100, 100))
                        .into(binding.itemPhoto)
            }
        }
    }

    private fun getFileIcon(file: File): Int {
        Timber.d("getFileIcon: $file")
        return when {
            file.isDirectory -> R.drawable.ic_twotone_folder_24px
            isMelody(file.name) -> R.drawable.ic_twotone_music_note_24px
            isPicture(file.name) -> R.drawable.ic_twotone_image_24px
            isMovie(file.name) -> R.drawable.ic_twotone_movie_24px
            isGif(file.name) -> R.drawable.ic_twotone_gif_24px
            isArchive(file.name) -> R.drawable.ic_twotone_archive_24px
            isAndroid(file.name) -> R.drawable.ic_twotone_android_24px
            isCode(file.name) -> R.drawable.ic_twotone_code_24px
            else -> R.drawable.ic_twotone_insert_drive_file_24px
        }
    }

    private fun isCode(file: String): Boolean {
        return file.contains(".xml") || file.contains(".html") || file.contains(".java")
                || file.contains(".py") || file.contains(".xhtml") || file.contains(".css")
                || file.contains(".json")
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

    companion object {
        fun isPicture(file: String): Boolean {
            return file.contains(".jpg") || file.contains(".jpeg") || file.contains(".png")
                    || file.contains(".tiff") || file.contains(".bmp")
        }
    }
}
