package com.Lyber.utils

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView

class CustomImageView : AppCompatImageView {
    constructor(context: Context?) : super(context!!)
    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!, attrs
    )

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context!!, attrs, defStyleAttr
    )

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val size = Math.min(measuredWidth, measuredHeight)
        setMeasuredDimension(size, size)
    }

    override fun onDraw(canvas: Canvas) {
        val drawable = drawable
        if (drawable != null) {
            val width = width
            val height = height
            val size = Math.min(width, height)
            val left = (width - size) / 2
            val top = (height - size) / 2
            drawable.setBounds(left, top, left + size, top + size)
            drawable.draw(canvas)
        }
    }
}
