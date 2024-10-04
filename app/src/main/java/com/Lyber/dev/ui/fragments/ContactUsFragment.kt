package com.Lyber.dev.ui.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.Lyber.dev.R
import com.Lyber.dev.databinding.FragmentContactUsBinding
import com.Lyber.dev.ui.activities.SplashActivity
import com.Lyber.dev.viewmodels.PortfolioViewModel
import com.Lyber.dev.utils.App
import com.Lyber.dev.utils.CommonMethods
import com.Lyber.dev.utils.CommonMethods.Companion.showToast
import com.google.android.gms.tasks.Task
import com.google.android.play.core.integrity.StandardIntegrityManager
import org.json.JSONObject


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
                    getString(R.string.msgHasBeenSent).showToast(binding.root,requireContext())
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
            }
        }
        var ts = getString(R.string.send_us_email)
        // Create a SpannableString from the full text
        val spannableString = SpannableString(ts)


        // Define the start and end indexes of the email address
        val startIndex = ts.indexOf("contact@lyber.com")
        val endIndex = startIndex + "contact@lyber.com".length

        val color = ContextCompat.getColor(requireContext(), R.color.purple_500)
        val colorSpan = ForegroundColorSpan(color)
        spannableString.setSpan(colorSpan, startIndex, endIndex, 0)
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
              openEmailApp("contact@lyber.com")
            }

            override fun updateDrawState(ds: TextPaint) {
                ds.color =  ContextCompat.getColor(requireContext(),R.color.purple_500)
                ds.isUnderlineText = false
            }
        }
        spannableString.setSpan(clickableSpan, startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        binding.tvSendEmail.text = spannableString
        binding.tvSendEmail.movementMethod = LinkMovementMethod.getInstance() // Make links clickable

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
                        getString(R.string.msgIsEmpty).showToast(binding.root,requireContext())
                    else
                        try {
                            CommonMethods.checkInternet(binding.root, requireContext()) {
                                val msg = binding.etMsg.text.trim().toString()
                                val jsonObject = JSONObject()
                                jsonObject.put("message", msg)
                                val jsonString = jsonObject.toString()
                                // Generate the request hash
                                val requestHash = CommonMethods.generateRequestHash(jsonString)

                                val integrityTokenResponse: Task<StandardIntegrityManager.StandardIntegrityToken>? =
                                    SplashActivity.integrityTokenProvider?.request(
                                        StandardIntegrityManager.StandardIntegrityTokenRequest.builder()
                                            .setRequestHash(requestHash)
                                            .build()
                                    )
                                integrityTokenResponse?.addOnSuccessListener { response ->
                                    CommonMethods.showProgressDialog(requireContext())
                                    viewModel.contactSupport(msg, token = response.token())
                                }?.addOnFailureListener { exception ->
                                    Log.d("token", "${exception}")

                                }
                            }
                        } catch (e: IndexOutOfBoundsException) {
                            Log.i("error", e.message.toString())
                        }
                }
            }
        }
    }
    private fun openEmailApp(receiverEmail: String) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:") // Only email apps should handle this
            putExtra(Intent.EXTRA_EMAIL, arrayOf(receiverEmail)) // Pass the receiver's email address
        }
        if (intent.resolveActivity(requireContext().packageManager) != null) {
            startActivity(intent)
        }
    }
}