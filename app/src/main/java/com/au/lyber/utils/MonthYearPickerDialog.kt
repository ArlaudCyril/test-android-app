package com.au.lyber.utils

import android.app.AlertDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.au.lyber.R
import com.au.lyber.databinding.DatePickerDialogBinding
import java.util.*


class MonthYearPickerDialog : DialogFragment() {

    private var listener: OnDateSetListener? = null

    fun setListener(listener: OnDateSetListener?) {
        this.listener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireActivity())

        DatePickerDialogBinding.inflate(requireActivity().layoutInflater).apply {
            val cal: Calendar = Calendar.getInstance()
            monthPicker.minValue = 0
            monthPicker.maxValue = 11
            monthPicker.value = cal.get(Calendar.MONTH)
            val year: Int = cal.get(Calendar.YEAR)
            yearPicker.minValue = year
            yearPicker.maxValue = MAX_YEAR
            yearPicker.value = year
            builder.setView(root) // Add action buttons
                .setPositiveButton(
                    "Ok"
                ) { dialog, id ->
                    listener!!.onDateSet(
                        null,
                        yearPicker.value,
                        monthPicker.value,
                        0
                    )
                }
                .setNegativeButton(
                    R.string.cancel
                ) { dialog, id ->
                    this@MonthYearPickerDialog.dialog?.cancel()
                }
            return builder.create()
        }
    }

    companion object {
        private const val MAX_YEAR = 2099
    }
}