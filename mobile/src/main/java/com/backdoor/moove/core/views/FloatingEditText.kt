package com.backdoor.moove.core.views

import android.content.Context
import android.content.res.Resources
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.appcompat.widget.AppCompatEditText
import android.text.TextUtils
import android.util.AttributeSet

import com.backdoor.moove.R

/**
 * Created by IntelliJ IDEA.
 * User: keith.
 * Date: 14-10-30.
 * Time: 15:57.
 */
class FloatingEditText @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = android.R.attr.editTextStyle) : AppCompatEditText(context, attrs, android.R.attr.editTextStyle) {

    private var mState = StateHintNormal
    private var mStartTime: Long = 0
    private val mColor: Int
    private val mHighlightedColor: Int
    private val mErrorColor: Int
    private var mVerified = true
    private var mValidateMessage: String? = null
    private val mUnderlineHeight: Int
    private val mUnderlineHighlightedHeight: Int
    private var mTextEmpty: Boolean = false
    private val mHintScale: Float
    private val lineRect = Rect()

    private val mHintPaint: Paint

    init {
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.FloatingEditText)
        mHintScale = attributes.getFloat(R.styleable.FloatingEditText_floating_edit_text_hint_scale,
                HINT_SCALE)
        mColor = attributes.getColor(R.styleable.FloatingEditText_floating_edit_text_color,
                resources.getColor(R.color.floating_edit_text_color))
        mHighlightedColor = attributes.getColor(R.styleable.FloatingEditText_floating_edit_text_highlighted_color,
                resources.getColor(R.color.floating_edit_text_highlighted_color))
        mErrorColor = attributes.getColor(R.styleable.FloatingEditText_floating_edit_text_error_color,
                resources.getColor(R.color.floating_edit_text_error_color))
        mUnderlineHeight = attributes.getDimensionPixelSize(R.styleable.FloatingEditText_floating_edit_text_underline_height,
                resources.getDimensionPixelSize(R.dimen.floating_edit_text_underline_height))
        mUnderlineHighlightedHeight = attributes.getDimensionPixelSize(R.styleable.FloatingEditText_floating_edit_text_underline_highlighted_height,
                resources.getDimensionPixelSize(R.dimen.floating_edit_text_underline_highlighted_height))
        setHintTextColor(Color.TRANSPARENT)
        mTextEmpty = TextUtils.isEmpty(text)
        mHintPaint = Paint()
        mHintPaint.isAntiAlias = true

        val drawable = object : Drawable() {
            override fun draw(canvas: Canvas) {
                if (mVerified) {
                    if (isFocused) {
                        val rect = getThickLineRect(canvas)
                        mHintPaint.color = mHighlightedColor
                        canvas.drawRect(rect, mHintPaint)
                    } else {
                        val rect = getThinLineRect(canvas)
                        mHintPaint.color = mColor
                        canvas.drawRect(rect, mHintPaint)
                    }
                } else {
                    val rect = getThickLineRect(canvas)
                    mHintPaint.color = mErrorColor
                    canvas.drawRect(rect, mHintPaint)

                    mHintPaint.color = mErrorColor
                    mHintPaint.textSize = textSize * 0.6f
                    val x = compoundPaddingLeft.toFloat()
                    val y = (rect.bottom + (dpToPx(16) - mHintPaint.fontMetricsInt.top) / 2).toFloat()
                    canvas.drawText(mValidateMessage!!, x, y, mHintPaint)
                }
            }

            override fun setAlpha(alpha: Int) {
                mHintPaint.alpha = alpha
            }

            override fun setColorFilter(colorFilter: ColorFilter?) {
                mHintPaint.colorFilter = colorFilter
            }

            override fun getOpacity(): Int {
                return PixelFormat.TRANSPARENT
            }
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            setBackgroundDrawable(drawable)
        } else {
            background = drawable
        }
        val paddingTop = dpToPx(12)
        val paddingBottom = dpToPx(20)
        setPadding(0, paddingTop, 0, paddingBottom)
    }

    private fun getThinLineRect(canvas: Canvas): Rect {
        lineRect.left = paddingLeft
        lineRect.top = canvas.height - mUnderlineHeight - dpToPx(16)
        lineRect.right = width
        lineRect.bottom = canvas.height - dpToPx(16)
        return lineRect
    }

    private fun getThickLineRect(canvas: Canvas): Rect {
        lineRect.left = paddingLeft
        lineRect.top = canvas.height - mUnderlineHighlightedHeight - dpToPx(16)
        lineRect.right = width
        lineRect.bottom = canvas.height - dpToPx(16)
        return lineRect
    }

    override fun onTextChanged(text: CharSequence, start: Int, lengthBefore: Int, lengthAfter: Int) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter)
        this.mVerified = true
        this.mValidateMessage = null
        val isEmpty = TextUtils.isEmpty(getText())
        if (mTextEmpty != isEmpty) {
            this.mTextEmpty = isEmpty
            if (isEmpty && isShown) {
                mStartTime = System.currentTimeMillis()
                mState = StateHintZoomIn
            } else {
                mStartTime = System.currentTimeMillis()
                mState = StateHintZoomOut
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (!TextUtils.isEmpty(hint)) {
            mHintPaint.set(paint)
            val maxTextSize = textSize
            val minTextSize = textSize * mHintScale
            val maxHintY = baseline.toFloat()
            val minHintY = (baseline + paint.fontMetricsInt.top + scrollY - dpToPx(4)).toFloat()
            val textSize: Float
            val hintY: Float
            val hintX = (compoundPaddingLeft + scrollX).toFloat()
            val elapsed = System.currentTimeMillis() - mStartTime
            when (mState) {
                StateHintNormal -> {
                    textSize = maxTextSize
                    hintY = maxHintY
                    mHintPaint.color = mColor
                    mHintPaint.textSize = textSize
                    canvas.drawText(hint.toString(), hintX, hintY, mHintPaint)
                }
                StateHintZoomIn -> {
                    if (elapsed < ANIMATION_DURATION) {
                        textSize = (maxTextSize - minTextSize) * elapsed / ANIMATION_DURATION + minTextSize
                        hintY = (maxHintY - minHintY) * elapsed / ANIMATION_DURATION + minHintY
                        mHintPaint.color = mHighlightedColor
                        mHintPaint.textSize = textSize
                        canvas.drawText(hint.toString(), hintX, hintY, mHintPaint)
                        postInvalidate()
                    } else {
                        textSize = maxTextSize
                        hintY = maxHintY
                        mHintPaint.color = mColor
                        mHintPaint.textSize = textSize
                        canvas.drawText(hint.toString(), hintX, hintY, mHintPaint)
                    }
                }
                StateHintZoomOut -> {
                    if (elapsed < ANIMATION_DURATION) {
                        textSize = maxTextSize - (maxTextSize - minTextSize) * elapsed / ANIMATION_DURATION
                        hintY = maxHintY - (maxHintY - minHintY) * elapsed / ANIMATION_DURATION
                        mHintPaint.color = mHighlightedColor
                        mHintPaint.textSize = textSize
                        canvas.drawText(hint.toString(), hintX, hintY, mHintPaint)
                        postInvalidate()
                    } else {
                        textSize = minTextSize
                        hintY = minHintY
                        if (isFocused) {
                            mHintPaint.color = mHighlightedColor
                        } else {
                            mHintPaint.color = mColor
                        }
                        mHintPaint.textSize = textSize
                        canvas.drawText(hint.toString(), hintX, hintY, mHintPaint)
                    }
                }
            }
        }
    }

    companion object {

        private val ANIMATION_DURATION: Long = 120
        private val StateHintNormal = 0
        private val StateHintZoomIn = 1
        private val StateHintZoomOut = 2
        private val HINT_SCALE = 0.5f

        fun dpToPx(dp: Int): Int {
            return (dp * Resources.getSystem().displayMetrics.density).toInt()
        }
    }
}
