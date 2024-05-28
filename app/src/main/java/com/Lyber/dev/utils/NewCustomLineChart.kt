package com.Lyber.dev.utils

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
import android.view.animation.AccelerateInterpolator
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.core.content.res.ResourcesCompat
import com.Lyber.dev.R
import com.Lyber.dev.databinding.ItemHighlighterGraphBinding
import com.Lyber.dev.databinding.YAxisTextBinding
import com.Lyber.dev.utils.CommonMethods.Companion.currencyFormatted
import com.Lyber.dev.utils.CommonMethods.Companion.lineData
import com.Lyber.dev.utils.CommonMethods.Companion.toGraphTime
import com.bumptech.glide.Glide
import font.FontSize
import java.math.BigDecimal
import java.util.Date
import java.util.Locale

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

        binding.tvPrice.textSize = FontSize.MEDIUM
        binding.tvPrice.typeface = ResourcesCompat.getFont(context, R.font.atyptext_medium)
        binding.tvDate.textSize = FontSize.SMALL
        binding.tvPrice.typeface = ResourcesCompat.getFont(context, R.font.atyptext_medium)

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
            selectedPosition = value.count() - 1
            postInvalidate()
        }

    private var _timeSeries = MutableList<List<Double>>(0) { _ ->
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
    private var lastPoint: Point? = null


    private var animationDot: ImageView
    private var drawableView: ImageView
    private var textView: TextView
    private var dottedLineView: View


    var xUnit: Int = 0
    var textColor: Int = context.getColor(R.color.purple_500)
    var textTypeface: Typeface =
        ResourcesCompat.getFont(context, R.font.mabry_pro) ?: Typeface.DEFAULT
    var textSize: Float = 25f
    var selectorLineColor: Int = context.getColor(R.color.purple_500)

    var selectorDrawable: Drawable? = getDrawable(context, R.drawable.circle_drawable_purple_500)
    var selectorPointSize: Int = 25
//    var selectorPointSize: Int = 30

    var horizontalPadding: Int = 60
    var heightFraction: Float = 0.8f
    var bottomPadding: Int = 24

    var lineThickness = 7f
//    var lineThickness = 9f
    var lineColor = context.getColor(R.color.purple_500)

    var mCanvas: Canvas? = null
    var selectedPosition: Int = 0
    var selectedPoint: Point? = null

    data class Point(var x: Float, var y: Float)

    private val points: MutableList<Point> = mutableListOf()

    fun clearYAxis() {
        /*pointMin = null
        pointMax = null
        maxText.tvY.text = ""
        minText.tvY.text = ""
        midText.tvY.text = ""*/
    }


    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        parent.requestDisallowInterceptTouchEvent(true)
        return true
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
        val mid = (max!! + min!!) / 2
        if (lineData.count() > 1)
            xUnit = (width - (horizontalPadding)) / (lineData.count() - 1)

        if ((max - min) > 0)

            for (i in 0 until lineData.count()) {

                val startX: Float =
                    ((width - (horizontalPadding * 2)) * ((i / (lineData.count() - 1).toFloat()))) + horizontalPadding / 2
                val startY =
                    (height - ((height * heightFraction) * ((lineData[i] - min) / (max - min)))) - bottomPadding

                if (lineData[i] == max) pointMax = Point(width.toFloat(), startY)
                else if (lineData[i] == min) pointMin = Point(width.toFloat(), startY)

                if (i == lineData.count() - 1) lastPoint = Point(startX, startY)

                points.add(Point(startX, startY))
            }
        else {

            for (i in 0 until lineData.count()) {
                val startX: Float =
                    ((width - (horizontalPadding * 2)) * ((i / (lineData.count() - 1).toFloat()))) + horizontalPadding
                val startY =(height - ((height * heightFraction)))
                if (lineData[i] == max) pointMax = Point(width.toFloat(), startY)
                else if (lineData[i] == min) pointMin = Point(width.toFloat(), startY)

                if (i == lineData.count() - 1) lastPoint = Point(startX, startY)

                points.add(Point(startX, startY))
            }


        }


        mCanvas?.let {
            drawData(it)
        }

    }

    override fun onDraw(canvas: Canvas) {
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
        var xF = 0
        pointMax?.let { max ->
            pointMin?.let { min ->

                val valueMax: Float = lineData.max()
                val valueMin: Float = lineData.min()

//                maxText.tvY.text = valueMax.commaFormatted
                maxText.tvY.text = commaFormat(valueMax)
                minText.tvY.text = commaFormat(valueMin)

                midText.tvY.text = commaFormat((valueMax + valueMin) / 2)
                val maxTextWidth = maxText.root.width
                val minTextWidth = minText.root.width
                val midTextWidth = midText.root.width

                val maxOffset = maxTextWidth / 2
                val minOffset = minTextWidth / 2
                val midOffset = midTextWidth / 2

//                val xOffset = maxOf(maxOffset, minOffset, midOffset) + 5
                val xOffset = 0
                xF = xOffset
                maxText.root.y = max.y - maxText.root.height
                maxText.root.x = max.x - maxText.root.width - xOffset

                minText.root.y = min.y - minText.root.height
                minText.root.x = min.x - minText.root.width - xOffset

                midText.root.y = (max.y + min.y) / 2 - midText.root.height
                midText.root.x = min.x - midText.root.width - xOffset

            }
        }
        val paint = Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 4f
        }

        lastPoint?.let {
            mCanvas.drawLine(
                it.x - xF, 0F,
                it.x - xF, height.toFloat(), paint
            )
        }
    }

    private fun drawLines(xStart: Float, yStart: Float, xEnd: Float, yEnd: Float, mCanvas: Canvas) {
        mCanvas.drawLine(xStart, yStart, xEnd, yEnd, Paint().apply {
            strokeCap = Paint.Cap.ROUND
            strokeWidth = lineThickness
            color = lineColor
        })
    }

    @SuppressLint("SetTextI18n")
    private fun pointSelected(x: Float, y: Float, position: Int) {
        try {
            dottedLineView.visibility = View.GONE

            binding.tvPrice.text = "${lineData[position].toString().currencyFormatted}"
//            commaFormat(lineData[position])
//        binding.tvPrice.text =(lineData[position]).commaFormatted + "€"

            binding.tvDate.text = System.currentTimeMillis().toGraphTime()
            if (timeSeries.isNotEmpty())
                binding.tvDate.text = timeSeries[position][0].toLong().toGraphTime()

            binding.tvPrice.setTextColor(textColor)
            binding.tvDate.setTextColor(textColor)

            drawableView.layoutParams = LayoutParams(selectorPointSize, selectorPointSize)
            Glide.with(drawableView).load(selectorDrawable).into(drawableView)

            animationDot.layoutParams = LayoutParams(selectorPointSize, selectorPointSize)
            animationDot.background = getDrawable(context, R.drawable.point_drawable)


            if (x < (binding.root.width / 2)) binding.root.x = 0F
            else if (x > ((width - horizontalPadding) - (binding.root.width) / 2)) binding.root.x =
                (width - binding.root.width).toFloat() - horizontalPadding - (binding.root.paddingStart / 2)
            else binding.root.x = x - (binding.root.width / 2)

            if (y - binding.root.height * 1.5F > 0F) binding.root.y = y - binding.root.height * 1.5F
            else binding.root.y = y + binding.root.height * 0.5F

            drawableView.translationZ = 2F
            drawableView.x = x - (drawableView.width / 2)
            drawableView.y = y - (drawableView.width / 2)


            if (animator == null)
                animator =
                    ValueAnimator.ofInt(selectorPointSize, ((selectorPointSize * 2.5).toInt()))
                        .apply {
                            interpolator = AccelerateInterpolator(3F)
                            duration = 1000
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
                animator?.start()

        } catch (ex: Exception) {
            Log.d("exc", "$ex")
        }
    }

    fun updateValueLastPoint(value: Float) {
        try {

            lineData[lineData.lastIndex] = value
            timeSeries[timeSeries.lastIndex] =
                listOf(Date().time.toDouble(), lineData.last().toDouble())
//            calculatePoints()
            if (selectedPoint == points.last() && lineData[lineData.lastIndex] != value) {
                binding.tvPrice.text = value.toString().currencyFormatted
                binding.tvDate.text = System.currentTimeMillis().toGraphTime()
            }
//            invalidate()
        } catch (ex: Exception) {

        }
    }

    fun addPoint() {//add a new point
        timeSeries.removeFirst()
        timeSeries.add(listOf(Date().time.toDouble(), lineData.last().toDouble()))
        lineData.removeFirst()
        lineData.add(lineData.last())
        if (selectedPosition in 1 until points.count() - 1) {
            selectedPosition--
            selectedPoint = points[selectedPosition]
            selectedPoint?.let {
                pointSelected(it.x, it.y, selectedPosition)
            }
        }
    }

    fun commaFormat(it: Float): String {
        var ts = (String.format(Locale.US, "%.3f", it))
        val number = BigDecimal(ts).stripTrailingZeros()
        return number.toString()
    }
}