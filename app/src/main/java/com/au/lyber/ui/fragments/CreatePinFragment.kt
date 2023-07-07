package com.au.lyber.ui.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.au.lyber.R
import com.au.lyber.databinding.FragmentCreatePinBinding
import com.au.lyber.utils.App
import com.au.lyber.utils.CommonMethods.Companion.getViewModel
import com.au.lyber.utils.CommonMethods.Companion.replace
import com.au.lyber.utils.CommonMethods.Companion.requestKeyboard
import com.au.lyber.utils.OnTextChange
import com.au.lyber.viewmodels.SignUpViewModel

class CreatePinFragment : BaseFragment<FragmentCreatePinBinding>() {
    private lateinit var navController : NavController
    override fun bind() = FragmentCreatePinBinding.inflate(layoutInflater)

    private val pin get() = binding.etCreatePin.text.trim().toString()

    private lateinit var viewModel: SignUpViewModel

    override fun onResume() {
        super.onResume()
        binding.etCreatePin.requestKeyboard()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        //(requireParentFragment() as SignUpFragment).setIndicators(2)

        App.prefsManager.savedScreen = javaClass.name

        viewModel = getViewModel(requireParentFragment())
        binding.etCreatePin.addTextChangedListener(onTextChange)
        val navHostFragment =  requireActivity().supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.findNavController()
//        binding.etCreatePin.setOnEditorActionListener(onEditorActionListener)

        /* preparing ui */
        /*if (viewModel.forLogin) {
            binding.tvTitle.text = "Enter PIN"
            binding.tvSubTitle.text = "The code you’ve created during the signup."
        }*/

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
                Handler(Looper.getMainLooper()).postDelayed({
                    viewModel.createPin = pin
                   navController.navigate(R.id.confirmPinFragment)

                    /*App.prefsManager.user?.let {
                    if (it.login_pin_set) {
                        checkInternet(requireContext()) {
                            showProgressDialog(requireContext())
                            viewModel.verifyPin(pin)
                        }
                    } else {
                        viewModel.createPin = pin
                        (requireParentFragment() as SignUpFragment).replace(
                            R.id.frameLayoutSignUp,
                            (requireParentFragment() as SignUpFragment).fragments[3]
                        )
                    }
                }*/

                    clearFields()
                }, 300)
            }
        }
    }


}