package com.backdoor.moove.core.views

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import androidx.appcompat.widget.AppCompatImageView
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent

import com.backdoor.moove.R


class RoundImageView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = R.styleable.CircularImageViewStyle_circularImageViewDefault) : AppCompatImageView(context, attrs, defStyleAttr) {

    // Border & Selector configuration variables
    private var hasBorder: Boolean = false
    private var hasSelector: Boolean = false
    private var isSelected: Boolean = false
    private var borderWidth: Int = 0
    private var canvasSize: Int = 0
    private var selectorStrokeWidth: Int = 0

    // Shadow properties
    private var shadowEnabled: Boolean = false
    private var shadowRadius: Float = 0.toFloat()
    private var shadowDx: Float = 0.toFloat()
    private var shadowDy: Float = 0.toFloat()
    private var shadowColor: Int = 0

    // Objects used for the actual drawing
    private var shader: BitmapShader? = null
    private var image: Bitmap? = null
    private var paint: Paint? = null
    private var paintBorder: Paint? = null
    private var paintSelectorBorder: Paint? = null
    private var selectorFilter: ColorFilter? = null

    init {
        init(context, attrs, defStyleAttr)
    }

    /**
     * Initializes paint objects and sets desired attributes.
     *
     * @param context  Context
     * @param attrs    Attributes
     * @param defStyle Default Style
     */
    private fun init(context: Context, attrs: AttributeSet?, defStyle: Int) {
        // Initialize paint objects
        paint = Paint()
        paint!!.isAntiAlias = true
        paintBorder = Paint()
        paintBorder!!.isAntiAlias = true
        paintBorder!!.style = Paint.Style.STROKE
        paintSelectorBorder = Paint()
        paintSelectorBorder!!.isAntiAlias = true

        // Enable software rendering on HoneyComb and up. (needed for shadow)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            setLayerType(View.LAYER_TYPE_SOFTWARE, null)

        // Load the styled attributes and set their properties
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.RoundImageView, defStyle, 0)

        // Check for extra features being enabled
        hasBorder = attributes.getBoolean(R.styleable.RoundImageView_civ_border, false)
        hasSelector = attributes.getBoolean(R.styleable.RoundImageView_civ_selector, false)
        shadowEnabled = attributes.getBoolean(R.styleable.RoundImageView_civ_shadow, SHADOW_ENABLED)

        // Set border properties, if enabled
        if (hasBorder) {
            val defaultBorderSize = (2 * context.resources.displayMetrics.density + 0.5f).toInt()
            setBorderWidth(attributes.getDimensionPixelOffset(R.styleable.RoundImageView_civ_borderWidth, defaultBorderSize))
            setBorderColor(attributes.getColor(R.styleable.RoundImageView_civ_borderColor, Color.WHITE))
        }

        // Set selector properties, if enabled
        if (hasSelector) {
            val defaultSelectorSize = (2 * context.resources.displayMetrics.density + 0.5f).toInt()
            setSelectorColor(attributes.getColor(R.styleable.RoundImageView_civ_selectorColor, Color.TRANSPARENT))
            setSelectorStrokeWidth(attributes.getDimensionPixelOffset(R.styleable.RoundImageView_civ_selectorStrokeWidth, defaultSelectorSize))
            setSelectorStrokeColor(attributes.getColor(R.styleable.RoundImageView_civ_selectorStrokeColor, Color.BLUE))
        }

        // Set shadow properties, if enabled
        if (shadowEnabled) {
            shadowRadius = attributes.getFloat(R.styleable.RoundImageView_civ_shadowRadius, SHADOW_RADIUS)
            shadowDx = attributes.getFloat(R.styleable.RoundImageView_civ_shadowDx, SHADOW_DX)
            shadowDy = attributes.getFloat(R.styleable.RoundImageView_civ_shadowDy, SHADOW_DY)
            shadowColor = attributes.getColor(R.styleable.RoundImageView_civ_shadowColor, SHADOW_COLOR)
            setShadowEnabled(true)
        }

        // We no longer need our attributes TypedArray, give it back to cache
        attributes.recycle()
    }

    /**
     * Sets the CircularImageView's border width in pixels.
     *
     * @param borderWidth Width in pixels for the border.
     */
    fun setBorderWidth(borderWidth: Int) {
        this.borderWidth = borderWidth
        if (paintBorder != null)
            paintBorder!!.strokeWidth = borderWidth.toFloat()
        requestLayout()
        invalidate()
    }

    /**
     * Sets the CircularImageView's basic border color.
     *
     * @param borderColor The new color (including alpha) to set the border.
     */
    fun setBorderColor(borderColor: Int) {
        if (paintBorder != null)
            paintBorder!!.color = borderColor
        this.invalidate()
    }

    /**
     * Sets the color of the selector to be draw over the
     * CircularImageView. Be sure to provide some opacity.
     *
     * @param selectorColor The color (including alpha) to set for the selector overlay.
     */
    fun setSelectorColor(selectorColor: Int) {
        this.selectorFilter = PorterDuffColorFilter(selectorColor, PorterDuff.Mode.SRC_ATOP)
        this.invalidate()
    }

    /**
     * Sets the stroke width to be drawn around the CircularImageView
     * during click events when the selector is enabled.
     *
     * @param selectorStrokeWidth Width in pixels for the selector stroke.
     */
    fun setSelectorStrokeWidth(selectorStrokeWidth: Int) {
        this.selectorStrokeWidth = selectorStrokeWidth
        this.requestLayout()
        this.invalidate()
    }

    /**
     * Sets the stroke color to be drawn around the CircularImageView
     * during click events when the selector is enabled.
     *
     * @param selectorStrokeColor The color (including alpha) to set for the selector stroke.
     */
    fun setSelectorStrokeColor(selectorStrokeColor: Int) {
        if (paintSelectorBorder != null)
            paintSelectorBorder!!.color = selectorStrokeColor
        this.invalidate()
    }

    /**
     * Enables a dark shadow for this CircularImageView.
     *
     * @param enabled Set to true to draw a shadow or false to disable it.
     */
    fun setShadowEnabled(enabled: Boolean) {
        shadowEnabled = enabled
        updateShadow()
    }

    /**
     * Enables a dark shadow for this CircularImageView.
     * If the radius is set to 0, the shadow is removed.
     *
     * @param radius Radius for the shadow to extend to.
     * @param dx     Horizontal shadow offset.
     * @param dy     Vertical shadow offset.
     * @param color  The color of the shadow to apply.
     */
    fun setShadow(radius: Float, dx: Float, dy: Float, color: Int) {
        shadowRadius = radius
        shadowDx = dx
        shadowDy = dy
        shadowColor = color
        updateShadow()
    }

    public override fun onDraw(canvas: Canvas) {
        // Don't draw anything without an image
        if (image == null)
            return

        // Nothing to draw (Empty bounds)
        if (image!!.height == 0 || image!!.width == 0)
            return

        // Update shader if canvas size has changed
        val oldCanvasSize = canvasSize
        canvasSize = if (width < height) width else height
        if (oldCanvasSize != canvasSize)
            updateBitmapShader()

        // Apply shader to paint
        paint!!.shader = shader

        // Keep track of selectorStroke/border width
        var outerWidth = 0

        // Get the exact X/Y axis of the view
        var center = canvasSize / 2


        if (hasSelector && isSelected) { // Draw the selector stroke & apply the selector filter, if applicable
            outerWidth = selectorStrokeWidth
            center = (canvasSize - outerWidth * 2) / 2

            paint!!.colorFilter = selectorFilter
            canvas.drawCircle((center + outerWidth).toFloat(), (center + outerWidth).toFloat(), (canvasSize - outerWidth * 2) / 2 + outerWidth - 4.0f, paintSelectorBorder!!)
        } else if (hasBorder) { // If no selector was drawn, draw a border and clear the filter instead... if enabled
            outerWidth = borderWidth
            center = (canvasSize - outerWidth * 2) / 2

            paint!!.colorFilter = null
            val rekt = RectF((0 + outerWidth / 2).toFloat(), (0 + outerWidth / 2).toFloat(), (canvasSize - outerWidth / 2).toFloat(), (canvasSize - outerWidth / 2).toFloat())
            canvas.drawArc(rekt, 360f, 360f, false, paintBorder!!)
            //canvas.drawCircle(center + outerWidth, center + outerWidth, ((canvasSize - (outerWidth * 2)) / 2) + outerWidth - 4.0f, paintBorder);
        } else
        // Clear the color filter if no selector nor border were drawn
            paint!!.colorFilter = null

        // Draw the circular image itself
        canvas.drawCircle((center + outerWidth).toFloat(), (center + outerWidth).toFloat(), ((canvasSize - outerWidth * 2) / 2).toFloat(), paint!!)
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        // Check for clickable state and do nothing if disabled
        if (!this.isClickable) {
            this.isSelected = false
            return super.onTouchEvent(event)
        }

        // Set selected state based on Motion Event
        when (event.action) {
            MotionEvent.ACTION_DOWN -> this.isSelected = true
            MotionEvent.ACTION_UP, MotionEvent.ACTION_SCROLL, MotionEvent.ACTION_OUTSIDE, MotionEvent.ACTION_CANCEL -> this.isSelected = false
        }

        // Redraw image and return super type
        this.invalidate()
        return super.dispatchTouchEvent(event)
    }

    override fun setImageURI(uri: Uri?) {
        super.setImageURI(uri)

        // Extract a Bitmap out of the drawable & set it as the main shader
        image = drawableToBitmap(drawable)
        if (canvasSize > 0)
            updateBitmapShader()
    }

    override fun setImageResource(resId: Int) {
        super.setImageResource(resId)

        // Extract a Bitmap out of the drawable & set it as the main shader
        image = drawableToBitmap(drawable)
        if (canvasSize > 0)
            updateBitmapShader()
    }

    override fun setImageDrawable(drawable: Drawable?) {
        super.setImageDrawable(drawable)

        // Extract a Bitmap out of the drawable & set it as the main shader
        image = drawableToBitmap(getDrawable())
        if (canvasSize > 0)
            updateBitmapShader()
    }

    override fun setImageBitmap(bm: Bitmap) {
        super.setImageBitmap(bm)

        // Extract a Bitmap out of the drawable & set it as the main shader
        image = bm
        if (canvasSize > 0)
            updateBitmapShader()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = measureWidth(widthMeasureSpec)
        val height = measureHeight(heightMeasureSpec)
        setMeasuredDimension(width, height)
    }

    private fun measureWidth(measureSpec: Int): Int {
        val result: Int
        val specMode = View.MeasureSpec.getMode(measureSpec)
        val specSize = View.MeasureSpec.getSize(measureSpec)

        if (specMode == View.MeasureSpec.EXACTLY) {
            // The parent has determined an exact size for the child.
            result = specSize
        } else if (specMode == View.MeasureSpec.AT_MOST) {
            // The child can be as large as it wants up to the specified size.
            result = specSize
        } else {
            // The parent has not imposed any constraint on the child.
            result = canvasSize
        }

        return result
    }

    private fun measureHeight(measureSpecHeight: Int): Int {
        val result: Int
        val specMode = View.MeasureSpec.getMode(measureSpecHeight)
        val specSize = View.MeasureSpec.getSize(measureSpecHeight)

        if (specMode == View.MeasureSpec.EXACTLY) {
            // We were told how big to be
            result = specSize
        } else if (specMode == View.MeasureSpec.AT_MOST) {
            // The child can be as large as it wants up to the specified size.
            result = specSize
        } else {
            // Measure the text (beware: ascent is a negative number)
            result = canvasSize
        }

        return result + 2
    }

    // TODO: Update shadow layers based on border/selector state and visibility.
    private fun updateShadow() {
        val radius = if (shadowEnabled) shadowRadius else 0
        //paint.setShadowLayer(radius, shadowDx, shadowDy, shadowColor);
        paintBorder!!.setShadowLayer(radius, shadowDx, shadowDy, shadowColor)
        paintSelectorBorder!!.setShadowLayer(radius, shadowDx, shadowDy, shadowColor)
    }

    /**
     * Convert a drawable object into a Bitmap.
     *
     * @param drawable Drawable to extract a Bitmap from.
     * @return A Bitmap created from the drawable parameter.
     */
    fun drawableToBitmap(drawable: Drawable?): Bitmap? {
        if (drawable == null)
        // Don't do anything without a proper drawable
            return null
        else if (drawable is BitmapDrawable) {  // Use the getBitmap() method instead if BitmapDrawable
            Log.i(TAG, "Bitmap drawable!")
            return drawable.bitmap
        }

        val intrinsicWidth = drawable.intrinsicWidth
        val intrinsicHeight = drawable.intrinsicHeight

        if (!(intrinsicWidth > 0 && intrinsicHeight > 0))
            return null

        try {
            // Create Bitmap object out of the drawable
            val bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            return bitmap
        } catch (e: OutOfMemoryError) {
            // Simply return null of failed bitmap creations
            Log.e(TAG, "Encountered OutOfMemoryError while generating bitmap!")
            return null
        }

    }

    // TODO TEST REMOVE
    fun setIconModeEnabled(e: Boolean) {}

    /**
     * Re-initializes the shader texture used to fill in
     * the Circle upon drawing.
     */
    fun updateBitmapShader() {
        if (image == null)
            return

        shader = BitmapShader(image!!, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)

        if (canvasSize != image!!.width || canvasSize != image!!.height) {
            val matrix = Matrix()
            val scale = canvasSize.toFloat() / image!!.width.toFloat()
            matrix.setScale(scale, scale)
            shader!!.setLocalMatrix(matrix)
        }
    }

    /**
     * @return Whether or not this view is currently
     * in its selected state.
     */
    override fun isSelected(): Boolean {
        return this.isSelected
    }

    companion object {
        private val TAG = RoundImageView::class.java.simpleName

        // Default property values
        private val SHADOW_ENABLED = false
        private val SHADOW_RADIUS = 4f
        private val SHADOW_DX = 0f
        private val SHADOW_DY = 2f
        private val SHADOW_COLOR = Color.BLACK
    }
}
