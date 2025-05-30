package com.Lyber.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Lifecycle
import com.Lyber.R
import com.Lyber.databinding.FragmentAddBankBinding
import com.Lyber.utils.CommonMethods.Companion.checkInternet
import com.Lyber.utils.CommonMethods.Companion.dismissProgressDialog
import com.Lyber.utils.CommonMethods.Companion.getViewModel
import com.Lyber.utils.CommonMethods.Companion.showProgressDialog
import com.Lyber.utils.CommonMethods.Companion.showToast
import com.Lyber.viewmodels.ProfileViewModel

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
                    getString(R.string.please_enter_iban_number).showToast(binding.root,requireContext())
                    binding.etIBan.requestFocus()
                }

                bic.isEmpty() -> {
                    getString(R.string.please_enter_bic_number).showToast(binding.root,requireContext())
                    binding.etBicNumber.requestFocus()
                }

                else -> {
                    checkInternet(binding.root,requireContext()) {
                        showProgressDialog(requireContext())
                        viewModel.addBankInfo(iban, bic)
                    }
                }
            }
        }
    }

}