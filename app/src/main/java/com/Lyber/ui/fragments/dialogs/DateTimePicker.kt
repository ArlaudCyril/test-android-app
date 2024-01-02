package com.Lyber.ui.fragments.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import com.Lyber.R
import com.Lyber.databinding.DialogDateTimePickerBinding
import com.Lyber.utils.CommonMethods
import java.util.Calendar
import java.util.Locale

class DateTimePicker(private val context: Context, private val listener  : OnDialogClickListener): Dialog(context, R.style.TransparentDilaog) {
    private lateinit var binding : DialogDateTimePickerBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogDateTimePickerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setCancelable(true)
        binding.rlContainer.layoutParams.width = (CommonMethods.getScreenWidth(context) * (0.85)).toInt()
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)-18
        val calander = Calendar.getInstance()
        calander.set(Calendar.YEAR,currentYear)
        binding.datePicker.maxDate = calander.time
        binding.datePicker.setCustomLocale(Locale.getDefault())
        binding.datePicker.addOnDateChangedListener { _, date ->
            listener.onDateSelected(date.time)
        }
        binding.btnCancel.setOnClickListener {
            dismiss()
        }
        binding.btnNext.setOnClickListener {
            dismiss()
        }
    }
    interface OnDialogClickListener {
        fun onDateSelected(year:Long)
    }
}