package com.Lyber.dev.ui.fragments.bottomsheetfragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import com.Lyber.dev.R
import com.Lyber.dev.databinding.FragmentEnterPhoneNumberBottomSheetBinding
import com.Lyber.dev.ui.fragments.SendAmountFragment
import com.Lyber.dev.ui.fragments.WithdrawUsdcFragment
import com.Lyber.dev.utils.CommonMethods
import com.Lyber.dev.utils.CommonMethods.Companion.requestKeyboard
import com.Lyber.dev.utils.CommonMethods.Companion.showToast
import com.Lyber.dev.utils.Constants
import com.au.countrycodepicker.CountryPicker


class EnterPhoneNumberBottomSheetFragment(private val handle:(String, Boolean) -> Unit
) :
    BaseBottomSheet<FragmentEnterPhoneNumberBottomSheetBinding>() {
    override fun bind() = FragmentEnterPhoneNumberBottomSheetBinding.inflate(layoutInflater)
    private var countryCode = "33"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this.binding.etPhone.requestFocus()
        binding.etPhone.requestKeyboard()

        binding.tvCountryCode.setOnClickListener {
            CountryPicker.Builder().with(requireContext())
                .listener {
                    binding.tvCountryCode.text = it.dialCode
                    countryCode = it.dialCode
                }
                .style(R.style.CountryPickerStyle)
                .sortBy(CountryPicker.SORT_BY_NAME)
                .build()
                .showDialog(
                    requireActivity() as AppCompatActivity,
                    R.style.CountryPickerStyle,
                    false
                )
        }
        binding.btnSendCrypto.setOnClickListener {
            if (verifyMobile()) {
                val mobile=binding.etPhone.text.trim().toString()
                val modifiedMobile =
                    if (mobile.startsWith("0")) mobile.removeRange(
                        0,
                        1
                    ) else mobile
                val phnNo =countryCode.replace("+","")+ modifiedMobile
                handle!!.invoke(phnNo,false)
                dismiss()
            }
        }
    }

    private fun verifyMobile(): Boolean {
        return when {
            binding.etPhone.text.trim().toString().isEmpty() -> {
                getString(R.string.please_enter_phone_number).showToast(
                    binding.root,
                    requireContext()
                )
                false
            }

            binding.etPhone.text.trim().toString().length !in 7..15 -> {
                getString(R.string.please_enter_valid_phone_number).showToast(
                    binding.root,
                    requireContext()
                )
                false
            }

            else -> true
        }
    }
}