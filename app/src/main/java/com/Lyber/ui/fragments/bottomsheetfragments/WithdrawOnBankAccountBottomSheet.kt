package com.Lyber.ui.fragments.bottomsheetfragments

import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.Lyber.R
import com.Lyber.databinding.FragmentTransactionDetailsBottomSheetBinding
import com.Lyber.databinding.FragmentWithdrawOnBankAccountBottomSheetBinding


class WithdrawOnBankAccountBottomSheet : BaseBottomSheet<FragmentWithdrawOnBankAccountBottomSheetBinding>() {
    override fun bind()= FragmentWithdrawOnBankAccountBottomSheetBinding.inflate(layoutInflater)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val text = getString(R.string.please_include)
        val clickableText = getString(R.string.clicking_here) // Get localized clickable text
        val spannableString = SpannableString(text)

        val startIndex = text.indexOf(clickableText)
        val endIndex = startIndex + clickableText.length


        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                dismiss()
                findNavController().navigate(R.id.contactUsFragment)
            }

            override fun updateDrawState(ds: TextPaint) {
                ds.color =  ContextCompat.getColor(requireContext(),R.color.purple_500)
                ds.isUnderlineText = false
            }
        }


        spannableString.setSpan(clickableSpan, startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        binding.tvPleaseInclude.text = spannableString
        binding.tvPleaseInclude.movementMethod = LinkMovementMethod.getInstance() // Make links clickable
    }


}