package com.Lyber.dev.ui.fragments.bottomsheetfragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Lifecycle
import com.Lyber.dev.R
import com.Lyber.dev.databinding.BottomSheetVerificationBinding
import com.Lyber.dev.utils.App
import com.Lyber.dev.utils.CommonMethods
import com.Lyber.dev.utils.CommonMethods.Companion.gone
import com.Lyber.dev.utils.CommonMethods.Companion.requestKeyboard
import com.Lyber.dev.utils.CommonMethods.Companion.visible
import com.Lyber.dev.utils.Constants
import com.Lyber.dev.viewmodels.PersonalDataViewModel
import com.Lyber.dev.viewmodels.SignUpViewModel

class EmailVerificationBottomSheet(private val handle: ((String) -> Unit?)? = null) :
    BaseBottomSheet<BottomSheetVerificationBinding>() {

    private val codeOne get() = binding.etCodeOne.text.trim().toString()
    private val codeTwo get() = binding.etCodeTwo.text.trim().toString()
    private val codeThree get() = binding.etCodeThree.text.trim().toString()
    private val codeFour get() = binding.etCodeFour.text.trim().toString()
    private val codeFive get() = binding.etCodeFive.text.trim().toString()
    private val codeSix get() = binding.etCodeSix.text.trim().toString()

    lateinit var typeVerification: String
    lateinit var viewToDelete: View
    lateinit var mainView: ViewGroup
    lateinit var viewModel: PersonalDataViewModel
    private var timer = 60
    private var isTimerRunning = false
    private lateinit var handler: Handler
    var fromResend = false


    override fun bind() = BottomSheetVerificationBinding.inflate(layoutInflater)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogStyle)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpView()
        binding.etCodeOne.requestFocus()
        binding.etCodeOne.requestKeyboard()
        binding.btnCancel.setOnClickListener {
            val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
            imm?.hideSoftInputFromWindow(view?.windowToken, 0)

            dismiss()
        }
        viewModel.setUpEmailResponse.observe(viewLifecycleOwner) {
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
            handler = Handler(Looper.getMainLooper())
            binding.tvResendCode.gone()
            startTimer()
            title.text = getString(R.string.verification)
            subtitle.text = getString(R.string.enter_the_code_received_at_email)
            fieldToVerify.text = ""
            btnCancel.text = getString(R.string.back)
//            tvResendCode.visible()
            tvResendCode.setOnClickListener {
                CommonMethods.checkInternet(binding.root,requireContext()) {
                    CommonMethods.setProgressDialogAlert(requireContext())
                    handle!!.invoke("tg")
                    fromResend = true

                }

            }
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
                            dismiss()
                            CommonMethods.showProgressDialog(requireContext())
                            viewModel.verifyEmail(getCode())
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