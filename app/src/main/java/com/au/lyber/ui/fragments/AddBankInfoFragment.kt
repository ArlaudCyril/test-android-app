package com.au.lyber.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Lifecycle
import com.au.lyber.databinding.FragmentAddBankBinding
import com.au.lyber.utils.App
import com.au.lyber.utils.CommonMethods.Companion.checkInternet
import com.au.lyber.utils.CommonMethods.Companion.dismissProgressDialog
import com.au.lyber.utils.CommonMethods.Companion.getViewModel
import com.au.lyber.utils.CommonMethods.Companion.showProgressDialog
import com.au.lyber.utils.CommonMethods.Companion.showToast
import com.au.lyber.viewmodels.ProfileViewModel

class AddBankInfoFragment : BaseFragment<FragmentAddBankBinding>() {

    private val iban: String get() = binding.etIBan.text.trim().toString()
    private val bic: String get() = binding.etBicNumber.text.trim().toString()

    private lateinit var viewModel: ProfileViewModel
    override fun bind() = FragmentAddBankBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = getViewModel(this)
        viewModel.listener = this

        viewModel.addBankAccount.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                dismissProgressDialog()
//                App.prefsManager.setBankInfo(iban, bic)
                requireActivity().supportFragmentManager.popBackStackImmediate()
                requireActivity().onBackPressed()
            }
        }


        binding.ivTopAction.setOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.btnAdd.setOnClickListener {
            when {

                iban.isEmpty() -> {
                    "Please enter IBAN number".showToast(requireContext())
                    binding.etIBan.requestFocus()
                }

                bic.isEmpty() -> {
                    "Please enter BIC number".showToast(requireContext())
                    binding.etBicNumber.requestFocus()
                }

                else -> {
                    checkInternet(requireContext()) {
                        showProgressDialog(requireContext())
                        viewModel.addBankInfo(iban, bic)
                    }
                }
            }
        }
    }

}