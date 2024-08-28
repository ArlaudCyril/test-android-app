package com.Lyber.dev.ui.fragments


import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
import com.Lyber.dev.R
import com.Lyber.dev.databinding.FragmentInvenstmentExperienceBinding
import com.Lyber.dev.models.DataBottomSheet
import com.Lyber.dev.models.InvestmentExperienceLocal
import com.Lyber.dev.models.InvestmentExperienceLocalIds
import com.Lyber.dev.ui.fragments.bottomsheetfragments.BottomSheetDialog
import com.Lyber.dev.utils.App
import com.Lyber.dev.utils.CommonMethods
import com.Lyber.dev.utils.CommonMethods.Companion.showToast
import com.Lyber.dev.viewmodels.PersonalDataViewModel
import java.util.Locale

class InvestmentExperienceFragment : BaseFragment<FragmentInvenstmentExperienceBinding>(),
    View.OnClickListener {

    /* input fields */
    private val cryptoExp: String get() = binding.etCryptoExp.text.trim().toString()
    private var cryptoExpID: Int = 0
    private val sourceIncome: String get() = binding.etSourceIncome.text.trim().toString()
    private var sourceIncomeID: Int = 0
    private val workIndustry: String get() = binding.etChooseIndustry.text.trim().toString()
    private var workIndustryID: Int = 0

    private var annualIncome: String = ""
    private var annualIncomeID: Int = 0
    private var personalAssets: String = ""
    private var personalAssetsID: Int = 0

    private lateinit var viewModel: PersonalDataViewModel
    override fun bind() = FragmentInvenstmentExperienceBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireParentFragment() as FillDetailFragment).position = 1
        (requireParentFragment() as FillDetailFragment).setUpViews(1)

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
            cryptoExpID=App.prefsManager.investmentExperienceLocalIds!!.investmentExperience
            sourceIncomeID=App.prefsManager.investmentExperienceLocalIds!!.sourceOfIncome
            workIndustryID=App.prefsManager.investmentExperienceLocalIds!!.workIndustry
            annualIncomeID=App.prefsManager.investmentExperienceLocalIds!!.annualIncome
            personalAssetsID=App.prefsManager.investmentExperienceLocalIds!!.activity
            App.prefsManager.investmentExperienceLocal.let {
                binding.apply {
                    etCryptoExp.setText(it!!.investmentExperience)
                    etSourceIncome.setText(it.sourceOfIncome)
                    etChooseIndustry.setText(it.workIndustry)
                    annualIncome = it.annualIncome!!
                    if (annualIncome == "<500") binding.etAnnualIncome.setText("${getString(R.string.less_than_500)}k€/${getString(R.string.month)}")
                 else  if (annualIncome == "3001+") binding.etAnnualIncome.setText("${getString(R.string.over_3001)}k€/${getString(R.string.month)}")
                    else binding.etAnnualIncome.setText("${annualIncome}k€/month")
                    personalAssets = it.activity
                    etYourActivity.setText(it.activity)


                }
            }
        }

    }

    private fun handleClickEvent(tag: String, itemSelected: DataBottomSheet) {
        when (tag) {
            BottomSheetDialog.SheetType.CRYPTO_EXP.title(requireContext()) -> {
                binding.etCryptoExp.setText(itemSelected.title)
                cryptoExpID = itemSelected.id
            }

            BottomSheetDialog.SheetType.SOURCE_OF_INCOME.title(requireContext()) -> {
                binding.etSourceIncome.setText(itemSelected.title)
                sourceIncomeID = itemSelected.id
            }

            BottomSheetDialog.SheetType.WORK_INDUSTRY.title(requireContext()) -> {
                binding.etChooseIndustry.setText(itemSelected.title)
                workIndustryID = itemSelected.id
            }

            BottomSheetDialog.SheetType.ANNUAL_INCOME.title(requireContext()) -> {
                annualIncome =
                    if (itemSelected.title == getString(R.string.less_than_500)) "<500"
                    else if(itemSelected.title == getString(R.string.over_3001)) "3001+"
                    else itemSelected.title
                binding.etAnnualIncome.setText("${itemSelected.title}k€/${getString(R.string.month)}")
                annualIncomeID = itemSelected.id
            }

            else -> {
                personalAssets = itemSelected.title
                binding.etYourActivity.setText("${itemSelected.title}")
                personalAssetsID = itemSelected.id
            }
        }
    }


    fun checkData(): Boolean {

        when {
            cryptoExp.isEmpty() -> getString(R.string.please_tell_us_your_investment_experience_with_crypto).showToast(
                binding.root,requireContext()
            )

            sourceIncome.isEmpty() -> getString(R.string.please_tell_us_your_source_of_income).showToast(
                binding.root,requireContext()
            )

            workIndustry.isEmpty() -> getString(R.string.please_tell_us_which_work_industry_your_are_working).showToast(
                binding.root, requireContext()
            )

            annualIncome.isEmpty() -> getString(R.string.please_tell_us_your_annual_income).showToast(
                binding.root,requireContext()
            )

            personalAssets.isEmpty() -> {
                binding.scrollView.scrollTo(0, binding.root.bottom)
                getString(R.string.please_tell_us_what_do_you_plan_to_mainly_do).showToast(
                    binding.root, requireContext()
                )
            }

            else -> {

                val investmentExperienceLocal = InvestmentExperienceLocal(
                    investmentExperience =requireContext().getLocaleStringResource(
                        Locale("en"),
                        cryptoExpID
                    ),
                    sourceOfIncome = requireContext().getLocaleStringResource(
                        Locale("en"),
                        sourceIncomeID
                    ),
                    workIndustry = requireContext().getLocaleStringResource(
                        Locale("en"),
                        workIndustryID
                    ),
                    annualIncome = requireContext().getLocaleStringResource(
                        Locale("en"),
                        annualIncomeID
                    ),
                    activity = requireContext().getLocaleStringResource(
                        Locale("en"),
                        personalAssetsID
                    )
                )
                val investmentExperienceLocalids = InvestmentExperienceLocalIds(
                    investmentExperience =cryptoExpID,
                    sourceOfIncome = sourceIncomeID,
                    workIndustry = workIndustryID,
                    annualIncome = annualIncomeID,
                    activity = personalAssetsID
                )
                 App.prefsManager.investmentExperienceLocal = InvestmentExperienceLocal(
                    investmentExperience =cryptoExp,
                    sourceOfIncome = sourceIncome,
                    workIndustry = workIndustry,
                    annualIncome = annualIncome,
                    activity = binding.etYourActivity.text.toString().trim()
                )
                App.prefsManager.investmentExperienceLocalIds = investmentExperienceLocalids
                viewModel.let {
                    it.cryptoExp =investmentExperienceLocal.investmentExperience
                    it.sourceOfIncome = investmentExperienceLocal.sourceOfIncome
                    it.workIndustry = investmentExperienceLocal.workIndustry
                    it.annualIncome = annualIncome
                    it.personalAssets = investmentExperienceLocal.activity
                }
                return true
            }
        }
        return false
    }

    fun Context.getLocaleStringResource(
        requestedLocale: Locale?,
        resourceId: Int,
    ): String {
        val result: String
        val config =
            Configuration(resources.configuration)
        config.setLocale(requestedLocale)
        result = createConfigurationContext(config).getText(resourceId).toString()
        if(App.prefsManager.getLanguage().isNotEmpty())
            config.setLocale(Locale(App.prefsManager.getLanguage()))
        return result
    }

    override fun onClick(v: View?) {
        binding.apply {
            when (v!!) {
                etAnnualIncome -> {
                    BottomSheetDialog(::handleClickEvent).show(
                        requireActivity().supportFragmentManager,
                        BottomSheetDialog.SheetType.ANNUAL_INCOME.title(requireContext())
                    )
                }

                etChooseIndustry -> {
                    BottomSheetDialog(::handleClickEvent).show(
                        requireActivity().supportFragmentManager,
                        BottomSheetDialog.SheetType.WORK_INDUSTRY.title(requireContext())
                    )
                }

                etCryptoExp -> {
                    BottomSheetDialog(::handleClickEvent).show(
                        requireActivity().supportFragmentManager,
                        BottomSheetDialog.SheetType.CRYPTO_EXP.title(requireContext())
                    )
                }

                etSourceIncome -> {
                    BottomSheetDialog(::handleClickEvent).show(
                        requireActivity().supportFragmentManager,
                        BottomSheetDialog.SheetType.SOURCE_OF_INCOME.title(requireContext())
                    )
                }

                etYourActivity -> {
                    BottomSheetDialog(::handleClickEvent).show(
                        requireActivity().supportFragmentManager,
                        BottomSheetDialog.SheetType.YOUR_ACTIVITY_ON_LYBER.title(requireContext())
                    )
                }
            }
        }
    }
}