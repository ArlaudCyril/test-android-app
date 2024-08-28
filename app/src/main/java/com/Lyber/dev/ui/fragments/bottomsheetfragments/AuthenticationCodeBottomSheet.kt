package com.Lyber.dev.ui.fragments.bottomsheetfragments

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import com.Lyber.dev.databinding.BottomSheetEnterCodeBinding
import com.Lyber.dev.ui.fragments.StrongAuthenticationFragment
import com.Lyber.dev.utils.CommonMethods
import com.Lyber.dev.utils.CommonMethods.Companion.getViewModel
import com.Lyber.dev.utils.CommonMethods.Companion.gone
import com.Lyber.dev.utils.CommonMethods.Companion.visible
import com.Lyber.dev.viewmodels.ProfileViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior

class AuthenticationCodeBottomSheet : BaseBottomSheet<BottomSheetEnterCodeBinding>() {

    private val codeOne: String get() = binding.etCodeOne.text.trim().toString()
    private val codeTwo: String get() = binding.etCodeTwo.text.trim().toString()
    private val codeThree: String get() = binding.etCodeThree.text.trim().toString()
    private val codeFour: String get() = binding.etCodeFour.text.trim().toString()
    private val codeFive: String get() = binding.etCodeFive.text.trim().toString()
    private val codeSix: String get() = binding.etCodeSix.text.trim().toString()

    private var timer: CountDownTimer? = null

    private val completeCode: String get() = codeOne + codeTwo + codeThree + codeFour + codeFive + codeSix

    private lateinit var viewModel: ProfileViewModel
    override fun bind() = BottomSheetEnterCodeBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startTimer()
        viewModel = getViewModel(requireParentFragment())

        behavior.state = BottomSheetBehavior.STATE_EXPANDED

        binding.apply {
            etCodeOne.addTextChangedListener(onTextChange)
            etCodeTwo.addTextChangedListener(onTextChange)
            etCodeThree.addTextChangedListener(onTextChange)
            etCodeFour.addTextChangedListener(onTextChange)
            etCodeFive.addTextChangedListener(onTextChange)
            etCodeSix.addTextChangedListener(onTextChange)

            etCodeOne.setOnKeyListener(key)
            etCodeTwo.setOnKeyListener(key)
            etCodeThree.setOnKeyListener(key)
            etCodeFour.setOnKeyListener(key)
            etCodeFive.setOnKeyListener(key)
            etCodeSix.setOnKeyListener(key)
        }

        binding.tvResendCode.setOnClickListener {
            startTimer(15000)
        }
        binding.btnCancel.setOnClickListener {
            dismiss()
        }
    }

    private fun startTimer(time: Long = 15000) {
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

    private val key = View.OnKeyListener { v, keyCode, event ->

        val delPressed = event.action == KeyEvent.ACTION_DOWN
                && keyCode == KeyEvent.KEYCODE_DEL

        if (delPressed) {
            when {
                codeTwo.isEmpty() -> binding.etCodeOne.requestFocus()
                codeThree.isEmpty() -> binding.etCodeTwo.requestFocus()
                codeFour.isEmpty() -> binding.etCodeThree.requestFocus()
                codeFive.isEmpty() -> binding.etCodeFour.requestFocus()
                codeSix.isEmpty() -> binding.etCodeFive.requestFocus()
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
            if (completeCode.length == 6) {
                CommonMethods.checkInternet(binding.root,requireContext()) {
                    CommonMethods.showProgressDialog(requireContext())
                    viewModel.verifyStrongAuthentication(completeCode)
                }
            } else
                when {
                    codeOne.isEmpty() -> binding.etCodeOne.requestFocus()
                    codeTwo.isEmpty() -> binding.etCodeTwo.requestFocus()
                    codeThree.isEmpty() -> binding.etCodeThree.requestFocus()
                    codeFour.isEmpty() -> binding.etCodeFour.requestFocus()
                    codeFive.isEmpty() -> binding.etCodeFive.requestFocus()
                    codeSix.isEmpty() -> binding.etCodeSix.requestFocus()
                }

        }


    }

    fun clearFields() {
        binding.apply {
            etCodeOne.setText("")
            etCodeTwo.setText("")
            etCodeThree.setText("")
            etCodeFour.setText("")
            etCodeFive.setText("")
            etCodeSix.setText("")
        }
    }

    override fun onDestroyView() {
        cancelTimer()
        super.onDestroyView()
    }



}