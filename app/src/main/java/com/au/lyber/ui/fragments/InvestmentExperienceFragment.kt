package com.au.lyber.ui.fragments

import android.os.Bundle
import android.view.View
import com.au.lyber.R
import com.au.lyber.databinding.FragmentInvenstmentExperienceBinding
import com.au.lyber.ui.fragments.bottomsheetfragments.BottomSheetDialog
import com.au.lyber.utils.CommonMethods
import com.au.lyber.utils.CommonMethods.Companion.showToast
import com.au.lyber.viewmodels.PersonalDataViewModel

class InvestmentExperienceFragment : BaseFragment<FragmentInvenstmentExperienceBinding>(),
    View.OnClickListener {

    /* input fields */
    private val cryptoExp: String get() = binding.etCryptoExp.text.trim().toString()
    private val sourceIncome: String get() = binding.etSourceIncome.text.trim().toString()
    private val workIndustry: String get() = binding.etChooseIndustry.text.trim().toString()

    private var annualIncome: String = ""
    private var personalAssets: String = ""

    private lateinit var viewModel: PersonalDataViewModel
    override fun bind() = FragmentInvenstmentExperienceBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireParentFragment() as FillDetailFragment).position = 4
        (requireParentFragment() as FillDetailFragment).setUpViews(4)

//        if (App.prefsManager.user?.personal_info_step == Constants.ADDRESS)
//            (requireParentFragment() as FillDetailFragment).binding.ivTopAction.setBackgroundResource(
//                R.drawable.ic_close
//            )

        viewModel = CommonMethods.getViewModel(requireParentFragment())

        binding.apply {
            etAnnualIncome.setOnClickListener(this@InvestmentExperienceFragment)
            etChooseIndustry.setOnClickListener(this@InvestmentExperienceFragment)
            etCryptoExp.setOnClickListener(this@InvestmentExperienceFragment)
            etSourceIncome.setOnClickListener(this@InvestmentExperienceFragment)
            etYourActivity.setOnClickListener(this@InvestmentExperienceFragment)
        }

        binding.etCryptoExp.text.clear()
        binding.etChooseIndustry.text.clear()
        binding.etAnnualIncome.text.clear()
        binding.etSourceIncome.text.clear()
        binding.etYourActivity.text.clear()

        /*personalDataViewModel.personalData?.let {

            annualIncome = it.incomeRange
            personalAssets = it.personalAssets

//            binding.etCryptoExp.setText(it.investmentExp.cryptoExp)
//            binding.etChooseIndustry.setText(it.investmentExp.workIndustry)
            binding.etAnnualIncome.setText("${annualIncome}k€/month")
//            binding.etSourceIncome.setText(it.investmentExp.sourceOfIncome)
            binding.etPersonalAssets.setText("$personalAssets assets")
        }*/

    }

    private fun handleClickEvent(tag: String, itemSelected: String) {
        when (tag) {
            BottomSheetDialog.SheetType.CRYPTO_EXP.title ->
                binding.etCryptoExp.setText(itemSelected)

            BottomSheetDialog.SheetType.SOURCE_OF_INCOME.title ->
                binding.etSourceIncome.setText(itemSelected)

            BottomSheetDialog.SheetType.WORK_INDUSTRY.title ->
                binding.etChooseIndustry.setText(itemSelected)

            BottomSheetDialog.SheetType.ANNUAL_INCOME.title -> {
                annualIncome = itemSelected
                binding.etAnnualIncome.setText("${itemSelected}k€/month")
            }
            else -> {
                personalAssets = itemSelected
                binding.etYourActivity.setText("$itemSelected")
            }
        }
    }


    fun checkData(): Boolean {

        when {
            cryptoExp.isEmpty() -> getString(R.string.please_tell_us_your_investment_experience_with_crypto).showToast(
                requireContext()
            )
            sourceIncome.isEmpty() -> getString(R.string.please_tell_us_your_source_of_income).showToast(
                requireContext()
            )
            workIndustry.isEmpty() -> getString(R.string.please_tell_us_which_work_industry_your_are_working).showToast(
                requireContext()
            )
            annualIncome.isEmpty() -> getString(R.string.please_tell_us_your_annual_income).showToast(requireContext())
           personalAssets.isEmpty() -> {
                binding.scrollView.scrollTo(0, binding.root.bottom)
               getString(R.string.please_tell_us_what_do_you_plan_to_mainly_do).showToast(
                    requireContext()
                )
            }
            else -> {

                viewModel.let {
                    it.cryptoExp = cryptoExp
                    it.sourceOfIncome = sourceIncome
                    it.workIndustry = workIndustry
                    it.annualIncome = annualIncome
                    it.personalAssets = personalAssets
                }
                return true
            }
        }
        return false
    }

    override fun onClick(v: View?) {
        binding.apply {
            when (v!!) {
                etAnnualIncome -> {
                    BottomSheetDialog(::handleClickEvent).show(
                        requireActivity().supportFragmentManager,
                        BottomSheetDialog.SheetType.ANNUAL_INCOME.title
                    )
                }
                etChooseIndustry -> {
                    BottomSheetDialog(::handleClickEvent).show(
                        requireActivity().supportFragmentManager,
                        BottomSheetDialog.SheetType.WORK_INDUSTRY.title
                    )
                }
                etCryptoExp -> {
                    BottomSheetDialog(::handleClickEvent).show(
                        requireActivity().supportFragmentManager,
                        BottomSheetDialog.SheetType.CRYPTO_EXP.title
                    )
                }
                etSourceIncome -> {
                    BottomSheetDialog(::handleClickEvent).show(
                        requireActivity().supportFragmentManager,
                        BottomSheetDialog.SheetType.SOURCE_OF_INCOME.title
                    )
                }
                etYourActivity -> {
                    BottomSheetDialog(::handleClickEvent).show(
                        requireActivity().supportFragmentManager,
                        BottomSheetDialog.SheetType.YOUR_ACTIVITY_ON_LYBER.title
                    )
                }
            }
        }
    }
}