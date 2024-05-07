package com.Lyber.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import com.Lyber.R
import com.Lyber.databinding.FragmentContactUsBinding
import com.Lyber.viewmodels.PortfolioViewModel
import com.Lyber.utils.App
import com.Lyber.utils.CommonMethods
import com.Lyber.utils.CommonMethods.Companion.showToast


class ContactUsFragment : BaseFragment<FragmentContactUsBinding>(), OnClickListener {
    override fun bind() = FragmentContactUsBinding.inflate(layoutInflater)
    private lateinit var viewModel: PortfolioViewModel

    @SuppressLint("SuspiciousIndentation")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = CommonMethods.getViewModel(requireActivity())
        viewModel.listener = this

        viewModel.msgResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                CommonMethods.dismissProgressDialog()
                if (it.success) {
                    getString(R.string.msgHasBeenSent).showToast(requireContext())
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
            }
        }
        var ts = getString(R.string.send_us_email)
        // Create a SpannableString from the full text
        val spannableString = SpannableString(ts)
//        //for english
//        val startIndex = 20 // 28
//        val endIndex = 37 //44
//        //for french

        // Define the start and end indexes of the email address
        val startIndex = ts.indexOf("contact@lyber.com")
        val endIndex = startIndex + "contact@lyber.com".length

        val color = ContextCompat.getColor(requireContext(), R.color.purple_500)
        val colorSpan = ForegroundColorSpan(color)
        spannableString.setSpan(colorSpan, startIndex, endIndex, 0)
        val underlineSpan = UnderlineSpan()
        spannableString.setSpan(underlineSpan, startIndex, endIndex, 0)
        binding.tvSendEmail.text = spannableString
        binding.tvWillGetBack.text = getString(R.string.will_get_back)
        if (App.prefsManager.user!!.email.isNotEmpty())
            binding.tvWillGetBack.append(" ${App.prefsManager.user!!.email}")
        binding.btnSend.setOnClickListener(this)
        binding.ivTopAction.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        binding.apply {
            when (v!!) {
                ivTopAction -> requireActivity().onBackPressedDispatcher.onBackPressed()
                btnSend -> {
                    if (binding.etMsg.text.trim().toString().isEmpty())
                        getString(R.string.msgIsEmpty).showToast(requireContext())
                    else
                        try {
                            CommonMethods.checkInternet(requireContext()) {
                                var msg = binding.etMsg.text.trim().toString()
                                CommonMethods.showProgressDialog(requireContext())
                                viewModel.contactSupport(msg)
                            }

                        } catch (e: IndexOutOfBoundsException) {
                            Log.i("error", e.message.toString())
                        }
                }
            }
        }
    }

}