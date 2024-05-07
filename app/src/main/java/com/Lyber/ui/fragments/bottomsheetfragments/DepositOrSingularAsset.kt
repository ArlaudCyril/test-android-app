package com.Lyber.ui.fragments.bottomsheetfragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.Lyber.R
import com.Lyber.databinding.DepositSingularAssetBottomSheetBinding
import com.Lyber.utils.Constants
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class DepositOrSingularAsset(private val handle: (String) -> Unit = { _ -> }) :
    BottomSheetDialogFragment(), View.OnClickListener {

    private var _binding: DepositSingularAssetBottomSheetBinding? = null
    private val binding get() = _binding!!

    private val behaviour get() = BottomSheetBehavior.from(requireView().parent as View)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomDialogBottomSheet)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DepositSingularAssetBottomSheetBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ivTopAction.setOnClickListener(this)
        binding.clMoneyDeposit.setOnClickListener(this)
        binding.clInvestInSingleAsset.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        binding.apply {
            when (v!!) {
                ivTopAction -> dismiss()
               clMoneyDeposit -> {
                    handle(Constants.USING_DEPOSIT)
                    dismiss()
                }
                clInvestInSingleAsset -> {
                    handle(Constants.USING_SINGULAR_ASSET)
                    dismiss()
                }
            }
        }
    }
}