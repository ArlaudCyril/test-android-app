package com.Lyber.ui.fragments

import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import com.Lyber.R
import com.Lyber.databinding.FragmentChangePasswordBinding
import com.Lyber.ui.fragments.bottomsheetfragments.VerificationBottomSheet
import com.Lyber.utils.App
import com.Lyber.utils.CommonMethods
import com.Lyber.utils.CommonMethods.Companion.showToast
import com.Lyber.utils.Constants
import com.Lyber.viewmodels.SignUpViewModel
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
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
    private var resendCode=false
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
                if(!resendCode) {
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
                }
                resendCode=false
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

    private fun handle(txt:String){
        CommonMethods.checkInternet(requireActivity()) {
            resendCode=true
            viewModel.getPasswordChangeChallenge()
        }
    }
    override fun onClick(v: View?) {
        binding.apply {
            when (v) {
                ivTopAction -> requireActivity().onBackPressedDispatcher.onBackPressed()
                btnSavePass -> {
                    val snackbar = Snackbar.make(binding.root, "", Snackbar.LENGTH_LONG)
                    val params = snackbar.view.layoutParams as FrameLayout.LayoutParams
                    params.gravity = Gravity.TOP
                    params.setMargins(0, 0, 0, 0)
                    snackbar.view.layoutParams = params

                    snackbar.animationMode = BaseTransientBottomBar.ANIMATION_MODE_FADE
                    val layout = snackbar.view as Snackbar.SnackbarLayout
                    val textView =
                        layout.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
                    textView.visibility = View.INVISIBLE
                    val snackView =
                        LayoutInflater.from(context).inflate(R.layout.custom_snackbar, null)
                    val textViewMsg = snackView.findViewById<TextView>(R.id.tvMsg)
                    layout.setPadding(0, 0, 0, 0)
                    layout.addView(snackView, 0)
                    if (buttonClicked) {
                        if (binding.etPasswordOld.text.trim().toString()
                                .equals(binding.etPassword.text.trim().toString())
                        ) {
                            textViewMsg.text= getString(R.string.new_pass_cannot_be_same_as_old)
                            snackbar.show()
                        } else if (!binding.etPassword.text.trim().toString()
                                .equals(binding.etPasswordConfirm.text.trim().toString())
                        ) {
                            textViewMsg.text= getString(R.string.pass_should_be_same)
                            snackbar.show()
                        }
                        else {
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