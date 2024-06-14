package com.Lyber.dev.ui.fragments

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.Lyber.dev.R
import com.Lyber.dev.databinding.CustomDialogLayoutBinding
import com.Lyber.dev.databinding.CustomDialogVerticalLayoutBinding
import com.Lyber.dev.databinding.DocumentBeingVerifiedBinding
import com.Lyber.dev.network.RestClient
import com.Lyber.dev.ui.activities.WebViewActivity
import com.Lyber.dev.ui.portfolio.fragment.PortfolioHomeFragment
import com.Lyber.dev.viewmodels.PortfolioViewModel
import com.Lyber.dev.utils.App
import com.Lyber.dev.utils.CommonMethods
import com.Lyber.dev.utils.CommonMethods.Companion.dismissAlertDialog
import com.Lyber.dev.utils.CommonMethods.Companion.dismissProgressDialog
import com.Lyber.dev.utils.CommonMethods.Companion.gone
import com.Lyber.dev.utils.CommonMethods.Companion.showProgressDialog
import com.Lyber.dev.utils.CommonMethods.Companion.showToast
import com.Lyber.dev.utils.CommonMethods.Companion.visible
import com.Lyber.dev.utils.Constants
import com.Lyber.dev.utils.LoaderObject
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import okhttp3.ResponseBody

abstract class BaseFragment<viewBinding : ViewBinding> : Fragment(), RestClient.OnRetrofitError {
     val viewModel1: com.Lyber.dev.models.GetUserViewModal by activityViewModels()

    private var _binding: viewBinding? = null

    //     var isSign = false
//    var isKyc = false
    var isAddress = false
    private var verificationVisible = false
    val binding get() = _binding!!

    abstract fun bind(): viewBinding
    private lateinit var viewModel: PortfolioViewModel


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = bind()
        viewModel = CommonMethods.getViewModel(requireActivity())

        viewModel.kycResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
//                dismissProgressDialog()
                App.isSign = false
//                if (::bottomDialog.isInitialized)
//                    bottomDialog.dismiss()
                resultLauncher.launch(
                    Intent(requireActivity(), WebViewActivity::class.java)
                        .putExtra(Constants.URL, it.data.url)
                        .putExtra(Constants.FROM, true)
                        .putExtra(Constants.ASK_PERMISSION, true)
                )

            }
        }

        viewModel.signUrlResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                Handler(Looper.getMainLooper()).postDelayed({
                    dismissProgressDialog()
                    App.isSign = true
                    if (::bottomDialog.isInitialized)
                        bottomDialog.dismiss()
                    resultLauncher.launch(
                        Intent(requireActivity(), WebViewActivity::class.java)
                            .putExtra(Constants.URL, it.data.url)
                            .putExtra(Constants.FROM, true)
                            .putExtra(Constants.ASK_PERMISSION, true)
                    )
                }, 1000)

            }
        }
        viewModel.logoutResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                viewModel.totalPortfolio = 0.0
                dismissProgressDialog()
                dismissAlertDialog()
                CommonMethods.logOut(requireContext())
                viewModel1.stopFetchingUserData()
            }
        }
//        viewModel.getUserSignResponse.observe(viewLifecycleOwner) {
//            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
//                dismissProgressDialog()
//
//                if (it.data.kycStatus == "OK") {
//                    showDocumentDialog(App.appContext, Constants.LOADING, true)
//                }
//            }
//        }

        return binding.root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val navHostFragment =
                    requireActivity().supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
                val currentFragment = navHostFragment.childFragmentManager.primaryNavigationFragment

                if (App.isSign) {
                    activity?.runOnUiThread {
                        App.isSign = true
                        App.isLoader=true
                        findNavController().popBackStack(R.id.portfolioHomeFragment, false)
                       CommonMethods.showDocumentDialog(requireActivity(), Constants.LOADING, true)
                    }
                } else {
                    App.isKyc = true
                    App.isLoader=true
                    findNavController().popBackStack(R.id.portfolioHomeFragment, false)
                    CommonMethods.showDocumentDialog(requireActivity(), Constants.LOADING, false)

                }


            }
        }

    override fun onRetrofitError(responseBody: ResponseBody?) {
        dismissProgressDialog()
        dismissAlertDialog()
        val code = CommonMethods.showErrorMessage(requireContext(), responseBody, binding.root)
        Log.d("errorCode", "$code")
        if (code == 7023 || code == 10041 || code == 7025 || code == 10043)
            customDialog(code)
    }

    override fun onError() {
        dismissProgressDialog()
        getString(R.string.unable_to_connect_to_the_server).showToast(requireContext())
    }

    private lateinit var bottomDialog: BottomSheetDialog
    fun checkKyc(): Boolean {
        if (App.prefsManager.user!!.kycStatus != "OK") {
            if (App.prefsManager.user!!.kycStatus != "REVIEW")
                customDialog(7023)
            else
                CommonMethods.showSnackBar(binding.root, requireContext(), null)
            return false
        } else if (App.prefsManager.user!!.yousignStatus != "SIGNED") {
            customDialog(7025)
            return false
        } else
            return true
    }

    fun customDialog(code: Int, cancel: Boolean = false) {
        bottomDialog = BottomSheetDialog(requireContext(), R.style.CustomDialogBottomSheet).apply {
            CustomDialogVerticalLayoutBinding.inflate(layoutInflater).let { binding ->
                setContentView(binding.root)
                if (code == 7023 || code == 10041) {
                    binding.tvTitle.text = context.getString(R.string.validate_kyc)
                    binding.tvMessage.text = context.getString(R.string.please_validate_kyc)
                    binding.tvNegativeButton.text = context.getString(R.string.cancel)
                    binding.tvPositiveButton.text = context.getString(R.string.validate_kyc)
                } else if (code == 7025 || code == 10043) {
                    binding.tvTitle.text = context.getString(R.string.sign_my_contract)
                    binding.tvMessage.text = context.getString(R.string.contract_has_not_signed)
                    binding.tvNegativeButton.text = context.getString(R.string.cancel)
                    binding.tvPositiveButton.text = context.getString(R.string.sign_my_contract)
                }
                val navHostFragment =
                    requireActivity().supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
                val currentFragment = navHostFragment.childFragmentManager.primaryNavigationFragment


                binding.tvNegativeButton.setOnClickListener {
                    dismiss()
                    if (isAddress) {
                        navHostFragment.navController.popBackStack(
                            navHostFragment.navController.graph.startDestinationId,
                            false
                        )
                        navHostFragment.navController.navigate(
                            R.id.portfolioHomeFragment,
                            arguments
                        )
                    }
                    isAddress = false

                }
                binding.tvPositiveButton.setOnClickListener {
                    CommonMethods.checkInternet(requireContext()) {
                        LoaderObject.showLoader(requireContext())
//                        showProgressDialog(requireContext())
                        if (code == 7023 || code == 10041) {
                            viewModel.startKyc()
                            bottomDialog.dismiss()
                        } else if (code == 7025 || code == 10043) {
                            viewModel.startSignUrl()
                        }
                        bottomDialog.dismiss()
                    }
                }
                show()
            }
        }
    }

    fun stopRegistrationDialog() {
        Dialog(requireActivity(), R.style.DialogTheme).apply {

            CustomDialogLayoutBinding.inflate(layoutInflater).let {

                requestWindowFeature(Window.FEATURE_NO_TITLE)
                setCancelable(false)
                setCanceledOnTouchOutside(false)
                setContentView(it.root)

                it.tvTitle.text = getString(R.string.stop_reg)
                it.tvMessage.text = getString(R.string.reg_message)
                it.tvNegativeButton.text = getString(R.string.cancel)
                it.tvPositiveButton.text = getString(R.string.ok)

                it.tvNegativeButton.setOnClickListener { dismiss() }

                it.tvPositiveButton.setOnClickListener {
                    dismiss()
                    viewModel.totalPortfolio = 0.0
                    App.prefsManager.logout()
                    context.startActivity(
                        Intent(
                            context,
                            com.Lyber.dev.ui.activities.SplashActivity::class.java
                        ).apply {
                            putExtra("fromLogout", "fromLogout")
                            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        }
                    )
                }

                show()
            }
        }
    }

    fun startJob(){
        viewModel1.startFetchingUserData()
    }
}