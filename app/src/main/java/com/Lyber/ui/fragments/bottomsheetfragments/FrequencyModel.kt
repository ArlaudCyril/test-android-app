package com.Lyber.ui.fragments.bottomsheetfragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.Lyber.R
import com.Lyber.databinding.FrequencyBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class FrequencyModel(private val handle: (String) -> Unit = { _ -> }) :
    BottomSheetDialogFragment(),
    View.OnClickListener {

    private var _binding: FrequencyBottomSheetBinding? = null
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
        _binding = FrequencyBottomSheetBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        behaviour.state = BottomSheetBehavior.STATE_EXPANDED

        binding.ivTopAction.setOnClickListener(this)
        binding.tvSubTitleMonthly.setOnClickListener(this)
        binding.tvTitleMonthly.setOnClickListener(this)
        binding.tvSubTitleWeekly.setOnClickListener(this)
        binding.tvTitleWeekly.setOnClickListener(this)
        binding.tvSubTitleOnce.setOnClickListener(this)
        binding.tvTitleOnce.setOnClickListener(this)
        binding.tvSubTitleDaily.setOnClickListener(this)
        binding.tvTitleDaily.setOnClickListener(this)

    }

    override fun onClick(v: View?) {
        binding.apply {
            when (v!!) {
                ivTopAction -> dismiss()
                tvTitleDaily -> {
                    handle(getString(R.string.daily))
                    dismiss()
                }
                tvSubTitleDaily -> {
                    handle(getString(R.string.daily))
                    dismiss()
                }
                tvTitleOnce -> {
                    handle(getString(R.string.once))
                    dismiss()
                }
                tvSubTitleOnce -> {
                    handle(getString(R.string.once))
                    dismiss()
                }
                tvTitleWeekly -> {
                    handle(getString(R.string.weekly))
                    dismiss()
                }
                tvSubTitleWeekly -> {
                    handle(getString(R.string.weekly))
                    dismiss()
                }
                tvTitleMonthly -> {
                    handle(getString(R.string.monthly))
                    dismiss()
                }
                tvSubTitleMonthly -> {
                    handle(getString(R.string.monthly))
                    dismiss()
                }
            }
        }
    }

}