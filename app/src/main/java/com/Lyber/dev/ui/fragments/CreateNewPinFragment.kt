package com.Lyber.dev.ui.fragments

import android.os.Bundle
import android.view.View
import com.Lyber.dev.R
import com.Lyber.dev.databinding.FragmentCreatePinBinding
import com.Lyber.dev.utils.CommonMethods.Companion.getViewModel
import com.Lyber.dev.utils.CommonMethods.Companion.replace
import com.Lyber.dev.utils.CommonMethods.Companion.requestKeyboard
import com.Lyber.dev.utils.OnTextChange
import com.Lyber.dev.viewmodels.ProfileViewModel
/*
        Not in use
 */
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