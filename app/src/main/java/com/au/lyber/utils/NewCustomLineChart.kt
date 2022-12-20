package com.au.lyber.utils

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import com.au.lyber.R
import com.au.lyber.databinding.ItemHighlighterGraphBinding
import com.au.lyber.databinding.YAxisTextBinding
import com.au.lyber.utils.CommonMethods.Companion.commaFormatted
import com.au.lyber.utils.CommonMethods.Companion.lineData
import com.au.lyber.utils.CommonMethods.Companion.toGraphTime
import com.bumptech.glide.Glide

class NewCustomLineChart : RelativeLayout {


    constructor(context: Context) : this(context, null)
    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, 0)
    constructor(context: Context, attributeSet: AttributeSet?, defStyle: Int) : this(
        context, attributeSet, defStyle, 0
    )

    constructor(
        context: Context, attributeSet: AttributeSet?, defStyle: Int, defStyleRes: Int
    ) : super(context, attributeSet, defStyle, defStyleRes) {

        setWillNotDraw(true)

        drawableView = ImageView(context)
        dottedLineView = View(context)
        textView = TextView(context)
        animationDot = ImageView(context)

        addView(animationDot)
        addView(drawableView)
        addView(dottedLineView)
        addView(textView)

        addView(binding.root)
        addView(maxText.root)
        addView(minText.root)
        addView(midText.root)

        setBackgroundColor(Color.TRANSPARENT)

    }

    private var animator: ValueAnimator? = null

    private val binding: ItemHighlighterGraphBinding by lazy {
        ItemHighlighterGraphBinding.inflate(LayoutInflater.from(context))
    }

    private val maxText: YAxisTextBinding by lazy {
        YAxisTextBinding.inflate(LayoutInflater.from(context))
    }

    private val minText: YAxisTextBinding by lazy {
        YAxisTextBinding.inflate(LayoutInflater.from(context))
    }

    private val midText: YAxisTextBinding by lazy {
        YAxisTextBinding.inflate(LayoutInflater.from(context))
    }


    private var _lineData = mutableListOf<Float>()
    var lineData
        get() = _lineData
        set(value) {
            _lineData = value
            postInvalidate()
        }

    private var _timeSeries = List<List<Double>>(0) { _ ->
        emptyList()
    }
    var timeSeries
        get() = _timeSeries
        set(value) {
            _timeSeries = value
            selectedPosition = value.count() - 1
            lineData = value.lineData
            postInvalidate()
        }


    private var pointMax: Point? = null
    private var pointMin: Point? = null

    private var animationDot: ImageView
    private var drawableView: ImageView
    private var textView: TextView
    private var dottedLineView: View


    var xUnit: Int = 0
    var textColor: Int = Color.BLUE
    var textTypeface: Typeface = Typeface.DEFAULT
    var textSize: Float = 16f
    var selectorLineColor: Int = Color.BLUE

    var selectorDrawable: Drawable? = getDrawable(context, R.drawable.radio_select)
    var selectorPointSize: Int = 40

    var horizontalPadding: Int = 32
    var heightFraction: Float = 1.0f
    var bottomPadding: Int = 24

    var lineWidth = 4f
    var lineColor = Color.BLUE

    var mCanvas: Canvas? = null
    var selectedPosition: Int = 0
    var selectedPoint: Point? = null

    data class Point(val x: Float, val y: Float)

    private val points: MutableList<Point> = mutableListOf()

    fun clearYAxis() {
        /*pointMin = null
        pointMax = null
        maxText.tvY.text = ""
        minText.tvY.text = ""
        midText.tvY.text = ""*/
    }


    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return onTouchEvent(ev)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {

        val eventX: Float = event?.x ?: 0F
        when (event?.action) {

            MotionEvent.ACTION_MOVE -> {

                val position = ((eventX / xUnit).toInt())

                if (position in 0 until points.count() && position != selectedPosition) {
                    selectedPosition = position
                    selectedPoint = points[position]
                    selectedPoint?.let {
                        Log.d("pointSelected", "$position")
                        pointSelected(it.x, it.y, position)
                    }
                }
                return true

            }
            MotionEvent.ACTION_DOWN -> {

                val position = ((eventX / xUnit).toInt())

                if (position in 0 until points.count()) {

                    if (position != selectedPosition) {
                        selectedPosition = position
                        selectedPoint = points[position]
                        selectedPoint?.let {
                            Log.d("pointSelected", "$position")
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
        val max = lineData.max()
        val min = lineData.min()

        xUnit = (width - (horizontalPadding)) / (lineData.count() - 1)

        if ((max - min) > 0)

            for (i in 0 until lineData.count()) {

                val startX: Float =
                    ((width - (horizontalPadding * 2)) * ((i / (lineData.count() - 1).toFloat()))) + horizontalPadding / 2
                val startY =
                    (height - ((height * heightFraction) * ((lineData[i] - min) / (max - min)))) - bottomPadding

                if (lineData[i] == max) pointMax = Point(width.toFloat(), startY)
                else if (lineData[i] == min) pointMin = Point(width.toFloat(), startY)

                points.add(Point(startX, startY))
            }
        else {

            for (i in 0 until lineData.count()) {
                val startX: Float =
                    ((width - (horizontalPadding * 2)) * ((i / (lineData.count() - 1).toFloat()))) + horizontalPadding
                val startY = (height - ((height * heightFraction)))

                if (lineData[i] == max) pointMax = Point(width.toFloat(), startY)
                else if (lineData[i] == min) pointMin = Point(width.toFloat(), startY)

                points.add(Point(startX, startY))
            }


        }


        mCanvas?.let {
            drawData(it)
        }

    }

    override fun onDraw(canvas: Canvas?) {
        canvas?.let {
            mCanvas = it
            if (lineData.isNotEmpty()) calculatePoints()
        }
        super.onDraw(canvas)

    }


    @SuppressLint("SetTextI18n")
    private fun drawData(mCanvas: Canvas) {

        selectedPoint = points.last()

        for (i in 0 until points.count()) {
            if (i < points.count() - 1) {
                drawLines(points[i].x, points[i].y, points[i + 1].x, points[i + 1].y, mCanvas)
            }
        }

        if (selectedPosition in 0 until points.count()) {
            selectedPoint = points[selectedPosition]
            selectedPoint?.let {
                pointSelected(it.x, it.y, selectedPosition)
            }
        }

        pointMax?.let { max ->
            pointMin?.let { min ->

                val valueMax: Float = lineData.max()
                val valueMin: Float = lineData.min()

                maxText.tvY.text = valueMax.commaFormatted
                minText.tvY.text = valueMin.commaFormatted

                midText.tvY.text = ((valueMax + valueMin) / 2).commaFormatted

                maxText.root.y = max.y - maxText.root.height
                maxText.root.x = max.x - maxText.root.width

                minText.root.y = min.y - minText.root.height
                minText.root.x = min.x - minText.root.width

                midText.root.y = (max.y + min.y) / 2 - midText.root.height
                midText.root.x = min.x - midText.root.width

            }
        }

//        drawLines(width.toFloat(), height.toFloat(), width.toFloat(), 0F, mCanvas)

        /*pointMax?.let {
            mCanvas.drawLine(
                it.x - endPadding,
                it.y,
                it.x,
                it.y,
                Paint().apply {
                    strokeCap = Paint.Cap.ROUND
                    strokeWidth = lineWidth
                    color = lineColor
                })
        }

        pointMin?.let {
            mCanvas.drawLine(
                it.x - endPadding,
                it.y,
                it.x,
                it.y,
                Paint().apply {
                    strokeCap = Paint.Cap.ROUND
                    strokeWidth = lineWidth
                    color = lineColor
                })
        }

        pointMid?.let {
            mCanvas.drawLine(
                it.x - endPadding,
                it.y,
                it.x,
                it.y,
                Paint().apply {
                    strokeCap = Paint.Cap.ROUND
                    strokeWidth = lineWidth
                    color = lineColor
                })
        }*/

    }

    private fun drawLines(xStart: Float, yStart: Float, xEnd: Float, yEnd: Float, mCanvas: Canvas) {
        mCanvas.drawLine(xStart, yStart, xEnd, yEnd, Paint().apply {
            strokeCap = Paint.Cap.ROUND
            strokeWidth = lineWidth
            color = lineColor
        })
    }

    @SuppressLint("SetTextI18n")
    private fun pointSelected(x: Float, y: Float, position: Int) {

        dottedLineView.visibility = View.GONE

        binding.tvPrice.text = lineData[position].commaFormatted + "€"

        if (timeSeries.isNotEmpty())
            binding.tvDate.text = timeSeries[position][0].toLong().toGraphTime()

        binding.tvPrice.setTextColor(textColor)
        binding.tvDate.setTextColor(textColor)

        drawableView.layoutParams = LayoutParams(selectorPointSize, selectorPointSize)
        Glide.with(drawableView).load(R.raw.animation_1).into(drawableView)

//        drawableView.background = selectorDrawable

//        animationDot.layoutParams = LayoutParams(selectorPointSize, selectorPointSize)
//        animationDot.background = getDrawable(context, R.drawable.point_drawable)


        if (x < (binding.root.width / 2)) binding.root.x = 0F
        else if (x > ((width - horizontalPadding) - (binding.root.width) / 2)) binding.root.x =
            (width - binding.root.width).toFloat() - horizontalPadding - (binding.root.paddingStart / 2)
        else binding.root.x = x - (binding.root.width / 2)

        binding.root.y = 0F

        drawableView.translationZ = 2F
        drawableView.x = x - (drawableView.width / 2)
        drawableView.y = y - (drawableView.width / 2)


        /*if (animator == null)
            animator =
                ValueAnimator.ofInt(selectorPointSize, ((selectorPointSize * 2.5).toInt())).apply {
                    interpolator = AccelerateInterpolator(3F)
                    duration = 450
                    repeatCount = ValueAnimator.INFINITE

                    val diff = (selectorPointSize * 2.5) - selectorPointSize
                    addUpdateListener {

                        val params = animationDot.layoutParams
                        params.width = animatedValue as Int
                        params.height = animatedValue as Int
                        animationDot.layoutParams = params

                        animationDot.x = x - params.width
                        animationDot.y = x - params.height

                        animationDot.alpha =
                            (1 - ((((animatedValue as Int) - selectorPointSize) / diff)).toFloat())
                    }
                }

//        if (position == lineData.count() - 1) {
//            animationDot.visible()
            animationDot.x = x - (animationDot.width / 2)
            animationDot.y = y - (animationDot.width / 2)
            if (animator?.isRunning == false)
                animator?.start()*/


    }

}