package com.Lyber.dev.ui.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.ListPopupWindow
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Lifecycle
import com.Lyber.dev.databinding.FragmentExportOperationsBinding
import com.Lyber.dev.models.DataQuote
import com.Lyber.dev.models.MonthsList
import com.Lyber.dev.ui.activities.SplashActivity
import com.Lyber.dev.ui.fragments.bottomsheetfragments.ConfirmationBottomSheet
import com.Lyber.dev.ui.fragments.bottomsheetfragments.ErrorBottomSheet
import com.Lyber.dev.ui.fragments.bottomsheetfragments.ErrorResponseBottomSheet
import com.Lyber.dev.utils.App
import com.Lyber.dev.utils.CommonMethods
import com.Lyber.dev.utils.Constants
import com.Lyber.dev.viewmodels.PortfolioViewModel
import com.google.android.gms.tasks.Task
import com.google.android.play.core.integrity.StandardIntegrityManager
import com.google.gson.Gson
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.Month
import java.time.format.TextStyle
import java.util.Calendar
import java.util.Locale


/**
 * This fragment is used for performing export operations

 */
class ExportOperationsFragment : BaseFragment<FragmentExportOperationsBinding>(), OnClickListener {

    private lateinit var viewModel: PortfolioViewModel
    override fun bind() = FragmentExportOperationsBinding.inflate(layoutInflater)
    private lateinit var selectedDate: String
    private lateinit var monthsList: List<MonthsList>
    var loc: Locale = Locale.ENGLISH

    private lateinit var monthsAdapter: MonthsPopupAdapter

    private var listPopupWindow: ListPopupWindow? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = CommonMethods.getViewModel(requireActivity())
        viewModel.listener = this

        monthsAdapter = MonthsPopupAdapter()

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

        if (App.prefsManager.getLanguage() == Constants.FRENCH)
            loc = Locale.FRENCH

        if (Build.VERSION.SDK_INT >= 26) {
            val currentDate = LocalDate.now()
            val currentYear = currentDate.year
            val currentMonth = currentDate.monthValue

            binding.tvExportMonth.text = "${
                Month.of(currentMonth).getDisplayName(TextStyle.FULL, loc).toString().lowercase()
                    .replaceFirstChar { it.uppercase() }
            } $currentYear"
            selectedDate =
                CommonMethods.convertToYearMonthVersion26(
                    Month.of(currentMonth).getDisplayName(TextStyle.FULL, Locale.ENGLISH)
                        .toString() + " $currentYear"
                )
            monthsList = CommonMethods.getMonthsAndYearsBetweenWithApi26(
                App.prefsManager.user!!.registeredAt, CommonMethods.getCurrentDateTime()
            )
       } else {
            App.prefsManager.user
            val calendar = Calendar.getInstance()
            val currentYear = calendar.get(Calendar.YEAR)
            val currentMonth = calendar.get(Calendar.MONTH) + 1  // Month is 0-based, so add 1
            binding.tvExportMonth.text = "${getMonthName(currentMonth)} $currentYear"
            selectedDate = CommonMethods.convertToYearMonth(
                binding.tvExportMonth.text.toString())
            monthsList = CommonMethods.getMonthsAndYearsBetween(
                App.prefsManager.user!!.registeredAt, CommonMethods.getCurrentDateTime()
            )
        }

    }

    private fun getMonthName(monthNumber: Int): String {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.MONTH, monthNumber - 1)  // Month is 0-based, so subtract 1
        val date = calendar.time
        val sdf = SimpleDateFormat("MMMM", loc)
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
                        listPopupWindow!!.setOnItemClickListener { _, _, pos, _ ->

                            selectedDate = if (Build.VERSION.SDK_INT >= 26)
                                CommonMethods.convertToYearMonthVersion26(monthsList[pos].monthEnglish)
                            else
                                CommonMethods.convertToYearMonth(monthsList[pos].monthEnglish)
                            if (loc.language.equals(Constants.FRENCH, ignoreCase = true))
                                binding.tvExportMonth.text = monthsList[pos].monthFrench
                            else
                                binding.tvExportMonth.text = monthsList[pos].monthEnglish
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
                            CommonMethods.checkInternet(binding.root, requireContext()) {
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

    private fun setListPopupWindowForView(
        textView: View,
        array: List<MonthsList>
    ): ListPopupWindow {
        val listPopupWindow = ListPopupWindow(requireContext())

        monthsAdapter.setData(array)
        listPopupWindow.setAdapter(monthsAdapter)
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

    inner class MonthsPopupAdapter : android.widget.BaseAdapter() {

        private val list = mutableListOf<MonthsList>()


        fun setData(items: List<MonthsList>) {
            list.clear()
            list.addAll(items)
            notifyDataSetChanged()
        }

        override fun getCount(): Int {
            return list.count()
        }

        override fun getItem(position: Int): MonthsList {
            return list[position]
        }

        override fun getItemId(position: Int): Long {
            return 0
        }

        private val customTypeface: Typeface? =
            ResourcesCompat.getFont(requireContext(), com.Lyber.dev.R.font.mabry_pro)

        @SuppressLint("ViewHolder")
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val inflater =
                requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view = inflater.inflate(
                android.R.layout.simple_list_item_1,
                null
            ) // or use your custom layout

            val textView = view.findViewById<TextView>(android.R.id.text1)
            if (loc.language.equals(Constants.FRENCH, ignoreCase = true))
                textView.text = getItem(position).monthFrench
            else
                textView.text = getItem(position).monthEnglish
            textView.typeface = customTypeface
            return view
        }


    }

}