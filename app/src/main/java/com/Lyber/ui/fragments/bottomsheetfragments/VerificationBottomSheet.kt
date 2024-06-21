package com.Lyber.ui.fragments.bottomsheetfragments

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Lifecycle
import com.Lyber.R
import com.Lyber.databinding.BottomSheetVerificationBinding
import com.Lyber.ui.fragments.TwoFactorAuthenticationFragment
import com.Lyber.utils.App
import com.Lyber.utils.CommonMethods
import com.Lyber.utils.CommonMethods.Companion.gone
import com.Lyber.utils.CommonMethods.Companion.requestKeyboard
import com.Lyber.utils.CommonMethods.Companion.visible
import com.Lyber.utils.Constants
import com.Lyber.viewmodels.SignUpViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.stripe.android.googlepaylauncher.GooglePayLauncher

class VerificationBottomSheet(private val handle: ((String) -> Unit?)? = null) :
    BaseBottomSheet<BottomSheetVerificationBinding>() {

    private val codeOne get() = binding.etCodeOne.text.trim().toString()
    private val codeTwo get() = binding.etCodeTwo.text.trim().toString()
    private val codeThree get() = binding.etCodeThree.text.trim().toString()
    private val codeFour get() = binding.etCodeFour.text.trim().toString()
    private val codeFive get() = binding.etCodeFive.text.trim().toString()
    private val codeSix get() = binding.etCodeSix.text.trim().toString()

    private var googleOTP = ""
    var fromSignUp = true
    private var timer = 60
    private var isTimerRunning = false
    private lateinit var handler: Handler
    var fromResend = false

    lateinit var typeVerification: String
    lateinit var viewToDelete: View
    lateinit var mainView: ViewGroup
    lateinit var viewModel: SignUpViewModel

    override fun bind() = BottomSheetVerificationBinding.inflate(layoutInflater)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogStyle)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpView()
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        this.binding.etCodeOne.requestFocus()
        binding.etCodeOne.requestKeyboard()
        binding.btnCancel.setOnClickListener {
            val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
            imm?.hideSoftInputFromWindow(view?.windowToken, 0)
            dismiss()
        }
        binding.tvResendCode.setOnClickListener {
            CommonMethods.checkInternet(requireContext()) {
                if (googleOTP.isNotEmpty()) {
                    googleOTP = ""
                    binding.tvResendCode.gone()
                    binding.subtitle.text =
                        getString(R.string.enter_the_code_displayed_by_google_authenticator)
                    binding.etCodeSix.setText("")
                    binding.etCodeFive.setText("")
                    binding.etCodeFour.setText("")
                    binding.etCodeThree.setText("")
                    binding.etCodeTwo.setText("")
                    binding.etCodeOne.setText("")
                    binding.etCodeOne.requestFocus()
                    binding.etCodeOne.setSelection(binding.etCodeOne.text.length)
                    CommonMethods.setProgressDialogAlert(requireContext())
                    handle!!.invoke("tg")
                } else {
                    CommonMethods.setProgressDialogAlert(requireContext())
                    handle!!.invoke("tg")
                    if (fromSignUp)
                        fromResend = true
                }
            }
        }
        viewModel.setPhoneResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
              if(fromResend){
                  if(!::handler.isInitialized)
                      handler = Handler(Looper.getMainLooper())
                  timer=60
                  binding.tvTimeLeft.text="60"
                  startTimer()
              }
                fromResend=false
            }
        }
        viewModel.userChallengeResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                if (fromResend) {
                    if (!::handler.isInitialized)
                        handler = Handler(Looper.getMainLooper())
                    timer = 60
                    binding.tvTimeLeft.text = "60"
                    startTimer()
                }
                fromResend = false

            }
        }
        viewModel.booleanResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                if (fromResend) {
                    if (!::handler.isInitialized)
                        handler = Handler(Looper.getMainLooper())
                    timer = 60
                    binding.tvTimeLeft.text = "60"
                    startTimer()
                }
                fromResend = false

            }
        }
    }

    private fun startTimer() {
        if (binding.tvResendCode.isVisible)
            binding.tvResendCode.gone()
        if (!binding.llResendText.isVisible)
            binding.llResendText.visible()
        try {
            handler.postDelayed(runnable, 1000)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("SuspiciousIndentation")
    private val runnable = Runnable {
        isTimerRunning = true
        if (timer == 0) {
            binding.tvResendCode.visible()
            binding.llResendText.gone()
        } else {
            timer -= 1

            if (timer > 0) {
                binding.tvTimeLeft.text = timer.toString()
            } else {
                binding.tvResendCode.visible()
                binding.llResendText.gone()
            }
            startTimer()
        }
    }

    private fun setUpView() {
        binding.apply {

            title.text = getString(R.string.verification)
            if (arguments != null && requireArguments().containsKey(Constants.TYPE) && requireArguments().getBoolean(
                    Constants.GOOGLE
                )
            ) {
                binding.tvResendCode.gone()
                binding.llResendText.gone()
            }
            else if (  App.prefsManager.user!=null && App.prefsManager.user!!.type2FA==Constants.GOOGLE){
                binding.tvResendCode.gone()
                binding.llResendText.gone()
            }
            else if (fromSignUp) {
                handler = Handler(Looper.getMainLooper())
                binding.tvResendCode.gone()
                startTimer()
            }
            if (tag!!.isNotEmpty()) {
                if (arguments != null && requireArguments().containsKey(Constants.TYPE)) {
                    if (requireArguments().containsKey(Constants.GOOGLE) && requireArguments().getBoolean(
                            Constants.GOOGLE
                        )
                    ) {
                        tvResendCode.gone()
                        subtitle.text =
                            getString(R.string.enter_the_code_displayed_by_google_authenticator)
                    } else if (App.prefsManager.user!!.type2FA == Constants.EMAIL)
                        subtitle.text = getString(R.string.enter_the_code_received_at_email)
                    else if (App.prefsManager.user!!.type2FA == Constants.PHONE)
                        subtitle.text = getString(R.string.enter_the_code_received_by_sms)
                    else {
                        tvResendCode.gone()
                        subtitle.text =
                            getString(R.string.enter_the_code_displayed_by_google_authenticator)
                    }
                } else {
                    if (tag == Constants.EMAIL)
                        subtitle.text = getString(R.string.enter_the_code_received_at_email)
                    else if (tag == Constants.PHONE)
                        subtitle.text = getString(R.string.enter_the_code_received_by_sms)
                    else {
                        tvResendCode.gone()
                        subtitle.text =
                            getString(R.string.enter_the_code_displayed_by_google_authenticator)
                    }
                }
            } else if (::typeVerification.isInitialized && !typeVerification.isNullOrEmpty()) {
                if (typeVerification == Constants.CHANGE_PASSWORD) {
                    if (App.prefsManager.user!!.type2FA == Constants.EMAIL)
                        subtitle.text = getString(R.string.enter_the_code_received_at_email)
                    else if (App.prefsManager.user!!.type2FA == Constants.PHONE)
                        subtitle.text = getString(R.string.enter_the_code_received_by_sms)
                    else {
                        tvResendCode.gone()
                        subtitle.text =
                            getString(R.string.enter_the_code_displayed_by_google_authenticator)
                    }
                } else if (typeVerification == Constants.EMAIL)
                    subtitle.text = getString(R.string.enter_the_code_received_at_email)
                else if (typeVerification == Constants.PHONE)
                    subtitle.text = getString(R.string.enter_the_code_received_by_sms)
                else {
                    tvResendCode.gone()
                    subtitle.text =
                        getString(R.string.enter_the_code_displayed_by_google_authenticator)
                }
            } else if (viewModel.forLogin) {
                subtitle.text = getString(R.string.enter_the_code_received_at_email)
            } else {
                subtitle.text = getString(R.string.enter_the_code_displayed_on_your_sms)
            }
            fieldToVerify.text = ""
            btnCancel.text = getString(R.string.back)


            // Usage example
            val editTextArray: List<EditText> = listOf(
                etCodeOne, etCodeTwo, etCodeThree,
                etCodeFour, etCodeFive, etCodeSix
            )

            for (editText in editTextArray) {
                editText.addTextChangedListener(onTextChange)
                editText.setOnKeyListener(key)
            }

            // Set layout parameters to wrap content for the VerificationBottomSheet's root view
//            binding.root.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        this.mainView.removeView(this.viewToDelete)
    }


    fun getCode() = codeOne + codeTwo + codeThree + codeFour + codeFive + codeSix

    private val key = View.OnKeyListener { v, keyCode, event ->

        val delPressed = event.action == KeyEvent.ACTION_DOWN
                && keyCode == KeyEvent.KEYCODE_DEL

        if (delPressed) {
            when (v) {
                binding.etCodeTwo -> {
                    binding.etCodeOne.requestFocus()
                    binding.etCodeOne.setSelection(binding.etCodeOne.text.length)
                }

                binding.etCodeThree -> {
                    binding.etCodeTwo.requestFocus()
                    binding.etCodeTwo.setSelection(binding.etCodeTwo.text.length)
                }

                binding.etCodeFour -> {
                    binding.etCodeThree.requestFocus()
                    binding.etCodeThree.setSelection(binding.etCodeThree.text.length)
                }

                binding.etCodeFive -> {
                    binding.etCodeFour.requestFocus()
                    binding.etCodeFour.setSelection(binding.etCodeFour.text.length)
                }

                binding.etCodeSix -> {
                    binding.etCodeFive.requestFocus()
                    binding.etCodeFive.setSelection(binding.etCodeFive.text.length)
                }

                else -> {}
            }
            return@OnKeyListener false
        }
        false

    }

    private val onTextChange = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {}
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            val modifiedEditText = when (s) {
                binding.etCodeOne.text -> binding.etCodeOne
                binding.etCodeTwo.text -> binding.etCodeTwo
                binding.etCodeThree.text -> binding.etCodeThree
                binding.etCodeFour.text -> binding.etCodeFour
                binding.etCodeFive.text -> binding.etCodeFive
                binding.etCodeSix.text -> binding.etCodeSix
                else -> null
            }
            if (count > before) {
                // Character(s) added
                if (modifiedEditText != null && s != null && s.count() == 2) {
                    val lastCharacter = modifiedEditText.text[1]
                    modifiedEditText.text = Editable.Factory.getInstance()
                        .newEditable(modifiedEditText.text[0].toString())
                    val nextEditText = nextEditText(modifiedEditText)
                    nextEditText.text =
                        Editable.Factory.getInstance().newEditable(lastCharacter.toString())

                }

                when (modifiedEditText) {
                    binding.etCodeOne -> {
                        binding.etCodeTwo.requestFocus()
                        binding.etCodeTwo.setSelection(binding.etCodeTwo.text.length)
                    }

                    binding.etCodeTwo -> {
                        binding.etCodeThree.requestFocus()
                        binding.etCodeThree.setSelection(binding.etCodeThree.text.length)
                    }

                    binding.etCodeThree -> {
                        binding.etCodeFour.requestFocus()
                        binding.etCodeFour.setSelection(binding.etCodeFour.text.length)
                    }

                    binding.etCodeFour -> {
                        binding.etCodeFive.requestFocus()
                        binding.etCodeFive.setSelection(binding.etCodeFive.text.length)
                    }

                    binding.etCodeFive -> {
                        binding.etCodeSix.requestFocus()
                        binding.etCodeSix.setSelection(binding.etCodeSix.text.length)
                    }

                    binding.etCodeSix -> {
                        if (getCode().length == 6) {

                            if (tag!!.isNotEmpty()) {
                                if (arguments != null && requireArguments().containsKey("clickedOn")) {
                                    dismiss()
                                    var args = requireArguments().getString("clickedOn")
                                    Log.d("text", args!!)

                                    val hash = hashMapOf<String, Any>()
                                    if (args == "")
                                        hash["scope2FA"] = listOf("login")
                                    else
                                        hash["scope2FA"] = listOf(args)
                                    hash["otp"] = getCode()
                                    Log.d("hash", "$hash")
                                    CommonMethods.showProgressDialog(requireContext())
                                    viewModel.updateAuthentication(hash)
                                } else if (arguments != null && requireArguments().containsKey(
                                        Constants.TYPE
                                    )
                                ) {
                                    var args = requireArguments().getString(Constants.TYPE)
                                    if (requireArguments().containsKey("changeType")) {
                                        dismiss()
                                        Log.d("text", args!!)
                                        val hash = hashMapOf<String, Any>()
                                        hash["type2FA"] =
                                            requireArguments().getString("changeType").toString()
                                        hash["otp"] = getCode()
                                        Log.d("hash", "$hash")
                                        CommonMethods.showProgressDialog(requireContext())
                                        viewModel.updateAuthentication(hash)
                                    }
                                    if (args == Constants.GOOGLE) {
                                        if (googleOTP.isNotEmpty()) {
                                            dismiss()
                                            Log.d("text", args)
                                            val hash = hashMapOf<String, Any>()
                                            hash["type2FA"] = args
                                            hash["otp"] = getCode()
                                            hash["googleOtp"] = googleOTP
                                            Log.d("hash", "$hash")
                                            TwoFactorAuthenticationFragment.showOtp = true
                                            CommonMethods.showProgressDialog(requireContext())
                                            viewModel.updateAuthentication(hash)
                                        } else {
                                            googleOTP = getCode()
                                            if (App.prefsManager.user!!.type2FA == Constants.EMAIL)
                                                binding.subtitle.text =
                                                    getString(R.string.enter_the_code_received_at_email)
                                            else if (App.prefsManager.user!!.type2FA == Constants.PHONE)
                                                binding.subtitle.text =
                                                    getString(R.string.enter_the_code_received_by_sms)

                                            handler = Handler(Looper.getMainLooper())
                                            binding.tvResendCode.gone()
                                            timer = 60
                                            binding.tvTimeLeft.text = "60"
                                            startTimer()
//                                            binding.tvResendCode.visible()
                                            binding.etCodeSix.setText("")
                                            binding.etCodeFive.setText("")
                                            binding.etCodeFour.setText("")
                                            binding.etCodeThree.setText("")
                                            binding.etCodeTwo.setText("")
                                            binding.etCodeOne.setText("")
                                            binding.etCodeOne.requestFocus()
                                            binding.etCodeOne.setSelection(binding.etCodeOne.text.length)
                                        }

                                    } else {
                                        dismiss()
                                        Log.d("text", args!!)
                                        val hash = hashMapOf<String, Any>()
                                        hash["type2FA"] = args
                                        hash["otp"] = getCode()
                                        Log.d("hash", "$hash")
                                        CommonMethods.showProgressDialog(requireContext())
                                        viewModel.updateAuthentication(hash)
                                    }
                                }
                            } else if (::typeVerification.isInitialized && typeVerification != null && typeVerification == Constants.CHANGE_PASSWORD) {
                                dismiss()
                                CommonMethods.showProgressDialog(requireContext())
                                viewModel.verifyPasswordChange(code = getCode())
                            } else if (viewModel.forLogin) {
                                dismiss()
                                viewModel.verify2FA(code = getCode())
                            } else {
                                dismiss()
                                CommonMethods.showProgressDialog(requireContext())
                                viewModel.verifyPhone(getCode())
                            }
                        }
                    }
                }
            }

        }


        private fun nextEditText(modifiedEditText: EditText): EditText {

            when (modifiedEditText) {
                binding.etCodeOne -> return binding.etCodeTwo
                binding.etCodeTwo -> return binding.etCodeThree
                binding.etCodeThree -> return binding.etCodeFour
                binding.etCodeFour -> return binding.etCodeFive
                binding.etCodeFive -> return binding.etCodeSix
            }
            return binding.etCodeOne
        }


    }

    override fun onDestroy() {
        super.onDestroy()
        stopTimer()
    }
    private fun stopTimer() {
        try {
            handler.removeCallbacks(runnable)
            isTimerRunning = false
        } catch (_: Exception) {

        }
    }
}