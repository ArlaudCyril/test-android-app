package com.Lyber.ui.fragments.bottomsheetfragments

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.Lyber.R
import com.Lyber.databinding.BottomSheetVerificationBinding
import com.Lyber.ui.fragments.TwoFactorAuthenticationFragment
import com.Lyber.utils.CommonMethods
import com.Lyber.utils.CommonMethods.Companion.requestKeyboard
import com.Lyber.utils.Constants
import com.Lyber.viewmodels.SignUpViewModel


class VerificationBottomSheet() :
    BaseBottomSheet<BottomSheetVerificationBinding>() {

    private val codeOne get() = binding.etCodeOne.text.trim().toString()
    private val codeTwo get() = binding.etCodeTwo.text.trim().toString()
    private val codeThree get() = binding.etCodeThree.text.trim().toString()
    private val codeFour get() = binding.etCodeFour.text.trim().toString()
    private val codeFive get() = binding.etCodeFive.text.trim().toString()
    private val codeSix get() = binding.etCodeSix.text.trim().toString()

    private var googleOTP=""

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
//        viewModel.listener = this
        this.binding.etCodeOne.requestFocus()
        binding.etCodeOne.requestKeyboard()
        binding.tvBack.setOnClickListener {
            val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
            imm?.hideSoftInputFromWindow(view?.windowToken, 0)
            dismiss()
        }
    }

    private fun setUpView() {
        binding.apply {

            title.text = getString(R.string.verification)
            if (tag!!.isNotEmpty()) {
                btnCancel.visibility = View.GONE
                tvBack.visibility = View.VISIBLE
                if (arguments != null && requireArguments().containsKey(Constants.TYPE)) {
                    var type = requireArguments().getString(Constants.TYPE)
                    if (type == Constants.EMAIL)
                        subtitle.text = getString(R.string.enter_the_code_received_by_email)
                    else if (type == Constants.PHONE)
                        subtitle.text = getString(R.string.enter_the_code_received_by_sms)
                    else subtitle.text =
                        getString(R.string.enter_the_code_displayed_by_google_authenticator)
                } else {
                    if (tag == Constants.EMAIL)
                        subtitle.text = getString(R.string.enter_the_code_received_by_email)
                    else if (tag == Constants.PHONE)
                        subtitle.text = getString(R.string.enter_the_code_received_by_sms)
                    else subtitle.text =
                        getString(R.string.enter_the_code_displayed_by_google_authenticator)
                }
            } else if (viewModel.forLogin) {
                subtitle.text = getString(R.string.enter_the_code_displayed_on_your_email)
            } else {
                subtitle.text = getString(R.string.enter_the_code_displayed_on_your_sms)
            }
            fieldToVerify.text = ""
            btnCancel.text = getString(R.string.cancel)


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
                                    if (requireArguments().containsKey("changeType")){
                                        dismiss()
                                        Log.d("text", args!!)
                                        val hash = hashMapOf<String, Any>()
                                        hash["type2FA"] = requireArguments().getString("changeType").toString()
                                        hash["otp"] = getCode()
                                        Log.d("hash", "$hash")
                                        CommonMethods.showProgressDialog(requireContext())
                                        viewModel.updateAuthentication(hash)
                                    }
                                    if(args== Constants.GOOGLE){
                                        if(googleOTP.isNotEmpty()){
                                            dismiss()
                                            Log.d("text", args!!)
                                            val hash = hashMapOf<String, Any>()
                                            hash["type2FA"] = args
                                            hash["otp"] = getCode()
                                            hash["googleOtp"] = googleOTP
                                            Log.d("hash", "$hash")
                                            TwoFactorAuthenticationFragment.showOtp=true
                                            CommonMethods.showProgressDialog(requireContext())
                                            viewModel.updateAuthentication(hash)
                                        }
                                        else {
                                            googleOTP = getCode()
                                            binding.subtitle.text =
                                                getString(R.string.enter_the_code_received_by_email)
                                            binding.etCodeSix.setText("")
                                            binding.etCodeFive.setText("")
                                            binding.etCodeFour.setText("")
                                            binding.etCodeThree.setText("")
                                            binding.etCodeTwo.setText("")
                                            binding.etCodeOne.setText("")
                                            binding.etCodeOne.requestFocus()
                                            binding.etCodeOne.setSelection(binding.etCodeOne.text.length)
                                        }

                                    }
                                    else {
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
                            } else if (viewModel.forLogin) {
                                dismiss()
                                viewModel.verify2FA(code = getCode())
                            } else {
                                dismiss()
                                viewModel.verifyPhone(getCode())
                            }
                        }
                    }
                }
            }

        }


        private fun  nextEditText(modifiedEditText: EditText) : EditText{

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


}