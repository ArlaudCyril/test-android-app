package com.Lyber.ui.fragments

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.Lyber.R
import com.Lyber.databinding.CustomDialogVerticalLayoutBinding
import com.Lyber.databinding.FragmentVerifyYourIdentityBinding
import com.Lyber.network.RestClient
import com.Lyber.ui.activities.WebViewActivity
import com.Lyber.utils.App
import com.Lyber.utils.CommonMethods
import com.Lyber.utils.CommonMethods.Companion.checkInternet
import com.Lyber.utils.CommonMethods.Companion.dismissAlertDialog
import com.Lyber.utils.CommonMethods.Companion.dismissProgressDialog
import com.Lyber.utils.CommonMethods.Companion.getViewModel
import com.Lyber.utils.CommonMethods.Companion.visible
import com.Lyber.utils.Constants
import com.Lyber.viewmodels.PortfolioViewModel
import com.Lyber.viewmodels.VerifyIdentityViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import okhttp3.ResponseBody


class VerifyYourIdentityFragment : BaseFragment<FragmentVerifyYourIdentityBinding>(),
    View.OnClickListener, RestClient.OnRetrofitError {
    private lateinit var navController: NavController
    private lateinit var verifyIdentityViewModel: VerifyIdentityViewModel
    private lateinit var portfolioViewModel: PortfolioViewModel
    private var conditionsSelected = false
    private var privacySelected = false
    private var isVerificationEnabled = false
    private var isApiHit = false
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
        portfolioViewModel.listener = this
        verifyIdentityViewModel.uploadResponse.observe(viewLifecycleOwner) {
            dismissProgressDialog()
        }
        setObserver()
        binding.radioBtn.setOnClickListener(this)
        binding.radioBtn2.setOnClickListener(this)
        binding.tvGeneralTerms.setOnClickListener(this)
        binding.tvPrivacyPolicy.setOnClickListener(this)
        isVerificationEnabled()
        if (privacySelected)
            binding.radioBtn2.setImageResource(R.drawable.purple_checkbox)
        if (conditionsSelected)
            binding.radioBtn.setImageResource(R.drawable.purple_checkbox)
    }

    private fun setObserver() {
        portfolioViewModel.kycResponseIdentity.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                isApiHit = false
                dismissAnimation()
//                val bundle = Bundle()
//                bundle.putString(Constants.URL, it.data.url)
//                navController.navigate(R.id.webViewFragmentTrial, bundle)
//                resultLauncher.launch(
                val intent = Intent(requireActivity(), WebViewActivity::class.java)
                    .putExtra(Constants.URL, it.data.url)

                intent.putExtra("fragment_to_show", "fragment2")
                startActivity(intent)
//                )
            }
        }
//        portfolioViewModel.logoutResponse.observe(viewLifecycleOwner){
//            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
//                App.prefsManager.logout()
//                findNavController().popBackStack()
//                findNavController().navigate(R.id.discoveryFragment)
//            }
//        }
    }

    companion object {
        const val NONE: Int = 0
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.ivTopAction -> {
                if (!isApiHit)
                    stopRegistrationDialog()
            }

            binding.btnContinue -> {
                if (isVerificationEnabled)
                    showWarningDialog()
//                    hitAcpi()
            }

            binding.btnReviewMyInformations -> {
                if (!isApiHit) {
                    val bundle = Bundle().apply {
                        putBoolean(Constants.IS_REVIEW, true)
                    }
                    findNavController().navigate(R.id.fillDetailFragment, bundle)
                }
            }

            binding.radioBtn -> {
                if (!isApiHit) {
                    if (conditionsSelected)
                        binding.radioBtn.setImageResource(R.drawable.circle_stroke_profile)
                    else
                        binding.radioBtn.setImageResource(R.drawable.purple_checkbox)
                    conditionsSelected = !conditionsSelected
                    isVerificationEnabled()
                }
            }

            binding.radioBtn2 -> {
                if (!isApiHit) {
                    if (privacySelected)
                        binding.radioBtn2.setImageResource(R.drawable.circle_stroke_profile)
                    else
                        binding.radioBtn2.setImageResource(R.drawable.purple_checkbox)
                    privacySelected = !privacySelected
                    isVerificationEnabled()
                }
            }

            binding.tvGeneralTerms -> {
                if (!isApiHit) {
//                    val bundle = Bundle()
//                    bundle.putString("url", Constants.GENERAL_TERMS_CONDITIONS)
//                    navController.navigate(R.id.webViewFragment, bundle)
                    val intent = Intent(requireActivity(), WebViewActivity::class.java)
                        .putExtra(Constants.URL, Constants.GENERAL_TERMS_CONDITIONS)
                    startActivity(intent)
                }
            }

            binding.tvPrivacyPolicy -> {
                if (!isApiHit) {
//                    val bundle = Bundle()
//                    bundle.putString("url", Constants.PRIVACY_URL)
//                    navController.navigate(R.id.webViewFragment, bundle)
                    val intent = Intent(requireActivity(), WebViewActivity::class.java)
                        .putExtra(Constants.URL, Constants.PRIVACY_URL)
                    startActivity(intent)
                }
            }
        }
    }


    override fun onRetrofitError(responseBody: ResponseBody?) {
        super.onRetrofitError(responseBody)
        dismissAlertDialog()
        isApiHit = false
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
                CommonMethods.showProgressDialog(requireContext())
                portfolioViewModel.finishRegistration()
            }
        }

    private fun hitAcpi() {
        checkInternet(requireActivity()) {
            isApiHit = true
            binding.progress.visible()
            binding.progress.animation =
                AnimationUtils.loadAnimation(requireActivity(), R.anim.rotate_drawable)
            binding.btnContinue.text = ""
            portfolioViewModel.startKYCIdentity()
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

    private lateinit var bottomDialog: BottomSheetDialog
    private fun showWarningDialog() {
        bottomDialog = BottomSheetDialog(requireContext(), R.style.CustomDialogBottomSheet).apply {
            CustomDialogVerticalLayoutBinding.inflate(layoutInflater).let { binding ->
                setContentView(binding.root)
                binding.viewGap.visible()
                binding.tvTitle.text = context.getString(R.string.warning)
                binding.tvMessage.text = context.getString(R.string.certify_detail)
                binding.tvNegativeButton.text = context.getString(R.string.cancel)
                binding.tvPositiveButton.text = context.getString(R.string.yes_certify)

                binding.tvNegativeButton.setOnClickListener {
                    dismiss()
                }
                binding.tvPositiveButton.setOnClickListener {
                    dismiss()
                    hitAcpi()
                }
                show()
            }
        }
    }
}