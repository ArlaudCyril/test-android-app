package com.Lyber.dev.ui.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.Lyber.dev.R
import com.Lyber.dev.databinding.FragmentStrongAuthenticationBinding
import com.Lyber.dev.ui.activities.SplashActivity
import com.Lyber.dev.ui.fragments.bottomsheetfragments.VerificationBottomSheet
import com.Lyber.dev.utils.App
import com.Lyber.dev.utils.CommonMethods
import com.Lyber.dev.utils.CommonMethods.Companion.dismissAlertDialog
import com.Lyber.dev.utils.CommonMethods.Companion.dismissProgressDialog
import com.Lyber.dev.utils.CommonMethods.Companion.encodeToBase64
import com.Lyber.dev.utils.CommonMethods.Companion.getViewModel
import com.Lyber.dev.utils.CommonMethods.Companion.showSnack
import com.Lyber.dev.utils.Constants
import com.Lyber.dev.viewmodels.SignUpViewModel
import com.google.android.gms.tasks.Task
import com.google.android.play.core.integrity.StandardIntegrityManager
import okhttp3.ResponseBody
import org.json.JSONObject

class StrongAuthenticationFragment : BaseFragment<FragmentStrongAuthenticationBinding>(),
    View.OnClickListener {

    private lateinit var viewModel: SignUpViewModel
    private lateinit var clickedOn: String
    private lateinit var clickedSwitch: String
    private lateinit var scopeType: String
    private lateinit var bottomSheet: VerificationBottomSheet


    override fun bind() = FragmentStrongAuthenticationBinding.inflate(layoutInflater)
    private var switchOff: Boolean = false
    private var qrCodeUrl = ""
    var resendCode = -1
    private var resetView = false

    @SuppressLint("SuspiciousIndentation")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = getViewModel(this)
        viewModel.listener = this
        binding.tvEmail.text = "${getString(R.string.to)} ${App.prefsManager.user?.email}"
        if (App.prefsManager.user?.phoneNo!!.contains("+"))
            binding.tvNumber.text = "${getString(R.string.to)} ${App.prefsManager.user?.phoneNo}"
        else
            binding.tvNumber.text = "${getString(R.string.to)} +${App.prefsManager.user?.phoneNo}"
        setView()
        if (App.prefsManager.user?.type2FA != Constants.GOOGLE) {
                CommonMethods.checkInternet(binding.root, requireContext()) {
                   val integrityTokenResponse: Task<StandardIntegrityManager.StandardIntegrityToken>? =
                        SplashActivity.integrityTokenProvider?.request(
                            StandardIntegrityManager.StandardIntegrityTokenRequest.builder()
                                .build()
                        )
                    integrityTokenResponse?.addOnSuccessListener { response ->
                        viewModel.qrCodeUrl(
                            token = response.token()
                        )

                    }?.addOnFailureListener { exception ->
                        Log.d("token", "${exception}")

                    }
                }
            viewModel.qrCodeResponse.observe(viewLifecycleOwner) {
                if (Lifecycle.State.RESUMED == lifecycle.currentState) {
                    qrCodeUrl = it.data.url
                }
            }
        }
//        viewModel.logoutResponse.observe(viewLifecycleOwner){
//            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
//                App.prefsManager.logout()
//                findNavController().popBackStack()
//                findNavController().navigate(R.id.discoveryFragment)
//            }
//        }
        viewModel.booleanResponse.observe(viewLifecycleOwner) {
            if (Lifecycle.State.RESUMED == lifecycle.currentState) {
                dismissAlertDialog()
                if (::scopeType.isInitialized && scopeType == Constants.TYPE) {
                    if (!resetView) {
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

                        val vc = VerificationBottomSheet(::handle)
                        vc.viewToDelete = transparentView
                        vc.mainView = getView()?.rootView as ViewGroup
                        vc.viewModel = viewModel
                        vc.arguments = Bundle().apply {
                            putString(Constants.TYPE, clickedOn)
                        }
                        vc.show(childFragmentManager, App.prefsManager.user?.type2FA)
                        val mainView = getView()?.rootView as ViewGroup
                        mainView.addView(transparentView, viewParams)
                        bottomSheet = vc
                    }
                    resetView = false

                } else if (switchOff) {
                    if (!resetView) {
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

                        val vc = VerificationBottomSheet(::handle)
                        vc.viewToDelete = transparentView
                        vc.mainView = getView()?.rootView as ViewGroup
                        vc.viewModel = viewModel
                        vc.arguments = Bundle().apply {
                            putString("clickedOn", clickedOn)
                        }
                        vc.show(childFragmentManager, App.prefsManager.user?.type2FA)
                        val mainView = getView()?.rootView as ViewGroup
                        mainView.addView(transparentView, viewParams)
                        bottomSheet = vc
                    }
                    resetView = false
                } else {
                        viewModel.getUser()
                }
            }
        }
        viewModel.updateAuthenticateResponse.observe(viewLifecycleOwner) {
            if (Lifecycle.State.RESUMED == lifecycle.currentState) {
                if (::bottomSheet.isInitialized)
                    try {
                        bottomSheet.dismiss()
                    } catch (_: Exception) {

                    }
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
                    CommonMethods.checkInternet(binding.root, requireContext()) {
                        val hash = hashMapOf<String, Any>()
                        if (binding.switchValidateWithdraw.isChecked) hash["scope2FA"] =
                            listOf(Constants.WHITELISTING, Constants.WITHDRAWAL)
                        else hash["scope2FA"] = listOf(Constants.WHITELISTING)
                        clickedSwitch = Constants.WHITELISTING


                        val jsonObject = JSONObject()
                        jsonObject.put("scope2FA", hash["scope2FA"])
                        val jsonString = jsonObject.toString()
                        // Generate the request hash
                        val requestHash = CommonMethods.generateRequestHash(jsonString)

                        val integrityTokenResponse: Task<StandardIntegrityManager.StandardIntegrityToken>? =
                            SplashActivity.integrityTokenProvider?.request(
                                StandardIntegrityManager.StandardIntegrityTokenRequest.builder()
                                    .setRequestHash(requestHash)
                                    .build()
                            )
                        integrityTokenResponse?.addOnSuccessListener { response ->
                            CommonMethods.showProgressDialog(requireContext())
                            viewModel.updateAuthentication(hash, token = response.token())
                        }?.addOnFailureListener { exception ->
                            Log.d("token", "${exception}")

                        }
                    }
                } else {
                    CommonMethods.checkInternet(binding.root, requireContext()) {
                        switchOff = true
                        binding.switchWhitelisting.isChecked = true
                        resendCode = 3
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

                        val jsonObject = JSONObject()
                        jsonObject.put("details", detail)
                        jsonObject.put("action", scopeType)
                        val jsonString = jsonObject.toString()
                        // Generate the request hash
                        val requestHash = CommonMethods.generateRequestHash(jsonString)

                        val integrityTokenResponse: Task<StandardIntegrityManager.StandardIntegrityToken>? =
                            SplashActivity.integrityTokenProvider?.request(
                                StandardIntegrityManager.StandardIntegrityTokenRequest.builder()
                                    .setRequestHash(requestHash)
                                    .build()
                            )
                        integrityTokenResponse?.addOnSuccessListener { response ->
                            CommonMethods.showProgressDialog(requireContext())
                            viewModel.switchOffAuthentication(
                                detail,
                                scopeType,
                                token = response.token()
                            )

                        }?.addOnFailureListener { exception ->
                            Log.d("token", "${exception}")

                        }
                    }
                }
            }
        }
        binding.switchValidateWithdraw.setOnCheckedChangeListener { button, isChecked ->
            if (button.isPressed) {
                if (isChecked) {
                    switchOff = false
                    CommonMethods.checkInternet(binding.root, requireContext()) {
                        val hash = hashMapOf<String, Any>()
                        if (binding.switchWhitelisting.isChecked) hash["scope2FA"] =
                            listOf(Constants.WHITELISTING, Constants.WITHDRAWAL)
                        else hash["scope2FA"] = listOf(Constants.WITHDRAWAL)
                        clickedSwitch = Constants.WITHDRAWAL
                        val jsonObject = JSONObject()
                        jsonObject.put("scope2FA", hash["scope2FA"])
                        val jsonString = jsonObject.toString()
                        // Generate the request hash
                        val requestHash = CommonMethods.generateRequestHash(jsonString)

                        val integrityTokenResponse: Task<StandardIntegrityManager.StandardIntegrityToken>? =
                            SplashActivity.integrityTokenProvider?.request(
                                StandardIntegrityManager.StandardIntegrityTokenRequest.builder()
                                    .setRequestHash(requestHash)
                                    .build()
                            )
                        integrityTokenResponse?.addOnSuccessListener { response ->
                            CommonMethods.showProgressDialog(requireContext())
                            viewModel.updateAuthentication(hash, token = response.token())
                        }?.addOnFailureListener { exception ->
                            Log.d("token", "${exception}")

                        }
                    }
                } else {
                    CommonMethods.checkInternet(binding.root, requireContext()) {
                        switchOff = true
                        resendCode = 4
                        binding.switchValidateWithdraw.isChecked = true
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
                        val jsonObject = JSONObject()
                        jsonObject.put("details", detail)
                        jsonObject.put("action", scopeType)
                        val jsonString = jsonObject.toString()
                        // Generate the request hash
                        val requestHash = CommonMethods.generateRequestHash(jsonString)

                        val integrityTokenResponse: Task<StandardIntegrityManager.StandardIntegrityToken>? =
                            SplashActivity.integrityTokenProvider?.request(
                                StandardIntegrityManager.StandardIntegrityTokenRequest.builder()
                                    .setRequestHash(requestHash)
                                    .build()
                            )
                        integrityTokenResponse?.addOnSuccessListener { response ->
                            CommonMethods.showProgressDialog(requireContext())
                            viewModel.switchOffAuthentication(
                                detail,
                                scopeType,
                                token = response.token()
                            )

                        }?.addOnFailureListener { exception ->
                            Log.d("token", "${exception}")

                        }
                    }
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
                            CommonMethods.checkInternet(binding.root, requireContext()) {
                                CommonMethods.showProgressDialog(requireContext())
                                resendCode = 2
                                var json = """{"type2FA" : "email"}""".trimMargin()
                                val detail = encodeToBase64(json)
                                scopeType = Constants.TYPE
                                clickedOn = Constants.EMAIL
                                val jsonObject = JSONObject()
                                jsonObject.put("details", detail)
                                jsonObject.put("action", scopeType)
                                val jsonString = jsonObject.toString()
                                // Generate the request hash
                                val requestHash = CommonMethods.generateRequestHash(jsonString)

                                val integrityTokenResponse: Task<StandardIntegrityManager.StandardIntegrityToken>? =
                                    SplashActivity.integrityTokenProvider?.request(
                                        StandardIntegrityManager.StandardIntegrityTokenRequest.builder()
                                            .setRequestHash(requestHash)
                                            .build()
                                    )
                                integrityTokenResponse?.addOnSuccessListener { response ->
                                    CommonMethods.showProgressDialog(requireContext())
                                    viewModel.switchOffAuthentication(
                                        detail,
                                        scopeType,
                                        token = response.token()
                                    )

                                }?.addOnFailureListener { exception ->
                                    Log.d("token", "${exception}")

                                }
                            }
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
                            val vc = VerificationBottomSheet(::handle)
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
                            bottomSheet = vc
                        }
                    }
                }

                rlBySms -> {
                    if (App.prefsManager.user?.type2FA != Constants.PHONE) {
                        if (App.prefsManager.user?.type2FA != Constants.GOOGLE) {
                            CommonMethods.checkInternet(binding.root, requireContext()) {
                                resendCode = 1
                                var json = """{"type2FA" : "phone"}""".trimMargin()
                                val detail = encodeToBase64(json)
                                scopeType = Constants.TYPE
                                clickedOn = Constants.PHONE
                                val jsonObject = JSONObject()
                                jsonObject.put("details", detail)
                                jsonObject.put("action", scopeType)
                                val jsonString = jsonObject.toString()
                                // Generate the request hash
                                val requestHash = CommonMethods.generateRequestHash(jsonString)

                                val integrityTokenResponse: Task<StandardIntegrityManager.StandardIntegrityToken>? =
                                    SplashActivity.integrityTokenProvider?.request(
                                        StandardIntegrityManager.StandardIntegrityTokenRequest.builder()
                                            .setRequestHash(requestHash)
                                            .build()
                                    )
                                integrityTokenResponse?.addOnSuccessListener { response ->
                                    CommonMethods.showProgressDialog(requireContext())
                                    viewModel.switchOffAuthentication(
                                        detail,
                                        scopeType,
                                        token = response.token()
                                    )

                                }?.addOnFailureListener { exception ->
                                    Log.d("token", "${exception}")

                                }
                            }
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
                            val vc = VerificationBottomSheet(::handle)
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
                            bottomSheet = vc
                        }
                    }
                }
            }
        }
    }


    override fun onRetrofitError(errorCode: Int, msg: String) {
        dismissProgressDialog()
        dismissAlertDialog()
        when (errorCode) {
            26 -> showSnack(binding.root, requireContext(), getString(R.string.error_code_26))
            37 -> showSnack(binding.root, requireContext(), getString(R.string.error_code_37))
            40 -> showSnack(binding.root, requireContext(), getString(R.string.error_code_40))
            41 -> showSnack(binding.root, requireContext(), getString(R.string.error_code_41))
            44 -> showSnack(binding.root, requireContext(), getString(R.string.error_code_44))
            34 -> {
                if (::bottomSheet.isInitialized)
                    try {
                        bottomSheet.dismiss()
                    } catch (_: Exception) {

                    }
                showSnack(binding.root, requireContext(), getString(R.string.error_code_34))
            }

            35 -> {
                if (::bottomSheet.isInitialized)
                    try {
                        bottomSheet.dismiss()
                    } catch (_: Exception) {

                    }
                showSnack(binding.root, requireContext(), getString(R.string.error_code_35))
            }

            42 -> {
                if (::bottomSheet.isInitialized)
                    try {
                        bottomSheet.dismiss()
                    } catch (_: Exception) {

                    }
                showSnack(binding.root, requireContext(), getString(R.string.error_code_42))
            }

            24 -> bottomSheet.showErrorOnBottomSheet(24)
            18 -> bottomSheet.showErrorOnBottomSheet(18)
            38 -> bottomSheet.showErrorOnBottomSheet(38)
            39 -> bottomSheet.showErrorOnBottomSheet(39)
            43 -> bottomSheet.showErrorOnBottomSheet(43)
            45 -> bottomSheet.showErrorOnBottomSheet(45)
            else -> super.onRetrofitError(errorCode, msg)
        }
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

    fun handle(txt: String) {
        resetView = true
        when (resendCode) {
            1 -> {
                val json = """{"type2FA" : "phone"}""".trimMargin()
                val detail = encodeToBase64(json)
                scopeType = Constants.TYPE
                clickedOn = Constants.PHONE
                hitApi(detail, scopeType)
            }

            2 -> {
                val json = """{"type2FA" : "email"}""".trimMargin()
                val detail = encodeToBase64(json)
                scopeType = Constants.TYPE
                clickedOn = Constants.EMAIL
                hitApi(detail, scopeType)
            }

            3 -> {
                switchOff = true
                binding.switchWhitelisting.isChecked = true
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
                hitApi(detail, scopeType)
            }

            4 -> {
                switchOff = true
                binding.switchValidateWithdraw.isChecked = true
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
                hitApi(detail, scopeType)
            }
        }
    }

    private fun hitApi(detail: String, scope: String) {
        CommonMethods.checkInternet(binding.root, requireContext()) {
            val jsonObject = JSONObject()
            jsonObject.put("details", detail)
            jsonObject.put("action", scope)
            val jsonString = jsonObject.toString()
            // Generate the request hash
            val requestHash = CommonMethods.generateRequestHash(jsonString)

            val integrityTokenResponse: Task<StandardIntegrityManager.StandardIntegrityToken>? =
                SplashActivity.integrityTokenProvider?.request(
                    StandardIntegrityManager.StandardIntegrityTokenRequest.builder()
                        .setRequestHash(requestHash)
                        .build()
                )
            integrityTokenResponse?.addOnSuccessListener { response ->
                CommonMethods.showProgressDialog(requireContext())
                viewModel.switchOffAuthentication(
                    detail,
                    scope,
                    token = response.token()
                )

            }?.addOnFailureListener { exception ->
                Log.d("token", "${exception}")

            }
        }

    }
}