package com.Lyber.ui.fragments

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.Lyber.R
import com.Lyber.databinding.CustomDialogVerticalLayoutBinding
import com.Lyber.network.RestClient
import com.Lyber.ui.activities.WebViewActivity
import com.Lyber.ui.portfolio.viewModel.PortfolioViewModel
import com.Lyber.utils.CommonMethods
import com.Lyber.utils.CommonMethods.Companion.dismissProgressDialog
import com.Lyber.utils.CommonMethods.Companion.showProgressDialog
import com.Lyber.utils.CommonMethods.Companion.showToast
import com.Lyber.utils.Constants
import okhttp3.ResponseBody

abstract class BaseFragment<viewBinding : ViewBinding> : Fragment(), RestClient.OnRetrofitError {

    private var _binding: viewBinding? = null
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


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
           if (result.resultCode == Activity.RESULT_OK) {
//                portfolioViewModel.finishRegistration()
            }
        }
    override fun onRetrofitError(responseBody: ResponseBody?) {
        dismissProgressDialog()
       val code=  CommonMethods.showErrorMessage(requireContext(), responseBody, binding.root)
        Log.d("errorCode","$code")
        if(code==7023 || code == 10041 || code == 7025 || code == 10043)
           customDialog(code)
    }

    override fun onError() {
        dismissProgressDialog()
        getString(R.string.unable_to_connect_to_the_server).showToast(requireContext())
    }
private lateinit var  dialog:Dialog
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
                        showProgressDialog(requireContext())
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