package com.Lyber.ui.fragments

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.Lyber.R
import com.Lyber.databinding.FragmentVerifyYourIdentityBinding
import com.Lyber.network.RestClient
import com.Lyber.ui.activities.WebViewActivity
import com.Lyber.ui.portfolio.viewModel.PortfolioViewModel
import com.Lyber.utils.App
import com.Lyber.utils.CommonMethods.Companion.checkInternet
import com.Lyber.utils.CommonMethods.Companion.dismissProgressDialog
import com.Lyber.utils.CommonMethods.Companion.getViewModel
import com.Lyber.utils.CommonMethods.Companion.replaceFragment
import com.Lyber.utils.CommonMethods.Companion.visible
import com.Lyber.utils.Constants
import com.Lyber.viewmodels.VerifyIdentityViewModel
import okhttp3.ResponseBody


class VerifyYourIdentityFragment : BaseFragment<FragmentVerifyYourIdentityBinding>(),
    View.OnClickListener, RestClient.OnRetrofitError {
    private lateinit var navController: NavController
    private lateinit var verifyIdentityViewModel: VerifyIdentityViewModel
    private lateinit var portfolioViewModel: PortfolioViewModel
    private var conditionsSelected = false
    private var privacySelected = false
    private var isVerificationEnabled = false
    override fun bind() = FragmentVerifyYourIdentityBinding.inflate(layoutInflater)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val navHostFragment =
            requireActivity().supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.findNavController()
        //optional
        binding.btnContinue.setOnClickListener(this)
        binding.btnReviewMyInformations.setOnClickListener(this)
        binding.ivTopAction.setOnClickListener(this)

        verifyIdentityViewModel = getViewModel(this)
        portfolioViewModel = getViewModel(requireActivity())

        verifyIdentityViewModel.listener = this
        verifyIdentityViewModel.uploadResponse.observe(viewLifecycleOwner) {
            dismissProgressDialog()
        }
        setObserver()
        binding.radioBtn.setOnClickListener(this)
        binding.radioBtn2.setOnClickListener(this)
        binding.tvGeneralTerms.setOnClickListener(this)
        binding.tvPrivacyPolicy.setOnClickListener(this)
    }

    private fun setObserver() {
        portfolioViewModel.finishRegistrationResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                dismissProgressDialog()

                App.prefsManager.accessToken = it.data.access_token
                App.prefsManager.refreshToken = it.data.refresh_token

                App.prefsManager.personalDataSteps = Constants.INVESTMENT_EXP
                App.prefsManager.portfolioCompletionStep = Constants.PERSONAL_DATA_FILLED
                childFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                childFragmentManager.popBackStackImmediate()
                childFragmentManager.popBackStackImmediate()
                requireActivity().onBackPressed()
            }
        }
        portfolioViewModel.kycResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                dismissAnimation()
                resultLauncher.launch(
                    Intent(requireActivity(), WebViewActivity::class.java)
                        .putExtra(Constants.URL, it.data.url)
                )
            }
        }
    }

    companion object {
        const val NONE: Int = 0
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.ivTopAction -> {
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }

            binding.btnContinue -> {
                if(isVerificationEnabled)
                hitAcpi()
            }

            binding.btnReviewMyInformations -> {
                val bundle = Bundle().apply {
                    putBoolean(Constants.IS_REVIEW, true)
                }
                findNavController().navigate(R.id.fillDetailFragment, bundle)
            }

            binding.radioBtn -> {
                if (conditionsSelected)
                    binding.radioBtn.setImageResource(R.drawable.circle_stroke_profile)
                else
                    binding.radioBtn.setImageResource(R.drawable.purple_checkbox)
                conditionsSelected = !conditionsSelected
                isVerificationEnabled()
            }

            binding.radioBtn2 -> {
                if (privacySelected)
                    binding.radioBtn2.setImageResource(R.drawable.circle_stroke_profile)
                else
                    binding.radioBtn2.setImageResource(R.drawable.purple_checkbox)
                privacySelected = !privacySelected
                isVerificationEnabled()
            }
            binding.tvGeneralTerms->{
                val bundle = Bundle()
                bundle.putString("url", Constants.GENERAL_TERMS_CONDITIONS)
                navController.navigate(R.id.webViewFragment,bundle)
            }
            binding.tvPrivacyPolicy->{
                val bundle = Bundle()
                bundle.putString("url", Constants.PRIVACY_URL)
                navController.navigate(R.id.webViewFragment,bundle)
            }
        }
    }

    override fun onRetrofitError(responseBody: ResponseBody?) {
        super.onRetrofitError(responseBody)
        dismissAnimation()
    }

    private fun dismissAnimation() {
        binding.progress.clearAnimation()
        binding.progress.visibility = View.GONE
        binding.btnContinue.text = getString(R.string.start_identity_verifications)
    }

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_FIRST_USER) {
                val bundle = Bundle().apply {
                    putBoolean(Constants.IS_REVIEW, true)
                }
                findNavController().navigate(R.id.fillDetailFragment, bundle)
            } else if (result.resultCode == Activity.RESULT_OK) {
                portfolioViewModel.finishRegistration()
            }
        }

    private fun hitAcpi() {
        checkInternet(requireActivity()) {
            binding.progress.visible()
            binding.progress.animation =
                AnimationUtils.loadAnimation(requireActivity(), R.anim.rotate_drawable)
            binding.btnContinue.text = ""
            portfolioViewModel.startKyc()
        }

    }

    private fun isVerificationEnabled() {
        if (privacySelected && conditionsSelected) {
            binding.btnContinue.backgroundTintList =
                ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.purple_500))
            isVerificationEnabled = true
        } else {
            binding.btnContinue.backgroundTintList =
                ColorStateList.valueOf(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.purple_gray_500
                    )
                )
            isVerificationEnabled = false
        }
    }
}