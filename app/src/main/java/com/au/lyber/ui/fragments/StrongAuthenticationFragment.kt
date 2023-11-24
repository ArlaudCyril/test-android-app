package com.au.lyber.ui.fragments

import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import androidx.lifecycle.Lifecycle
import com.au.lyber.databinding.FragmentStrongAuthenticationBinding
import com.au.lyber.ui.fragments.bottomsheetfragments.AuthenticationCodeBottomSheet
import com.au.lyber.utils.App
import com.au.lyber.utils.CommonMethods
import com.au.lyber.utils.CommonMethods.Companion.dismissProgressDialog
import com.au.lyber.utils.CommonMethods.Companion.getViewModel
import com.au.lyber.utils.Constants
import com.au.lyber.viewmodels.ProfileViewModel

class StrongAuthenticationFragment : BaseFragment<FragmentStrongAuthenticationBinding>(),
    View.OnClickListener {

    private lateinit var viewModel: ProfileViewModel

    private lateinit var bottomSheet: AuthenticationCodeBottomSheet

    override fun bind() = FragmentStrongAuthenticationBinding.inflate(layoutInflater)
    private var switchOff: Boolean = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bottomSheet = AuthenticationCodeBottomSheet()
        viewModel = getViewModel(this)
        viewModel.listener = this
        binding.tvEmail.text = "To: ${App.prefsManager.user?.email}"
        binding.tvNumber.text = "To: ${App.prefsManager.user?.phoneNo}"
        if (App.prefsManager.user?.scope2FA != null && App.prefsManager.user?.scope2FA!!.isNotEmpty())
            for (i in App.prefsManager.user?.scope2FA!!)
                when (i) {
            Constants.LOGIN -> binding.switchLoginAcc.isChecked = true
            Constants.WITHDRAWAL -> binding.switchValidateWithdraw.isChecked = true
            Constants.WHITELISTING -> binding.switchWhitelisting.isChecked = true
        }
        if (App.prefsManager.user?.type2FA != null && App.prefsManager.user?.type2FA!!.isNotEmpty()) if (App.prefsManager.user?.type2FA == Constants.EMAIL) binding.ivEmail.visibility =
            View.VISIBLE
        else if (App.prefsManager.user?.type2FA == Constants.PHONE) binding.ivSms.visibility =
            View.VISIBLE
        else binding.ivGoogle.visibility = View.VISIBLE



        viewModel.enableStrongAuthentication.observe(viewLifecycleOwner) {
            if (Lifecycle.State.RESUMED == lifecycle.currentState) {
                dismissProgressDialog()
                bottomSheet.show(childFragmentManager, "")
            }
        }

        viewModel.booleanResponse.observe(viewLifecycleOwner) {
            if (Lifecycle.State.RESUMED == lifecycle.currentState) {
                if (switchOff) {
                    val hash = hashMapOf<String, Any>()
                    if (binding.switchValidateWithdraw.isChecked)
                        hash["scope2FA"] = "withdrawal"
                    else if (binding.switchWhitelisting.isChecked)
                        hash["scope2FA"] = "whitelisting"
                    else
                        hash["scope2FA"] = ""
                    hash["otp"] = "111111"  //TODO for now
                    Log.d("hash","$hash")
                    viewModel.updateAuthentication(hash)
                } else
                    dismissProgressDialog()
            }
        }
        viewModel.verifyStrongAuthentication.observe(viewLifecycleOwner) {
            if (Lifecycle.State.RESUMED == lifecycle.currentState) {
                dismissProgressDialog()
                bottomSheet.dismiss()
                bottomSheet.clearFields()
            }
        }

        binding.switchWhitelisting.setOnCheckedChangeListener { button, isChecked ->
            if (button.isPressed) {
                if (isChecked) {
                    switchOff = false
                    CommonMethods.showProgressDialog(requireContext())
                    val hash = hashMapOf<String, Any>()
                    hash["scope2FA"] = listOf(Constants.WHITELISTING)
                    viewModel.updateAuthentication(hash)
                } else {
                    switchOff = true
                    CommonMethods.showProgressDialog(requireContext())
                    val json = """{"scope2FA":["whitelisting"]}""".trimMargin()
                    val detail = encodeToBase64(json)
                    viewModel.switchOffAuthentication(detail, "scope")
                }
            }
        }
        binding.switchValidateWithdraw.setOnCheckedChangeListener { button, isChecked ->
            if (button.isPressed) {
                if (isChecked) {
                    switchOff = false
                    CommonMethods.showProgressDialog(requireContext())
                    val hash = hashMapOf<String, Any>()
                    hash["scope2FA"] = listOf(Constants.WITHDRAWAL)
                    viewModel.updateAuthentication(hash)
                } else {
                    switchOff = true
                    CommonMethods.showProgressDialog(requireContext())
                    val json = """{"scope2FA":["withdrawal"]}""".trimMargin()
                    val detail = encodeToBase64(json)
                    viewModel.switchOffAuthentication(detail, "scope")
                }
            }
        }

        binding.ivTopAction.setOnClickListener(this)
    }

    fun encodeToBase64(input: String): String {
        val bytes = input.toByteArray(Charsets.UTF_8)
        val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
        return base64
    }


    override fun onClick(v: View?) {
        binding.apply {
            when (v!!) {
                ivTopAction -> requireActivity().onBackPressed()
            }
        }
    }


}