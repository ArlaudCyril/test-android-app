package com.au.lyber.ui.fragments

import android.graphics.drawable.Icon
import android.os.Bundle
import android.view.View
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.core.content.ContextCompat.getColor
import androidx.core.view.setPadding
import com.au.lyber.R
import com.au.lyber.databinding.AppItemLayoutBinding
import com.au.lyber.databinding.FragmentAddPaymentMethodBinding
import com.au.lyber.utils.CommonMethods.Companion.px
import com.au.lyber.utils.CommonMethods.Companion.replaceFragment
import com.au.lyber.utils.CommonMethods.Companion.setBackgroundTint

class AddPaymentMethodFragment : BaseFragment<FragmentAddPaymentMethodBinding>(),
    View.OnClickListener {

    override fun bind() = FragmentAddPaymentMethodBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpPaymentViews()
        binding.ivTopAction.setOnClickListener(this)
    }

    private fun setUpPaymentViews() {
        binding.llAddPaymentMethods.apply {
            for (i in 0..1) {
                AppItemLayoutBinding.inflate(layoutInflater).let {
                    it.ivItem.background = getDrawable(requireContext(), R.drawable.circle_drawable)
                    it.ivItem.setBackgroundTint(R.color.purple_gray_50)
                    it.ivItem.setPadding(4.px)
                    it.ivEndIcon.setImageResource(R.drawable.ic_right_arrow_grey)
                    when (i) {
                        0 -> {
                            it.tvStartTitle.text = "Add a bank account"
                            it.tvStartSubTitle.text = "Limited to 25 000€ per week"
                            it.ivItem.setImageResource(R.drawable.ic_bank_outline)
                        }
                        1 -> {
                            it.tvStartTitle.text = "Add a credit card"
                            it.tvStartSubTitle.text = "Limited to 1000€ per week"
                            it.ivItem.setImageIcon(
                                Icon.createWithResource(
                                    requireContext(),
                                    R.drawable.ic_credit_card_outline
                                ).setTint(getColor(requireContext(), R.color.purple_500_))
                            )
                        }
                        /*  2 -> {
                              it.tvStartTitle.text = "Paypal"
                              it.tvStartSubTitle.text = "Used only for withdrawals"
                              it.ivItem.setImageResource(R.drawable.ic_paypal)
                          }*/
                    }
                    it.root.setOnClickListener(this@AddPaymentMethodFragment)
                    addView(it.root, i)
                }
            }
        }
    }

    override fun onClick(v: View?) {
        binding.apply {
            when (v!!) {
                // add credit
                llAddPaymentMethods.getChildAt(1) -> {
                    requireActivity().replaceFragment(
                        R.id.flSplashActivity,
                        AddCreditCardFragment()
                    )
                }
                //bank info
                llAddPaymentMethods.getChildAt(0) -> {
                    requireActivity().replaceFragment(
                        R.id.flSplashActivity,
                        AddBankInfoFragment()
                    )
                }
                ivTopAction -> requireActivity().onBackPressed()

            }
        }
    }
}