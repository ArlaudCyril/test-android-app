package com.Lyber.ui.fragments.bottomsheetfragments

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import com.Lyber.databinding.BottomSheetInvestWithStrategyBinding
import com.Lyber.viewmodels.PortfolioViewModel
import com.Lyber.utils.CommonMethods.Companion.gone
import com.Lyber.utils.CommonMethods.Companion.visible

class InvestWithStrategyBottomSheet (val clickListener: (Int) -> Unit) :
    BaseBottomSheet<BottomSheetInvestWithStrategyBinding>(), View.OnClickListener {
    lateinit var viewToDelete: View
    lateinit var mainView: ViewGroup
    lateinit var viewModel: PortfolioViewModel
    var clickVal=-1
    override fun bind() = BottomSheetInvestWithStrategyBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        isCancelable = false
        viewModel.listener=this
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
        //
        binding.tvOneTime.setOnClickListener(this)
        binding.tvSubTitleOneTime.setOnClickListener(this)
        binding.ivOneTime.setOnClickListener(this)
        binding.ivRightArrowInvestInStrategy.setOnClickListener(this)
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
                    clickVal=-1
                     dismiss()
                }
                tvSubTitleAdjust, tvTitleAdjust, ivAdjust -> {
                    clickVal=0
                    dismiss()
                }
                ivPAuse, tvTitlePAuse, ivRightArrowPAuse -> {
                   clickVal=1
                    dismiss()
                }
                ivDelete, tvTitleDelete, ivRightArrowDelete -> {
                    clickVal=2
                    dismiss()
                }
                tvSubTitleTailor, tvTitleTailor, ivTailor -> {
                   clickVal=3
                    dismiss()
                }
                tvSubTitleOneTime, tvOneTime, ivOneTime -> {
                   clickVal=4
                    dismiss()
                }
            }
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        clickListener(clickVal)
    }
    override fun onDestroyView() {
        super.onDestroyView()
        this.mainView.removeView(this.viewToDelete)
    }



}