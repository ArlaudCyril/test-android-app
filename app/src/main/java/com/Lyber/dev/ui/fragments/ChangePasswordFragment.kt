package com.Lyber.dev.ui.fragments

import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import com.Lyber.dev.R
import com.Lyber.dev.databinding.FragmentChangePasswordBinding
import com.Lyber.dev.ui.activities.SplashActivity
import com.Lyber.dev.ui.fragments.bottomsheetfragments.VerificationBottomSheet
import com.Lyber.dev.utils.App
import com.Lyber.dev.utils.CommonMethods
import com.Lyber.dev.utils.CommonMethods.Companion.showSnack
import com.Lyber.dev.utils.CommonMethods.Companion.showToast
import com.Lyber.dev.utils.Constants
import com.Lyber.dev.viewmodels.SignUpViewModel
import com.google.android.gms.tasks.Task
import com.google.android.play.core.integrity.StandardIntegrityManager
import com.nimbusds.srp6.SRP6ClientSession
import com.nimbusds.srp6.SRP6CryptoParams
import com.nimbusds.srp6.SRP6VerifierGenerator
import com.nimbusds.srp6.XRoutineWithUserIdentity
import okhttp3.ResponseBody
import org.json.JSONObject
import java.math.BigInteger


class ChangePasswordFragment : BaseFragment<FragmentChangePasswordBinding>(), OnClickListener {
    private lateinit var viewModel: SignUpViewModel
    var buttonClicked = false
    override fun bind() = FragmentChangePasswordBinding.inflate(layoutInflater)
    private lateinit var config: SRP6CryptoParams
    lateinit var generator: SRP6VerifierGenerator
    lateinit var client: SRP6ClientSession
    private var resendCode = false
    private lateinit var bottomSheet: VerificationBottomSheet

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = CommonMethods.getViewModel(this)
        viewModel.listener = this
        config = SRP6CryptoParams.getInstance(2048, "SHA-512")
        generator = SRP6VerifierGenerator(config)
        generator.xRoutine = XRoutineWithUserIdentity()

        client = SRP6ClientSession()
        client.xRoutine = XRoutineWithUserIdentity()
        binding.ivTopAction.setOnClickListener(this)
        binding.btnSavePass.setOnClickListener(this)
        binding.etPasswordOld.addTextChangedListener(onTextChange)
        binding.etPassword.addTextChangedListener(onTextChange)
        binding.etPasswordConfirm.addTextChangedListener(onTextChange)
        viewModel.booleanResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                CommonMethods.dismissProgressDialog()
                CommonMethods.dismissAlertDialog()
                if (!resendCode) {
                    val transparentView = View(context)
                    transparentView.setBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.semi_transparent_dark
                        )
                    )

                    // Set layout parameters for the transparent view
                    val viewParams = RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.MATCH_PARENT
                    )

                    val vc = VerificationBottomSheet(::handle)
                    vc.typeVerification = Constants.CHANGE_PASSWORD
                    vc.viewToDelete = transparentView
                    vc.mainView = getView()?.rootView as ViewGroup
                    vc.viewModel = viewModel
                    vc.show(childFragmentManager, "")

                    // Add the transparent view to the RelativeLayout
                    val mainView = getView()?.rootView as ViewGroup
                    mainView.addView(transparentView, viewParams)
                    bottomSheet = vc
                }
                resendCode = false
            }
        }
        viewModel.exportOperationResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                if (::bottomSheet.isInitialized) {
                    try {
                        bottomSheet.dismiss()
                    } catch (_: Exception) {

                    }
                }
                CommonMethods.dismissProgressDialog()
                getString(R.string.pass_changed_success).showToast(binding.root, requireContext())
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }
        viewModel.changePasswordData.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                client = SRP6ClientSession()
                client.xRoutine = XRoutineWithUserIdentity()
                client.step1(
                    App.prefsManager.user?.email!!.lowercase(),
                    binding.etPasswordOld.text.trim().toString()
                )
                val creds =
                    client.step2(config, it.data.salt.toBigInteger(), it.data.B.toBigInteger())
                val password = binding.etPassword.text.trim().toString()
                val emailSalt = BigInteger(1, generator.generateRandomSalt())
                val emailVerifier = generator.generateVerifier(
                    emailSalt, App.prefsManager.user?.email!!.lowercase(), password
                )
                val phoneSalt = BigInteger(1, generator.generateRandomSalt())
                val phoneVerifier = generator.generateVerifier(
                    phoneSalt, App.prefsManager.user?.phoneNo, password
                )
                val hashMap = hashMapOf<String, Any>()
                hashMap["A"] = creds.A.toString()
                hashMap["M1"] = creds.M1.toString()
                hashMap["emailSalt"] = emailSalt.toString()
                hashMap["emailVerifier"] = emailVerifier.toString()
                hashMap["phoneSalt"] = phoneSalt.toString()
                hashMap["phoneVerifier"] = phoneVerifier.toString()

                val jsonObject = JSONObject()
                jsonObject.put("A", creds.A.toString())
                jsonObject.put("M1", creds.M1.toString())
                jsonObject.put("emailSalt", emailSalt.toString())
                jsonObject.put("emailVerifier", emailVerifier.toString())
                jsonObject.put("phoneSalt", phoneSalt.toString())
                jsonObject.put("phoneVerifier", phoneVerifier.toString())

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
                    viewModel.changePassword(
                        hashMap,
                        token = response.token()
                    )
                }?.addOnFailureListener { exception ->
                    Log.d("token", "${exception}")

                }
            }
        }

    }

    private val onTextChange = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {}
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (binding.etPasswordOld.text.trim().toString().isNotEmpty() &&
                binding.etPassword.text.trim().toString().isNotEmpty() &&
                binding.etPasswordConfirm.text.trim().toString().isNotEmpty()
            ) {
                if (CommonMethods.isValidPassword(binding.etPasswordOld.text.trim().toString())
                    && CommonMethods.isValidPassword(binding.etPassword.text.trim().toString())
                    && CommonMethods.isValidPassword(
                        binding.etPasswordConfirm.text.trim().toString()
                    )
                ) {

                    val colorStateList =
                        ColorStateList.valueOf(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.purple_500
                            )
                        )
                    binding.btnSavePass.backgroundTintList = colorStateList
                    binding.tvPassValidMsg.text = getString(R.string.you_have_strong_password)
                    binding.tvPassValidMsg.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.green_500
                        )
                    )
                    // Set background tint mode to SRC_ATOP
                    binding.btnSavePass.backgroundTintMode = PorterDuff.Mode.SRC_ATOP
                    buttonClicked = true
                } else {
                    val colorStateList =
                        ColorStateList.valueOf(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.purple_gray_600
                            )
                        )
                    binding.btnSavePass.backgroundTintList = colorStateList
                    binding.tvPassValidMsg.text = getString(R.string.pass_valid_msg)
                    binding.tvPassValidMsg.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.red_500
                        )
                    )
                    // Set background tint mode to SRC_ATOP
                    binding.btnSavePass.backgroundTintMode = PorterDuff.Mode.SRC_ATOP
                    buttonClicked = false
                }
            } else
                buttonClicked = false

        }


    }

    private fun handle(txt: String) {
        CommonMethods.checkInternet(binding.root, requireActivity()) {
            val integrityTokenResponse: Task<StandardIntegrityManager.StandardIntegrityToken>? =
                SplashActivity.integrityTokenProvider?.request(
                    StandardIntegrityManager.StandardIntegrityTokenRequest.builder()
                        .build()
                )
            integrityTokenResponse?.addOnSuccessListener { response ->
                resendCode = true
                viewModel.getPasswordChangeChallenge(
                    token = response.token()
                )
            }?.addOnFailureListener { exception ->
                Log.d("token", "${exception}")

            }
        }

    }

    override fun onClick(v: View?) {
        binding.apply {
            when (v) {
                ivTopAction -> requireActivity().onBackPressedDispatcher.onBackPressed()
                btnSavePass -> {
                    if (buttonClicked) {
                        if (binding.etPasswordOld.text.trim().toString()
                                .equals(binding.etPassword.text.trim().toString())
                        ) {
                            getString(R.string.new_pass_cannot_be_same_as_old).showToast(
                                binding.root,
                                requireContext()
                            )
                        } else if (!binding.etPassword.text.trim().toString()
                                .equals(binding.etPasswordConfirm.text.trim().toString())
                        ) {
                            getString(R.string.pass_should_be_same).showToast(
                                binding.root,
                                requireContext()
                            )
                        } else {
                            CommonMethods.checkInternet(binding.root, requireActivity()) {
                                val integrityTokenResponse: Task<StandardIntegrityManager.StandardIntegrityToken>? =
                                    SplashActivity.integrityTokenProvider?.request(
                                        StandardIntegrityManager.StandardIntegrityTokenRequest.builder()
                                            .build()
                                    )
                                integrityTokenResponse?.addOnSuccessListener { response ->
                                    CommonMethods.showProgressDialog(requireContext())
                                    viewModel.getPasswordChangeChallenge(
                                        token = response.token()
                                    )
                                }?.addOnFailureListener { exception ->
                                    Log.d("token", "${exception}")

                                }
                            }

                        }
                    }
                }

            }
        }

    }

    override fun onRetrofitError(errorCode: Int, msg: String) {
        CommonMethods.dismissProgressDialog()
        CommonMethods.dismissAlertDialog()
        when (errorCode) {
            13 -> showSnack(binding.root, requireContext(), getString(R.string.error_code_13))
            14 -> showSnack(binding.root, requireContext(), getString(R.string.error_code_14))
            18 -> bottomSheet.showErrorOnBottomSheet(18)
            26 -> {
                if (::bottomSheet.isInitialized) {
                    try {
                        bottomSheet.dismiss()
                    } catch (_: Exception) {

                    }
                }
                showSnack(binding.root, requireContext(), getString(R.string.error_code_26))
            }

            34 -> {
                if (::bottomSheet.isInitialized) {
                    try {
                        bottomSheet.dismiss()
                    } catch (_: Exception) {

                    }
                }
                showSnack(binding.root, requireContext(), getString(R.string.error_code_34))
            }

            35 -> {
                if (::bottomSheet.isInitialized) {
                    try {
                        bottomSheet.dismiss()
                    } catch (_: Exception) {

                    }
                }
                showSnack(binding.root, requireContext(), getString(R.string.error_code_35))
            }

            37 -> showSnack(binding.root, requireContext(), getString(R.string.error_code_37))
            40 -> showSnack(binding.root, requireContext(), getString(R.string.error_code_40))
            41 -> showSnack(binding.root, requireContext(), getString(R.string.error_code_41))
            42 -> {
                if (::bottomSheet.isInitialized) {
                    try {
                        bottomSheet.dismiss()
                    } catch (_: Exception) {

                    }
                }
                showSnack(binding.root, requireContext(), getString(R.string.error_code_42))
            }

            24 -> bottomSheet.showErrorOnBottomSheet(24)
            38 -> bottomSheet.showErrorOnBottomSheet(38)
            39 -> bottomSheet.showErrorOnBottomSheet(39)
            43 -> bottomSheet.showErrorOnBottomSheet(43)
            45 -> bottomSheet.showErrorOnBottomSheet(45)
            else -> super.onRetrofitError(errorCode, msg)
        }
    }

}