package com.au.lyber.ui.fragments

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.au.lyber.R
import com.au.lyber.databinding.FragmentCompletePortfolioBinding
import com.au.lyber.ui.fragments.bottomsheetfragments.BottomSheetDialog
import com.au.lyber.utils.App
import com.au.lyber.utils.CommonMethods.Companion.getViewModel
import com.au.lyber.utils.CommonMethods.Companion.gone
import com.au.lyber.utils.CommonMethods.Companion.strikeText
import com.au.lyber.utils.CommonMethods.Companion.visible
import com.au.lyber.utils.Constants
import com.au.lyber.ui.portfolio.viewModel.PortfolioViewModel

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

        binding.btnInvestMoney.setOnClickListener(this)
        binding.tvFillPersonalData.setOnClickListener(this)
        binding.btnMenu.setOnClickListener(this)

//        binding.tvEditPersonalData.setOnClickListener(this)
//        binding.tvVerifyYourIdentity.setOnClickListener(this)

        Log.d(TAG, "onViewCreated: ")
        setUpUi(App.prefsManager.portfolioCompletionStep)
    }

    private fun setUpUi(state: Int) {

        Log.d(TAG, "setUpUi: $state")

        when (state) {
            Constants.ACCOUNT_CREATED -> {
                personalDataFilled(personalDataFilled = false, true)
            }
            Constants.PERSONAL_DATA_FILLED -> { // focussed verify identity
                personalDataFilled(true)
                verifyIdentityFocussed(true)
            }
            Constants.KYC_COMPLETED -> { // verified
                personalDataFilled(true)
                identityVerified()
//                makeYourFirstPayment()
            }
            else -> {
                personalDataFilled(true)
                identityVerified()
//                makeYourFirstPayment()
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
                if (showProgress)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        progressSteps.setProgress(30, true)
                    } else
                        progressSteps.progress = 30

                tvNumFillPersonalData.visible()
                tvFillPersonalData.setOnClickListener(this@CompletePortfolioFragment)
                ivFillPersonalData.setBackgroundResource(R.drawable.circle_drawable_purple_500)
                tvNumFillPersonalData.text = getString(R.string._2)
                tvNumFillPersonalData.setTextColor(requireContext().getColor(R.color.white))
                tvFillPersonalData.text = getString(R.string.fill_personal_data)
                tvFillPersonalData.setTextColor(requireContext().getColor(R.color.purple_500))
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
//            tvEditPersonalData.visible()
            tvVerifyYourIdentity.visible()
            llVerificationOnGoing.gone()
        }
    }

    /* private fun identityVerificationInitiated() {
         binding.apply {
             tvNumVerifyYourIdentity.text = ""
             tvVerifyYourIdentity.setOnClickListener(null)
 //            tvEditPersonalData.visible()
             tvVerifyYourIdentity.gone()
             llVerificationOnGoing.visible()
             ivVerifyYourIdentity.setImageResource(R.drawable.progress_drawable)
             postDelay(2000) {
                 identityVerified()
                 makeYourFirstPayment()
                 portfolioViewModel.identityVerified = true
             }
         }
     }
 */

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
//            tvEditPersonalData.gone()
            tvVerifyYourIdentity.setTextColor(requireContext().getColor(R.color.purple_gray_600))
        }
    }

    /*@SuppressLint("UseCompatLoadingForDrawables", "ResourceAsColor")
    private fun makeYourFirstPayment() {
        binding.apply {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                progressSteps.setProgress(75, true)
            } else progressSteps.progress = 75

            tvStepsCompleted.text = "3/4 steps completed"

            ivMakeFirstInvestment.setImageResource(R.drawable.circle_drawable_purple_500)

            btnInvestMoney.setOnClickListener(this@CompletePortfolioFragment)

            tvInvestMoney.setTextColor(requireContext().getColor(R.color.white))
            ivInvestMoney.background =
                AppCompatResources.getDrawable(
                    requireContext(),
                    R.drawable.ic_invest_money_white
                )

            btnInvestMoney.setBackgroundTint(R.color.purple_500)

            tvNumMakeFirstInvestment.setTextColor(requireContext().getColor(R.color.white))
            tvNumMakeFirstInvestment.text = "4"


            tvMakeFirstInvestment.setCompoundDrawablesWithIntrinsicBounds(
                null, null, AppCompatResources.getDrawable(
                    requireContext(),
                    R.drawable.ic_right_arrow_purple
                ), null
            )
            tvMakeFirstInvestment.setTextColor(requireContext().getColor(R.color.purple_500))
            tvMakeFirstInvestment.setOnClickListener(this@CompletePortfolioFragment)

        }
    }*/

    override fun onClick(v: View?) {
        binding.apply {
            when (v!!) {

                btnMenu -> BottomSheetDialog().show(
                    requireActivity().supportFragmentManager,
                    BottomSheetDialog.SheetType.COMPLETE_ACCOUNT.title
                )

                /*tvMakeFirstInvestment*/
                btnInvestMoney -> {

                    if (App.prefsManager.portfolioCompletionStep == Constants.KYC_COMPLETED) {
//                        requireActivity().clearBackStack()
                        findNavController().navigate(R.id.educationStrategyHolderFragment)
                    }
                }

//                tvEditPersonalData,
                tvFillPersonalData -> findNavController().navigate(R.id.fillDetailFragment)

                tvVerifyYourIdentity -> findNavController().navigate(R.id.verifyYourIdentityFragment)


            }
        }
    }
}