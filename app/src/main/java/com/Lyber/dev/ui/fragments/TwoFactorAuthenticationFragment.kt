package com.Lyber.dev.ui.fragments

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import com.Lyber.dev.R
import com.Lyber.dev.databinding.CustomDialogVerticalLayoutBinding
import com.Lyber.dev.databinding.DownloadGoogleAuthenticatorBinding
import com.Lyber.dev.databinding.FragmentTwoFactorAuthenticationBinding
import com.Lyber.dev.ui.fragments.bottomsheetfragments.VerificationBottomSheet
import com.Lyber.dev.utils.App
import com.Lyber.dev.utils.CommonMethods
import com.Lyber.dev.utils.CommonMethods.Companion.dismissAlertDialog
import com.Lyber.dev.utils.CommonMethods.Companion.showToast
import com.Lyber.dev.utils.Constants
import com.Lyber.dev.viewmodels.SignUpViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import okhttp3.ResponseBody
import java.net.MalformedURLException


class TwoFactorAuthenticationFragment : BaseFragment<FragmentTwoFactorAuthenticationBinding>(),
    OnClickListener {
    override fun bind() = FragmentTwoFactorAuthenticationBinding.inflate(layoutInflater)
    var qrCodeUrl = ""
    private var isResend = false

    companion object {
        var showOtp = true
    }

    private lateinit var viewModel: SignUpViewModel
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = CommonMethods.getViewModel(this)
        viewModel.listener = this
//        viewModel.logoutResponse.observe(viewLifecycleOwner){
//            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
//                App.prefsManager.logout()
//                findNavController().popBackStack()
//                findNavController().navigate(R.id.discoveryFragment)
//            }
//        }
        if (arguments != null && requireArguments().containsKey("QrCode")) {
            var url = requireArguments().getString("QrCode")
            if (url!!.isNotEmpty()) {
//                qrCodeUrl = url.removePrefix("otpauth:")
                qrCodeUrl = url
                binding.ivQrCode.setImageBitmap(
                    getQrCodeBitmap(
                        qrCodeUrl,
                        768
                    )
                )
            } else {
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
                showOtp = false
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
        customDialog1()
        binding.tvAddGoogleAuthenticator.setOnClickListener(this)
        binding.btnVerify.setOnClickListener(this)
        binding.ivTopAction.setOnClickListener(this)
        viewModel.booleanResponse.observe(viewLifecycleOwner) {
            if (Lifecycle.State.RESUMED == lifecycle.currentState) {
                dismissAlertDialog()
                if (!isResend) {
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
                    val vc = VerificationBottomSheet(::handle)
                    vc.viewToDelete = transparentView
                    vc.mainView = getView()?.rootView as ViewGroup
                    vc.viewModel = viewModel
                    vc.arguments = Bundle().apply {
                        putString(Constants.TYPE, Constants.GOOGLE)
                        putBoolean(Constants.GOOGLE, true)
                    }
                    vc.show(childFragmentManager, App.prefsManager.user?.type2FA)
                    val mainView = getView()?.rootView as ViewGroup
                    mainView.addView(transparentView, viewParams)
                }
                isResend = false
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
        dismissAlertDialog()
        if (showOtp) {
            CommonMethods.checkInternet(requireContext()) {
                isResend=true
                CommonMethods.showProgressDialog(requireContext())
                var json = """{"type2FA" : "google"}""".trimMargin()
                val detail = CommonMethods.encodeToBase64(json)
                viewModel.switchOffAuthentication(detail, Constants.TYPE)
            }
            showOtp = false
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
            val vc = VerificationBottomSheet(::handle)
            vc.viewToDelete = transparentView
            vc.mainView = getView()?.rootView as ViewGroup
            vc.viewModel = viewModel
            vc.arguments = Bundle().apply {
                putString(Constants.TYPE, Constants.GOOGLE)
                putBoolean(Constants.GOOGLE, true)
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
                    if (isAuthenticatorAppInstalled()) {
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
                    } else
                        getString(R.string.you_must_install_authenticator).showToast(requireContext())

                }

                btnVerify -> {
                    CommonMethods.checkInternet(requireContext()) {
                        showOtp = false
                        CommonMethods.showProgressDialog(requireContext())
                        var json = """{"type2FA" : "google"}""".trimMargin()
                        val detail = CommonMethods.encodeToBase64(json)
                        viewModel.switchOffAuthentication(detail, Constants.TYPE)
                    }
                }

                ivTopAction -> {
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
            }
        }
    }

    private lateinit var bottomDialog: BottomSheetDialog

    fun customDialog1() {
        bottomDialog = BottomSheetDialog(requireContext(), R.style.CustomDialogBottomSheet).apply {
            DownloadGoogleAuthenticatorBinding.inflate(layoutInflater).let { binding ->
                val dimmedBackgroundColor = Color.argb(82,0,0,0)//alpha = .32F

//                window?.apply {
//                    setDimAmount(0f)// remove dimmed back on older devices
//                    decorView.setBackgroundColor(dimmedBackgroundColor)
//                }
                setContentView(binding.root)

                binding.tvAddGoogleAuthenticator.setOnClickListener {
                    val appPackageName =
                        "com.google.android.apps.authenticator2" // Package name of Google Authenticator app

                    try {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("market://details?id=$appPackageName")
                        )
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                    } catch (e: android.content.ActivityNotFoundException) {
                        // If Google Play Store app is not installed, open the web browser
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")
                        )
                        startActivity(intent)
                    }
                }
                binding.tvPositiveButton.setOnClickListener {
                    dismiss()
                }
                show()
            }
        }
    }
    fun handle(tx: String) {
        isResend = true
        CommonMethods.checkInternet(requireContext()) {
            showOtp = false
            var json = """{"type2FA" : "google"}""".trimMargin()
            val detail = CommonMethods.encodeToBase64(json)
            viewModel.switchOffAuthentication(detail, Constants.TYPE)
        }
    }
}