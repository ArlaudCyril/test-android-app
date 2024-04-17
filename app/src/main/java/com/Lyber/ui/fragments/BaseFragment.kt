package com.Lyber.ui.fragments

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
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.Lyber.R
import com.Lyber.databinding.CustomDialogLayoutBinding
import com.Lyber.databinding.CustomDialogVerticalLayoutBinding
import com.Lyber.databinding.DocumentBeingVerifiedBinding
import com.Lyber.network.RestClient
import com.Lyber.ui.activities.SplashActivity
import com.Lyber.ui.activities.WebViewActivity
import com.Lyber.viewmodels.PortfolioViewModel
import com.Lyber.utils.App
import com.Lyber.utils.CommonMethods
import com.Lyber.utils.CommonMethods.Companion.checkInternet
import com.Lyber.utils.CommonMethods.Companion.dismissAlertDialog
import com.Lyber.utils.CommonMethods.Companion.dismissProgressDialog
import com.Lyber.utils.CommonMethods.Companion.gone
import com.Lyber.utils.CommonMethods.Companion.logOut
import com.Lyber.utils.CommonMethods.Companion.showProgressDialog
import com.Lyber.utils.CommonMethods.Companion.showToast
import com.Lyber.utils.CommonMethods.Companion.visible
import com.Lyber.utils.Constants
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import okhttp3.ResponseBody

abstract class BaseFragment<viewBinding : ViewBinding> : Fragment(), RestClient.OnRetrofitError {

    private var _binding: viewBinding? = null
    private var isSign = false
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
                dismissProgressDialog()
                isSign = false
                if (::bottomDialog.isInitialized)
                    bottomDialog.dismiss()
                resultLauncher.launch(
                    Intent(requireActivity(), WebViewActivity::class.java)
                        .putExtra(Constants.URL, it.data.url)
                )
            }
        }
        viewModel.signUrlResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                dismissProgressDialog()
                isSign = true
                if (::bottomDialog.isInitialized)
                    bottomDialog.dismiss()
                resultLauncher.launch(
                    Intent(requireActivity(), WebViewActivity::class.java)
                        .putExtra(Constants.URL, it.data.url)
                )

            }
        }
        viewModel.logoutResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                dismissProgressDialog()
                dismissAlertDialog()
                CommonMethods.logOut(requireContext())
            }
        }
        return binding.root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                if (isSign) {
                    activity?.runOnUiThread {
                        findNavController().popBackStack(R.id.portfolioHomeFragment, false)
                        showDocumentDialog(App.appContext, Constants.LOADING)
                        Handler(Looper.getMainLooper()).postDelayed({
                            showDocumentDialog(App.appContext, Constants.LOADING_SUCCESS)
                        }, 1500)
                    }
//                    findNavController().popBackStack(R.id.portfolioHomeFragment,false)
//                    showDocumentDialog(requireActivity(), Constants.LOADING)
//                    Handler(Looper.getMainLooper()).postDelayed({
//                        showDocumentDialog(requireActivity(), Constants.LOADING_SUCCESS)
//                    }, 1500)
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

    fun customDialog(code: Int) {
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
                binding.tvNegativeButton.setOnClickListener {
                    dismiss()
                }
                binding.tvPositiveButton.setOnClickListener {
                    CommonMethods.checkInternet(requireContext()) {
                        showProgressDialog(requireContext())
                        if (code == 7023 || code == 10041)
                            viewModel.startKyc()
                        else if (code == 7025 || code == 10043)
                            viewModel.startSignUrl()
                    }
                }
                show()
            }
        }
    }

    private lateinit var dialog: Dialog
    private fun showDocumentDialog(context: Context, typeOfLoader: Int) {
        if (!::dialog.isInitialized) {
            dialog = Dialog(requireContext())
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.window!!.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            dialog.window!!.setDimAmount(0.3F)
            dialog.setCancelable(false)
            dialog.setContentView(DocumentBeingVerifiedBinding.inflate(LayoutInflater.from(context)).root)
        }

        try {
            val viewImage = dialog.findViewById<LottieAnimationView>(R.id.animationView)
            val llVIew = dialog.findViewById<LinearLayout>(R.id.llProgress)
            val tvDocVerified = dialog.findViewById<TextView>(R.id.tvDocVerified)
            val imageView = dialog.findViewById<ImageView>(R.id.ivCorrect)!!
            when (typeOfLoader) {
                Constants.LOADING -> {
                    viewImage!!.playAnimation()
                    viewImage.setMinAndMaxProgress(0f, .32f)
                }

                Constants.LOADING_SUCCESS -> {
                    viewImage.clearAnimation()
                    tvDocVerified.gone()
                    Handler(Looper.getMainLooper()).postDelayed({
                        imageView.visible()
                        imageView.setImageResource(R.drawable.baseline_done_24)
                    }, 50)
                    Handler(Looper.getMainLooper()).postDelayed({
                        dialog.dismiss()
                    }, 400)
                }

            }


            /*(0f,.32f) for loader
            * (0f,.84f) for success
            * (0.84f,1f) for failure*/


            dialog.show()
        } catch (e: WindowManager.BadTokenException) {
            Log.d("Exception", "showProgressDialog: ${e.message}")
            dialog.dismiss()
        } catch (e: Exception) {
            Log.d("Exception", "showProgressDialog: ${e.message}")
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
                    App.prefsManager.logout()
                    context.startActivity(
                        Intent(
                            context,
                            com.Lyber.ui.activities.SplashActivity::class.java
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
}