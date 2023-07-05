package com.au.lyber.ui.portfolio.bottomSheetFragment

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.marginTop
import androidx.core.view.updatePadding
import androidx.fragment.app.DialogFragment
import com.au.lyber.R
import com.au.lyber.databinding.PortfolioThreeDotsBinding
import com.au.lyber.ui.portfolio.fragment.PortfolioHomeFragment
import com.au.lyber.ui.portfolio.viewModel.PortfolioViewModel
import com.au.lyber.utils.CommonMethods.Companion.getViewModel
import com.au.lyber.utils.CommonMethods.Companion.gone
import com.au.lyber.utils.Constants

class PortfolioThreeDots(private val listenItemClicked: (String, String) -> Unit = { _, _ -> }) :
    View.OnClickListener, DialogFragment() {

    private lateinit var viewModel: PortfolioViewModel
    private lateinit var binding: PortfolioThreeDotsBinding
    var dismissListener: PortfolioThreeDotsDismissListener? = null
    var typePopUp : String = ""


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = PortfolioThreeDotsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = getViewModel(requireActivity())

        prepareView()

        binding.llWithdraw.root.setOnClickListener(this)
        binding.llExchange.root.setOnClickListener(this)
        binding.llDepositFiat.root.setOnClickListener(this)
        binding.llSell.root.setOnClickListener(this)
        binding.ivTopAction.setOnClickListener(this)

    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        // Set the window to non-floating
        dialog.window?.setGravity(Gravity.BOTTOM)
        dialog.window?.setWindowAnimations(R.style.DialogAnimation)
        return dialog
    }

    override fun onDestroyView() {
        super.onDestroyView()
        val view = PortfolioHomeFragment.fragmentPortfolio.binding.screenContent
        val viewToDelete = view.getChildAt(view.childCount-1)
        view.removeView(viewToDelete)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.MyDialogStyle)
    }

    @SuppressLint("SetTextI18n")
    private fun prepareView() {

        binding.llWithdraw.let {
            it.root.updatePadding(left = 0, right = 0)
            if (this.typePopUp == "AssetPopUp") it.root.gone()
            it.ivItem.setImageResource(R.drawable.ic_withdraw)
            it.tvStartTitle.text = "Withdraw"
            it.tvStartSubTitle.text = "Send assets to your bank account"
            it.ivEndIcon.setImageResource(R.drawable.ic_right_arrow_grey)
        }

        binding.llExchange.let {
            it.root.updatePadding(left = 0, right = 0)
            it.ivItem.setImageResource(R.drawable.ic_exchange)
            it.tvStartTitle.text = "Exchange"
            it.tvStartSubTitle.text = "Trade one asset for another"
            it.ivEndIcon.setImageResource(R.drawable.ic_right_arrow_grey)

        }

        binding.llDepositFiat.let {

            it.root.updatePadding(left = 0, right = 0)
            it.ivItem.setImageResource(R.drawable.ic_deposit)
            it.ivEndIcon.setImageResource(R.drawable.ic_right_arrow_grey)
            if (this.typePopUp == "AssetPopUp") {
                it.tvStartTitle.text = "Deposit"
                it.tvStartSubTitle.text = "Add ${viewModel.selectedAsset?.id?.uppercase()} on Lyber"
            } else {
                it.tvStartTitle.text = "Deposit"
                it.tvStartSubTitle.text = "Add money on Lyber"
            }
        }

        binding.llSell.let {
            if (this.typePopUp == "AssetPopUp") {
                it.root.updatePadding(left = 0, right = 0)
                it.ivItem.setImageResource(R.drawable.ic_sell)
                it.tvStartSubTitle.gone()
                val layoutParams = it.tvStartTitle.layoutParams as ConstraintLayout.LayoutParams
                layoutParams.topMargin = 20
                it.tvStartTitle.text = "Sell"
                it.ivEndIcon.setImageResource(R.drawable.ic_right_arrow_grey)
            } else it.root.gone()
        }

    }

    override fun onClick(v: View?) {
        binding.apply {
            when (v!!) {
                llWithdraw.root -> {
                    listenItemClicked(tag ?: "PortfolioThreeDots", "withdraw")
                    dismiss()
                }
                llExchange.root -> {
                    listenItemClicked(tag ?: "PortfolioThreeDots", "exchange")
                    dismiss()
                }
                llDepositFiat.root -> {
                    listenItemClicked(tag ?: "PortfolioThreeDots", "deposit")
                    dismiss()
                }
                llSell.root -> {
                    listenItemClicked(tag ?: "PortfolioThreeDots", "sell")
                    dismiss()
                }
                ivTopAction -> dismiss()
            }
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        dismissListener?.onPortfolioThreeDotsDismissed()
    }


}
interface PortfolioThreeDotsDismissListener {
    fun onPortfolioThreeDotsDismissed()
}