package com.Lyber.dev.utils

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import com.Lyber.dev.R
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

class AppLineChart : LineChart {

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

    fun setLineData(
        list: List<Entry>,
        colorId: Int = R.color.green_500,
        backgroundRes: Int = R.drawable.drawable_green_fill_line_chart
    ) {
        LineDataSet(list, null).let {

            it.lineWidth = 1F

            it.setDrawCircleHole(false)
            it.setDrawCircleHole(false)
            it.setDrawCircles(false)
            it.setDrawFilled(true) // for second layout use this.
            it.setDrawValues(false)

            it.color = ContextCompat.getColor(context, colorId)
            it.fillDrawable = ContextCompat.getDrawable(context, backgroundRes)

            it.isHighlightEnabled = false

            data = LineData(it)

            it.mode = LineDataSet.Mode.CUBIC_BEZIER

//            animateXY(2000, 2000, Easing.Linear)
        }
    }


}