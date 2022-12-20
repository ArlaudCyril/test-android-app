package com.au.lyber.utils

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.au.lyber.R

@SuppressLint("AppCompatCustomView")
class ExpandableTextView : TextView {

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, 0)
    constructor(context: Context, attributeSet: AttributeSet?, defStyle: Int) : super(
        context,
        attributeSet,
        defStyle
    ) {
        init()
    }

    private var startHeight: Int = 0
    private var endHeight: Int = 0

    private var _collapsedText: String = ""
    var collapsedText: String
        get() = _collapsedText
        set(value) {
            _collapsedText = value
        }

    private var _expandedText: String = ""
    var expandedText: String
        get() = _expandedText
        set(value) {
            _expandedText = value
        }


    private fun init() {

    }

    fun expandWith(textToShow: String) {

        setSpan(true, textToShow) {

            /*measureLayout()
            startHeight = layout.height + paddingBottom + paddingTop

            text = textToShow
            measureLayout()

            endHeight = layout.height + paddingBottom + paddingTop

            ValueAnimator.ofInt(startHeight, endHeight).apply {
                duration = 400
                addUpdateListener {
                    if ((animatedValue as Int) < endHeight) {
                        val lp = layoutParams
                        lp.height = animatedValue as Int
                        layoutParams = lp
                    } else collapseWith(textToShow)
                }
                start()
            }*/

            collapseWith(textToShow)
        }
    }

    private fun measureLayout() {
        val wSpecification = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY)
        val hSpecification = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
        measure(wSpecification, hSpecification)
    }

    @SuppressLint("SetTextI18n")
    private fun collapseWith(textToShow: String) {

//        measureLayout()

        setSpan(false, textToShow) {
//            text = textToShow.substring(0..74)
//            measureLayout()
           /* ValueAnimator.ofInt(endHeight, startHeight).apply {
                duration = 200
                addUpdateListener {
                    Log.d("text", "$animatedValue")
                    if ((animatedValue as Int) < endHeight) {
                        val lp = layoutParams
                        lp.height = animatedValue as Int
                        layoutParams = lp
                    } else expandWith(textToShow)
                }
                start()
            }*/

            expandWith(textToShow)
        }
    }

    // function to span the string
    private fun setSpan(viewMore: Boolean, string: String, function: () -> Unit) {

        val optionText = if (viewMore) "View More" else "View Less"

        if (viewMore)
            if (string.trim().length > 80) {
                val finalString = "${string.substring(0..70)}...$optionText"
                SpannableString(finalString).let {

                    it.setSpan(
                        object : ClickableSpan() {
                            override fun onClick(widget: View) {
                                function()
                            }

                            override fun updateDrawState(ds: TextPaint) {
                                highlightColor = Color.TRANSPARENT
                                ds.color = resources.getColor(R.color.purple_500_, context.theme)
                                ds.typeface = Typeface.DEFAULT_BOLD
                                ds.isUnderlineText = true
                                super.updateDrawState(ds)
                            }
                        },
                        73,
                        finalString.length,
                        Spannable.SPAN_EXCLUSIVE_INCLUSIVE
                    )

                    text = it
                    movementMethod = LinkMovementMethod()

//                    measureLayout()
                }
            } else text = string.trim()

        else {
            val finalString = "$string $optionText"
            SpannableString(finalString).let {

//                it.setSpan(
//                    ForegroundColorSpan(ContextCompat.getColor(context, R.color.purple_500_)),
//                    string.length + 3,
//                    finalString.length,
//                    Spannable.SPAN_EXCLUSIVE_INCLUSIVE
//                )

                it.setSpan(
                    object : ClickableSpan() {
                        override fun onClick(widget: View) {
                            function()
                        }

                        override fun updateDrawState(ds: TextPaint) {
                            highlightColor = Color.TRANSPARENT
                            ds.color = resources.getColor(R.color.purple_500_, context.theme)
                            ds.typeface = Typeface.DEFAULT_BOLD
                            ds.isUnderlineText = true
                            super.updateDrawState(ds)
                        }
                    },
                    string.length + 1,
                    finalString.length,
                    Spannable.SPAN_EXCLUSIVE_INCLUSIVE
                )

                text = it
                movementMethod = LinkMovementMethod()

                measureLayout()
            }
        }


    }

}