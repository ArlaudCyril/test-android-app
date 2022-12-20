package com.au.lyber.ui.fragments

import android.os.Bundle
import android.view.View
import com.au.lyber.R
import com.au.lyber.databinding.FragmentCreatePinBinding
import com.au.lyber.utils.CommonMethods.Companion.getViewModel
import com.au.lyber.utils.CommonMethods.Companion.replace
import com.au.lyber.utils.CommonMethods.Companion.requestKeyboard
import com.au.lyber.utils.OnTextChange
import com.au.lyber.viewmodels.ProfileViewModel

class CreateNewPinFragment : BaseFragment<FragmentCreatePinBinding>() {

    private lateinit var viewModel: ProfileViewModel
    private val pin: String get() = binding.etCreatePin.text.trim().toString()

    override fun bind() = FragmentCreatePinBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = getViewModel(requireParentFragment())

        binding.etCreatePin.addTextChangedListener(onTextChange)
        binding.etCreatePin.requestKeyboard()
    }


    /* On Text Change */
    private val onTextChange = object : OnTextChange {
        override fun onTextChange() {
            changeDots(pin.count())
        }
    }

    private fun clearFields() {
        binding.etCreatePin.setText("")
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
            viewModel.createPin = pin
            (requireParentFragment() as UpdatePinFragment).replace(
                R.id.flUpdatePin,
                ConfirmNewPinFragment()
            )
            clearFields()
        }
    }

}