package com.backdoor.moove.core.views

import android.animation.ObjectAnimator
import androidx.recyclerview.widget.RecyclerView
import android.util.Log
import android.view.View

import com.backdoor.moove.core.consts.QuickReturnViewType
import com.backdoor.moove.core.utils.QuickReturnUtils

import java.util.ArrayList

class ReturnScrollListener
// endregion

// region Constructors
private constructor(builder: Builder) : RecyclerView.OnScrollListener() {

    // region Member Variables
    private val mQuickReturnViewType: QuickReturnViewType
    private val mHeader: View?
    private val mMinHeaderTranslation: Int
    private val mFooter: View?
    private val mMinFooterTranslation: Int
    private val mIsSnappable: Boolean // Can Quick Return view snap into place?
    private val mIsGrid: Boolean

    private var mPrevScrollY = 0
    private var mHeaderDiffTotal = 0
    private var mFooterDiffTotal = 0
    private val mColumnCount: Int
    private val mExtraOnScrollListenerList = ArrayList<RecyclerView.OnScrollListener>()

    init {
        mQuickReturnViewType = builder.mQuickReturnViewType
        mHeader = builder.mHeader
        mMinHeaderTranslation = builder.mMinHeaderTranslation
        mFooter = builder.mFooter
        mColumnCount = builder.mColumnCount
        mMinFooterTranslation = builder.mMinFooterTranslation
        mIsSnappable = builder.mIsSnappable
        mIsGrid = builder.isGrid
    }
    // endregion

    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        Log.d(javaClass.simpleName, "onScrollStateChanged() : scrollState - $newState")
        // apply another list' s on scroll listener
        for (listener in mExtraOnScrollListenerList) {
            listener.onScrollStateChanged(recyclerView, newState)
        }

        if (newState == RecyclerView.SCROLL_STATE_IDLE && mIsSnappable) {

            val midHeader = -mMinHeaderTranslation / 2
            val midFooter = mMinFooterTranslation / 2

            when (mQuickReturnViewType) {
                QuickReturnViewType.HEADER -> if (-mHeaderDiffTotal > 0 && -mHeaderDiffTotal < midHeader) {
                    val anim = ObjectAnimator.ofFloat(mHeader, "translationY", mHeader!!.translationY, 0)
                    anim.setDuration(100)
                    anim.start()
                    mHeaderDiffTotal = 0
                } else if (-mHeaderDiffTotal < -mMinHeaderTranslation && -mHeaderDiffTotal >= midHeader) {
                    val anim = ObjectAnimator.ofFloat(mHeader, "translationY", mHeader!!.translationY, mMinHeaderTranslation)
                    anim.setDuration(100)
                    anim.start()
                    mHeaderDiffTotal = mMinHeaderTranslation
                }
                QuickReturnViewType.FOOTER -> if (-mFooterDiffTotal > 0 && -mFooterDiffTotal < midFooter) { // slide up
                    val anim = ObjectAnimator.ofFloat(mFooter, "translationY", mFooter!!.translationY, 0)
                    anim.setDuration(100)
                    anim.start()
                    mFooterDiffTotal = 0
                } else if (-mFooterDiffTotal < mMinFooterTranslation && -mFooterDiffTotal >= midFooter) { // slide down
                    val anim = ObjectAnimator.ofFloat(mFooter, "translationY", mFooter!!.translationY, mMinFooterTranslation)
                    anim.setDuration(100)
                    anim.start()
                    mFooterDiffTotal = -mMinFooterTranslation
                }
                QuickReturnViewType.BOTH -> {
                    if (-mHeaderDiffTotal > 0 && -mHeaderDiffTotal < midHeader) {
                        val anim = ObjectAnimator.ofFloat(mHeader, "translationY", mHeader!!.translationY, 0)
                        anim.setDuration(100)
                        anim.start()
                        mHeaderDiffTotal = 0
                    } else if (-mHeaderDiffTotal < -mMinHeaderTranslation && -mHeaderDiffTotal >= midHeader) {
                        val anim = ObjectAnimator.ofFloat(mHeader, "translationY", mHeader!!.translationY, mMinHeaderTranslation)
                        anim.setDuration(100)
                        anim.start()
                        mHeaderDiffTotal = mMinHeaderTranslation
                    }

                    if (-mFooterDiffTotal > 0 && -mFooterDiffTotal < midFooter) { // slide up
                        val anim = ObjectAnimator.ofFloat(mFooter, "translationY", mFooter!!.translationY, 0)
                        anim.setDuration(100)
                        anim.start()
                        mFooterDiffTotal = 0
                    } else if (-mFooterDiffTotal < mMinFooterTranslation && -mFooterDiffTotal >= midFooter) { // slide down
                        val anim = ObjectAnimator.ofFloat(mFooter, "translationY", mFooter!!.translationY, mMinFooterTranslation)
                        anim.setDuration(100)
                        anim.start()
                        mFooterDiffTotal = -mMinFooterTranslation
                    }
                }
                QuickReturnViewType.TWITTER -> {
                    if (-mHeaderDiffTotal > 0 && -mHeaderDiffTotal < midHeader) {
                        val anim = ObjectAnimator.ofFloat(mHeader, "translationY", mHeader!!.translationY, 0)
                        anim.setDuration(100)
                        anim.start()
                        mHeaderDiffTotal = 0
                    } else if (-mHeaderDiffTotal < -mMinHeaderTranslation && -mHeaderDiffTotal >= midHeader) {
                        val anim = ObjectAnimator.ofFloat(mHeader, "translationY", mHeader!!.translationY, mMinHeaderTranslation)
                        anim.setDuration(100)
                        anim.start()
                        mHeaderDiffTotal = mMinHeaderTranslation
                    }

                    if (-mFooterDiffTotal > 0 && -mFooterDiffTotal < midFooter) { // slide up
                        val anim = ObjectAnimator.ofFloat(mFooter, "translationY", mFooter!!.translationY, 0)
                        anim.setDuration(100)
                        anim.start()
                        mFooterDiffTotal = 0
                    } else if (-mFooterDiffTotal < mMinFooterTranslation && -mFooterDiffTotal >= midFooter) { // slide down
                        val anim = ObjectAnimator.ofFloat(mFooter, "translationY", mFooter!!.translationY, mMinFooterTranslation)
                        anim.setDuration(100)
                        anim.start()
                        mFooterDiffTotal = -mMinFooterTranslation
                    }
                }
            }

        }
    }

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        // apply extra on scroll listener
        for (listener in mExtraOnScrollListenerList) {
            /*RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
            int firstVisibleItem = 0;
            int visibleItemCount = 0;
            int totalItemCount = 0;
            if (manager != null){
                totalItemCount = manager.getItemCount();
                visibleItemCount = manager.getChildCount();
            }*/
            listener.onScrolled(recyclerView, dx, dy)
        }
        val scrollY = QuickReturnUtils.getScrollY(recyclerView, mColumnCount, mIsGrid)
        val diff = mPrevScrollY - scrollY

        //        Log.d(getClass().getSimpleName(), "onScroll() : scrollY - "+scrollY);
        //        Log.d(getClass().getSimpleName(), "onScroll() : diff - "+diff);
        //        Log.d(getClass().getSimpleName(), "onScroll() : mMinHeaderTranslation - "+mMinHeaderTranslation);
        //        Log.d(getClass().getSimpleName(), "onScroll() : mMinFooterTranslation - "+mMinFooterTranslation);

        if (diff != 0) {
            when (mQuickReturnViewType) {
                QuickReturnViewType.HEADER -> {
                    if (diff < 0) { // scrolling down
                        mHeaderDiffTotal = Math.max(mHeaderDiffTotal + diff, mMinHeaderTranslation)
                    } else { // scrolling up
                        mHeaderDiffTotal = Math.min(Math.max(mHeaderDiffTotal + diff, mMinHeaderTranslation), 0)
                    }

                    mHeader!!.translationY = mHeaderDiffTotal.toFloat()
                }
                QuickReturnViewType.FOOTER -> {
                    if (diff < 0) { // scrolling down
                        mFooterDiffTotal = Math.max(mFooterDiffTotal + diff, -mMinFooterTranslation)
                    } else { // scrolling up
                        mFooterDiffTotal = Math.min(Math.max(mFooterDiffTotal + diff, -mMinFooterTranslation), 0)
                    }

                    mFooter!!.translationY = (-mFooterDiffTotal).toFloat()
                }
                QuickReturnViewType.BOTH -> {
                    if (diff < 0) { // scrolling down
                        mHeaderDiffTotal = Math.max(mHeaderDiffTotal + diff, mMinHeaderTranslation)
                        mFooterDiffTotal = Math.max(mFooterDiffTotal + diff, -mMinFooterTranslation)
                    } else { // scrolling up
                        mHeaderDiffTotal = Math.min(Math.max(mHeaderDiffTotal + diff, mMinHeaderTranslation), 0)
                        mFooterDiffTotal = Math.min(Math.max(mFooterDiffTotal + diff, -mMinFooterTranslation), 0)
                    }

                    mHeader!!.translationY = mHeaderDiffTotal.toFloat()
                    mFooter!!.translationY = (-mFooterDiffTotal).toFloat()
                }
                QuickReturnViewType.TWITTER -> {
                    if (diff < 0) { // scrolling down
                        if (scrollY > -mMinHeaderTranslation)
                            mHeaderDiffTotal = Math.max(mHeaderDiffTotal + diff, mMinHeaderTranslation)

                        if (scrollY > mMinFooterTranslation)
                            mFooterDiffTotal = Math.max(mFooterDiffTotal + diff, -mMinFooterTranslation)
                    } else { // scrolling up
                        mHeaderDiffTotal = Math.min(Math.max(mHeaderDiffTotal + diff, mMinHeaderTranslation), 0)
                        mFooterDiffTotal = Math.min(Math.max(mFooterDiffTotal + diff, -mMinFooterTranslation), 0)
                    }

                    mHeader!!.translationY = mHeaderDiffTotal.toFloat()
                    mFooter!!.translationY = (-mFooterDiffTotal).toFloat()
                }
                else -> {
                }
            }
        }

        mPrevScrollY = scrollY
    }

    // region Helper Methods
    fun registerExtraOnScrollListener(listener: RecyclerView.OnScrollListener) {
        mExtraOnScrollListenerList.add(listener)
    }
    // endregion

    // region Inner Classes

    class Builder(// Required parameters
            private val mQuickReturnViewType: QuickReturnViewType) {

        // Optional parameters - initialized to default values
        private var mHeader: View? = null
        private var mMinHeaderTranslation = 0
        private var mFooter: View? = null
        private var mMinFooterTranslation = 0
        private var mIsSnappable = false
        private var mColumnCount = 1
        private var isGrid = false

        fun header(header: View): Builder {
            mHeader = header
            return this
        }

        fun minHeaderTranslation(minHeaderTranslation: Int): Builder {
            mMinHeaderTranslation = minHeaderTranslation
            return this
        }

        fun footer(footer: View): Builder {
            mFooter = footer
            return this
        }

        fun minFooterTranslation(minFooterTranslation: Int): Builder {
            mMinFooterTranslation = minFooterTranslation
            return this
        }

        fun columnCount(columnCount: Int): Builder {
            mColumnCount = columnCount
            return this
        }

        fun isSnappable(isSnappable: Boolean): Builder {
            mIsSnappable = isSnappable
            return this
        }

        fun isGrid(isGrid: Boolean): Builder {
            this.isGrid = isGrid
            return this
        }

        fun build(): ReturnScrollListener {
            return ReturnScrollListener(this)
        }
    }
}
