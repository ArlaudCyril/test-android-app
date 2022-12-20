package com.au.lyber.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import androidx.core.content.ContextCompat.getColor
import com.au.lyber.R
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

class AppBoldLineChart : LineChart {

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, 0)
    constructor(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attributeSet,
        defStyleAttr
    ) {
        init()
        prepareLayout()
    }

    private var _marker: Int = R.drawable.radio_select
    var marker
        get() = _marker
        set(value) {
            _marker = value
        }

    private var _lineColorRes: Int = 0
    var lineColorRes
        get() = _lineColorRes
        set(value) {
            _lineColorRes = value
        }

    private var _circleColorRes: Int = 0
    var circleColorRes
        get() = _circleColorRes
        set(value) {
            _circleColorRes = value
        }

    private var _lineWidth: Float = 4F
    var lineWidth
        get() = _lineWidth
        set(value) {
            _lineWidth = value
        }

    private var _titleTextList: MutableList<String> = mutableListOf()
    var titleTextList
        get() = _titleTextList
        set(value) {
            _titleTextList = value
        }

    private fun prepareLayout() {
        setBackgroundColor(Color.TRANSPARENT)
        description.isEnabled = false
        legend.isEnabled = false
        axisLeft.isEnabled = false
        axisRight.isEnabled = false
        xAxis.isEnabled = false
        setPinchZoom(false)
        setScaleEnabled(false)
        isDoubleTapToZoomEnabled = false
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    fun setData(list: List<Entry>) {

        LineDataSet(list, "").let {


            it.lineWidth = lineWidth
            it.setDrawCircles(false)
            it.setDrawCircleHole(false)

            it.color = getColor(context, lineColorRes)
            it.circleHoleColor = getColor(context, circleColorRes)
            it.setCircleColor(getColor(context, circleColorRes))

            it.valueTextSize = 0F

//            val data = it.getEntryForIndex(list.count() - 1).data

            it.mode = LineDataSet.Mode.CUBIC_BEZIER
            it.cubicIntensity = 0.05f



            it.highLightColor = getColor(context,lineColorRes)
            it.highlightLineWidth = 2F
            it.enableDashedHighlightLine(10F,7F,0F)
            it.isHighlightEnabled = true
            it.setDrawHighlightIndicators(true)
            it.setDrawHorizontalHighlightIndicator(false)

            it.getEntryForIndex(list.count() - 1).icon = context.getDrawable(marker)

            data = LineData(it)

        }
    }
}