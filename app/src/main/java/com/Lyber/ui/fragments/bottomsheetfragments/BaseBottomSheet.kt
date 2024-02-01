package com.Lyber.ui.fragments.bottomsheetfragments

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.Lifecycle
import androidx.viewbinding.ViewBinding
import com.Lyber.R
import com.Lyber.databinding.CustomDialogVerticalLayoutBinding
import com.Lyber.network.RestClient
import com.Lyber.ui.activities.WebViewActivity
import com.Lyber.ui.portfolio.viewModel.PortfolioViewModel
import com.Lyber.utils.CommonMethods
import com.Lyber.utils.CommonMethods.Companion.showToast
import com.Lyber.utils.Constants
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import okhttp3.ResponseBody

abstract class BaseBottomSheet<viewBinding : ViewBinding> : BottomSheetDialogFragment(),
    RestClient.OnRetrofitError {
    private var _binding: viewBinding? = null
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
                CommonMethods.dismissProgressDialog()
                if(::dialog.isInitialized)
                    dialog.dismiss()
                resultLauncher.launch(
                    Intent(requireActivity(), WebViewActivity::class.java)
                        .putExtra(Constants.URL, it.data.url)
                )
            }
        }
        return binding.root
    }
    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
//                portfolioViewModel.finishRegistration()
            }
        }
    override fun onRetrofitError(responseBody: ResponseBody?) {
        CommonMethods.dismissProgressDialog()
        val code=  CommonMethods.showErrorMessage(requireContext(), responseBody, binding.root)
        Log.d("errorCode","$code")
        if(code==7023 || code == 10041 || code == 7025 || code == 10043)
            customDialog(code)
    }

    override fun onError() {
        CommonMethods.dismissProgressDialog()
        "Error occurred!".showToast(requireContext())
    }

    private lateinit var  dialog: Dialog
     fun customDialog(code: Int) {
        dialog= Dialog(requireContext(), R.style.DialogTheme).apply {
            CustomDialogVerticalLayoutBinding.inflate(layoutInflater).let {
                requestWindowFeature(Window.FEATURE_NO_TITLE)
                setCancelable(false)
                setCanceledOnTouchOutside(false)
                setContentView(it.root)
                if (code == 7023 || code == 10041) {
                    it.tvTitle.text = context.getString(R.string.validate_kyc)
                    it.tvMessage.text = context.getString(R.string.please_validate_kyc)
                    it.tvNegativeButton.text = context.getString(R.string.cancel)
                    it.tvPositiveButton.text = context.getString(R.string.validate_kyc)
                } else if (code == 7025 || code == 10043) {
                    it.tvTitle.text = context.getString(R.string.sign_my_contract)
                    it.tvMessage.text = context.getString(R.string.contract_has_not_signed)
                    it.tvNegativeButton.text = context.getString(R.string.cancel)
                    it.tvPositiveButton.text = context.getString(R.string.sign_my_contract)
                }
                it.tvNegativeButton.setOnClickListener {
                    dismiss()
                }
                it.tvPositiveButton.setOnClickListener {
                    CommonMethods.checkInternet(requireContext()){
                        CommonMethods.showProgressDialog(requireContext())
                        if (code == 7023 || code == 10041)
                            viewModel.startKyc()
                        else if (code == 7025 || code == 10043)
                            viewModel
                    }

                }
                show()
            }
        }
    }
}