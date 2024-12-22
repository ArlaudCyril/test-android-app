package com.Lyber.ui.fragments.bottomsheetfragments

import android.content.DialogInterface
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.Lyber.R
import com.Lyber.databinding.FragmentConfirmationBinding
import com.Lyber.ui.activities.SplashActivity
import com.Lyber.utils.CommonMethods
import com.Lyber.utils.CommonMethods.Companion.getViewModel
import com.Lyber.utils.CommonMethods.Companion.gone
import com.Lyber.utils.CommonMethods.Companion.visible
import com.Lyber.utils.Constants
import com.Lyber.viewmodels.PortfolioViewModel
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.play.core.integrity.StandardIntegrityManager
import org.json.JSONObject

class ConfirmationBottomSheet : BaseBottomSheet<FragmentConfirmationBinding>() {

    private lateinit var viewModel: PortfolioViewModel
    var reqAmount = 0f
    var currentAmount = 0.0
    var isEdit = false
    override fun bind() = FragmentConfirmationBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Step 1: Get the screen height in pixels
        val displayMetrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
        val screenHeight = displayMetrics.heightPixels

        // Step 2: Convert 40dp to pixels
        val marginInDp = 50
        val marginInPx = (marginInDp * displayMetrics.density).toInt()

        // Step 3: Set the height of the BottomSheet dynamically
        val bottomSheet = binding.root.parent as ViewGroup
        val layoutParams = bottomSheet.layoutParams
        layoutParams.height = screenHeight - marginInPx
        bottomSheet.layoutParams = layoutParams

        behavior.state = BottomSheetBehavior.STATE_EXPANDED


        viewModel = getViewModel(requireActivity())
        binding.ivTopAction.setOnClickListener {
            dismiss()
        }

        binding.btnThanks.setOnClickListener {
            if (tag!!.isEmpty() && viewModel.selectedOption != "" && viewModel.selectedOption == Constants.ACTION_TAILOR_STRATEGY) {
                CommonMethods.showProgressDialog(requireContext())
//                viewModel.editOwnStrategy(viewModel.selectedStrategy!!.name)
                isEdit = true
               viewModel.editEnabledStrategy(
                        viewModel.selectedStrategy!!.ownerUuid,
                        viewModel.selectedStrategy!!.activeStrategy!!.frequency,
                        reqAmount.toDouble(),
                        viewModel.selectedStrategy!!.name
                    )

                dismiss()
            } else
                dismiss()
        }
        binding.btnYes.setOnClickListener {
            if (tag!!.isEmpty() && viewModel.selectedOption != "" && viewModel.selectedOption == Constants.ACTION_TAILOR_STRATEGY) {
                CommonMethods.showProgressDialog(requireContext())
//                viewModel.editOwnStrategy(viewModel.selectedStrategy!!.name)
                isEdit = true
                 // Generate the request hash
               viewModel.editEnabledStrategy(
                        viewModel.selectedStrategy!!.ownerUuid,
                        viewModel.selectedStrategy!!.activeStrategy!!.frequency,
                        reqAmount.toDouble(),
                        viewModel.selectedStrategy!!.name
                    )

                dismiss()
            } else
                dismiss()
        }
        binding.btnNo.setOnClickListener {
            dismiss()
        }
        if (tag!!.isNotEmpty()) {
            binding.tvInfoOne.text = getString(R.string.we_have_sent_an_email)
            binding.tvInfoTwo.visibility = View.GONE
        } else {

            when (viewModel.selectedOption) {
                Constants.ACTION_WITHDRAW_EURO -> binding.tvInfoOne.text =
                    getString(R.string.amount_withdrawn)

                Constants.USING_WITHDRAW -> binding.tvInfoOne.text =
                    getString(R.string.your_withdrawal_has_been_taken_into_account)

                Constants.EXPORT_DONE -> {
                    binding.tvTitle.text = getString(R.string.successful)
                    binding.tvInfoTwo.visibility = View.GONE
                    binding.tvInfoOne.text = getString(R.string.successfulMsg)
                }

                Constants.ACTION_TAILOR_STRATEGY -> {
                    binding.root.setBackgroundTintList(
                        ContextCompat.getColorStateList(
                            requireContext(),
                            R.color.orangeColor
                        )
                    )
                    binding.tvTitle.text = getString(R.string.oh_my)
                    binding.imgTick.visibility = View.GONE
                    binding.imgOh.visibility = View.VISIBLE
                    binding.tvInfoTwo.visibility = View.GONE
                    binding.tvInfoOne.visibility = View.GONE
                    binding.tvInfo.visibility = View.VISIBLE
                    if (arguments != null) {
                        reqAmount = requireArguments().getFloat("requiredAmount")
                        currentAmount = requireArguments().getDouble("currentAmount")
                    }
                    val str = getString(
                        R.string.amount_invested_is_insufficient,
                        currentAmount.toString(),
                        reqAmount.toString()
                    )
                    binding.tvInfo.text = str
                    binding.btnThanks.gone()
                    binding.llBtns.visible()
                }

                Constants.USING_SEND_MONEY -> {
                    binding.tvTitle.text=getString(R.string.congrats)
//                    binding.view1.visible()
//                    binding.view2.visible()
                    binding.tvInfoOne.text = getString(R.string.shipment_success)
                    binding.tvInfoTwo.text =getString(R.string.rec_has_received)

                }

                else -> binding.tvInfoOne.text =
                    getString(R.string.your_investment_has_been_taken_into_account)
            }
        }
    }


    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if (tag!!.isNotEmpty()) {
            findNavController().popBackStack(R.id.discoveryFragment, false)
        } else if (viewModel.selectedOption != "" && viewModel.selectedOption == Constants.EXPORT_DONE) {
            viewModel.selectedOption = ""
        } else if (viewModel.selectedOption != "" && viewModel.selectedOption == Constants.ACTION_WITHDRAW_EURO) {
            viewModel.selectedAsset = CommonMethods.getAsset(Constants.MAIN_ASSET)
            findNavController().navigate(R.id.action_withdrawUsdc_to_portfolioDetail)
        } else if (viewModel.selectedOption != "" && viewModel.selectedOption == Constants.ACTION_TAILOR_STRATEGY) {
            viewModel.selectedOption = ""
            if (!isEdit)
                requireActivity().onBackPressed()
//            dismiss()
        } else if (viewModel.selectedOption != "" && viewModel.selectedOption == Constants.USING_SEND_MONEY) {
            viewModel.selectedOption = ""
            findNavController().navigate(R.id.action_confirm_withdraw_to_home_fragment)
//            dismiss()
        } else {
            findNavController().popBackStack(R.id.portfolioHomeFragment, false)
        }
    }

}