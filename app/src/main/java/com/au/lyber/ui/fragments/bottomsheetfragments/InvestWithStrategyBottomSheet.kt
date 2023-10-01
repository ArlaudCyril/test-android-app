package com.au.lyber.ui.fragments.bottomsheetfragments

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import com.au.lyber.databinding.BottomSheetInvestWithStrategyBinding

class InvestWithStrategyBottomSheet (val clickListener: (Boolean) -> Unit) :
    BaseBottomSheet<BottomSheetInvestWithStrategyBinding>(), View.OnClickListener {
    lateinit var viewToDelete: View
    lateinit var mainView: ViewGroup
    override fun bind() = BottomSheetInvestWithStrategyBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvSubTitleAdjust.setOnClickListener(this)
        binding.tvTitleAdjust.setOnClickListener(this)
        binding.ivAdjust.setOnClickListener(this)

        binding.ivTopAction.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        binding.apply {
            when (v!!) {

                ivTopAction -> dismiss()
                tvSubTitleAdjust, tvTitleAdjust, ivAdjust -> {
                    clickListener(true)
                    dismiss()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        this.mainView.removeView(this.viewToDelete)
    }



}