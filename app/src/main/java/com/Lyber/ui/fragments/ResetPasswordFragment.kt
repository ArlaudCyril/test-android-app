package com.Lyber.ui.fragments

import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.Lyber.R
import com.Lyber.databinding.FragmentResetPasswordBinding
import com.Lyber.ui.activities.SplashActivity
import com.Lyber.utils.App
import com.Lyber.utils.CommonMethods
import com.Lyber.utils.CommonMethods.Companion.sortAndFormatJson
import com.Lyber.utils.Constants
import com.Lyber.viewmodels.SignUpViewModel
import com.google.android.gms.tasks.Task
import com.google.android.play.core.integrity.StandardIntegrityManager
import com.nimbusds.srp6.SRP6ClientSession
import com.nimbusds.srp6.SRP6CryptoParams
import com.nimbusds.srp6.SRP6VerifierGenerator
import com.nimbusds.srp6.XRoutineWithUserIdentity
import org.json.JSONObject
import java.math.BigInteger


class ResetPasswordFragment : BaseFragment<FragmentResetPasswordBinding>(), OnClickListener {
    private lateinit var viewModel: SignUpViewModel
    var buttonClicked = false
    override fun bind() = FragmentResetPasswordBinding.inflate(layoutInflater)
    private lateinit var config: SRP6CryptoParams
    lateinit var generator: SRP6VerifierGenerator
    lateinit var client: SRP6ClientSession
    var resetToken = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            resetToken = it.getString("resetToken", "") ?: ""
            Log.d("token", "$resetToken")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = CommonMethods.getViewModel(this)
        viewModel.listener = this
        config = SRP6CryptoParams.getInstance(2048, "SHA-512")
        generator = SRP6VerifierGenerator(config)
        generator.xRoutine = XRoutineWithUserIdentity()

        client = SRP6ClientSession()
        client.xRoutine = XRoutineWithUserIdentity()
        binding.etPassword.addTextChangedListener(onTextChange)
        binding.btnSendResetLink.setOnClickListener(this)
        binding.ivTopAction.setOnClickListener(this)
        viewModel.resetPasswordResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
//                CommonMethods.dismissProgressDialog()
                val password = binding.etPassword.text!!.trim().toString()
                val emailSalt = BigInteger(1, generator.generateRandomSalt())
                val emailVerifier = generator.generateVerifier(
                    emailSalt, it.data.email.lowercase(), password
                )

                val phoneSalt = BigInteger(1, generator.generateRandomSalt())
                val phoneVerifier = generator.generateVerifier(
                    phoneSalt, it.data.phoneNo, password
                )
                CommonMethods.checkInternet(binding.root,requireContext()) {
                    val jsonObject = JSONObject()
                    jsonObject.put("emailSalt", emailSalt.toString())
                    jsonObject.put("emailVerifier", emailVerifier.toString())
                    jsonObject.put("phoneSalt", phoneSalt.toString())
                    jsonObject.put("phoneVerifier", phoneVerifier.toString())
                    val jsonString = sortAndFormatJson(jsonObject)
                    // Generate the request hash
                    val requestHash = CommonMethods.generateRequestHash(jsonString)

                    val integrityTokenResponse: Task<StandardIntegrityManager.StandardIntegrityToken>? =
                        SplashActivity.integrityTokenProvider?.request(
                            StandardIntegrityManager.StandardIntegrityTokenRequest.builder()
                                .setRequestHash(requestHash)
                                .build()
                        )
                    integrityTokenResponse?.addOnSuccessListener { response ->
                        viewModel.resetNewPass(
                            emailSalt.toString(),emailVerifier.toString(),
                            phoneSalt.toString(),phoneVerifier.toString(),
                            token = response.token()
                        )
//                            viewModel.switchOffAuthentication(detail, Constants.TYPE)

                    }?.addOnFailureListener { exception ->
                        Log.d("token", "${exception}")

                    }
                }
            }
        }
        viewModel.booleanResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                CommonMethods.dismissProgressDialog()
                if (it.success) {
                    App.prefsManager.accessToken = ""
                    val bundle = Bundle().apply {
                        putBoolean(Constants.FOR_LOGIN, true)
                    }
                    findNavController().navigate(R.id.createAccountFragment, bundle)
                }
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            // Handle back button press
            findNavController().popBackStack()
            findNavController().navigate(R.id.discoveryFragment)
        }
    }

    override fun onRetrofitError(errorCode: Int, msg: String) {
        CommonMethods.dismissProgressDialog()
        CommonMethods.dismissAlertDialog()
        when (errorCode) {
            48 -> {
                // Handle back button press
                // Keep popping the stack until it can't pop anymore
                while (findNavController().navigateUp()) {
                }

// Now navigate to ForgotPasswordFragment
                findNavController().navigate(R.id.forgotPasswordFragment)

                CommonMethods.showSnack(
                    binding.root,
                    requireContext(),
                    getString(R.string.error_code_48)
                )
            }

            else -> super.onRetrofitError(errorCode, msg)


        }
    }

    private val onTextChange = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {}
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (binding.etPassword.text!!.trim().toString().isNotEmpty()) {
                if (CommonMethods.isValidPassword(binding.etPassword.text!!.trim().toString())) {
                    val colorStateList =
                        ColorStateList.valueOf(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.purple_500
                            )
                        )
                    binding.btnSendResetLink.backgroundTintList = colorStateList
                    binding.tvPassValidMsg.text = getString(R.string.you_have_strong_password)
                    binding.tvPassValidMsg.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.green_500
                        )
                    )
                    // Set background tint mode to SRC_ATOP
                    binding.btnSendResetLink.backgroundTintMode = PorterDuff.Mode.SRC_ATOP
                    buttonClicked = true
                } else {
                    val colorStateList =
                        ColorStateList.valueOf(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.purple_gray_600
                            )
                        )
                    binding.btnSendResetLink.backgroundTintList = colorStateList
                    binding.tvPassValidMsg.text = getString(R.string.pass_valid_msg)
                    binding.tvPassValidMsg.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.red_500
                        )
                    )
                    // Set background tint mode to SRC_ATOP
                    binding.btnSendResetLink.backgroundTintMode = PorterDuff.Mode.SRC_ATOP
                    buttonClicked = false
                }
            } else
                buttonClicked = false

        }


    }

    override fun onClick(v: View?) {
        binding.apply {
            when (v!!) {
                ivTopAction -> requireActivity().onBackPressedDispatcher.onBackPressed()
                btnSendResetLink -> {
                    if (buttonClicked) {
                         CommonMethods.checkInternet(binding.root,requireContext()) {
                          CommonMethods.showProgressDialog(requireContext())
                                App.prefsManager.accessToken = resetToken
                                viewModel.getResetPass(
                                )

                        }

                    }

                }

            }
        }
    }

}