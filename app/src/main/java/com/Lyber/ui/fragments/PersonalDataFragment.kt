package com.Lyber.ui.fragments

import android.R
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.au.countrycodepicker.CountryPicker
import com.Lyber.databinding.FragmentPersonalDataBinding
import com.Lyber.utils.CommonMethods.Companion.dismissProgressDialog
import com.Lyber.utils.CommonMethods.Companion.getViewModel
import com.Lyber.utils.CommonMethods.Companion.requestKeyboard
import com.Lyber.utils.CommonMethods.Companion.showToast
import com.Lyber.utils.CommonMethods.Companion.takesAlphabetOnly
import com.Lyber.viewmodels.PersonalDataViewModel
import java.text.SimpleDateFormat
import java.util.*

class PersonalDataFragment : BaseFragment<FragmentPersonalDataBinding>(), View.OnClickListener {


    /* input fields */
    private val firstName: String get() = binding.etFirstName.text.trim().toString()
    private val lastName: String get() = binding.etLastName.text.trim().toString()
    private val birthPlace: String get() = binding.etBirthPlace.text.trim().toString()
    private val specifiedUsPerson: String get() = binding.etSpecifiedUsPerson.text.trim().toString()

    private var birthCountry: String = ""
    private var birthDate: String = ""
    private var nationality: String = ""

    private lateinit var specifiedUsPersonAdapter: ArrayAdapter<String>

    /* for date picker */
    private var mDay: Int = 0
    private var mMonth: Int = 0
    private var mYear: Int = 0

    private lateinit var viewModel: PersonalDataViewModel

    override fun bind() = FragmentPersonalDataBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireParentFragment() as FillDetailFragment).position = 0
        (requireParentFragment() as FillDetailFragment).setUpViews(0)

        specifiedUsPersonAdapter = ArrayAdapter(
            requireContext(), R.layout.simple_list_item_1,
            listOf("Yes", "No")
        )

        viewModel = getViewModel(requireParentFragment())

        viewModel.personDataResponse.observe(viewLifecycleOwner) {

            dismissProgressDialog()
            viewModel.personalData = it

//            binding.etFirstName.setText(it.first_name)
//            binding.etLastName.setText("${it.last_name}")

//            val countryBirth = CountryPicker.getCountryName(it.birth_country)
//            birthCountry = countryBirth.code
//            binding.etBirthCountry.setText(countryBirth.name)

//            binding.etBirthPlace.setText(it.birth_place)
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

//            birthDate = it.dob
            binding.etBirthDate.setText(outputFormat.format(inputFormat.parse(birthDate) ?: ""))

//            binding.etSpecifiedUsPerson.setText(if (it.specifiedUSPerson) "Yes" else "No")

            val national = CountryPicker.getCountryName(it.nationality)
            nationality = national.code
            binding.etNationality.setText(national.name)

        }


        val calender = Calendar.getInstance(TimeZone.getDefault())

        mDay = calender.get(Calendar.DAY_OF_MONTH)
        mMonth = calender.get(Calendar.MONTH)
        mYear = calender.get(Calendar.YEAR)

        binding.etSpecifiedUsPerson.setAdapter(specifiedUsPersonAdapter)
        binding.etBirthDate.setOnClickListener(this)
        binding.etNationality.setOnClickListener(this)
        binding.etBirthCountry.setOnClickListener(this)
        binding.etSpecifiedUsPerson.setOnClickListener(this)

        binding.etFirstName.takesAlphabetOnly()
        binding.etLastName.takesAlphabetOnly()
        binding.etFirstName.requestKeyboard()

        viewModel.email = ""
        viewModel.password = ""

    }

    fun checkData(): Boolean {
        when {
            firstName.isEmpty() -> {
                binding.etFirstName.requestKeyboard()
                getString(com.Lyber.R.string.please_enter_your_first_name).showToast(requireContext())
            }
            lastName.isEmpty() -> {
                binding.etLastName.requestKeyboard()
                getString(com.Lyber.R.string.please_enter_your_last_name).showToast(requireContext())
            }
            birthDate.isEmpty() -> getString(com.Lyber.R.string.please_enter_your_birth_date).showToast(requireContext())
            birthPlace.isEmpty() -> {
                binding.etBirthPlace.requestKeyboard()
                getString(com.Lyber.R.string.please_enter_your_birth_place).showToast(requireContext())
            }
            birthCountry.isEmpty() -> getString(com.Lyber.R.string.please_select_your_birth_country).showToast(requireContext())
            nationality.isEmpty() -> getString(com.Lyber.R.string.please_enter_your_nationality).showToast(requireContext())
            specifiedUsPerson.isEmpty() -> getString(com.Lyber.R.string.please_tell_us_that_you_have_us_citizenship_or_not).showToast(
                requireContext()
            )

            else -> {
                viewModel.let {
                    it.firstName = firstName
                    it.lastName = lastName
                    it.birthDate = birthDate
                    it.birthPlace = birthPlace
                    it.birthCountry = birthCountry
                    it.nationality = nationality
                    it.specifiedUsPerson = if (specifiedUsPerson == "Yes") 1 else 0
                }
                return true
            }
        }
        return false
    }

    @SuppressLint("SimpleDateFormat")
    private fun showDatePicker() {
        DatePickerDialog(
            requireContext(),
            com.Lyber.R.style.DatePickerTheme,
            { _, year, month, day ->
                val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                birthDate =
                    inputFormat.parse("$year-${month + 1}-$day")?.let { inputFormat.format(it) }
                        ?: ""
                val result = inputFormat.parse(birthDate)?.let { outputFormat.format(it) }
                binding.etBirthDate.setText(result)
            },
            mYear,
            mMonth,
            mDay
        ).apply {
            datePicker.maxDate = System.currentTimeMillis() - 18 * (31556952000) // 18 year old date
            show()
        }
    }

    override fun onClick(v: View?) {
        binding.apply {
            when (v!!) {
                // open date picker
                etBirthDate -> showDatePicker()

                // open country picker
                etNationality -> showCountryPicker()
                etBirthCountry -> showBirthCountryPicker()
                etSpecifiedUsPerson -> etSpecifiedUsPerson.showDropDown()

            }
        }
    }

    companion object {
        private const val TAG = "PersonalDataFragment"
    }

    private fun showBirthCountryPicker() {
        CountryPicker.Builder().with(requireContext())
            .listener {
                binding.etBirthCountry.setText(it.name)
                birthCountry = it.code
            }.style(com.Lyber.R.style.CountryPickerStyle)
            .sortBy(CountryPicker.SORT_BY_NAME)
            .build()
            .showDialog(
                requireActivity() as AppCompatActivity,
                com.Lyber.R.style.CountryPickerStyle,
                true
            )
    }

    private fun showCountryPicker() {
        CountryPicker.Builder().with(requireContext())
            .listener {
                binding.etNationality.setText(it.name)
                nationality = it.code
            }.style(com.Lyber.R.style.CountryPickerStyle)
            .sortBy(CountryPicker.SORT_BY_NAME)
            .build()
            .showDialog(
                requireActivity() as AppCompatActivity,
                com.Lyber.R.style.CountryPickerStyle,
                true
            )

    }

}