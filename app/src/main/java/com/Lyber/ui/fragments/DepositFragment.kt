package com.Lyber.ui.fragments

import android.os.Bundle
import android.view.View
import com.Lyber.databinding.FragmentDepositBinding
import com.Lyber.utils.App

class DepositFragment : BaseFragment<FragmentDepositBinding>() {

    override fun bind() = FragmentDepositBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        App.prefsManager.user?.let {
//            binding.tvIbanNumber.text = "${it.iban}"
//            binding.tvBicNumber.text = "${it.bic}"
//        }

        binding.ivTopAction.setOnClickListener { requireActivity().onBackPressed() }
    }


}