package com.Lyber.ui.portfolio.bottomSheetFragment

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.updatePadding
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.Lyber.R
import com.Lyber.databinding.PortfolioThreeDotsBinding
import com.Lyber.ui.portfolio.fragment.PortfolioHomeFragment
import com.Lyber.viewmodels.PortfolioViewModel
import com.Lyber.utils.CommonMethods.Companion.getViewModel
import com.Lyber.utils.CommonMethods.Companion.gone
import com.Lyber.utils.Constants

class PortfolioThreeDots(private val listenItemClicked: (String, String) -> Unit = { _, _ -> }) :
    View.OnClickListener, DialogFragment() {

    private lateinit var viewModel: PortfolioViewModel
    private lateinit var binding: PortfolioThreeDotsBinding
    var dismissListener: PortfolioThreeDotsDismissListener? = null
    var typePopUp: String = ""


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
        if (tag!!.isNotEmpty())
            binding.llSell.root.gone()
        if(viewModel.selectedAsset?.id!=null && viewModel.selectedAsset?.id=="usdt")
            binding.llBuy.root.gone()
        binding.llWithdraw.root.setOnClickListener(this)
        binding.llExchange.root.setOnClickListener(this)
        binding.llDepositFiat.root.setOnClickListener(this)
        binding.llBuy.root.setOnClickListener(this)
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
        if (tag != "PortfolioThreeDots") {
            val view = PortfolioHomeFragment.fragmentPortfolio.binding.screenContent
            val viewToDelete = view.getChildAt(view.childCount - 1)
            view.removeView(viewToDelete)
        }

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
            it.tvStartTitle.text = getString(R.string.withdraw)
            it.tvStartSubTitle.text = getString(R.string.send_assets_to_your_bank_account)
            it.ivEndIcon.setImageResource(R.drawable.ic_right_arrow_grey)
        }
        binding.llBuy.let {
            it.root.updatePadding(left = 0, right = 0)
            if (this.typePopUp == "AssetPopUp") it.root.gone()
            it.ivItem.setImageResource(R.drawable.euro)
//            if(viewModel.selectedAsset?.id!=null)
//            it.tvStartTitle.text = "${getString(R.string.buy)} ${viewModel.selectedAsset?.id?.uppercase()}"
//            else
            it.tvStartTitle.text = "${getString(R.string.buy)}"
            if (this.typePopUp == "AssetPopUp" || this.typePopUp == "AssetPopUpWithdraw") {
                if (viewModel.selectedAsset!!.id!!.lowercase() == Constants.MAIN_ASSET) {
                    it.tvStartSubTitle.text = getString(R.string.buy_usdt_with_euro)

                } else
                    it.tvStartSubTitle.text =
                        getString(R.string.buy__with_usdt, viewModel.selectedAsset?.id?.uppercase())
            } else {
                it.tvStartSubTitle.text = getString(R.string.buy_assets_with_usdt)
            }

            it.ivEndIcon.setImageResource(R.drawable.ic_right_arrow_grey)
        }

        binding.llExchange.let {
            it.root.updatePadding(left = 0, right = 0)
            it.ivItem.setImageResource(R.drawable.ic_exchange)
            it.tvStartTitle.text = getString(R.string.exchange)
            it.tvStartSubTitle.text = getString(R.string.trade_one_asset_for_another)
            it.ivEndIcon.setImageResource(R.drawable.ic_right_arrow_grey)

        }

        binding.llDepositFiat.let {

            it.root.updatePadding(left = 0, right = 0)
            it.ivItem.setImageResource(R.drawable.ic_deposit)
            it.ivEndIcon.setImageResource(R.drawable.ic_right_arrow_grey)
            //    {
            it.tvStartTitle.text = getString(R.string.deposit_)
            if (this.typePopUp == "AssetPopUp" || this.typePopUp == "AssetPopUpWithdraw") {
                it.tvStartSubTitle.text =
                    getString(R.string.add_on_lyber, viewModel.selectedAsset?.id?.uppercase())
            } else {
                it.tvStartSubTitle.text = getString(R.string.add_money_on_lyber)
            }
            // } else {
            //   it.tvStartTitle.text = "Deposit"
            // it.tvStartSubTitle.text = "Add money on Lyber"
            //}
        }

        binding.llSell.let {
            if (this.typePopUp == "AssetPopUp" || this.typePopUp == "AssetPopUpWithdraw") {
                it.root.updatePadding(left = 0, right = 0)
                it.ivItem.setImageResource(R.drawable.ic_sell)
                it.tvStartSubTitle.gone()
                val layoutParams = it.tvStartTitle.layoutParams as ConstraintLayout.LayoutParams
                layoutParams.topMargin = 20
                it.tvStartTitle.text = getString(R.string.sell_)
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

                llBuy.root -> {
                    if (typePopUp != "" && viewModel.selectedAsset != null && viewModel.selectedAsset!!.id!!.lowercase() == Constants.MAIN_ASSET)
                        findNavController().navigate(R.id.buyUsdt)
                    else
                        listenItemClicked(tag ?: "PortfolioThreeDots", "buy")
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