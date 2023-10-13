package com.au.lyber.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.au.countrycodepicker.CountryPicker
import com.au.lyber.R
import com.au.lyber.databinding.FragmentAddressBinding
import com.au.lyber.models.AddressDataLocal
import com.au.lyber.utils.App
import com.au.lyber.utils.CommonMethods
import com.au.lyber.utils.CommonMethods.Companion.requestKeyboard
import com.au.lyber.utils.CommonMethods.Companion.showToast
import com.au.lyber.utils.CommonMethods.Companion.takesAlphabetOnly
import com.au.lyber.utils.Constants
import com.au.lyber.viewmodels.PersonalDataViewModel

class AddressFragment : BaseFragment<FragmentAddressBinding>() {

    /* input fields */
    private val streetHouseNumber: String get() = binding.etStreetNumber.text.trim().toString()
    private val buildingFloor: String get() = binding.etBuildingNumberFloor.text.trim().toString()
    private val city: String get() = binding.etCity.text.trim().toString()
    private val zipCode: String get() = binding.etZipCode.text.trim().toString()
    private val country: String get() = binding.etCountry.text.trim().toString()
    private val state: String get() = binding.etState.text.trim().toString()


    private lateinit var personalDataViewModel: PersonalDataViewModel

    override fun bind() = FragmentAddressBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireParentFragment() as FillDetailFragment).position = 1
        (requireParentFragment() as FillDetailFragment).setUpViews(1)
        (requireParentFragment() as FillDetailFragment).binding.ivTopAction.setBackgroundResource(
            R.drawable.ic_close
        )

        personalDataViewModel = CommonMethods.getViewModel(requireParentFragment())
        binding.etCountry.setOnClickListener { openCountryPicker() }

        personalDataViewModel.personalData?.let {

            binding.etCountry.setText(country)
        }
        if (personalDataViewModel.isReview){
            App.prefsManager.addressDataLocal.let {
                binding.apply {
                    etStreetNumber.setText(it!!.streetNumber)
                    etBuildingNumberFloor.setText(it.buildingFloorName)
                    etCity.setText(it.city)
                    etState.setText(it.state)
                    etZipCode.setText(it.zipCode)
                    etCountry.setText(it.country)

                }
            }
        }

        binding.etCity.takesAlphabetOnly()
        binding.etState.takesAlphabetOnly()

        binding.etStreetNumber.requestKeyboard()
    }

    private fun openCountryPicker() {
        CountryPicker.Builder().with(requireContext())
            .listener {
                binding.etCountry.setText(it.name)
            }.style(R.style.CountryPickerStyle).sortBy(CountryPicker.SORT_BY_NAME).build()
            .showDialog(requireActivity() as AppCompatActivity, R.style.CountryPickerStyle, true)
    }

    fun checkData(): Boolean {
        when {
       /*     streetHouseNumber.isEmpty() -> {
                getString(R.string.please_enter_street_number).showToast(requireContext())
                binding.etStreetNumber.requestKeyboard()
            }
*/
         /*   buildingFloor.isEmpty() -> {
                getString(R.string.please_enter_building_name_or_floor_name).showToast(
                    requireContext()
                )
                binding.etBuildingNumberFloor.requestKeyboard()
            }*/

            city.isEmpty() -> {
                getString(R.string.please_enter_city_name).showToast(requireContext())
                binding.etCity.requestKeyboard()
            }

            state.isEmpty() -> {
                getString(R.string.please_enter_state).showToast(requireContext())
                binding.etState.requestKeyboard()
            }

            zipCode.isEmpty() -> {
                getString(R.string.please_enter_zip_code).showToast(requireContext())
                binding.etZipCode.requestKeyboard()
            }

            country.isEmpty() -> getString(R.string.please_enter_country_name).showToast(
                requireContext()
            )

            else -> {
                val addressDataLocal = AddressDataLocal(streetNumber = streetHouseNumber, buildingFloorName =
                buildingFloor, city = city,zipCode = zipCode,country= country,state = state)
                App.prefsManager.addressDataLocal = addressDataLocal
                personalDataViewModel.let {
                    it.streetNumber = streetHouseNumber
                    it.buildingFloorName = buildingFloor
                    it.city = city
                    it.zipCode = zipCode
                    it.country = country
                    it.state = state
                    it.completeAddress =
                        "$streetHouseNumber,$buildingFloor,$city,$state,$country,$zipCode"
                    return true
                }
            }
        }
        return false
    }

}