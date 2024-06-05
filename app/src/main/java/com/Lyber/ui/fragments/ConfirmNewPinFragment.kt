package com.Lyber.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.Lyber.R
import com.Lyber.databinding.FragmentConfirmPinBinding
import com.Lyber.utils.App
import com.Lyber.utils.CommonMethods
import com.Lyber.utils.CommonMethods.Companion.getViewModel
import com.Lyber.utils.CommonMethods.Companion.requestKeyboard
import com.Lyber.utils.CommonMethods.Companion.showProgressDialog
import com.Lyber.utils.CommonMethods.Companion.showToast
import com.Lyber.utils.OnTextChange
import com.Lyber.viewmodels.ProfileViewModel
import okhttp3.ResponseBody
/*
        Not in use
 */
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
            } else getString(R.string.pin_doesn_t_matches).showToast(requireContext())
        }
    }

    override fun onRetrofitError(responseBody: ResponseBody?) {
          clearField()
    }


}