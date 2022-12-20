package com.au.lyber.ui.fragments

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getDrawable
import androidx.core.view.updatePadding
import com.au.lyber.R
import com.au.lyber.databinding.FragmentVerificationEmailBinding
import com.au.lyber.utils.CommonMethods
import com.au.lyber.utils.CommonMethods.Companion.getViewModel
import com.au.lyber.utils.CommonMethods.Companion.gone
import com.au.lyber.utils.CommonMethods.Companion.px
import com.au.lyber.utils.CommonMethods.Companion.setBackgroundTint
import com.au.lyber.utils.CommonMethods.Companion.showToast
import com.au.lyber.viewmodels.SignUpViewModel

class VerifyEmailLoginFragment : BaseFragment<FragmentVerificationEmailBinding>() {

    private lateinit var viewModel: SignUpViewModel
    override fun bind() = FragmentVerificationEmailBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.root.updatePadding(top = 20.px, bottom = 20.px)
        addButton()
        binding.tvResendEmail.gone()

        viewModel = getViewModel(requireParentFragment())

        (requireParentFragment() as SignUpFragment).binding.root.setBackgroundColor(Color.WHITE)

        binding.tvOpenGmail.setOnClickListener {

            try {
                val intent = Intent(Intent.ACTION_MAIN)
                intent.addCategory(Intent.CATEGORY_APP_EMAIL)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(Intent.createChooser(intent, "Email"))
            } catch (e: ActivityNotFoundException) {
                "Application Not Found".showToast(requireContext())
            } catch (e: Exception) {
            }
        }

        binding.tvResendEmail.setOnClickListener {
//            CommonMethods.checkInternet(requireContext()) {
//                CommonMethods.showProgressDialog(requireContext())
//                viewModel.sendEmail(true)
//            }
        }


    }

    private fun addButton() {
        Button(requireContext(), null, 0, R.style.ButtonStyle).let {
            it.background = getDrawable(requireContext(), R.drawable.button_purple_500)
            it.setTextColor(Color.WHITE)
            it.text = "Email Verified"
            it.gravity = Gravity.CENTER
            it.height = 44.px
            it.setOnClickListener {
                checkVerificationStatus()
            }
            binding.root.addView(it)
        }
    }

    private fun checkVerificationStatus() {
        CommonMethods.checkInternet(requireContext()) {
            CommonMethods.showProgressDialog(requireContext())
            viewModel.emailVerification()
        }
    }

    override fun onDestroyView() {
        (requireParentFragment() as SignUpFragment).binding.root.background =
            getDrawable(
                requireContext(),
                R.drawable.splash_background
            )
        super.onDestroyView()
    }

}