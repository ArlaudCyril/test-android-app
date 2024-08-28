package com.Lyber.dev.ui.fragments

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Html
import android.view.View
import com.Lyber.dev.R
import com.Lyber.dev.databinding.FragmentVerificationEmailBinding
import com.Lyber.dev.utils.CommonMethods
import com.Lyber.dev.utils.CommonMethods.Companion.showProgressDialog
import com.Lyber.dev.utils.CommonMethods.Companion.showToast
import com.Lyber.dev.viewmodels.PersonalDataViewModel
/*
    **Not in use right not
 */
class VerificationEmailAddressFragment : BaseFragment<FragmentVerificationEmailBinding>() {

    private lateinit var viewModel: PersonalDataViewModel
    private var canCheck: Boolean = false
    override fun bind() = FragmentVerificationEmailBinding.inflate(layoutInflater)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireParentFragment() as FillDetailFragment).position = 2
        (requireParentFragment() as FillDetailFragment).setUpViews(2)

        canCheck = false
        viewModel = CommonMethods.getViewModel(requireParentFragment())

       /* viewModel.resendEmailResponse.observe(viewLifecycleOwner) {
            CommonMethods.dismissProgressDialog()
            it.message.showToast(requireContext())
        }*/

        /*binding.tvOpenApple.setOnClickListener {
            (requireParentFragment() as FillPersonalDetailFragment).moveToNext()
//            try {
//                val intent = Intent(Intent.ACTION_MAIN)
//                intent.addCategory(Intent.CATEGORY_APP_EMAIL)
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                startActivity(Intent.createChooser(intent, "Email"))
//                canCheck = true
//            } catch (e: ActivityNotFoundException) {
//                "Application Not Found".showToast(requireContext())
//            } catch (e: Exception) {
//            }
        }*/
        binding.tvOpenGmail.setOnClickListener {

            try {
                val intent = Intent(Intent.ACTION_MAIN)
                intent.addCategory(Intent.CATEGORY_APP_EMAIL)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(Intent.createChooser(intent, "Email"))
                canCheck = true
            } catch (e: ActivityNotFoundException) {
                getString(R.string.application_not_found).showToast(binding.root,requireContext())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        binding.tvResendEmail.setOnClickListener {
            showProgressDialog(requireContext())
            Handler(Looper.getMainLooper()).postDelayed({
                CommonMethods.dismissProgressDialog()
                getString(R.string.email_sent_successfully).showToast(binding.root,requireContext())
            }, 2000)

//            CommonMethods.checkInternet(binding.root,requireContext()) {
//                CommonMethods.showProgressDialog(requireContext())
//                viewModel.sendEmail(true)
//            }

        }
    }

    fun checkVerificationStatus() {
        CommonMethods.checkInternet(binding.root,requireContext()) {
            CommonMethods.showProgressDialog(requireContext())
            viewModel.emailVerification()
        }
    }

    override fun onResume() {
        super.onResume()
        val string =
            getString(
                R.string.we_have_sent_an_email_to_b_b_check_your_mailbox_and_click_on_the_confirmation_link_to_continue,
                viewModel.email
            )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            binding.tvSubTitle.text = Html.fromHtml(string, 0)
        } else binding.tvSubTitle.text = Html.fromHtml(string)

    }

}