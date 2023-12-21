package com.Lyber.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import com.Lyber.R

class CustomLineChart : RelativeLayout {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, 0)
    constructor(context: Context, attributeSet: AttributeSet?, defStyle: Int) : this(
        context,
        attributeSet,
        defStyle,
        0
    )

    constructor(
        context: Context,
        attributeSet: AttributeSet?,
        defStyle: Int,
        defStyleRes: Int
    ) : super(context, attributeSet, defStyle, defStyleRes) {

        drawableView = View(context)
        dottedLineView = View(context)
        textView = TextView(context)

        setWillNotDraw(false)

        addView(drawableView)
        addView(dottedLineView)
        addView(textView)
    }

    private var drawableView: View
    private var dottedLineView: View
    private var textView: TextView


    data class Points(val x: Float, val y: Float)

    /* smallest axis unit for drawing */
    private var yUnit: Float = 0F
    private var xUnit: Float = 0F

    /* text Properties */
    var textsize: Float = 24F
    var textColor: Int = Color.BLUE
    var textMargin: Float = 0F
    var textTypeface: Typeface = Typeface.DEFAULT

    /* line properties */
    var lineColor: Int = Color.BLUE
    var lineWidth: Float = 6F

    /* selector properties */
    var selectorLineColor: Int = Color.BLUE
    var selectorLineWidth: Float = 6F
    var selectorDrawable: Drawable? = getDrawable(context, R.drawable.radio_select)
    var selectorPointSize: Int = 24

    /* line data*/
    private var _lineData: MutableList<Float> = mutableListOf()
    var lineData
        get() = _lineData
        set(value) {
            _lineData = value
            selectedPosition = value.count() - 1
        }

    private var _points: MutableList<Points> = mutableListOf()
    var points
        get() = _points
        set(value) {
            _points = value
        }

    private var _selectedPoint: Points? = null
    var selectedPoint
        get() = _selectedPoint
        set(value) {
            _selectedPoint = value
        }

    private var _selectedPosition: Int = 0
    var selectedPosition
        get() = _selectedPosition
        set(value) {
            _selectedPosition = value
        }

    private var linePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var textPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private fun getMax(list: List<Float>): Float {
        var max = 0F
        for (i in list)
            if (i > max) max = i
        return max
    }

    private fun getMin(list: List<Float>): Float {
        var min = Float.MAX_VALUE
        for (i in list)
            if (i < min) min = i
        return min
    }

    private fun List<Float>.median(): Float {
        return sorted().let {
            if (it.size % 2 == 0)
                (it[it.size / 2] + it[(it.size - 1) / 2]) / 2
            else
                it[it.size / 2]
        }
    }

    override fun onDraw(canvas: Canvas?) {
        canvas?.let {
            if (lineData.isNotEmpty()) {
                calculatePoints()
                drawData(it)
            }
        }
        super.onDraw(canvas)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {

        val eventX: Float = event?.x ?: 0F
        when (event?.action) {

            MotionEvent.ACTION_MOVE -> {

                val position = ((eventX / xUnit).toInt() - 1)
                if (position in 0 until points.count() && position != selectedPosition) {
                    selectedPosition = position
                    selectedPoint = points[position]
                    selectedPoint?.let {
                        Log.d("pointSelected", "pointSelected: $position")
                        pointSelected(it.x, it.y, position)
                    }
                }
                return true
            }
            MotionEvent.ACTION_DOWN -> {

                val position = ((eventX / xUnit).toInt() - 1)
                if (position in 0 until points.count()) {

                    if (position != selectedPosition) {
                        selectedPosition = position
                        selectedPoint = points[position]
                        selectedPoint?.let {
                            Log.d("pointSelected", "pointSelected: $position")
                            pointSelected(it.x, it.y, position)

                        }
                    }
                }
                return true
            }
        }

        return false

    }


    private fun calculatePoints() {

        points.clear()
        val count = lineData.count()
        val max = lineData.max()
        val min = lineData.min()
        val avg = lineData.average()

        xUnit = ((width - selectorPointSize) / (count)).toFloat()

        // original
        yUnit = (height - (2 * selectorPointSize + textsize)) / max


        for (i in 0 until count) {

            val startX: Float = (xUnit + (i * xUnit)) + (selectorPointSize / 2)

            val startY: Float = (height - ((lineData[i]) * yUnit)) - (selectorPointSize / 2)

//            val startY: Float = ((((max - lineData[i])/(max-min)) * height) - (selectorPointSize / 2))

            points.add(Points(startX, startY))

        }

    }


    private fun drawData(mCanvas: Canvas) {

        selectedPoint = points.last()
        for (i in 0 until points.count()) {
            if (i < points.count() - 1) {
                drawLines(points[i].x, points[i].y, points[i + 1].x, points[i + 1].y, mCanvas)
            }
        }
        selectedPoint = points[selectedPosition]
        selectedPoint?.let {
            pointSelected(it.x, it.y, selectedPosition)
        }
    }


    private fun drawLines(xStart: Float, yStart: Float, xEnd: Float, yEnd: Float, mCanvas: Canvas) {
        mCanvas.drawLine(
            xStart,
            yStart,
            xEnd,
            yEnd,
            linePaint.apply {
                strokeCap = Paint.Cap.ROUND
                strokeWidth = lineWidth
                color = lineColor
            })
    }


    private fun pointSelected(x: Float, y: Float, position: Int) {

        drawableView.background = selectorDrawable
        dottedLineView.background = getDrawable(context, R.drawable.vertical_dotted_line)?.apply {
            setTint(selectorLineColor)
        }

        drawableView.layoutParams = LayoutParams(selectorPointSize, selectorPointSize)
        dottedLineView.layoutParams = LayoutParams(lineWidth.toInt(), MATCH_PARENT)

        textView.translationZ = 8F
        textView.text = lineData[position].toInt().toString() + "€"

        textView.textSize = textsize
        textView.typeface = textTypeface
        textView.setTextColor(textColor)

        if (position == lineData.count() - 1)
            textView.x = (x - textView.width)
        else textView.x = x - (textView.width / 2)

        textView.y = 0F

        drawableView.translationZ = 8F
        drawableView.x = x - (drawableView.width / 2)
        drawableView.y = y - (drawableView.width / 2)

        /* line position */
        dottedLineView.y = y - (drawableView.width / 2)
        dottedLineView.x = x - (dottedLineView.width / 2)

        postInvalidate()
    }


}