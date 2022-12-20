package com.au.lyber.ui.fragments

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import androidx.recyclerview.widget.GridLayoutManager
import com.au.lyber.R
import com.au.lyber.databinding.StrategyViewBinding

class StrategyView : RelativeLayout, View.OnClickListener {

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, 0)
    constructor(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int) : this(
        context,
        attributeSet,
        defStyleAttr,
        0
    )

    constructor(
        context: Context,
        attributeSet: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attributeSet, defStyleAttr, defStyleRes) {
        init()
    }


    private lateinit var _binding: StrategyViewBinding
    val binding get() = _binding

    /* to get and set text fields */
    var topText
        get() = binding.tvTop.text.trim().toString()
        set(value) {
            binding.tvTop.text = value.trim().toString()
        }

    var yeild
        get() = binding.tvValueYield.text.trim().toString()
        set(value) {
            binding.tvValueYield.text = value.trim().toString()
        }

    var risk
        get() = binding.tvValueRisk.text.trim().toString()
        set(value) {
            binding.tvValueRisk.text = value.trim().toString()
        }

    var strategyPrice
        get() = binding.tvPriceStrategy.text.trim().toString()
        set(value) {
            binding.tvPriceStrategy.text = value.trim().toString()
        }

    private var _mSelected = false
    var isStrategySelected: Boolean
        get() = _mSelected
        set(value) {
            _mSelected = value
        }

    val allocationView get() = binding.allocationView

    val radioButton: ImageView get() = binding.radioButton
    val btnInvestUsingStrategy: TextView get() = binding.btnInvestUsingMyStrategy
    val btnPickAnotherStrategy: TextView get() = binding.btnPickAnotherStrategy
    val tvEditMyStrategy: TextView get() = binding.tvEditMyStrategy

    /* progress indicator */

    /* fir recycler views */
    private lateinit var layoutManager: GridLayoutManager

    private fun init() {
        _binding = StrategyViewBinding.inflate(LayoutInflater.from(context), this)

        background = ContextCompat.getDrawable(
            context, R.drawable.round_stroke_gray_100
        )

        setPadding(20)
        layoutManager = GridLayoutManager(
            context, 2
        )


        binding.radioButton.setOnClickListener(this)
        postInvalidate()
    }

    fun setOnRadioButtonClickListener(onClickListener: OnClickListener) {
        binding.radioButton.setOnClickListener(onClickListener)
    }


    override fun onClick(v: View?) {
        binding.apply {
            when (v!!) {
                radioButton -> {
                    Log.d(TAG, "onClick: radioButton")
                }
            }
            Log.d(TAG, "onClick: ItemClicked")
        }
    }

    companion object {
        private const val TAG = "StrategyView"
    }


}