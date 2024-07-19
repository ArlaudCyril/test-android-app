package com.Lyber.ui.fragments

import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.View.OnClickListener
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.Lyber.R
import com.Lyber.databinding.FragmentAddRibBinding
import com.Lyber.models.RIBData
import com.Lyber.models.Strategy
import com.Lyber.ui.fragments.bottomsheetfragments.WithdrawalUsdcAddressBottomSheet
import com.Lyber.utils.App
import com.Lyber.utils.CommonMethods
import com.Lyber.utils.CommonMethods.Companion.gone
import com.Lyber.utils.CommonMethods.Companion.showToast
import com.Lyber.utils.CommonMethods.Companion.visible
import com.Lyber.utils.Constants
import com.Lyber.viewmodels.PortfolioViewModel
import com.au.countrycodepicker.CountryPicker
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class AddRibFragment : BaseFragment<FragmentAddRibBinding>(), OnClickListener {
    override fun bind() = FragmentAddRibBinding.inflate(layoutInflater)
    var selectedCountry = ""
    private lateinit var viewModel: PortfolioViewModel
    private lateinit var ribData: RIBData
    private var fromWithdraw = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = CommonMethods.getViewModel(requireActivity())
        viewModel.listener = this
        if (arguments != null && requireArguments().containsKey(Constants.TO_EDIT)) {
            ribData = Gson().fromJson<RIBData>(
                requireArguments().getString(Constants.TO_EDIT),
                object :
                    TypeToken<RIBData>() {}.type
            )
            binding.apply {
                etRibName.setText(ribData.name)
                etBic.setText(ribData.bic)
                etIBanNo.setText(ribData.iban)
                etOwnerName.setText(ribData.userName)
                selectedCountry = ribData.bankCountry
                tvBankCountry.text = ribData.bankCountry
            }
        } else if (arguments != null && requireArguments().containsKey(Constants.FROM) &&
            requireArguments().getString(Constants.FROM) == WithdrawalUsdcAddressBottomSheet::class.java.name
        )
            fromWithdraw = true
        binding.ivBack.setOnClickListener(this)
        binding.tvBankCountry.setOnClickListener(this)
        binding.btnAdd.setOnClickListener(this)
        binding.etOwnerName.addTextChangedListener(onTextChange)

        viewModel.booleanResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                if (it.success) {
                    viewModel.getWalletRib()
                }
            }
        }
        viewModel.exportOperationResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                CommonMethods.dismissProgressDialog()
                if (it.success) {
                    val hashMap = HashMap<String, Any>()
                    hashMap["iban"] = binding.etIBanNo.text.trim().toString()
                    hashMap["bic"] = binding.etBic.text.trim().toString()
                    hashMap["name"] = binding.etRibName.text.trim().toString()
                    hashMap["userName"] = binding.etOwnerName.text.trim().toString()
                    hashMap["bankCountry"] = selectedCountry
                    viewModel.addRib(hashMap)
                }
            }
        }
        viewModel.walletRibResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                CommonMethods.dismissProgressDialog()

                if (!it.data.isNullOrEmpty())
                    com.Lyber.ui.activities.BaseActivity.ribWalletList =
                        it.data as ArrayList<RIBData>
                if (fromWithdraw)
                    findNavController().navigate(R.id.action_addRibFragment_to_ribListingFragment)
                else
                    requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    private fun openCountryPicker() {
        CountryPicker.Builder().with(requireContext())
            .listener {
                binding.tvBankCountry.text = it.name
                selectedCountry = it.name
            }.style(R.style.CountryPickerStyle).sortBy(CountryPicker.SORT_BY_NAME).build()
            .showDialog(requireActivity() as AppCompatActivity, R.style.CountryPickerStyle, true)
    }

    override fun onClick(p0: View?) {
        binding.apply {
            when (p0) {
                ivBack -> requireActivity().onBackPressedDispatcher.onBackPressed()
                tvBankCountry -> openCountryPicker()
                btnAdd -> {
                    if (binding.etRibName.text.trim().toString().isEmpty())
                        getString(R.string.please_complete_all_fields).showToast(requireContext())
                    else if (binding.etIBanNo.text.trim().toString().isEmpty())
                        getString(R.string.please_complete_all_fields).showToast(requireContext())
                    else if (binding.etBic.text.trim().toString().isEmpty())
                        getString(R.string.please_complete_all_fields).showToast(requireContext())
                    else if (binding.etOwnerName.text.trim().toString().isEmpty())
                        getString(R.string.please_complete_all_fields).showToast(requireContext())
                    else if (selectedCountry.isEmpty())
                        getString(R.string.please_complete_all_fields).showToast(requireContext())
                    else if (!isValidInput(binding.etOwnerName.text.trim().toString()))
                        getString(R.string.owner_name_error).showToast(requireContext())
                    else {
                        CommonMethods.checkInternet(requireContext()) {
                            CommonMethods.showProgressDialog(requireContext())
                            val hashMap = HashMap<String, Any>()
                            hashMap["iban"] = binding.etIBanNo.text.trim().toString()
                            hashMap["bic"] = binding.etBic.text.trim().toString()
                            hashMap["name"] = binding.etRibName.text.trim().toString()
                            hashMap["userName"] = binding.etOwnerName.text.trim().toString()
                            hashMap["bankCountry"] = selectedCountry
                            if (::ribData.isInitialized) {
                                viewModel.deleteRIB(ribData.ribId)
                            } else viewModel.addRib(
                                hashMap
                            )
                        }
                    }
                }
            }
        }
    }
    private val onTextChange = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {}
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
           if(isValidInput(binding.etOwnerName.text.trim().toString()))
               binding.tvOwnerValid.gone()
            else if(binding.etOwnerName.text.trim().toString().isNotEmpty())
               binding.tvOwnerValid.visible()
        }

    }
    fun isValidInput(input: String): Boolean {
        val regex = Regex("^[a-zA-Z\\s-]+$")
        return regex.matches(input)
    }
}