package com.Lyber.ui.fragments

import android.os.Bundle
import android.view.View
import com.Lyber.R
import com.Lyber.databinding.FragmentAddCreditCardBinding
import com.Lyber.utils.CommonMethods.Companion.requestKeyboard
import com.Lyber.utils.CommonMethods.Companion.showToast
import com.Lyber.utils.MonthYearPickerDialog

class AddCreditCardFragment : BaseFragment<FragmentAddCreditCardBinding>(), View.OnClickListener {

    private val name: String get() = binding.etName.text.trim().toString()
    private val cardNumber: String get() = binding.etCardNumber.text.trim().toString()
    private val expiry: String get() = binding.etExpire.text.trim().toString()
    private val cvv: String get() = binding.etCVV.text.trim().toString()
    private val zipCode: String get() = binding.etZipCode.text.trim().toString()

    override fun bind() = FragmentAddCreditCardBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.ivTopAction.setOnClickListener(this)
        binding.etExpire.setOnClickListener(this)
        binding.btnAdd.setOnClickListener(this)
    }


    private fun checkData(): Boolean {
        return when {
            name.isEmpty() -> {
                getString(R.string.please_enter_your_name).showToast(requireContext())
                binding.etName.requestKeyboard()
                false
            }
            cardNumber.isEmpty() -> {
                getString(R.string.please_enter_your_card_number).showToast(requireContext())
                binding.etCardNumber.requestKeyboard()
                false
            }
            cvv.isEmpty() -> {
                getString(R.string.please_enter_your_cvv).showToast(requireContext())
                binding.etCVV.requestKeyboard()
                false
            }
            zipCode.isEmpty() -> {
                getString(R.string.please_enter_zip_code).showToast(requireContext())
                binding.etZipCode.requestKeyboard()
                false
            }
            expiry.isEmpty() -> {
                getString(R.string.please_provide_cards_expiry_date).showToast(requireContext())
                false
            }
            else -> {
                true
            }
        }
    }

    override fun onClick(v: View?) {
        binding.apply {
            when (v!!) {
                btnAdd -> {
                    if (checkData()) {
                        getString(R.string.credit_card_added).showToast(requireContext())
                        requireActivity().onBackPressed()
                    }
                }
                ivTopAction -> requireActivity().onBackPressed()
                etExpire -> {
                    val dialog = MonthYearPickerDialog()
                    dialog.setListener { view, year, month, dayOfMonth ->
                        binding.etExpire.setText("$month/$year")
                    }
                    dialog.show(requireActivity().supportFragmentManager, "")
//                    DatePickerDialog(requireContext(), R.style.CardExpire, { view, v1, v2, v3 ->
//                    }, 0, 0, 0).apply {
//                        datePicker.minDate = System.currentTimeMillis()
//                        show()
//                    }
                }
            }
        }
    }

}