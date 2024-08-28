package com.Lyber.dev.ui.fragments.bottomsheetfragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.Lyber.dev.R
import com.Lyber.dev.databinding.BottomSheetVerificationBinding
import com.Lyber.dev.ui.activities.SplashActivity
import com.Lyber.dev.utils.App
import com.Lyber.dev.utils.CommonMethods
import com.Lyber.dev.utils.CommonMethods.Companion.gone
import com.Lyber.dev.utils.CommonMethods.Companion.requestKeyboard
import com.Lyber.dev.utils.CommonMethods.Companion.visible
import com.Lyber.dev.utils.Constants
import com.Lyber.dev.viewmodels.SignUpViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior


class VerificationBottomSheet2FA(private val handle: (String) -> Unit) :
    BaseBottomSheet<BottomSheetVerificationBinding>() {

    private val codeOne get() = binding.etCodeOne.text.trim().toString()
    private val codeTwo get() = binding.etCodeTwo.text.trim().toString()
    private val codeThree get() = binding.etCodeThree.text.trim().toString()
    private val codeFour get() = binding.etCodeFour.text.trim().toString()
    private val codeFive get() = binding.etCodeFive.text.trim().toString()
    private val codeSix get() = binding.etCodeSix.text.trim().toString()

    lateinit var viewToDelete: View
    lateinit var mainView: ViewGroup

    private var timer = 60
    private var isTimerRunning = false
    private lateinit var handler: Handler
    var fromResend = false

    lateinit var viewModel: SignUpViewModel
    override fun bind() = BottomSheetVerificationBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        viewModel = CommonMethods.getViewModel(this)
//        viewModel.listener=this
        setUpView()
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        this.binding.etCodeOne.requestFocus()
        binding.etCodeOne.requestKeyboard()
        viewModel.userLoginResponse.observe(viewLifecycleOwner) {

            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                CommonMethods.dismissProgressDialog()
                if (it.data.access_token != null) {
                    dismiss()
                    App.prefsManager.accessToken = it.data.access_token
                    App.accessToken = it.data.access_token
                    App.prefsManager.refreshToken = it.data.refresh_token

                    childFragmentManager.popBackStack(
                        null, FragmentManager.POP_BACK_STACK_INCLUSIVE
                    )
                    val bundle = Bundle().apply {
                        putBoolean(Constants.FOR_LOGIN, viewModel.forLogin)
                    }
                    findNavController().navigate(R.id.createPinFragment, bundle)
                }

            }
        }
        viewModel.booleanResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                CommonMethods.dismissProgressDialog()
                if(it.success){
                    App.prefsManager.logout()
                    startActivity(
                        Intent(
                            requireActivity(),
                            SplashActivity::class.java
                        ).apply {
                            putExtra("fromLogout", "fromLogout")
                            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        })
                }
            }
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }
        if (!::handler.isInitialized)
            handler = Handler(Looper.getMainLooper())

        binding.tvResendCode.setOnClickListener {
            CommonMethods.checkInternet(binding.root,requireContext()){
                CommonMethods.setProgressDialogAlert(requireContext())
               fromResend=true
                handle.invoke("Resend")
            }
        }
        viewModel.commonResponse.observe(viewLifecycleOwner) {

            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                CommonMethods.dismissProgressDialog()
                CommonMethods.dismissAlertDialog()
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

    private fun setUpView() {
        binding.apply {

            title.text = getString(R.string.verification)
            if (viewModel.forLogin) {
                subtitle.text = getString(R.string.enter_the_code_displayed_on_your_email)
            } else {
                subtitle.text = getString(R.string.enter_the_code_displayed_on_your_sms)
            }
            if (App.prefsManager.user!=null && App.prefsManager.user?.type2FA!=null) {
                val type = App.prefsManager.user?.type2FA
                when (type) {
                    Constants.EMAIL -> {
                        subtitle.text = getString(R.string.enter_the_code_received_by_email)
                        handler = Handler(Looper.getMainLooper())
                        binding.tvResendCode.gone()
                        startTimer()
                    }
                    Constants.PHONE -> {
                        subtitle.text = getString(R.string.enter_the_code_received_by_sms)
                        handler = Handler(Looper.getMainLooper())
                        binding.tvResendCode.gone()
                        startTimer()
                    }
                    else -> {
                        tvResendCode.gone()
                        subtitle.text =
                            getString(R.string.enter_the_code_displayed_by_google_authenticator)
                    }
                }
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
            binding.root.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
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
                            if(tag!=null && tag==Constants.ACTION_CLOSE_ACCOUNT){
                                CommonMethods.showProgressDialog(requireContext())
                                val hash = hashMapOf<String, Any>()
                                hash["otp"] = getCode()
                                Log.d("hash", "$hash")
                                viewModel.closeAccount(hash)
                            }
                         else   {
                                 if (viewModel.forLogin) {
                                CommonMethods.showProgressDialog(requireContext())
                                viewModel.verify2FA(code = getCode())
                            } else {
                                dismiss()
                                handle.invoke(getCode())
                            }
                            //  viewModel.verify2FAWithdraw(code = getCode())

                        }
                    }
                }
            }

        }}

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