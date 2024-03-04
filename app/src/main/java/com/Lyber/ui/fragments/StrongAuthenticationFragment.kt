package com.Lyber.ui.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.Lyber.R
import com.Lyber.databinding.FragmentStrongAuthenticationBinding
import com.Lyber.ui.fragments.bottomsheetfragments.VerificationBottomSheet
import com.Lyber.utils.App
import com.Lyber.utils.CommonMethods
import com.Lyber.utils.CommonMethods.Companion.dismissProgressDialog
import com.Lyber.utils.CommonMethods.Companion.encodeToBase64
import com.Lyber.utils.CommonMethods.Companion.getViewModel
import com.Lyber.utils.Constants
import com.Lyber.viewmodels.SignUpViewModel
import okhttp3.ResponseBody

class StrongAuthenticationFragment : BaseFragment<FragmentStrongAuthenticationBinding>(),
    View.OnClickListener {

    private lateinit var viewModel: SignUpViewModel
    private lateinit var clickedOn: String
    private lateinit var clickedSwitch: String
    private lateinit var scopeType: String


    override fun bind() = FragmentStrongAuthenticationBinding.inflate(layoutInflater)
    private var switchOff: Boolean = false
    private var qrCodeUrl = ""

    @SuppressLint("SuspiciousIndentation")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = getViewModel(this)
        viewModel.listener = this
        binding.tvEmail.text = "${getString(R.string.to)} ${App.prefsManager.user?.email}"
        if(App.prefsManager.user?.phoneNo!!.contains("+"))
        binding.tvNumber.text = "${getString(R.string.to)} ${App.prefsManager.user?.phoneNo}"
        else
        binding.tvNumber.text = "${getString(R.string.to)} +${App.prefsManager.user?.phoneNo}"
        setView()

        if (App.prefsManager.user?.type2FA != Constants.GOOGLE) {
            viewModel.qrCodeUrl()
            viewModel.qrCodeResponse.observe(viewLifecycleOwner) {
                if (Lifecycle.State.RESUMED == lifecycle.currentState) {
                    qrCodeUrl = it.data.url
                }
            }
        }

        viewModel.booleanResponse.observe(viewLifecycleOwner) {
            if (Lifecycle.State.RESUMED == lifecycle.currentState) {
                if (::scopeType.isInitialized && scopeType == Constants.TYPE) {

                    val transparentView = View(context)
                    transparentView.setBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(), R.color.semi_transparent_dark
                        )
                    )
                    dismissProgressDialog()
                    switchOff = false
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
                        putString(Constants.TYPE, clickedOn)
                    }
                    vc.show(childFragmentManager, App.prefsManager.user?.type2FA)
                    val mainView = getView()?.rootView as ViewGroup
                    mainView.addView(transparentView, viewParams)

                } else if (switchOff) {
                    val transparentView = View(context)
                    transparentView.setBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(), R.color.semi_transparent_dark
                        )
                    )
                    dismissProgressDialog()
                    switchOff = false
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
                        putString("clickedOn", clickedOn)
                    }
                    vc.show(childFragmentManager, App.prefsManager.user?.type2FA)
                    val mainView = getView()?.rootView as ViewGroup
                    mainView.addView(transparentView, viewParams)
                } else {
                    viewModel.getUser()
                }
            }
        }
        viewModel.updateAuthenticateResponse.observe(viewLifecycleOwner) {
            if (Lifecycle.State.RESUMED == lifecycle.currentState) {
                viewModel.getUser()

            }
        }
        viewModel.getUserResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                dismissProgressDialog()
                App.prefsManager.user = it.data
                setView()

            }
        }

        binding.switchWhitelisting.setOnCheckedChangeListener { button, isChecked ->
            if (button.isPressed) {
                if (isChecked) {
                    switchOff = false
                    CommonMethods.showProgressDialog(requireContext())
                    val hash = hashMapOf<String, Any>()
                    if (binding.switchValidateWithdraw.isChecked) hash["scope2FA"] =
                        listOf(Constants.WHITELISTING, Constants.WITHDRAWAL)
                    else hash["scope2FA"] = listOf(Constants.WHITELISTING)
                    clickedSwitch = Constants.WHITELISTING
                    viewModel.updateAuthentication(hash)
                } else {
                    switchOff = true
                    binding.switchWhitelisting.isChecked = true

                    CommonMethods.showProgressDialog(requireContext())
                    var json = ""
                    if (binding.switchValidateWithdraw.isChecked) {
                        json = """{"scope2FA":["withdrawal"]}""".trimMargin()
                        clickedOn = Constants.WITHDRAWAL
                    } else {
                        json = """{"scope2FA":[]}""".trimMargin()
                        clickedOn = ""
                    }
                    val detail = encodeToBase64(json)
                    scopeType = Constants.SCOPE
                    viewModel.switchOffAuthentication(detail, scopeType)
                }
            }
        }
        binding.switchValidateWithdraw.setOnCheckedChangeListener { button, isChecked ->
            if (button.isPressed) {
                if (isChecked) {
                    switchOff = false
                    CommonMethods.showProgressDialog(requireContext())
                    val hash = hashMapOf<String, Any>()
                    if (binding.switchWhitelisting.isChecked) hash["scope2FA"] =
                        listOf(Constants.WHITELISTING, Constants.WITHDRAWAL)
                    else hash["scope2FA"] = listOf(Constants.WITHDRAWAL)
                    clickedSwitch = Constants.WITHDRAWAL
                    viewModel.updateAuthentication(hash)
                } else {
                    switchOff = true
                    binding.switchValidateWithdraw.isChecked = true
                    CommonMethods.showProgressDialog(requireContext())
                    var json = ""
                    if (binding.switchWhitelisting.isChecked) {
                        json = """{"scope2FA":["whitelisting"]}""".trimMargin()
                        clickedOn = Constants.WHITELISTING
                    } else {
                        json = """{"scope2FA":[]}""".trimMargin()
                        clickedOn = ""
                    }
                    val detail = encodeToBase64(json)
                    scopeType = Constants.SCOPE
                    viewModel.switchOffAuthentication(detail, scopeType)
                }
            }
        }

        binding.ivTopAction.setOnClickListener(this)
        binding.rlGoogle.setOnClickListener(this)
        binding.rlEmail.setOnClickListener(this)
        binding.rlBySms.setOnClickListener(this)
    }

    private fun setView() {
        if (App.prefsManager.user?.scope2FA != null && App.prefsManager.user?.scope2FA!!.isNotEmpty()) {
            binding.switchWhitelisting.isChecked =
                App.prefsManager.user?.scope2FA!!.contains(Constants.WHITELISTING)
            binding.switchValidateWithdraw.isChecked =
                App.prefsManager.user?.scope2FA!!.contains(Constants.WITHDRAWAL)
            binding.switchLoginAcc.isChecked = true

            if (App.prefsManager.user?.type2FA != null && App.prefsManager.user?.type2FA!!.isNotEmpty()) {
                if (App.prefsManager.user?.type2FA == Constants.EMAIL) {
                    binding.ivEmail.visibility = View.VISIBLE
                    binding.ivSms.visibility = View.GONE
                    binding.ivGoogle.visibility = View.GONE
                    binding.ivGoogleAuth.visibility = View.VISIBLE
                } else if (App.prefsManager.user?.type2FA == Constants.PHONE) {
                    binding.ivSms.visibility = View.VISIBLE
                    binding.ivEmail.visibility = View.GONE
                    binding.ivGoogle.visibility = View.GONE
                    binding.ivGoogleAuth.visibility = View.VISIBLE
                } else {
                    binding.ivGoogle.visibility = View.VISIBLE
                    binding.ivGoogleAuth.visibility = View.GONE
                    binding.ivSms.visibility = View.GONE
                    binding.ivEmail.visibility = View.GONE
                }
            }
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
                ivTopAction -> requireActivity().onBackPressed()
                rlGoogle -> {
                    if (App.prefsManager.user?.type2FA != Constants.GOOGLE) {
                        var args = Bundle().apply {
                            putString("QrCode", qrCodeUrl)
                        }
                        findNavController().navigate(R.id.twoFactorAuthentication, args)
                    }
                }

                rlEmail -> {
                    if (App.prefsManager.user?.type2FA != Constants.EMAIL) {
                        if (App.prefsManager.user?.type2FA != Constants.GOOGLE) {
                            CommonMethods.showProgressDialog(requireContext())
                            var json = """{"type2FA" : "email"}""".trimMargin()
                            val detail = encodeToBase64(json)
                            scopeType = Constants.TYPE
                            clickedOn = Constants.EMAIL
                            viewModel.switchOffAuthentication(detail, scopeType)
                        } else {
                            val transparentView = View(context)
                            transparentView.setBackgroundColor(
                                ContextCompat.getColor(
                                    requireContext(), R.color.semi_transparent_dark
                                )
                            )
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
                                putString("changeType", Constants.EMAIL)
                            }
                            vc.show(childFragmentManager, App.prefsManager.user?.type2FA)
                            val mainView = getView()?.rootView as ViewGroup
                            mainView.addView(transparentView, viewParams)
                        }
                    }
                }

                rlBySms -> {
                    if (App.prefsManager.user?.type2FA != Constants.PHONE) {
                        if (App.prefsManager.user?.type2FA != Constants.GOOGLE) {
                            CommonMethods.showProgressDialog(requireContext())
                            var json = """{"type2FA" : "phone"}""".trimMargin()
                            val detail = encodeToBase64(json)
                            scopeType = Constants.TYPE
                            clickedOn = Constants.PHONE
                            viewModel.switchOffAuthentication(detail, scopeType)
                        } else {
                            val transparentView = View(context)
                            transparentView.setBackgroundColor(
                                ContextCompat.getColor(
                                    requireContext(), R.color.semi_transparent_dark
                                )
                            )
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
                                putString("changeType", Constants.PHONE)
                            }
                            vc.show(childFragmentManager, App.prefsManager.user?.type2FA)
                            val mainView = getView()?.rootView as ViewGroup
                            mainView.addView(transparentView, viewParams)

                        }
                    }
                }
            }
        }
    }


    override fun onRetrofitError(responseBody: ResponseBody?) {
        super.onRetrofitError(responseBody)
        if (App.prefsManager.user?.scope2FA != null && App.prefsManager.user?.scope2FA!!.isNotEmpty())
            for (i in App.prefsManager.user?.scope2FA!!) when (i) {
                Constants.LOGIN -> binding.switchLoginAcc.isChecked = true
                Constants.WITHDRAWAL -> binding.switchValidateWithdraw.isChecked = true
                Constants.WHITELISTING -> binding.switchWhitelisting.isChecked = true
            }
        if (!switchOff) {
            if (clickedSwitch == Constants.WHITELISTING)
                binding.switchWhitelisting.isChecked = false
            else if (clickedSwitch == Constants.WITHDRAWAL)
                binding.switchValidateWithdraw.isChecked = false
        }


    }

}