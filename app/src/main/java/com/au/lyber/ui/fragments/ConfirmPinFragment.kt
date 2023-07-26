package com.au.lyber.ui.fragments

import android.os.Bundle
import android.view.View
import com.au.lyber.R
import com.au.lyber.databinding.FragmentConfirmPinBinding
import com.au.lyber.ui.portfolio.fragment.PortfolioHomeFragment
import com.au.lyber.utils.App
import com.au.lyber.utils.CommonMethods.Companion.clearBackStack
import com.au.lyber.utils.CommonMethods.Companion.getViewModel
import com.au.lyber.utils.CommonMethods.Companion.replaceFragment
import com.au.lyber.utils.CommonMethods.Companion.requestKeyboard
import com.au.lyber.utils.CommonMethods.Companion.showToast
import com.au.lyber.utils.OnTextChange
import com.au.lyber.viewmodels.SignUpViewModel

class ConfirmPinFragment : BaseFragment<FragmentConfirmPinBinding>() {

    override fun bind() = FragmentConfirmPinBinding.inflate(layoutInflater)
    private val pinConfirm get() = binding.etConfirmPin.text.trim().toString()

    private lateinit var viewModel: SignUpViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //(requireParentFragment() as SignUpFragment).mPosition = 3

        viewModel = getViewModel(requireParentFragment())
        viewModel.forLogin = requireArguments().getBoolean("forLogin",false)
        viewModel.listener = this

//        binding.etConfirmPin.setOnEditorActionListener(onEditorActionListener)
        binding.etConfirmPin.addTextChangedListener(onTextChange)
        binding.etConfirmPin.requestKeyboard()
    }

    /*private val onEditorActionListener =
        TextView.OnEditorActionListener { v, actionId, event ->
            val isDonePressed = actionId == EditorInfo.IME_ACTION_DONE
            if (isDonePressed) {
                if (pinConfirm.count() == 4) {
                    if (viewModel.createPin == pinConfirm) {
                        checkInternet(requireContext()) {
                            viewModel.confirmPin = pinConfirm
                            clearField()
                            showProgressDialog(requireContext())
                            viewModel.setPin()
                        }
                    } else "Pin doesn't matches".showToast(requireContext())
                    return@OnEditorActionListener true
                } else return@OnEditorActionListener false
            }
            false
        }*/


    /* On Text Change */
    private val onTextChange = object : OnTextChange {
        override fun onTextChange() {
            changeDots(pinConfirm.count())
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

            if (pinCount == 4) {

                if (viewModel.createPin == pinConfirm) {

                    App.prefsManager.userPin = pinConfirm

                    if (viewModel.forLogin) {

                        requireActivity().clearBackStack()
                        requireActivity().replaceFragment(
                            R.id.flSplashActivity,
                            PortfolioHomeFragment()
                        )
                    } else (requireParentFragment() as SignUpFragment).showDialog()

                    /*checkInternet(requireContext()) {
                        viewModel.confirmPin = pinConfirm
                        clearField()
                        showProgressDialog(requireContext())
                        viewModel.setPin()
                    }*/

                } else {
                    clearField()
                    "Please enter the correct pin.".showToast(requireContext())
                }

            }
        }
    }

}