package com.Lyber.dev.ui.fragments

import android.app.Activity
import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Filter
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import com.Lyber.dev.R
import com.Lyber.dev.databinding.FragmentAddressBinding
import com.Lyber.dev.models.AddressDataLocal
import com.Lyber.dev.utils.App
import com.Lyber.dev.utils.CommonMethods
import com.Lyber.dev.utils.CommonMethods.Companion.requestKeyboard
import com.Lyber.dev.utils.CommonMethods.Companion.showToast
import com.Lyber.dev.utils.CommonMethods.Companion.takesAlphabetOnly
import com.Lyber.dev.viewmodels.PersonalDataViewModel
import com.au.countrycodepicker.CountryPicker
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import org.json.JSONException


class AddressFragment : BaseFragment<FragmentAddressBinding>() {

    private var streetNumber = ""
    private var streetAddress = ""
    private var selectedCountry=""
    private val city: String get() = binding.etCity.text.trim().toString()
    private val zipCode: String get() = binding.etZipCode.text.trim().toString()
    private val country: String get() = binding.etCountry.text.trim().toString()

    private val specifiedUsPerson: String get() = binding.etSpecifiedUsPerson.text.trim().toString()
    private lateinit var specifiedUsPersonAdapter: ArrayAdapter<String>
    private lateinit var personalDataViewModel: PersonalDataViewModel

    override fun bind() = FragmentAddressBinding.inflate(layoutInflater)
    override fun onResume() {
        super.onResume()
        // Reinitialize adapter and set it again when the fragment is resumed
        specifiedUsPersonAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            listOf(
                requireContext().getString(R.string.yes_t),
                requireContext().getString(R.string.no_t)
            )
        )
        binding.etSpecifiedUsPerson.setAdapter(specifiedUsPersonAdapter)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireParentFragment() as FillDetailFragment).position = 0
        (requireParentFragment() as FillDetailFragment).setUpViews(0)
        (requireParentFragment() as FillDetailFragment).binding.ivTopAction.setBackgroundResource(
            R.drawable.ic_close
        )
        if (streetNumber.isNotEmpty())
            binding.tvSearchAddress.text = streetNumber + ", " + streetAddress
        else if (streetAddress.isNotEmpty())
            binding.tvSearchAddress.text = streetAddress
        specifiedUsPersonAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            listOf(
                requireContext().getString(R.string.yes_t),
                requireContext().getString(R.string.no_t)
            )
        )
        personalDataViewModel = CommonMethods.getViewModel(requireParentFragment())
        binding.etCountry.setOnClickListener { openCountryPicker() }

        personalDataViewModel.personalData?.let {

            binding.etCountry.setText(country)
        }
        if (personalDataViewModel.isReview) {
            App.prefsManager.addressDataLocal.let {
                binding.apply {
                    if (it!!.streetNumber.isNotEmpty()) {
                        streetNumber = it.streetNumber
                        streetAddress = it.streetAddress
                        tvSearchAddress.text = it.streetNumber + ", " + it.streetAddress
                    } else {
                        streetAddress = it.streetAddress
                        tvSearchAddress.text = it.streetAddress
                    }
                    etCity.setText(it.city)
                    etZipCode.setText(it.zipCode)
                    etCountry.setText(it.country)
                    etSpecifiedUsPerson.setText(it.specifiedUsPerson)

                }
            }
        }

        binding.etCity.takesAlphabetOnly()
        binding.etSpecifiedUsPerson.setAdapter(specifiedUsPersonAdapter)
        binding.etSpecifiedUsPerson.setOnClickListener {

            binding.etSpecifiedUsPerson.showDropDown()
        }
        binding.tvSearchAddress.setOnClickListener {
            val fields = listOf(
                Place.Field.ID, Place.Field.NAME,
                Place.Field.ADDRESS, Place.Field.ADDRESS_COMPONENTS
            )
            val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                .setCountries(listOf(selectedCountry))
                .build(requireContext())
            val options = ActivityOptionsCompat.makeCustomAnimation(
                requireContext(), // Context
                R.anim.enter_from_bottom, // Animation for enter
                R.anim.fade_out // Animation for exit (you can create another custom animation or use the built-in ones)
            )
            startAutocomplete.launch(intent, options)
        }
    }

    private val startAutocomplete =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val intent = result.data
                if (intent != null) {
                    val place = Autocomplete.getPlaceFromIntent(intent)
                    try {
                        var city = ""
                        var zipcode = ""
                        var adr = ""
                        for (component in place.addressComponents.asList()) {
                            if ("locality" in component.types) {
                                city = component.name
                            }
                            if ("postal_code" in component.types) {
                                zipcode = component.shortName
                                zipcode = zipcode.filter { it.isDigit() }
                            }
                        }
                        if (place.name.isNotEmpty()) {
                            adr = place.name + ", " + adr
                            val result = separateAddress(place.name)
                            if (result != null) {
                                val (number, street) = result
                                streetNumber=number
                                adr=street
                            }
                        }
                        streetAddress = adr
                        Log.i(
                            TAG, "city: ${streetNumber} ${streetAddress}"
                        )
                        if (streetNumber.isNotEmpty())
                            binding.tvSearchAddress.text = streetNumber + " " + adr
                        else
                            binding.tvSearchAddress.text = adr
                        binding.etCity.setText(city)
                        binding.etZipCode.setText(zipcode)
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        Log.e("Error", "Error parsing JSON response")
                    }

                }
            } else if (result.resultCode == Activity.RESULT_CANCELED) {
                // The user canceled the operation.
                Log.i(TAG, "User canceled autocomplete")
            }
        }
    fun separateAddress(address: String): Pair<String, String>? {
        val regex = Regex("^\\s*(\\d+[a-zA-Z]?)(\\s+.*)")
        val matchResult = regex.find(address)

        matchResult?.let { match ->
            if (match.groupValues.size == 3) {
                val number = address.substring(match.groups[1]!!.range).trim()
                val street = address.substring(match.groups[2]!!.range).trim()
                return Pair(number, street)
            }
        }

        return Pair("", address)
    }
    private fun openCountryPicker() {
        CountryPicker.Builder().with(requireContext())
            .listener {
                binding.etCountry.setText(it.name)
                selectedCountry=it.code
            }.style(R.style.CountryPickerStyle).sortBy(CountryPicker.SORT_BY_NAME).build()
            .showDialog(requireActivity() as AppCompatActivity, R.style.CountryPickerStyle, true)
    }

    fun checkData(): Boolean {
        when {
            country.isEmpty() -> getString(R.string.please_enter_country_name).showToast(
                binding.root,requireContext()
            )

            streetAddress.isEmpty() -> {
                getString(R.string.please_enter_address_).showToast(binding.root,requireContext())
            }


            city.isEmpty() -> {
                getString(R.string.please_enter_city_name).showToast(binding.root,requireContext())
                binding.etCity.requestKeyboard()
            }


            zipCode.isEmpty() -> {
                getString(R.string.please_enter_zip_code).showToast(binding.root,requireContext())
                binding.etZipCode.requestKeyboard()
            }


            specifiedUsPerson.isEmpty() -> getString(R.string.please_tell_us_that_you_have_us_citizenship_or_not).showToast(
                binding.root,   requireContext()
            )

            else -> {
                val addressDataLocal = AddressDataLocal(
                    streetNumber = streetNumber, streetAddress =
                    streetAddress, city = city, zipCode = zipCode, country = country,
                    specifiedUsPerson = specifiedUsPerson
                )
                App.prefsManager.addressDataLocal = addressDataLocal
                personalDataViewModel.let {
                    it.streetNumber = streetNumber
                    it.street = streetAddress
                    it.city = city
                    it.zipCode = zipCode
                    it.country = country
                    it.completeAddress = "$streetNumber,$streetAddress,$city,$country,$zipCode"
                    it.specifiedUsPerson =
                        if (specifiedUsPerson == requireContext().getString(R.string.yes_t)) 1 else 0

                    return true
                }
            }
        }
        return false
    }

}