package com.Lyber.ui.fragments

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.navigation.fragment.findNavController
import com.Lyber.R
import com.Lyber.databinding.FragmentCompletePortfolioBinding
import com.Lyber.ui.fragments.bottomsheetfragments.BottomSheetDialog
import com.Lyber.ui.portfolio.viewModel.PortfolioViewModel
import com.Lyber.utils.App
import com.Lyber.utils.CommonMethods.Companion.getViewModel
import com.Lyber.utils.CommonMethods.Companion.gone
import com.Lyber.utils.CommonMethods.Companion.strikeText
import com.Lyber.utils.CommonMethods.Companion.visible
import com.Lyber.utils.Constants

class CompletePortfolioFragment : BaseFragment<FragmentCompletePortfolioBinding>(),
    View.OnClickListener {

    private val TAG = "CompletePortfolioFragme"

    private lateinit var portfolioViewModel: PortfolioViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.prefsManager.savedScreen = javaClass.name
    }

    override fun bind() = FragmentCompletePortfolioBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        portfolioViewModel = getViewModel(requireActivity())

//        binding.btnInvestMoney.setOnClickListener(this)
        binding.tvFillPersonalData.setOnClickListener(this)
        binding.tvCreateAnAccount.setOnClickListener(this)
//        binding.btnMenu.setOnClickListener(this)

        Log.d(TAG, "onViewCreated: ")
        setUpUi(App.prefsManager.portfolioCompletionStep)
    }

    private fun setUpUi(state: Int) {

        Log.d(TAG, "setUpUi: $state")

        when (state) {
            -1->{
                binding.tvFillPersonalData.setOnClickListener(null)

            }
            Constants.ACCOUNT_CREATING -> {
                accountCreationFilled(false,true)
            personalDataFilled(personalDataFilled = false, false)
            }
            Constants.ACCOUNT_CREATED -> {
                accountCreationFilled(true,false)
                personalDataFilled(personalDataFilled = false, true)
            }
            Constants.PERSONAL_DATA_FILLED -> { // focussed verify identity
                personalDataFilled(true)
                accountCreationFilled(true,false)
                verifyIdentityFocussed(true)
            }
            Constants.KYC_COMPLETED -> {
                accountCreationFilled(true,false)// verified
                personalDataFilled(true)
                identityVerified()
            }
            else -> {
                personalDataFilled(true)
                accountCreationFilled(true,false)
                identityVerified()
            }
        }
    }

    private fun accountCreationFilled(personalDataFilled: Boolean, showProgress: Boolean = false) {
        binding.apply {
            if (personalDataFilled) {

                if (showProgress)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        progressSteps.setProgress(60, true)
                    } else
                        progressSteps.progress = 60

                tvStepsCompleted.text = getString(R.string._2_3_steps_completed)

                tvNumFillCreateAccount.gone()
                tvNumFillCreateAccount.setOnClickListener(null)
                ivCreateAccount.setImageResource(R.drawable.drawable_circle_checked)
                tvCreateAnAccount.strikeText()
                tvCreateAnAccount.setCompoundDrawables(null, null, null, null)
                tvCreateAnAccount.setTextColor(requireContext().getColor(R.color.purple_gray_600))
            } else {
                if (showProgress)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        progressSteps.setProgress(0, true)
                    } else
                        progressSteps.progress = 0
                tvNumFillCreateAccount.gone()
                tvNumFillCreateAccount.visible()
                tvCreateAnAccount.setOnClickListener(this@CompletePortfolioFragment)
                ivCreateAccount.setBackgroundResource(R.drawable.circle_drawable_purple_500)
                tvNumFillCreateAccount.text = "1"
                tvNumFillCreateAccount.setTextColor(requireContext().getColor(R.color.white))
                tvCreateAnAccount.setTextColor(requireContext().getColor(R.color.purple_500))
            }
        }
    }

    private fun personalDataFilled(personalDataFilled: Boolean, showProgress: Boolean = false) {
        binding.apply {
            if (personalDataFilled) {

                if (showProgress)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        progressSteps.setProgress(60, true)
                    } else
                        progressSteps.progress = 60

                tvStepsCompleted.text = getString(R.string._2_3_steps_completed)

                tvNumFillPersonalData.gone()
                tvFillPersonalData.setOnClickListener(null)
                ivFillPersonalData.setImageResource(R.drawable.drawable_circle_checked)
                tvFillPersonalData.strikeText()
                tvFillPersonalData.setCompoundDrawables(null, null, null, null)
                tvFillPersonalData.setTextColor(requireContext().getColor(R.color.purple_gray_600))
            } else {
                if (showProgress) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        progressSteps.setProgress(30, true)
                    } else
                        progressSteps.progress = 30
                    tvNumFillCreateAccount.gone()
                    tvNumFillPersonalData.visible()
                    tvFillPersonalData.setOnClickListener(this@CompletePortfolioFragment)
                    ivFillPersonalData.setBackgroundResource(R.drawable.circle_drawable_purple_500)
                    tvNumFillPersonalData.text = getString(R.string._2)
                    tvNumFillPersonalData.setTextColor(requireContext().getColor(R.color.white))
                    tvFillPersonalData.text = getString(R.string.fill_personal_data)
                    tvFillPersonalData.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_right_arrow_purple, 0)
                    tvFillPersonalData.setTextColor(requireContext().getColor(R.color.purple_500))
                }else{
                    ivFillPersonalData.setBackgroundResource(R.drawable.circle_drawable)
                    tvNumFillPersonalData.text = getString(R.string._2)
                    tvNumFillPersonalData.setTextColor(requireContext().getColor(R.color.purple_gray_600))
                    tvFillPersonalData.text = getString(R.string.fill_personal_data)
                    tvFillPersonalData.setTextColor(requireContext().getColor(R.color.purple_gray_600))
                }
            }
        }
    }

    private fun verifyIdentityFocussed(showProgress: Boolean = false) {
        binding.apply {
            if (showProgress)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    progressSteps.setProgress(60, true)
                } else
                    progressSteps.progress = 60

            ivVerifyYourIdentity.setImageResource(R.drawable.circle_drawable_purple_500)
            tvNumVerifyYourIdentity.setTextColor(requireContext().getColor(R.color.white))
            tvNumVerifyYourIdentity.text = getString(R.string._3)

            tvVerifyYourIdentity.setTextColor(requireContext().getColor(R.color.purple_500))
            tvVerifyYourIdentity.setOnClickListener(this@CompletePortfolioFragment)
            tvVerifyYourIdentity.setCompoundDrawablesWithIntrinsicBounds(
                null,
                null,
                AppCompatResources.getDrawable(
                    requireContext(),
                    R.drawable.ic_right_arrow_purple
                ),
                null
            )
            tvVerifyYourIdentity.visible()
            llVerificationOnGoing.gone()
        }
    }


    private fun identityVerified(showProgress: Boolean = false) {
        binding.apply {
            if (showProgress)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    progressSteps.setProgress(90, true)
                } else
                    progressSteps.progress = 90

            tvStepsCompleted.text = getString(R.string._3_3_steps_completed)

            tvNumVerifyYourIdentity.text = ""
            ivVerifyYourIdentity.setImageResource(R.drawable.drawable_circle_checked)
            tvVerifyYourIdentity.setTextColor(requireContext().getColor(R.color.purple_gray_900))
            tvVerifyYourIdentity.setOnClickListener(null)
            tvVerifyYourIdentity.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
            tvVerifyYourIdentity.visibility = View.VISIBLE
            llVerificationOnGoing.visibility = View.GONE
            tvVerifyYourIdentity.strikeText()
            tvVerifyYourIdentity.setTextColor(requireContext().getColor(R.color.purple_gray_600))
        }
    }



    override fun onClick(v: View?) {
        binding.apply {
            when (v!!) {

                btnMenu -> BottomSheetDialog().show(
                    requireActivity().supportFragmentManager,
                    BottomSheetDialog.SheetType.COMPLETE_ACCOUNT.title(requireContext())
                )

                btnInvestMoney -> {

                    if (App.prefsManager.portfolioCompletionStep == Constants.KYC_COMPLETED) {
                        findNavController().navigate(R.id.educationStrategyHolderFragment)
                    }
                }
                tvCreateAnAccount->{
                    clickCreateAccount()
                }
                tvFillPersonalData -> findNavController().navigate(R.id.fillDetailFragment)

                tvVerifyYourIdentity -> findNavController().navigate(R.id.verifyYourIdentityFragment)


            }
        }
    }

    private fun clickCreateAccount() {
        when(App.prefsManager.accountCreationSteps) {
            Constants.Account_CREATION_STEP_PHONE -> {
                val bundle = Bundle().apply {
                    putBoolean(Constants.FOR_LOGIN, false)
                }
                findNavController().navigate(R.id.emailAddressFragment,bundle)
            }

            Constants.Account_CREATION_STEP_EMAIL -> {
                val bundle = Bundle().apply {
                    putBoolean(Constants.FOR_LOGIN, false)
                }
                App.prefsManager.accountCreationSteps= Constants.Account_CREATION_STEP_PHONE
                findNavController().navigate(R.id.createPinFragment,bundle)
            }
            else->{
                val bundle = Bundle().apply {
                    putBoolean(Constants.FOR_LOGIN, false)
                }
                findNavController().navigate(R.id.createAccountFragment, bundle)
            }
        }
    }
}