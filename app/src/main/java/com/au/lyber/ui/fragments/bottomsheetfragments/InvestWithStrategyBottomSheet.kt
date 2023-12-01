package com.au.lyber.ui.fragments.bottomsheetfragments

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import com.au.lyber.databinding.BottomSheetInvestWithStrategyBinding
import com.au.lyber.ui.portfolio.viewModel.PortfolioViewModel
import com.au.lyber.utils.CommonMethods.Companion.gone
import com.au.lyber.utils.CommonMethods.Companion.visible

class InvestWithStrategyBottomSheet (val clickListener: (Int) -> Unit) :
    BaseBottomSheet<BottomSheetInvestWithStrategyBinding>(), View.OnClickListener {
    lateinit var viewToDelete: View
    lateinit var mainView: ViewGroup
    lateinit var viewModel: PortfolioViewModel
    override fun bind() = BottomSheetInvestWithStrategyBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isCancelable = false
        binding.tvSubTitleAdjust.setOnClickListener(this)
        binding.tvTitleAdjust.setOnClickListener(this)
        binding.ivAdjust.setOnClickListener(this)
        binding.ivTopAction.setOnClickListener(this)
        //
        binding.ivPAuse.setOnClickListener(this)
        binding.tvTitlePAuse.setOnClickListener(this)
        binding.ivRightArrowPAuse.setOnClickListener(this)
        //
        binding.ivDelete.setOnClickListener(this)
        binding.tvTitleDelete.setOnClickListener(this)
        binding.ivRightArrowDelete.setOnClickListener(this)
        //
        binding.tvSubTitleTailor.setOnClickListener(this)
        binding.tvTitleTailor.setOnClickListener(this)
        binding.ivTailor.setOnClickListener(this)
        if (viewModel.selectedStrategy!!.activeStrategy!=null){
            binding.apply {
                ivDelete.gone()
                tvTitleDelete.gone()
                ivRightArrowDelete.gone()
                ivPAuse.visible()
                tvTitlePAuse.visible()
                ivRightArrowPAuse.visible()
                    //
                tvTitleAdjust.gone()
                ivAdjust.gone()
                ivRightArrowAdjust.gone()
                tvSubTitleAdjust.gone()
                tvTitleAdjustNew.visible()
                ivAdjustNew.visible()
                tvSubTitleAdjustNew.visible()
                ivRightArrowAdjustNew.visible()
            }
        }else{
            binding.apply {
                ivDelete.visible()
                tvTitleDelete.visible()
                ivRightArrowDelete.visible()
                ivPAuse.gone()
                tvTitlePAuse.gone()
                ivRightArrowPAuse.gone()
                tvTitleAdjust.visible()
                ivAdjust.visible()
                ivRightArrowAdjust.visible()
                tvSubTitleAdjust.visible()
                tvTitleAdjustNew.gone()
                ivAdjustNew.gone()
                tvSubTitleAdjustNew.gone()
                ivRightArrowAdjustNew.gone()
                if (viewModel.selectedStrategy!!.publicType!=null && viewModel.selectedStrategy!!.publicType
                    == "lyber"){
                    ivDelete.gone()
                    tvTitleDelete.gone()
                    ivRightArrowDelete.gone()
                }
            }
        }
    }

    override fun onClick(v: View?) {
        binding.apply {
            when (v!!) {

                ivTopAction -> {
                    clickListener(-1)
                    dismiss()
                }
                tvSubTitleAdjust, tvTitleAdjust, ivAdjust -> {
                    clickListener(0)
                    dismiss()
                }
                ivPAuse, tvTitlePAuse, ivRightArrowPAuse -> {
                    clickListener(1)
                    dismiss()
                }
                ivDelete, tvTitleDelete, ivRightArrowDelete -> {
                    clickListener(2)
                    dismiss()
                }
                tvSubTitleTailor, tvTitleTailor, ivTailor -> {
                    clickListener(3)
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