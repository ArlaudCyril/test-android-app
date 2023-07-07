package com.au.lyber.ui.fragments.bottomsheetfragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import com.au.lyber.databinding.BottomSheetVerificationBinding
import com.au.lyber.utils.CommonMethods.Companion.requestKeyboard
import com.au.lyber.viewmodels.PersonalDataViewModel
import com.au.lyber.viewmodels.SignUpViewModel

class EmailVerificationBottomSheet () :
    BaseBottomSheet<BottomSheetVerificationBinding>(){

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

    override fun bind() = BottomSheetVerificationBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpView()
        this.binding.etCodeOne.requestFocus()
        binding.etCodeOne.requestKeyboard()

    }

    private fun setUpView(){
        binding.apply {

            title.text = "Verification"
            subtitle.text = "Enter the code displayed on your email"
            fieldToVerify.text = ""
            btnCancel.text = "Cancel"

            // Usage example
            val editTextArray : List<EditText> = listOf(etCodeOne, etCodeTwo, etCodeThree,
                etCodeFour, etCodeFive, etCodeSix)

            for (editText in editTextArray){
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
                    modifiedEditText.text = Editable.Factory.getInstance().newEditable(modifiedEditText.text[0].toString())
                    val nextEditText = nextEditText(modifiedEditText)
                    nextEditText.text = Editable.Factory.getInstance().newEditable(lastCharacter.toString())

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
                                viewModel.verifyEmail(getCode())
                        }
                    }
                }
            }

        }

        private fun  nextEditText(modifiedEditText: EditText) : EditText {
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