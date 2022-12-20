package com.au.lyber.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import com.au.lyber.R
import com.au.lyber.databinding.FragmentConfirmPinBinding
import com.au.lyber.utils.CommonMethods
import com.au.lyber.utils.CommonMethods.Companion.getViewModel
import com.au.lyber.utils.CommonMethods.Companion.requestKeyboard
import com.au.lyber.utils.CommonMethods.Companion.showProgressDialog
import com.au.lyber.utils.CommonMethods.Companion.showToast
import com.au.lyber.utils.OnTextChange
import com.au.lyber.viewmodels.ProfileViewModel
import okhttp3.ResponseBody

class ConfirmNewPinFragment : BaseFragment<FragmentConfirmPinBinding>() {

    private lateinit var viewModel: ProfileViewModel
    private val confirmPin: String get() = binding.etConfirmPin.text.trim().toString()

    override fun bind() = FragmentConfirmPinBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = getViewModel(requireParentFragment())
        viewModel.listener = this


        binding.etConfirmPin.addTextChangedListener(onTextChange)
        binding.etConfirmPin.requestKeyboard()

    }

    /* On Text Change */
    private val onTextChange = object : OnTextChange {
        override fun onTextChange() {
            changeDots(confirmPin.count())
        }
    }

    private fun clearField() {
        binding.etConfirmPin.setText("")
    }

    /* Present the dots */
    private fun changeDots(pinCount: Int) {

        binding.llPinIndicators.let {
            if (pinCount == 0)
                for (i in 0 until it.childCount) {
                    val imageView = it.getChildAt(i)
                    imageView.setBackgroundResource(R.drawable.circle_dot_unselected)
                }
            else
                for (i in 0 until it.childCount) {
                    val imageView = it.getChildAt(i)
                    if (i <= pinCount - 1)
                        imageView.setBackgroundResource(R.drawable.circle_dot_selected)
                    else
                        imageView.setBackgroundResource(R.drawable.circle_dot_unselected)
                }
        }
        if (pinCount == 4) {
            if (viewModel.createPin == confirmPin) {
                CommonMethods.checkInternet(requireContext()) {
                    showProgressDialog(requireContext())
                    viewModel.updatePin(confirmPin)
                    clearField()
                }
            } else "Pin doesn't matches".showToast(requireContext())
        }
    }

    override fun onRetrofitError(responseBody: ResponseBody?) {
        super.onRetrofitError(responseBody)
        clearField()
    }


}