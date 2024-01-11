package com.Lyber.ui.fragments

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
import com.Lyber.R
import com.Lyber.databinding.FragmentChangePasswordBinding
import com.Lyber.ui.fragments.bottomsheetfragments.VerificationBottomSheet
import com.Lyber.utils.App
import com.Lyber.utils.CommonMethods
import com.Lyber.utils.CommonMethods.Companion.showToast
import com.Lyber.utils.Constants
import com.Lyber.viewmodels.NetworkViewModel
import com.Lyber.viewmodels.SignUpViewModel
import com.nimbusds.srp6.SRP6ClientSession
import com.nimbusds.srp6.SRP6CryptoParams
import com.nimbusds.srp6.SRP6VerifierGenerator
import com.nimbusds.srp6.XRoutineWithUserIdentity
import java.math.BigInteger


class ChangePasswordFragment : BaseFragment<FragmentChangePasswordBinding>(), OnClickListener {
    private lateinit var viewModel: SignUpViewModel
    var buttonClicked = false
    override fun bind() = FragmentChangePasswordBinding.inflate(layoutInflater)
    private lateinit var config: SRP6CryptoParams
    lateinit var generator: SRP6VerifierGenerator
    lateinit var client: SRP6ClientSession
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
        binding.btnSendResetLink.setOnClickListener(this)
        binding.etPasswordOld.addTextChangedListener(onTextChange)
        binding.etPassword.addTextChangedListener(onTextChange)
        viewModel.booleanResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                CommonMethods.dismissProgressDialog()
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

                val vc = VerificationBottomSheet()
                vc.typeVerification = Constants.CHANGE_PASSWORD
                vc.viewToDelete = transparentView
                vc.mainView = getView()?.rootView as ViewGroup
                vc.viewModel = viewModel
                vc.show(childFragmentManager, "")

                // Add the transparent view to the RelativeLayout
                val mainView = getView()?.rootView as ViewGroup
                mainView.addView(transparentView, viewParams)

            }
        }
        viewModel.exportOperationResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                CommonMethods.dismissProgressDialog()
                getString(R.string.pass_changed_success).showToast(requireContext())
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }
        viewModel.changePasswordData.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                client = SRP6ClientSession()
                client.xRoutine = XRoutineWithUserIdentity()
                client.step1(
                    App.prefsManager.user?.email,
                    binding.etPasswordOld.text.toString()
                )
                val creds =
                    client.step2(config, it.data.salt.toBigInteger(), it.data.B.toBigInteger())
                val password = binding.etPassword.text.trim().toString()
                val emailSalt = BigInteger(1, generator.generateRandomSalt())
                val emailVerifier = generator.generateVerifier(
                    emailSalt, App.prefsManager.user?.email, password
                )
                val phoneSalt = BigInteger(1, generator.generateRandomSalt())
                val phoneVerifier = generator.generateVerifier(
                    phoneSalt, App.prefsManager.user?.phoneNo, password
                )
                Log.d("A", creds.A.toString())
                Log.d("M1", creds.M1.toString())
                Log.d("emailSalt", "$emailSalt")
                Log.d("emailVerifier", " $emailVerifier  ")
                Log.d("phoneSalt", "  $phoneSalt ")
                Log.d("phoneVerifier", "   $phoneVerifier")
                val hashMap = hashMapOf<String, Any>()
                hashMap["A"] = creds.A.toString()
                hashMap["M1"] = creds.M1.toString()
                hashMap["emailSalt"] = emailSalt.toString()
                hashMap["emailVerifier"] = emailVerifier.toString()
                hashMap["phoneSalt"] = phoneSalt.toString()
                hashMap["phoneVerifier"] = phoneVerifier.toString()
                viewModel.changePassword(hashMap)
            }
        }

    }

    private val onTextChange = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {}
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (binding.etPasswordOld.text.trim().toString().isNotEmpty() &&
                binding.etPassword.text.trim().toString().isNotEmpty()
            ) {
                if (CommonMethods.isValidPassword(binding.etPasswordOld.text.trim().toString())
                    && CommonMethods.isValidPassword(binding.etPassword.text.trim().toString())
                ) {

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
            when (v) {
                ivTopAction -> requireActivity().onBackPressedDispatcher.onBackPressed()
                btnSendResetLink -> {
                    if (buttonClicked) {
                        if (binding.etPasswordOld.text.toString()
                                .equals(binding.etPassword.text.toString())
                        ) {
                            getString(R.string.new_pass_cannot_be_same_as_old).showToast(
                                requireContext()
                            )
                        } else {
                            CommonMethods.checkInternet(requireActivity()) {
                                CommonMethods.showProgressDialog(requireContext())
                                viewModel.getPasswordChangeChallenge()
                            }
                        }
                    }
                }

            }
        }

    }

}