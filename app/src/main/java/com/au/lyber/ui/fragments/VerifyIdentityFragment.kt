package com.au.lyber.ui.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.Window
import androidx.lifecycle.Lifecycle
import com.au.lyber.R
import com.au.lyber.databinding.CustomDialogLayoutBinding
import com.au.lyber.databinding.FragmentVerifyIdentityBinding
import com.au.lyber.utils.App
import com.au.lyber.utils.CommonMethods.Companion.checkInternet
import com.au.lyber.utils.CommonMethods.Companion.clearBackStack
import com.au.lyber.utils.CommonMethods.Companion.dismissProgressDialog
import com.au.lyber.utils.CommonMethods.Companion.getViewModel
import com.au.lyber.utils.CommonMethods.Companion.gone
import com.au.lyber.utils.CommonMethods.Companion.replaceFragment
import com.au.lyber.utils.CommonMethods.Companion.showProgressDialog
import com.au.lyber.utils.CommonMethods.Companion.visible
import com.au.lyber.utils.Constants
import com.au.lyber.utils.PreferenceManager
import com.au.lyber.viewmodels.PortfolioViewModel
import com.au.lyber.viewmodels.VerifyIdentityViewModel

class VerifyIdentityFragment : BaseFragment<FragmentVerifyIdentityBinding>() {

    private lateinit var verifyIdentityViewModel: VerifyIdentityViewModel
    private lateinit var portfolioViewModel: PortfolioViewModel

    private var startClicked = false

    override fun bind() = FragmentVerifyIdentityBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        verifyIdentityViewModel = getViewModel(this)
        verifyIdentityViewModel.listener = this
        portfolioViewModel = getViewModel(requireActivity())

        binding.btnStart.text = "Verify"

        verifyIdentityViewModel.kycInitiateResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                dismissProgressDialog()
                requireActivity().replaceFragment(
                    R.id.flSplashActivity,
                    WebViewFragment.get(it.identification.identification_url)
                )
            }
//            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(it.identification.identification_url))
//            startActivity(Intent.createChooser(intent, "Choose a browser"))
        }

        verifyIdentityViewModel.kycStatusResponse.observe(viewLifecycleOwner) {
            dismissProgressDialog()
            if (it.is_liveness_initiated) {
                when (it.kyc_review) {

                    // undefined yet
                    2 -> {
                        binding.tvSubTitle.text = "Your KYC is completed!"
                        binding.btnStart.gone()
                        binding.btnVerificationInProcess.gone()
                        binding.btnEditPersonalInfo.gone()
                    }

                    // kyc rejected
                    3 -> {
                        if (it.kyc_level in 1..3) {
                            binding.tvSubTitle.text =
                                "OOPS! verification failed.\nReason: ${it.comment}"
                            binding.btnEditPersonalInfo.visible()
                            binding.btnStart.visible()
                            binding.btnVerificationInProcess.gone()
                        } else binding.tvSubTitle.text = "Sorry,but your KYC is rejected."
                    }

                    // kyc review is not successful
                    else -> {

                        //payment done waiting for response
                        if (it.is_payin_done) {
                            binding.tvSubTitle.text =
                                "Please wait your verification is under progress"
                            binding.btnStart.gone()
                            binding.btnEditPersonalInfo.gone()
                            binding.btnVerificationInProcess.visible()
                        } else {
                            when {

                                it.kyc_status == "processed" && it.score == 1 -> {
                                    binding.tvSubTitle.text =
                                        "Please add €1 to your wallet to complete the KYC process\nInternational bank Account Number\n ${it.iban}"
                                    binding.btnStart.gone()
                                    binding.btnEditPersonalInfo.gone()
                                    binding.btnVerificationInProcess.visible()
                                }

                                it.kyc_status == "processed" && it.score == -1 -> {
                                    binding.tvSubTitle.text =
                                        "OOPS! verification failed.\nReason : ${it.comment}"
                                    binding.btnStart.text = "Start again"
                                    binding.btnEditPersonalInfo.visible()
                                    binding.btnStart.visible()
                                    binding.btnVerificationInProcess.gone()
                                }

                                it.kyc_status == "processed" && it.score == 0 -> {
                                    binding.tvSubTitle.text =
                                        "OOPS! verification failed.\nReason : ${it.comment}"
                                    binding.btnStart.text = "Start again"
                                    binding.btnStart.visible()
                                    binding.btnVerificationInProcess.gone()
                                    binding.btnEditPersonalInfo.visible()
                                }

                                it.score == -1 || it.score == 0 -> {
                                    binding.tvSubTitle.text =
                                        "OOPS! verification failed.\nReason : ${it.comment}"
                                    binding.btnStart.text = "Start again"
                                    binding.btnStart.visible()
                                    binding.btnVerificationInProcess.gone()
                                    binding.btnEditPersonalInfo.visible()
                                }

                            }
                        }

                    }
                }
            } else {
                binding.tvSubTitle.text =
                    "OOPS! verification failed.\nReason : ${it.comment}"
                binding.btnStart.text = "Start again"
                binding.btnEditPersonalInfo.visible()
                binding.btnStart.visible()
                binding.btnVerificationInProcess.gone()
            }
        }

        binding.btnStart.setOnClickListener {

            dialogVerify("Verify identity", "It will open Ubble link to verify your identity.")
            /*checkInternet(requireContext()) {
                startClicked = true
                showProgressDialog(requireContext())
                verifyIdentityViewModel.initiatedKyc()
            }*/
        }

        binding.btnEditPersonalInfo.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("toEdit", "toEdit")
            requireActivity().replaceFragment(
                R.id.flSplashActivity,
                FillDetailFragment().apply {
                    arguments = bundle
                },
                topBottom = true
            )
        }

        binding.ivTopAction.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    private fun dialogVerify(title: String, message: String) {

        Dialog(requireContext(), R.style.DialogTheme).apply {
            CustomDialogLayoutBinding.inflate(layoutInflater).let {
                requestWindowFeature(Window.FEATURE_NO_TITLE)
                setCancelable(false)
                setCanceledOnTouchOutside(false)
                setContentView(it.root)
                it.tvTitle.text = title
                it.tvMessage.text = message
                it.tvNegativeButton.setTextColor(requireContext().getColor(R.color.red_500))
                it.tvNegativeButton.text = getString(R.string.cancel)
                it.tvPositiveButton.text = "Ok"
                it.tvNegativeButton.setOnClickListener { dismiss() }
                it.tvPositiveButton.setOnClickListener {
                    dismiss()
                    App.prefsManager.portfolioCompletionStep = Constants.PROFILE_COMPLETED
                    requireActivity().clearBackStack()
                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.flSplashActivity, PortfolioFragment()).commit()
                }
                show()
            }
        }

    }

    override fun onResume() {
        super.onResume()
        if (startClicked)
            checkInternet(requireContext()) {
                showProgressDialog(requireContext())
                verifyIdentityViewModel.kycStatus()
            }
    }

}