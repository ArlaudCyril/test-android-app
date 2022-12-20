package com.au.lyber.ui.fragments

import android.os.Bundle
import android.view.View
import com.au.lyber.databinding.FragmentFundFromAccountBinding

class FundFromBankFragment : BaseFragment<FragmentFundFromAccountBinding>() {

    override fun bind() = FragmentFundFromAccountBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ivTopAction.setOnClickListener { requireActivity().onBackPressed() }

    }
}