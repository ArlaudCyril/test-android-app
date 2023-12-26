package com.Lyber.ui.fragments

import android.os.Bundle
import android.view.View
import com.Lyber.R
import com.Lyber.databinding.FragmentInvenstmentExperienceBinding
import com.Lyber.models.InvestmentExperienceLocal
import com.Lyber.ui.fragments.bottomsheetfragments.BottomSheetDialog
import com.Lyber.utils.App
import com.Lyber.utils.CommonMethods
import com.Lyber.utils.CommonMethods.Companion.showToast
import com.Lyber.viewmodels.PersonalDataViewModel

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

        (requireParentFragment() as FillDetailFragment).position = 2
        (requireParentFragment() as FillDetailFragment).setUpViews(2)

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
        if (viewModel.isReview) {
            App.prefsManager.investmentExperienceLocal.let {
                binding.apply {
                    etCryptoExp.setText(it!!.investmentExperience)
                    etSourceIncome.setText(it.sourceOfIncome)
                    etChooseIndustry.setText(it.workIndustry)
                    annualIncome = it.annualIncome!!
                    if (annualIncome == "<500>") binding.etAnnualIncome.setText("Less then 500k€/month")
                    else binding.etAnnualIncome.setText("${annualIncome}k€/month")
                    personalAssets = it.activity
                    etYourActivity.setText(it.activity)


                }
            }
        }

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
                annualIncome = if (itemSelected == "Less than 500") "<500" else itemSelected
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

            annualIncome.isEmpty() -> getString(R.string.please_tell_us_your_annual_income).showToast(
                requireContext()
            )

            personalAssets.isEmpty() -> {
                binding.scrollView.scrollTo(0, binding.root.bottom)
                getString(R.string.please_tell_us_what_do_you_plan_to_mainly_do).showToast(
                    requireContext()
                )
            }

            else -> {
                val investmentExperienceLocal = InvestmentExperienceLocal(
                    investmentExperience = cryptoExp,
                    sourceOfIncome = sourceIncome,
                    workIndustry = workIndustry,
                    annualIncome = annualIncome,
                    activity = personalAssets
                )
                App.prefsManager.investmentExperienceLocal = investmentExperienceLocal
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