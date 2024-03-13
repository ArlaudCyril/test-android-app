package com.Lyber.ui.fragments

import android.R
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.widget.ListPopupWindow
import androidx.lifecycle.Lifecycle
import com.Lyber.databinding.FragmentExportOperationsBinding
import com.Lyber.ui.adapters.CustomArrayAdapter
import com.Lyber.ui.fragments.bottomsheetfragments.ConfirmationBottomSheet
import com.Lyber.ui.fragments.bottomsheetfragments.ErrorResponseBottomSheet
import com.Lyber.viewmodels.PortfolioViewModel
import com.Lyber.utils.App
import com.Lyber.utils.CommonMethods
import com.Lyber.utils.Constants
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.Month
import java.util.Calendar
import java.util.Locale


/**
 * This fragment is used for performing export operations

 */
class ExportOperationsFragment : BaseFragment<FragmentExportOperationsBinding>(), OnClickListener {

    private lateinit var viewModel: PortfolioViewModel
    override fun bind() = FragmentExportOperationsBinding.inflate(layoutInflater)
    lateinit var selectedDate: String
    lateinit var monthsList: List<String>

    var listPopupWindow: ListPopupWindow? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = CommonMethods.getViewModel(requireActivity())
        viewModel.listener = this

        viewModel.exportOperationResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                CommonMethods.dismissProgressDialog()
                if (it.success) {
                    viewModel.selectedOption = Constants.EXPORT_DONE
                    ConfirmationBottomSheet().show(childFragmentManager, "")
                } else {
                    ErrorResponseBottomSheet().show(childFragmentManager, "")
                }
                if (listPopupWindow != null)
                    listPopupWindow?.dismiss()
            }
        }
        binding.ivTopAction.setOnClickListener(this)
        binding.rlBalance.setOnClickListener(this)
        binding.btnExport.setOnClickListener(this)

        if (Build.VERSION.SDK_INT >= 26) {
            val currentDate = LocalDate.now()
            val currentYear = currentDate.year
            val currentMonth = currentDate.monthValue
            binding.tvExportMonth.text = "${
                Month.of(currentMonth).name.lowercase().replaceFirstChar { it.uppercase() }
            } $currentYear"
            selectedDate =
                CommonMethods.convertToYearMonthVersion26(binding.tvExportMonth.text.toString())
            monthsList = CommonMethods.getMonthsAndYearsBetweenWithApi26(
                App.prefsManager.user!!.registeredAt, CommonMethods.getCurrentDateTime()
            )
//            Log.d("months", "$monthsList") //dummy date="2023-01-15T13:26:56.642Z"
        } else {
            App.prefsManager.user
            val calendar = Calendar.getInstance()
            val currentYear = calendar.get(Calendar.YEAR)
            val currentMonth = calendar.get(Calendar.MONTH) + 1  // Month is 0-based, so add 1
            binding.tvExportMonth.text = "${getMonthName(currentMonth)} $currentYear"
            selectedDate = CommonMethods.convertToYearMonth(binding.tvExportMonth.text.toString())
            monthsList = CommonMethods.getMonthsAndYearsBetween(
                App.prefsManager.user!!.registeredAt, CommonMethods.getCurrentDateTime()
            )
//            Log.d("months", "$monthsList")
        }

    }

    fun getMonthName(monthNumber: Int): String {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.MONTH, monthNumber - 1)  // Month is 0-based, so subtract 1
        val date = calendar.time
        val sdf = SimpleDateFormat("MMMM", Locale.getDefault())
        return sdf.format(date).lowercase().replaceFirstChar { it.uppercase() }
    }

    override fun onClick(v: View?) {
        binding.apply {
            when (v!!) {
                ivTopAction -> requireActivity().onBackPressedDispatcher.onBackPressed()
                rlBalance -> {

                    listPopupWindow = setListPopupWindowForView(
                        binding.viewPopup, monthsList
                    )
                    if (listPopupWindow != null) {
                        listPopupWindow!!.setOnItemClickListener { adapterView, view, pos, l ->

                            if (Build.VERSION.SDK_INT >= 26)
                                selectedDate =
                                    CommonMethods.convertToYearMonthVersion26(monthsList[pos])
                            else
                                selectedDate = CommonMethods.convertToYearMonth(monthsList[pos])
                            binding.tvExportMonth.text = monthsList[pos]
                            listPopupWindow!!.dismiss()
                        }
                    }

                    Handler(Looper.getMainLooper()).postDelayed({
                        listPopupWindow!!.show()
                    }, 50)
                }

                btnExport -> {
                    if (::selectedDate.isInitialized)
                        try {
                            CommonMethods.checkInternet(requireContext()) {
                                CommonMethods.showProgressDialog(requireContext())
                                viewModel.getExportOperations(selectedDate)
                            }

                        } catch (e: IndexOutOfBoundsException) {
                            Log.i("error", e.message.toString())
                        }
                }
            }
        }
    }

    fun setListPopupWindowForView(
        textView: View,
        array: List<String>
    ): ListPopupWindow {
        val listPopupWindow = ListPopupWindow(requireContext())

//        val arrayAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, array)
        val arrayAdapter = CustomArrayAdapter(requireContext(), R.layout.simple_list_item_1, array)

        listPopupWindow.setAdapter(arrayAdapter)
        listPopupWindow.anchorView = textView
        val displayMetrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
        listPopupWindow.isModal = true
        return listPopupWindow
    }

    override fun onDestroy() {
        super.onDestroy()
        if (listPopupWindow != null)
            listPopupWindow!!.dismiss()
    }
}