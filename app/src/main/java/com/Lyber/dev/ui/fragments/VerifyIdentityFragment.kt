//package com.Lyber.ui.fragments
//
//import android.app.Dialog
//import android.os.Bundle
//import android.view.View
//import android.view.Window
//import androidx.lifecycle.Lifecycle
//import com.Lyber.R
//import com.Lyber.databinding.CustomDialogLayoutBinding
//import com.Lyber.databinding.FragmentVerifyIdentityBinding
//import com.Lyber.ui.portfolio.fragment.PortfolioHomeFragment
//import com.Lyber.utils.App
//import com.Lyber.utils.CommonMethods.Companion.checkInternet
//import com.Lyber.utils.CommonMethods.Companion.clearBackStack
//import com.Lyber.utils.CommonMethods.Companion.dismissProgressDialog
//import com.Lyber.utils.CommonMethods.Companion.getViewModel
//import com.Lyber.utils.CommonMethods.Companion.gone
//import com.Lyber.utils.CommonMethods.Companion.replaceFragment
//import com.Lyber.utils.CommonMethods.Companion.showProgressDialog
//import com.Lyber.utils.CommonMethods.Companion.visible
//import com.Lyber.utils.Constants
//import com.Lyber.viewmodels.PortfolioViewModel
//import com.Lyber.viewmodels.VerifyIdentityViewModel
///*
//    ** not in use
// */
//class VerifyIdentityFragment : BaseFragment<FragmentVerifyIdentityBinding>() {
//
//    private lateinit var verifyIdentityViewModel: VerifyIdentityViewModel
//    private lateinit var portfolioViewModel: PortfolioViewModel
//
//    private var startClicked = false
//
//    override fun bind() = FragmentVerifyIdentityBinding.inflate(layoutInflater)
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        verifyIdentityViewModel = getViewModel(this)
//        verifyIdentityViewModel.listener = this
//        portfolioViewModel = getViewModel(requireActivity())
//
//        binding.btnStart.text = getString(R.string.verify)
//
//        verifyIdentityViewModel.kycInitiateResponse.observe(viewLifecycleOwner) {
//            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
//                dismissProgressDialog()
//                requireActivity().replaceFragment(
//                    R.id.flSplashActivity,
//                    WebViewFragment.get(it.identification.identification_url)
//                )
//            }
////            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(it.identification.identification_url))
////            startActivity(Intent.createChooser(intent, "Choose a browser"))
//        }
//
//        verifyIdentityViewModel.kycStatusResponse.observe(viewLifecycleOwner) {
//            dismissProgressDialog()
//            if (it.is_liveness_initiated) {
//                when (it.kyc_review) {
//
//                    // undefined yet
//                    2 -> {
//                        binding.tvSubTitle.text = getString(R.string.your_kyc_is_completed)
//                        binding.btnStart.gone()
//                        binding.btnVerificationInProcess.gone()
//                        binding.btnEditPersonalInfo.gone()
//                    }
//
//                    // kyc rejected
//                    3 -> {
//                        if (it.kyc_level in 1..3) {
//                            binding.tvSubTitle.text =
//                                getString(R.string.oops_verification_failed_reason, it.comment)
//                            binding.btnEditPersonalInfo.visible()
//                            binding.btnStart.visible()
//                            binding.btnVerificationInProcess.gone()
//                        } else binding.tvSubTitle.text =
//                            getString(R.string.sorry_but_your_kyc_is_rejected)
//                    }
//
//                    // kyc review is not successful
//                    else -> {
//
//                        //payment done waiting for response
//                        if (it.is_payin_done) {
//                            binding.tvSubTitle.text =
//                                getString(R.string.please_wait_your_verification_is_under_progress)
//                            binding.btnStart.gone()
//                            binding.btnEditPersonalInfo.gone()
//                            binding.btnVerificationInProcess.visible()
//                        } else {
//                            when {
//
//                                it.kyc_status == "processed" && it.score == 1 -> {
//                                    binding.tvSubTitle.text =
//                                        getString(
//                                            R.string.please_add_1_to_your_wallet_to_complete_the_kyc_process_international_bank_account_number,
//                                            it.iban
//                                        )
//                                    binding.btnStart.gone()
//                                    binding.btnEditPersonalInfo.gone()
//                                    binding.btnVerificationInProcess.visible()
//                                }
//
//                                it.kyc_status == "processed" && it.score == -1 -> {
//                                    binding.tvSubTitle.text =
//                                        getString(R.string.oops_verification_failed_reason, it.comment)
//                                    binding.btnStart.text = getString(R.string.start_again)
//                                    binding.btnEditPersonalInfo.visible()
//                                    binding.btnStart.visible()
//                                    binding.btnVerificationInProcess.gone()
//                                }
//
//                                it.kyc_status == "processed" && it.score == 0 -> {
//                                    binding.tvSubTitle.text =
//                                        getString(R.string.oops_verification_failed_reason, it.comment)
//                                    binding.btnStart.text =  getString(R.string.start_again)
//                                    binding.btnStart.visible()
//                                    binding.btnVerificationInProcess.gone()
//                                    binding.btnEditPersonalInfo.visible()
//                                }
//
//                                it.score == -1 || it.score == 0 -> {
//                                    binding.tvSubTitle.text =
//                                        getString(R.string.oops_verification_failed_reason, it.comment)
//                                    binding.btnStart.text = getString(R.string.start_again)
//                                    binding.btnStart.visible()
//                                    binding.btnVerificationInProcess.gone()
//                                    binding.btnEditPersonalInfo.visible()
//                                }
//
//                            }
//                        }
//
//                    }
//                }
//            } else {
//                binding.tvSubTitle.text =
//                    getString(R.string.oops_verification_failed_reason, it.comment)
//                binding.btnStart.text = getString(R.string.start_again)
//                binding.btnEditPersonalInfo.visible()
//                binding.btnStart.visible()
//                binding.btnVerificationInProcess.gone()
//            }
//        }
//
//        binding.btnStart.setOnClickListener {
//
//            dialogVerify("Verify identity",
//                getString(R.string.it_will_open_ubble_link_to_verify_your_identity))
//            /*checkInternet(binding.root,requireContext()) {
//                startClicked = true
//                showProgressDialog(requireContext())
//                verifyIdentityViewModel.initiatedKyc()
//            }*/
//        }
//
//        binding.btnEditPersonalInfo.setOnClickListener {
//            val bundle = Bundle()
//            bundle.putString(Constants.TO_EDIT, Constants.TO_EDIT)
//            requireActivity().replaceFragment(
//                R.id.flSplashActivity,
//                FillDetailFragment().apply {
//                    arguments = bundle
//                },
//                topBottom = true
//            )
//        }
//
//        binding.ivTopAction.setOnClickListener {
//            requireActivity().onBackPressed()
//        }
//    }
//
//    private fun dialogVerify(title: String, message: String) {
//
//        Dialog(requireContext(), R.style.DialogTheme).apply {
//            CustomDialogLayoutBinding.inflate(layoutInflater).let {
//                requestWindowFeature(Window.FEATURE_NO_TITLE)
//                setCancelable(false)
//                setCanceledOnTouchOutside(false)
//                setContentView(it.root)
//                it.tvTitle.text = title
//                it.tvMessage.text = message
//                it.tvNegativeButton.setTextColor(requireContext().getColor(R.color.red_500))
//                it.tvNegativeButton.text = getString(R.string.cancel)
//                it.tvPositiveButton.text = getString(R.string.ok)
//                it.tvNegativeButton.setOnClickListener { dismiss() }
//                it.tvPositiveButton.setOnClickListener {
//                    dismiss()
//                    App.prefsManager.portfolioCompletionStep = Constants.PROFILE_COMPLETED
//                    requireActivity().clearBackStack()
//                    requireActivity().supportFragmentManager.beginTransaction()
//                        .replace(R.id.flSplashActivity, PortfolioHomeFragment()).commit()
//                }
//                show()
//            }
//        }
//
//    }
//
//    override fun onResume() {
//        super.onResume()
//        if (startClicked)
//            checkInternet(binding.root,requireContext()) {
//                showProgressDialog(requireContext())
//                verifyIdentityViewModel.kycStatus()
//            }
//    }
//
//}