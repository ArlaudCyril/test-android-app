
package com.Lyber.ui.fragments.bottomsheetfragments

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
import androidx.lifecycle.Lifecycle
import androidx.viewbinding.ViewBinding
import com.Lyber.R
import com.Lyber.databinding.CustomDialogVerticalLayoutBinding
import com.Lyber.databinding.DocumentBeingVerifiedBinding
import com.Lyber.network.RestClient
import com.Lyber.ui.activities.WebViewActivity
import com.Lyber.utils.CommonMethods
import com.Lyber.utils.CommonMethods.Companion.gone
import com.Lyber.utils.CommonMethods.Companion.showToast
import com.Lyber.utils.CommonMethods.Companion.visible
import com.Lyber.utils.Constants
import com.Lyber.utils.LoaderObject
import com.Lyber.viewmodels.PortfolioViewModel
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

abstract class BaseBottomSheet<viewBinding : ViewBinding> : BottomSheetDialogFragment(),
    RestClient.OnRetrofitError {
    private var _binding: viewBinding? = null
    private var isSign = false
    val binding get() = _binding!!

    abstract fun bind(): viewBinding
    private lateinit var viewModel: PortfolioViewModel

    val behavior get() = BottomSheetBehavior.from(requireView().parent as View)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().overridePendingTransition(R.anim.enter_from_bottom, R.anim.exit_from_top)
        setStyle(STYLE_NORMAL, R.style.CustomDialogBottomSheet)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = bind()
        viewModel = CommonMethods.getViewModel(requireActivity())

        viewModel.kycResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                isSign = false
                CommonMethods.dismissProgressDialog()
                if (::bottomDialog.isInitialized)
                    bottomDialog.dismiss()
                resultLauncher.launch(
                    Intent(requireActivity(), WebViewActivity::class.java)
                        .putExtra(Constants.URL, it.data.url)
                        .putExtra(Constants.ASK_PERMISSION, true)
                )
            }
        }
        viewModel.signUrlResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                Handler(Looper.getMainLooper()).postDelayed(
                    {
                        CommonMethods.dismissProgressDialog()
                        isSign = true
                        if (::bottomDialog.isInitialized)
                            bottomDialog.dismiss()
                        resultLauncher.launch(
                            Intent(requireActivity(), WebViewActivity::class.java)
                                .putExtra(Constants.URL, it.data.url)
//                                .putExtra(Constants.ASK_PERMISSION, true)
                        )
                    }, 1000
                )


            }
        }
        viewModel.logoutResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                CommonMethods.logOut(requireContext())

            }
        }
        return binding.root
    }

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                if (isSign) {
                    showDocumentDialog(requireActivity(), Constants.LOADING)
                    Handler(Looper.getMainLooper()).postDelayed({
                        showDocumentDialog(requireActivity(), Constants.LOADING_SUCCESS)
                    }, 1500)
                }
            }
        }

    override fun onRetrofitError(errorCode: Int, msg: String) {
        CommonMethods.dismissProgressDialog()
        CommonMethods.dismissAlertDialog()
        LoaderObject.hideLoader()
        handleCommonErrors(errorCode, msg)
    }
    private fun handleCommonErrors(errorCode: Int, msg: String) {
        when (errorCode) {
            7023, 10041 -> customDialog(7023)
            7025, 10043 -> customDialog(7025)
            else -> CommonMethods.showError(errorCode, requireContext(), msg, binding.root)
        }
    }
    override fun onError() {
        CommonMethods.dismissProgressDialog()
        "Error occurred!".showToast(binding.root,requireContext())
    }

    private lateinit var bottomDialog: BottomSheetDialog
    private fun customDialog(code: Int) {
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
                    CommonMethods.checkInternet(binding.root,requireContext()) {
                        CommonMethods.showProgressDialog(requireContext())
                        if (code == 7023 || code == 10041) {
                            viewModel.startKyc()

                        }
                        else if (code == 7025 || code == 10043) {
                            viewModel.startSignUrl()
                        }
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
}
