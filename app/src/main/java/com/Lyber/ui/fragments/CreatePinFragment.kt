package com.Lyber.ui.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.activity.addCallback
import androidx.navigation.fragment.findNavController
import com.Lyber.R
import com.Lyber.databinding.FragmentCreatePinBinding
import com.Lyber.utils.App
import com.Lyber.utils.CommonMethods.Companion.getViewModel
import com.Lyber.utils.CommonMethods.Companion.gone
import com.Lyber.utils.CommonMethods.Companion.requestKeyboard
import com.Lyber.utils.CommonMethods.Companion.visible
import com.Lyber.utils.Constants
import com.Lyber.utils.OnTextChange
import com.Lyber.viewmodels.SignUpViewModel

class CreatePinFragment : BaseFragment<FragmentCreatePinBinding>() {
    override fun bind() = FragmentCreatePinBinding.inflate(layoutInflater)

    private val pin get() = binding.etCreatePin.text.trim().toString()

    private lateinit var viewModel: SignUpViewModel

    override fun onResume() {
        super.onResume()
        binding.etCreatePin.requestKeyboard()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        App.prefsManager.savedScreen = javaClass.name
        viewModel = getViewModel(requireParentFragment())
        viewModel.forLogin = requireArguments().getBoolean(Constants.FOR_LOGIN, false)
        binding.etCreatePin.addTextChangedListener(onTextChange)
        if (requireArguments().containsKey(Constants.IS_CHANGE_PIN)
                && requireArguments().getBoolean(Constants.IS_CHANGE_PIN)
        ) {
            binding.ivTopAction.visible()
            binding.llIndicators.gone()
        } else if (arguments != null && requireArguments().containsKey(Constants.FOR_LOGIN) && !requireArguments().getBoolean(
                        Constants.FOR_LOGIN
                )) {
            binding.llIndicators.visible()
            binding.ivTopClose.visible()
            binding.ivTopAction.gone()
            requireActivity().onBackPressedDispatcher.addCallback(this) {
                stopRegistrationDialog()
            }
        } else {
            binding.llIndicators.visible()
            binding.ivBack.visible()
        }
        binding.ivTopClose.setOnClickListener {
            stopRegistrationDialog()
        }

        binding.ivTopAction.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        binding.ivBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

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
            if (pinCount == 4) {
                val bundle = Bundle().apply {
                    putBoolean(Constants.FOR_LOGIN, viewModel.forLogin)
                    if (requireArguments().containsKey(Constants.IS_CHANGE_PIN))
                        putBoolean(
                                Constants.IS_CHANGE_PIN,
                                requireArguments().getBoolean(Constants.IS_CHANGE_PIN)
                        )
                }
                Handler(Looper.getMainLooper()).postDelayed({
                    viewModel.createPin = pin
                    findNavController().navigate(R.id.confirmPinFragment, bundle)


                    clearFields()
                }, 300)
            }
        }
    }


}