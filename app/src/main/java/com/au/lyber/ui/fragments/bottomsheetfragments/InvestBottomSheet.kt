package com.au.lyber.ui.fragments.bottomsheetfragments

import android.os.Bundle
import android.view.View
import com.au.lyber.databinding.BottomSheetInvestBinding

class InvestBottomSheet(val clickListener: (Boolean) -> Unit) :
    BaseBottomSheet<BottomSheetInvestBinding>(), View.OnClickListener {

    override fun bind() = BottomSheetInvestBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvTitleInvestInStrategy.setOnClickListener(this)
        binding.tvSubTitleInvestInStrategy.setOnClickListener(this)
        binding.ivRightArrowInvestInStrategy.setOnClickListener(this)

        binding.tvTitleSingleAsset.setOnClickListener(this)
        binding.tvSubTitleSingleAsset.setOnClickListener(this)
        binding.ivRightArrowSingleAsset.setOnClickListener(this)

        binding.ivTopAction.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        binding.apply {
            when (v!!) {

                ivTopAction -> dismiss()
                tvSubTitleInvestInStrategy, tvTitleInvestInStrategy, ivRightArrowInvestInStrategy -> {
                    clickListener(true)
                    dismiss()
                }
                tvTitleSingleAsset, tvSubTitleSingleAsset, ivRightArrowSingleAsset -> {
                    clickListener(false)
                    dismiss()
                }
            }
        }
    }


}