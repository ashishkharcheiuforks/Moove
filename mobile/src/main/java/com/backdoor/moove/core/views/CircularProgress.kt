package com.backdoor.moove.core.views

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Property
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator

import com.backdoor.moove.R


/**
 * https://github.com/castorflex/SmoothProgressBar
 *
 *
 * Copyright 2014 Antoine Merle
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
class CircularProgress @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr) {

    private var mColor: Int = 0
    private val mSize: Int
    private val mIndeterminate: Boolean

    private var mIndeterminateProgressDrawable: IndeterminateProgressDrawable? = null
    private var mDeterminateProgressDrawable: DeterminateProgressDrawable? = null

    init {
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.CircularProgress)
        mColor = attributes.getColor(R.styleable.CircularProgress_circular_progress_color,
                resources.getColor(R.color.circular_progress_color))
        mSize = attributes.getInt(R.styleable.CircularProgress_circular_progress_size, NORMAL_SIZE)
        mIndeterminate = attributes.getBoolean(R.styleable.CircularProgress_circular_progress_indeterminate,
                resources.getBoolean(R.bool.circular_progress_indeterminate))
        val mBorderWidth = attributes.getDimensionPixelSize(R.styleable.CircularProgress_circular_progress_border_width,
                resources.getDimensionPixelSize(R.dimen.circular_progress_border_width))
        attributes.recycle()

        if (mIndeterminate) {
            mIndeterminateProgressDrawable = IndeterminateProgressDrawable(mColor, mBorderWidth.toFloat())
            mIndeterminateProgressDrawable!!.callback = this
        } else {
            mDeterminateProgressDrawable = DeterminateProgressDrawable(mColor, mBorderWidth, 0)
            mDeterminateProgressDrawable!!.callback = this
        }
    }

    fun setColor(color: Int) {
        mColor = color
        invalidate()
    }

    fun startAnimation() {
        if (visibility != View.VISIBLE) {
            return
        }
        mIndeterminateProgressDrawable!!.start()
    }

    fun stopAnimation() {
        mIndeterminateProgressDrawable!!.stop()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var widthMeasureSpec = widthMeasureSpec
        var heightMeasureSpec = heightMeasureSpec
        val widthSpecMode = View.MeasureSpec.getMode(widthMeasureSpec)
        val heightSpecMode = View.MeasureSpec.getMode(heightMeasureSpec)
        if (widthSpecMode == View.MeasureSpec.AT_MOST && heightSpecMode == View.MeasureSpec.AT_MOST) {
            var size = 0
            when (mSize) {
                SMALL_SIZE -> size = resources.getDimensionPixelSize(R.dimen.circular_progress_small_size)
                NORMAL_SIZE -> size = resources.getDimensionPixelSize(R.dimen.circular_progress_normal_size)
                LARGE_SIZE -> size = resources.getDimensionPixelSize(R.dimen.circular_progress_large_size)
            }
            widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(size, View.MeasureSpec.EXACTLY)
            heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(size, View.MeasureSpec.EXACTLY)
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        if (mIndeterminate) {
            mIndeterminateProgressDrawable!!.draw(canvas)
        } else {
            mDeterminateProgressDrawable!!.draw(canvas)
        }
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        if (mIndeterminate) {
            if (visibility == View.VISIBLE) {
                mIndeterminateProgressDrawable!!.start()
            } else {
                mIndeterminateProgressDrawable!!.stop()
            }
        }
    }

    override fun verifyDrawable(drawable: Drawable): Boolean {
        return if (mIndeterminate) {
            drawable === mIndeterminateProgressDrawable || super.verifyDrawable(drawable)
        } else {
            drawable === mDeterminateProgressDrawable || super.verifyDrawable(drawable)
        }
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)
        if (mIndeterminate) {
            mIndeterminateProgressDrawable!!.setBounds(0, 0, width, height)
        } else {
            mDeterminateProgressDrawable!!.setBounds(0, 0, width, height)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (mIndeterminate) {
            startAnimation()
        }
    }

    override fun onDetachedFromWindow() {
        if (mIndeterminate) {
            stopAnimation()
        }
        super.onDetachedFromWindow()
    }

    private inner class DeterminateProgressDrawable(color: Int, borderWidth: Int, angle: Int) : Drawable() {

        private val mPaint: Paint
        private val mBorderWidth: Float
        private val mDrawableBounds = RectF()

        init {
            mPaint = Paint()
            mPaint.isAntiAlias = true
            mPaint.style = Paint.Style.STROKE
            mPaint.strokeWidth = borderWidth.toFloat()
            mPaint.color = color
            mBorderWidth = borderWidth.toFloat()
        }

        override fun draw(canvas: Canvas) {
            canvas.drawArc(mDrawableBounds, -90f, 20f, false, mPaint)
        }

        override fun setAlpha(alpha: Int) {
            mPaint.alpha = alpha
        }

        override fun setColorFilter(colorFilter: ColorFilter?) {
            mPaint.colorFilter = colorFilter
        }

        override fun getOpacity(): Int {
            return PixelFormat.TRANSPARENT
        }

        override fun onBoundsChange(bounds: Rect) {
            super.onBoundsChange(bounds)
            mDrawableBounds.left = bounds.left.toFloat() + mBorderWidth / 2f + .5f
            mDrawableBounds.right = bounds.right.toFloat() - mBorderWidth / 2f - .5f
            mDrawableBounds.top = bounds.top.toFloat() + mBorderWidth / 2f + .5f
            mDrawableBounds.bottom = bounds.bottom.toFloat() - mBorderWidth / 2f - .5f
        }
    }

    /**
     * https://gist.github.com/castorflex/4e46a9dc2c3a4245a28e
     */
    private inner class IndeterminateProgressDrawable(color: Int, private val mBorderWidth: Float) : Drawable(), Animatable {

        private val ANGLE_INTERPOLATOR = LinearInterpolator()
        private val SWEEP_INTERPOLATOR = DecelerateInterpolator()
        private val mDrawableBounds = RectF()

        private var mObjectAnimatorSweep: ObjectAnimator? = null
        private var mObjectAnimatorAngle: ObjectAnimator? = null
        private var mModeAppearing: Boolean = false
        private val mPaint: Paint
        private var mCurrentGlobalAngleOffset: Float = 0.toFloat()
        var currentGlobalAngle: Float = 0.toFloat()
            set(currentGlobalAngle) {
                field = currentGlobalAngle
                invalidateSelf()
            }
        var currentSweepAngle: Float = 0.toFloat()
            set(currentSweepAngle) {
                field = currentSweepAngle
                invalidateSelf()
            }
        private var mRunning: Boolean = false

        ///////////////////////////////////////// Animation /////////////////////////////////////////

        private val mAngleProperty = object : Property<IndeterminateProgressDrawable, Float>(Float::class.java, "angle") {
            override fun get(`object`: IndeterminateProgressDrawable): Float {
                return `object`.currentGlobalAngle
            }

            override fun set(`object`: IndeterminateProgressDrawable, value: Float?) {
                `object`.currentGlobalAngle = value
            }
        }

        private val mSweepProperty = object : Property<IndeterminateProgressDrawable, Float>(Float::class.java, "arc") {
            override fun get(`object`: IndeterminateProgressDrawable): Float {
                return `object`.currentSweepAngle
            }

            override fun set(`object`: IndeterminateProgressDrawable, value: Float?) {
                `object`.currentSweepAngle = value
            }
        }

        init {

            mPaint = Paint()
            mPaint.isAntiAlias = true
            mPaint.style = Paint.Style.STROKE
            mPaint.strokeWidth = mBorderWidth
            mPaint.color = color

            setupAnimations()
        }

        override fun draw(canvas: Canvas) {
            var startAngle = currentGlobalAngle - mCurrentGlobalAngleOffset
            var sweepAngle = currentSweepAngle
            if (!mModeAppearing) {
                startAngle = startAngle + sweepAngle
                sweepAngle = 360f - sweepAngle - MIN_SWEEP_ANGLE.toFloat()
            } else {
                sweepAngle += MIN_SWEEP_ANGLE.toFloat()
            }
            canvas.drawArc(mDrawableBounds, startAngle, sweepAngle, false, mPaint)
        }

        override fun setAlpha(alpha: Int) {
            mPaint.alpha = alpha
        }

        override fun setColorFilter(cf: ColorFilter?) {
            mPaint.colorFilter = cf
        }

        override fun getOpacity(): Int {
            return PixelFormat.TRANSPARENT
        }

        private fun toggleAppearingMode() {
            mModeAppearing = !mModeAppearing
            if (mModeAppearing) {
                mCurrentGlobalAngleOffset = (mCurrentGlobalAngleOffset + MIN_SWEEP_ANGLE * 2) % 360
            }
        }

        override fun onBoundsChange(bounds: Rect) {
            super.onBoundsChange(bounds)
            mDrawableBounds.left = bounds.left.toFloat() + mBorderWidth / 2f + .5f
            mDrawableBounds.right = bounds.right.toFloat() - mBorderWidth / 2f - .5f
            mDrawableBounds.top = bounds.top.toFloat() + mBorderWidth / 2f + .5f
            mDrawableBounds.bottom = bounds.bottom.toFloat() - mBorderWidth / 2f - .5f
        }

        private fun setupAnimations() {
            mObjectAnimatorAngle = ObjectAnimator.ofFloat(this, mAngleProperty, 360f)
            mObjectAnimatorAngle!!.interpolator = ANGLE_INTERPOLATOR
            mObjectAnimatorAngle!!.duration = ANGLE_ANIMATOR_DURATION.toLong()
            mObjectAnimatorAngle!!.repeatMode = ValueAnimator.RESTART
            mObjectAnimatorAngle!!.repeatCount = ValueAnimator.INFINITE

            mObjectAnimatorSweep = ObjectAnimator.ofFloat(this, mSweepProperty, 360f - MIN_SWEEP_ANGLE * 2)
            mObjectAnimatorSweep!!.interpolator = SWEEP_INTERPOLATOR
            mObjectAnimatorSweep!!.duration = SWEEP_ANIMATOR_DURATION.toLong()
            mObjectAnimatorSweep!!.repeatMode = ValueAnimator.RESTART
            mObjectAnimatorSweep!!.repeatCount = ValueAnimator.INFINITE
            mObjectAnimatorSweep!!.addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {

                }

                override fun onAnimationEnd(animation: Animator) {

                }

                override fun onAnimationCancel(animation: Animator) {

                }

                override fun onAnimationRepeat(animation: Animator) {
                    toggleAppearingMode()
                }
            })
        }

        override fun start() {
            if (isRunning) {
                return
            }
            mRunning = true
            mObjectAnimatorAngle!!.start()
            mObjectAnimatorSweep!!.start()
            invalidateSelf()
        }

        override fun stop() {
            if (!isRunning) {
                return
            }
            mRunning = false
            mObjectAnimatorAngle!!.cancel()
            mObjectAnimatorSweep!!.cancel()
            invalidateSelf()
        }

        override fun isRunning(): Boolean {
            return mRunning
        }

        companion object {
            private val ANGLE_ANIMATOR_DURATION = 2000
            private val SWEEP_ANIMATOR_DURATION = 600
            private val MIN_SWEEP_ANGLE = 30
        }

    }

    companion object {

        private val SMALL_SIZE = 0
        private val NORMAL_SIZE = 1
        private val LARGE_SIZE = 2
    }
}