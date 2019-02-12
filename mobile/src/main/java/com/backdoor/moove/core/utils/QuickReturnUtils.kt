package com.backdoor.moove.core.utils

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import android.util.DisplayMetrics
import android.view.Display
import android.view.View
import android.view.WindowManager

import java.util.Dictionary
import java.util.Hashtable

object QuickReturnUtils {

    private val sRecyclerViewItemHeights = Hashtable<Int, Int>()

    fun dp2px(context: Context, dp: Int): Int {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = wm.defaultDisplay

        val displaymetrics = DisplayMetrics()
        display.getMetrics(displaymetrics)

        return (dp * displaymetrics.density + 0.5f).toInt()
    }

    fun getScrollY(rv: RecyclerView, columnCount: Int, mIsGrid: Boolean): Int {
        val c = rv.getChildAt(0) ?: return 0

        var firstVisiblePosition: Int
        if (mIsGrid) {
            try {
                val layoutManager = rv.layoutManager as StaggeredGridLayoutManager?
                val pos = IntArray(4)
                layoutManager!!.findFirstVisibleItemPositions(pos)
                firstVisiblePosition = pos[0]
            } catch (e: ClassCastException) {
                val layoutManager = rv.layoutManager as LinearLayoutManager?
                firstVisiblePosition = layoutManager!!.findFirstVisibleItemPosition()
            }

        } else {
            try {
                val layoutManager = rv.layoutManager as LinearLayoutManager?
                firstVisiblePosition = layoutManager!!.findFirstVisibleItemPosition()
            } catch (e: ClassCastException) {
                val layoutManager = rv.layoutManager as StaggeredGridLayoutManager?
                val pos = IntArray(4)
                layoutManager!!.findFirstVisibleItemPositions(pos)
                firstVisiblePosition = pos[0]
            }

        }

        var scrollY = -c.top

        if (columnCount > 1) {
            sRecyclerViewItemHeights[firstVisiblePosition] = c.height + QuickReturnUtils.dp2px(rv.context, 8) / columnCount
        } else {
            sRecyclerViewItemHeights[firstVisiblePosition] = c.height
        }

        if (scrollY < 0)
            scrollY = 0

        for (i in 0 until firstVisiblePosition) {
            if (sRecyclerViewItemHeights[i] != null)
            // (this is a sanity check)
                scrollY += sRecyclerViewItemHeights[i] //add all heights of the views that are gone
        }

        return scrollY
    }
}
