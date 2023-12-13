package com.au.lyber.ui.fragments

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import com.au.lyber.R
import com.au.lyber.databinding.FragmentTwoFactorAuthenticationBinding
import com.au.lyber.ui.fragments.bottomsheetfragments.VerificationBottomSheet
import com.au.lyber.utils.App
import com.au.lyber.utils.CommonMethods
import com.au.lyber.utils.CommonMethods.Companion.showToast
import com.au.lyber.utils.Constants
import com.au.lyber.viewmodels.SignUpViewModel
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import okhttp3.ResponseBody
import java.net.MalformedURLException


class TwoFactorAuthenticationFragment : BaseFragment<FragmentTwoFactorAuthenticationBinding>(),
    OnClickListener {
    override fun bind() = FragmentTwoFactorAuthenticationBinding.inflate(layoutInflater)
    var qrCodeUrl = ""

    companion object{
        var showOtp=true
    }
    private lateinit var viewModel: SignUpViewModel
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = CommonMethods.getViewModel(this)
        viewModel.listener = this
        if (arguments != null && requireArguments().containsKey("QrCode")) {
            var url = requireArguments().getString("QrCode")
            if(url!!.isNotEmpty())
            {
//                qrCodeUrl = url.removePrefix("otpauth:")
                qrCodeUrl = url
                binding.ivQrCode.setImageBitmap(
                    getQrCodeBitmap(
                        qrCodeUrl,
                        768
                    )
                )
            }else{
                viewModel.qrCodeUrl()

            }
        }
        viewModel.qrCodeResponse.observe(viewLifecycleOwner) {
            if (Lifecycle.State.RESUMED == lifecycle.currentState) {
                qrCodeUrl = it.data.url
                binding.ivQrCode.setImageBitmap(
                    getQrCodeBitmap(
                        qrCodeUrl,
                        768
                    )
                )
            }
        }
        viewModel.updateAuthenticateResponse.observe(viewLifecycleOwner) {
            if (Lifecycle.State.RESUMED == lifecycle.currentState) {
                showOtp=false
                viewModel.getUser()

            }
        }
        viewModel.getUserResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                CommonMethods.dismissProgressDialog()
                App.prefsManager.user = it.data
              requireActivity().onBackPressed()

            }
        }
        binding.tvAddGoogleAuthenticator.setOnClickListener(this)
        binding.btnVerify.setOnClickListener(this)
        viewModel.booleanResponse.observe(viewLifecycleOwner) {
            if (Lifecycle.State.RESUMED == lifecycle.currentState) {
                   val transparentView = View(context)
                    transparentView.setBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(), R.color.semi_transparent_dark
                        )
                    )
                    CommonMethods.dismissProgressDialog()
                    // Set layout parameters for the transparent view
                    val viewParams = RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.MATCH_PARENT
                    )
                    val vc = VerificationBottomSheet()
                    vc.viewToDelete = transparentView
                    vc.mainView = getView()?.rootView as ViewGroup
                    vc.viewModel = viewModel
                    vc.arguments = Bundle().apply {
                        putString(Constants.TYPE, Constants.GOOGLE)
                    }
                    vc.show(childFragmentManager, App.prefsManager.user?.type2FA)
                    val mainView = getView()?.rootView as ViewGroup
                    mainView.addView(transparentView, viewParams)

            }
        }

    }

    private fun getQrCodeBitmap(link: String, size: Int): Bitmap {
        val size = size //pixels
        val qrCodeContent = "$link"
        val bits = QRCodeWriter().encode(qrCodeContent, BarcodeFormat.QR_CODE, size, size)
        return Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888).also {
            for (x in 0 until size) {
                for (y in 0 until size) {
                    it.setPixel(x, y, if (bits[x, y]) Color.BLACK else Color.TRANSPARENT)
                }
            }
        }
    }

    override fun onRetrofitError(responseBody: ResponseBody?) {
        super.onRetrofitError(responseBody)
        if(showOtp) {
            showOtp=false
            val transparentView = View(context)
            transparentView.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(), R.color.semi_transparent_dark
                )
            )
            CommonMethods.dismissProgressDialog()
            // Set layout parameters for the transparent view
            val viewParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT
            )
            val vc = VerificationBottomSheet()
            vc.viewToDelete = transparentView
            vc.mainView = getView()?.rootView as ViewGroup
            vc.viewModel = viewModel
            vc.arguments = Bundle().apply {
                putString(Constants.TYPE, Constants.GOOGLE)
            }
            vc.show(childFragmentManager, App.prefsManager.user?.type2FA)
            val mainView = getView()?.rootView as ViewGroup
            mainView.addView(transparentView, viewParams)
        }

    }


    fun isAuthenticatorAppInstalled(): Boolean {
           val intent = Intent(Intent.ACTION_VIEW, Uri.parse("otpauth://"))
            val activities = requireActivity().packageManager.queryIntentActivities(intent, 0)
            return activities.isNotEmpty()

    }
    override fun onClick(v: View?) {
        binding.apply {
            when (v!!) {
                tvAddGoogleAuthenticator -> {
                    if(isAuthenticatorAppInstalled()){
                        qrCodeUrl.let { urlString ->
                            try {
                                val uriString = urlString
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uriString))

                                try {
                                    startActivity(intent)
                                } catch (e: ActivityNotFoundException) {
                                    e.printStackTrace()
                                }
                            } catch (e: MalformedURLException) {
                                // Handle the case where the URL is malformed
                                e.printStackTrace()
                            }
                        }
                    }else
                        getString(R.string.you_must_install_authenticator).showToast(requireContext())

                }
                btnVerify->{
                    CommonMethods.checkInternet(requireContext()) {
                        showOtp=false
                       CommonMethods.showProgressDialog(requireContext())
                        var  json = """{"type2FA" : "google"}""".trimMargin()
                        val detail = CommonMethods.encodeToBase64(json)
                        viewModel.switchOffAuthentication(detail,Constants.TYPE)
                    }
                }
            }
        }
    }
}