package com.backdoor.moove.utils

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Point
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.SeekBar
import androidx.appcompat.widget.PopupMenu
import com.backdoor.moove.R
import com.backdoor.moove.databinding.DialogBottomColorSliderBinding
import com.backdoor.moove.databinding.DialogBottomSeekAndTitleBinding
import com.google.android.material.bottomsheet.BottomSheetDialog

class Dialogues(val prefs: Prefs) {

    private var mSelectedItem = 0

    fun showColorBottomDialog(activity: Activity, current: Int, colors: IntArray, onChange: (Int) -> Unit) {
        val dialog = BottomSheetDialog(activity)
        val b = DialogBottomColorSliderBinding.inflate(LayoutInflater.from(activity))
        b.colorSlider.setColors(colors)
        b.colorSlider.setSelectorColorResource(R.color.whitePrimary)
        b.colorSlider.setSelection(current)
        b.colorSlider.setListener { i, _ ->
            onChange.invoke(i)
        }
        dialog.setContentView(b.root)
        dialog.show()
    }

    fun showRadiusBottomDialog(activity: Activity, current: Int, listener: (Int) -> String) {
        val dialog = BottomSheetDialog(activity)
        val b = DialogBottomSeekAndTitleBinding.inflate(LayoutInflater.from(activity))
        b.seekBar.max = MAX_DEF_RADIUS
        if (b.seekBar.max < current && b.seekBar.max < MAX_RADIUS) {
            b.seekBar.max = (current + (b.seekBar.max * 0.2)).toInt()
        }
        if (current > MAX_RADIUS) {
            b.seekBar.max = MAX_RADIUS
        }
        b.seekBar.max = current * 2
        if (current == 0) {
            b.seekBar.max = MAX_DEF_RADIUS
        }
        b.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                b.titleView.text = listener.invoke(progress)
                val perc = progress.toFloat() / b.seekBar.max.toFloat() * 100f
                if (perc > 95f && b.seekBar.max < MAX_RADIUS) {
                    b.seekBar.max = (b.seekBar.max + (b.seekBar.max * 0.2)).toInt()
                } else if (perc < 10f && b.seekBar.max > 5000) {
                    b.seekBar.max = (b.seekBar.max - (b.seekBar.max * 0.2)).toInt()
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {

            }
        })
        b.seekBar.progress = current
        b.titleView.text = listener.invoke(current)
        dialog.setContentView(b.root)
        dialog.show()
    }

    fun imageDialog(context: Activity, listener: () -> Unit) {
        val builder = AlertDialog.Builder(context)
        builder.setCancelable(true)
        builder.setTitle(context.getString(R.string.background_image))
        val types = arrayOf(context.getString(R.string.none), context.getString(R.string.default_string), context.getString(R.string.select_image))

        val adapter = ArrayAdapter(context,
                android.R.layout.simple_list_item_single_choice, types)

        val image = prefs.reminderImage
        mSelectedItem = when {
            image.matches(Module.NONE.toRegex()) -> 0
            image.matches(Module.DEFAULT.toRegex()) -> 1
            else -> 2
        }

        builder.setSingleChoiceItems(adapter, mSelectedItem) { _, which ->
            if (which != -1) {
                mSelectedItem = which
            }
        }
        builder.setPositiveButton(context.getString(R.string.ok)) { dialog, _ ->
            dialog.dismiss()
            when (mSelectedItem) {
                0 -> prefs.reminderImage = Module.NONE
                1 -> prefs.reminderImage = Module.DEFAULT
                2 -> {
                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                    intent.addCategory(Intent.CATEGORY_OPENABLE)
                    intent.type = "image/*"
                    val chooser = Intent.createChooser(intent, context.getString(R.string.select_image))
                    context.startActivityForResult(chooser, Module.ACTION_REQUEST_GALLERY)
                }
            }
        }
        val dialog = builder.create()
        dialog.setOnDismissListener{ listener.invoke() }
        dialog.show()
    }

    companion object {
        private const val MAX_RADIUS = 100000
        private const val MAX_DEF_RADIUS = 5000

        fun showPopup(anchor: View,
                      listener: ((Int) -> Unit)?, vararg actions: String) {
            val popupMenu = PopupMenu(anchor.context, anchor)
            popupMenu.setOnMenuItemClickListener { item ->
                listener?.invoke(item.order)
                true
            }
            for (i in actions.indices) {
                popupMenu.menu.add(1, i + 1000, i, actions[i])
            }
            popupMenu.show()
        }

        fun setFullWidthDialog(dialog: AlertDialog, activity: Activity) {
            val window = dialog.window
            window?.setGravity(Gravity.CENTER)
            window?.setLayout((getScreenWidth(activity) * .9).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
        }

        private fun getScreenWidth(activity: Activity): Int {
            val size = Point()
            activity.windowManager.defaultDisplay.getSize(size)
            return size.x
        }
    }
}
