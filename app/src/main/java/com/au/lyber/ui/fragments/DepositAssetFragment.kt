package com.au.lyber.ui.fragments

import android.os.Bundle
import android.view.View
import com.au.lyber.databinding.FragmentDepositAssetBinding

class DepositAssetFragment : BaseFragment<FragmentDepositAssetBinding>() {

    override fun bind() = FragmentDepositAssetBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.ivTopAction.setOnClickListener { requireActivity().onBackPressed() }
    }

}