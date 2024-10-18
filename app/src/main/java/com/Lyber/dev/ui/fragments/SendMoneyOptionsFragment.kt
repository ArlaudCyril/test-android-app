package com.Lyber.dev.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.Lyber.dev.R
import com.Lyber.dev.databinding.FragmentSendMoneyOptionsBinding
import com.Lyber.dev.ui.portfolio.fragment.PortfolioDetailFragment
import com.Lyber.dev.ui.portfolio.fragment.PortfolioHomeFragment
import com.Lyber.dev.utils.CommonMethods.Companion.showToast
import com.Lyber.dev.utils.Constants
import com.Lyber.dev.viewmodels.PortfolioViewModel

class SendMoneyOptionsFragment : BaseFragment<FragmentSendMoneyOptionsBinding>(), OnClickListener {
    private lateinit var viewModel: PortfolioViewModel
    private var fromFrag = PortfolioHomeFragment::class.java.name
    private var assetId = ""

    override fun bind() = FragmentSendMoneyOptionsBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (arguments != null && requireArguments().containsKey(Constants.FROM)) {
            fromFrag = PortfolioDetailFragment::class.java.name
            assetId=requireArguments().getString(Constants.ID).toString()
        }
        binding.apply {
            ivTopAction.setOnClickListener(this@SendMoneyOptionsFragment)
            llPhoneNo.setOnClickListener(this@SendMoneyOptionsFragment)
            llQrCode.setOnClickListener(this@SendMoneyOptionsFragment)
        }
    }

    override fun onClick(p0: View?) {
        binding.apply {
            when (p0) {
                ivTopAction -> requireActivity().onBackPressedDispatcher.onBackPressed()
                llPhoneNo -> {
                    if(fromFrag==PortfolioDetailFragment::class.java.name){
                        val bundle = Bundle()
                        bundle.putString(Constants.ID, assetId)
                        bundle.putString(Constants.SELECTED_METHOD, Constants.PHONE)
                        findNavController().navigate(R.id.sendAmountFragment,bundle)
                    }
                    else {
                        val args = Bundle().apply {
                            putString("selectedOption", Constants.USING_SEND_MONEY)
                            putString(Constants.SELECTED_METHOD, Constants.PHONE)
                            putString(Constants.FROM, fromFrag)
                        }
                        findNavController().navigate(R.id.swapWithdrawFromFragment, args)
                    }
                }
                llQrCode->{
                    if(fromFrag==PortfolioDetailFragment::class.java.name){
                        val bundle = Bundle()
                        bundle.putString(Constants.ID, assetId)
                        bundle.putString(Constants.SELECTED_METHOD, Constants.QR)
                        findNavController().navigate(R.id.sendAmountFragment,bundle)
                    }
                    else {
                        val args = Bundle().apply {
                            putString("selectedOption", Constants.USING_SEND_MONEY)
                            putString(Constants.SELECTED_METHOD, Constants.QR)
                            putString(Constants.FROM, fromFrag)
                        }
                        findNavController().navigate(R.id.swapWithdrawFromFragment, args)
                    }

                }
            }
        }
    }


}