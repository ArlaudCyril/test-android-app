package com.au.lyber.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import com.au.lyber.databinding.FragmentEnterOtpBinding
import com.au.lyber.utils.CommonMethods.Companion.checkInternet
import com.au.lyber.utils.CommonMethods.Companion.dismissProgressDialog
import com.au.lyber.utils.CommonMethods.Companion.getViewModel
import com.au.lyber.utils.CommonMethods.Companion.gone
import com.au.lyber.utils.CommonMethods.Companion.requestKeyboard
import com.au.lyber.utils.CommonMethods.Companion.showProgressDialog
import com.au.lyber.utils.CommonMethods.Companion.showToast
import com.au.lyber.utils.CommonMethods.Companion.visible
import com.au.lyber.viewmodels.SignUpViewModel
import okhttp3.ResponseBody

class EnterOtpFragment : BaseFragment<FragmentEnterOtpBinding>() {

    private val codeOne get() = binding.etCodeOne.text.trim().toString()
    private val codeTwo get() = binding.etCodeTwo.text.trim().toString()
    private val codeThree get() = binding.etCodeThree.text.trim().toString()
    private val codeFour get() = binding.etCodeFour.text.trim().toString()

    private var timer: CountDownTimer? = null
    private lateinit var viewModel: SignUpViewModel

    override fun bind() = FragmentEnterOtpBinding.inflate(layoutInflater)

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.etCodeOne.requestFocus()
        binding.etCodeOne.requestKeyboard()

        (requireParentFragment() as SignUpFragment).setIndicators(1)
        startTimer(15000)

        viewModel = getViewModel(requireParentFragment())
        viewModel.listener = this

        viewModel.resendOtp.observe(viewLifecycleOwner) {
            dismissProgressDialog()
            if (it.message.isNotEmpty())
                it.message.showToast(requireContext())
            else
                it.msg.showToast(requireContext())
            startTimer(15000)
        }

        binding.tvPhoneNumber.text =
            "${viewModel.countryCode} ${viewModel.mobileNumber}"

        binding.apply {

//            etCodeOne.clearFocus()

            etCodeOne.addTextChangedListener(onTextChange)
            etCodeTwo.addTextChangedListener(onTextChange)
            etCodeThree.addTextChangedListener(onTextChange)
            etCodeFour.addTextChangedListener(onTextChange)

            etCodeOne.setOnKeyListener(key)
            etCodeTwo.setOnKeyListener(key)
            etCodeThree.setOnKeyListener(key)
            etCodeFour.setOnKeyListener(key)



            tvResendCode.setOnClickListener {
                showProgressDialog(requireContext())
                Handler(Looper.getMainLooper()).postDelayed({
                    dismissProgressDialog()
                    "Otp sent successfully.".showToast(requireContext())
                    startTimer(15000)
                }, 2000)
            }
        }

    }


    override fun onDestroyView() {
        cancelTimer()
        clearFields()
        super.onDestroyView()
    }

    private fun startTimer(time: Long) {
        timer = object : CountDownTimer(time, 1000) {
            @SuppressLint("SetTextI18n")
            override fun onTick(millisUntilFinished: Long) {
                (millisUntilFinished / 1000).toString().let {
                    when (it.length) {
                        1 -> binding.tvTimerOptSent.text = "00:0$it"
                        else -> binding.tvTimerOptSent.text = "00:$it"
                    }
                }
            }

            override fun onFinish() {
                binding.llNewCodeSentIn.gone()
                binding.tvResendCode.visible()
            }
        }
        binding.llNewCodeSentIn.visible()
        binding.tvResendCode.gone()

        timer?.start()
    }

    private fun cancelTimer() {
        binding.tvTimerOptSent.text = "00:15"
        timer?.cancel()
        timer = null
    }


    fun getCode() = codeOne + codeTwo + codeThree + codeFour


    private val key = View.OnKeyListener { v, keyCode, event ->

        val delPressed = event.action == KeyEvent.ACTION_DOWN
                && keyCode == KeyEvent.KEYCODE_DEL

        if (delPressed) {
            when {
                codeTwo.isEmpty() -> binding.etCodeOne.requestFocus()
                codeThree.isEmpty() -> binding.etCodeTwo.requestFocus()
                codeFour.isEmpty() -> binding.etCodeThree.requestFocus()
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
            if (getCode().length == 4) {
                checkInternet(requireContext()) {
                    showProgressDialog(requireContext())
                    viewModel.verifyPhone(getCode())
//                    onBoardingViewModel.enterOtp(getCode())
                }
            } else
                when {
                    codeOne.isEmpty() -> binding.etCodeOne.requestFocus()
                    codeTwo.isEmpty() -> binding.etCodeTwo.requestFocus()
                    codeThree.isEmpty() -> binding.etCodeThree.requestFocus()
                    codeFour.isEmpty() -> binding.etCodeFour.requestFocus()
                }

        }


    }

    private fun clearFields() {
        binding.apply {
            etCodeOne.setText("")
            etCodeTwo.setText("")
            etCodeThree.setText("")
            etCodeFour.setText("")
        }
    }

    override fun onRetrofitError(responseBody: ResponseBody?) {
        super.onRetrofitError(responseBody)
        clearFields()
    }


}