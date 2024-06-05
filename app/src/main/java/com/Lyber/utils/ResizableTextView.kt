package com.Lyber.utils

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.text.SpannableString
import android.util.AttributeSet
import android.widget.TextView

@SuppressLint("AppCompatCustomView")
class ResizableTextView : TextView {

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, 0)
    constructor(context: Context, attributeSet: AttributeSet?, defStyle: Int) : super(
        context,
        attributeSet,
        defStyle
    ) {
        init()
    }

    private fun init() {

    }

    private val widthSpec get() = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY)
    private val heightSpec get() = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY)

    private lateinit var _collapseText: SpannableString
    var collapseText
        get() = _collapseText
        set(value) {
            _collapseText = value
            text = value
        }

    private lateinit var _expendedText: SpannableString
    var expendedText
        get() = _expendedText
        set(value) {
            _expendedText = value
        }

    fun collapse() {

        measure(widthSpec, heightSpec)
        val initialWidth = layout.width
        val initialHeight = layout.height

        setMeasuredDimension(initialWidth,initialHeight)

        text = collapseText

        measure(widthSpec, heightSpec)
        val endWidth = layout.width
        val endHeight = layout.height

        setMeasuredDimension(endWidth,endHeight)

        animateLayout(initialHeight,endHeight)
    }

    @SuppressLint("Recycle")
    fun animateLayout(from: Int, to: Int) {
        ValueAnimator.ofInt(from, to).apply {
            duration = 300
            addUpdateListener {
                val lp = layoutParams
                lp.height = animatedValue as Int
                layoutParams = lp
            }

            start()
        }
    }


    fun expend() {

        measure(widthSpec, heightSpec)
        val initialWidth = layout.width
        val initialHeight = layout.height

        setMeasuredDimension(initialWidth,initialHeight)

        text = expendedText

        measure(widthSpec, heightSpec)
        val endWidth = layout.width
        val endHeight = layout.height

        setMeasuredDimension(endWidth,endHeight)

        animateLayout(initialHeight, endHeight)

    }

}