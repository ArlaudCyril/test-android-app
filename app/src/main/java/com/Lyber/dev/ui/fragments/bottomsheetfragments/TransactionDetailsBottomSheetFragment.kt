package com.Lyber.dev.ui.fragments.bottomsheetfragments

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.widget.TextView
import com.Lyber.dev.R
import com.Lyber.dev.databinding.FragmentTransactionDetailsBottomSheetBinding
import com.Lyber.dev.models.TransactionData
import com.Lyber.dev.utils.CommonMethods
import com.Lyber.dev.utils.CommonMethods.Companion.formattedAsset
import com.Lyber.dev.utils.CommonMethods.Companion.gone
import com.Lyber.dev.utils.CommonMethods.Companion.showToast
import com.Lyber.dev.utils.CommonMethods.Companion.toFormat
import com.Lyber.dev.utils.CommonMethods.Companion.visible
import com.Lyber.dev.utils.Constants
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.math.BigDecimal
import java.math.RoundingMode


class TransactionDetailsBottomSheetFragment :
    BaseBottomSheet<FragmentTransactionDetailsBottomSheetBinding>(), OnClickListener {
    override fun bind() = FragmentTransactionDetailsBottomSheetBinding.inflate(layoutInflater)
    lateinit var transactionData: TransactionData
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        transactionData = Gson().fromJson<TransactionData>(
            requireArguments().getString("data"),
            object :
                TypeToken<TransactionData>() {}.type
        )
        Log.d("data", "$transactionData")
        when (transactionData.type) {
            Constants.ORDER -> {
                binding.tvTitle.text = transactionData.type.replaceFirstChar(Char::uppercase)
                binding.tvOrder.text = getString(R.string.order_id)
                binding.tvOrderId.text = transactionData.id
                binding.tvStatusValue.text =
                    transactionData.status.lowercase().replaceFirstChar(Char::uppercase)
                binding.tvFromValue.text =
                    "${transactionData.fromAmount} ${transactionData.fromAsset.uppercase()}"
                binding.ivCopyFrom.visibility = View.GONE
                binding.ivTransactionHash.visibility = View.GONE
                binding.tvToValue.text =
                    "${
                        transactionData.toAmount.formattedAsset(
                            0.0,
                            rounding = RoundingMode.DOWN, 8
                        )
                    } ${transactionData.toAsset.uppercase()}"
                binding.tvFeePaid.text =
                    "${transactionData.fees} ${transactionData.fromAsset.uppercase()}"
                binding.tvDateValue.text =
                    transactionData.date.toFormat("yyyy-MM-dd'T'hh:mm:ss", "dd MMMM yyyy HH:mm")

            }

            Constants.STRATEGY -> {
                binding.tvTitle.text = transactionData.type.replaceFirstChar(Char::uppercase)
                binding.tvOrder.text = getString(R.string.execution_id)
                binding.tvOrderId.text = transactionData.executionId
                binding.tvStatus.text = getString(R.string.nameOrder)
                binding.tvStatusValue.text =
                    transactionData.strategyName.lowercase().replaceFirstChar(Char::uppercase)
                binding.tvFrom.text = getString(R.string.type)
                if (transactionData.nextExecution != null)
                    binding.tvFromValue.text = getString(R.string.recurrent)
                else
                    binding.tvFromValue.text = getString(R.string.single_execution)
                binding.tvTo.visibility = View.GONE
                binding.tvToValue.visibility = View.GONE
                binding.tvFee.visibility = View.GONE
                binding.tvFeePaid.visibility = View.GONE
                binding.tvDate.visibility = View.GONE
                binding.tvDateValue.visibility = View.GONE
                binding.ivCopyFrom.visibility = View.GONE

            }

            Constants.DEPOSIT -> {
                binding.tvTitle.text = transactionData.type.replaceFirstChar(Char::uppercase)
                binding.tvOrder.text = getString(R.string.transaction_id)
                binding.tvOrderId.text = transactionData.id
                binding.tvStatusValue.text =
                    transactionData.status.lowercase().replaceFirstChar(Char::uppercase)
                binding.tvFromValue.text = transactionData.fromAddress
                binding.tvTo.text = getString(R.string.amount)
                binding.tvToValue.text =
                    "${transactionData.amount} ${transactionData.asset.uppercase()}"
                binding.tvFee.text = getString(R.string.network)
                binding.tvFeePaid.text = transactionData.network
                binding.tvDate.text = getString(R.string.transaction_hash)
                binding.tvDateValue.text = transactionData.txId
                binding.tvDateDeposit.visible()
                binding.tvDateDepositValue.visible()
                binding.tvDateDepositValue.text =
                    transactionData.date.toFormat("yyyy-MM-dd'T'hh:mm:ss", "dd MMMM yyyy HH:mm")

            }

            Constants.WITHDRAW -> { // single asset
                binding.tvTitle.text = getString(R.string.withdrawal)
//                   transactionData.type.replaceFirstChar(Char::uppercase)
                binding.tvOrder.text = getString(R.string.transaction_id)
                binding.tvOrderId.text = transactionData.id
                binding.tvStatusValue.text =
                    transactionData.status.lowercase().replaceFirstChar(Char::uppercase)
                binding.tvFrom.text = getString(R.string.toCaps)
                binding.tvFromValue.text = transactionData.toAddress
                binding.tvTo.text = getString(R.string.amount)
                binding.tvToValue.text =
                    "${transactionData.amount} ${transactionData.asset.uppercase()}"
                binding.tvFee.text = getString(R.string.date)
                binding.tvFeePaid.text =
                    transactionData.date.toFormat("yyyy-MM-dd'T'hh:mm:ss", "dd MMMM yyyy HH:mm")
                binding.tvDate.visibility = View.GONE
                binding.tvDateValue.visibility = View.GONE
                binding.ivTransactionHash.visibility = View.GONE
            }

            Constants.WITHDRAW_EURO -> { // single asset


                binding.tvTitle.text = "Euro " + getString(R.string.withdrawal)
//                   transactionData.type.replaceFirstChar(Char::uppercase)
                binding.tvOrder.text = getString(R.string.transaction_id)
                val maxLength=20
                binding.tvOrderId.text =CommonMethods.getTruncatedText(transactionData.id,maxLength)
//                binding.tvOrderId.text = transactionData.id
                binding.tvStatusValue.text = transactionData.status
                binding.ivCopyFrom.gone()
                binding.tvFrom.text = getString(R.string.iban)
                val truncatedText = CommonMethods.getTruncatedText(transactionData.iban, maxLength)
                binding.tvFromValue.text = truncatedText
//                binding.tvFromValue.text = transactionData.iban
                binding.tvTo.text = getString(R.string.amount)
                binding.tvToValue.text =
                    "${transactionData.amount} ${transactionData.asset.uppercase()}"
                binding.tvEuroAmount.visible()
                binding.tvEuroAmountValue.visible()
                binding.tvEuroAmountValue.text="${transactionData.eurAmount} ${Constants.EUR}"
                binding.tvFeePaid.text =
                    BigDecimal.valueOf(transactionData.eurAmount.toDouble()).subtract(BigDecimal.valueOf(transactionData.eurAmountDeductedLyberFees.toDouble()))
                        .toString()+" ${Constants.EUR}"
                binding.tvDate.text = getString(R.string.date)
                binding.tvDateValue.text =
                    transactionData.date.toFormat("yyyy-MM-dd'T'hh:mm:ss", "dd MMMM yyyy HH:mm")

                binding.ivTransactionHash.visibility = View.GONE
            }

        }
        binding.ivCopyId.setOnClickListener(this)
        binding.ivCopyFrom.setOnClickListener(this)
        binding.ivTransactionHash.setOnClickListener(this)
        binding.ivTopAction.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        binding.apply {
            when (v!!) {
                ivCopyId -> {
                    var textToCopy = ""
                    when (transactionData.type) {
                        Constants.ORDER -> textToCopy = transactionData.id
                        Constants.STRATEGY -> textToCopy = transactionData.executionId
                        Constants.DEPOSIT -> textToCopy = transactionData.id
                        Constants.WITHDRAW -> textToCopy = transactionData.id
                    }
                    val clipMan =
                        requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
                    val clip = ClipData.newPlainText("label", textToCopy)
                    clipMan?.setPrimaryClip(clip)
                    getString(R.string.copied).showToast(requireContext())
                }

                ivCopyFrom -> {
                    var textToCopy = ""
                    when (transactionData.type) {
                        Constants.DEPOSIT -> textToCopy = transactionData.fromAddress
                        Constants.WITHDRAW -> textToCopy = transactionData.toAddress
                    }
                    val clipMan =
                        requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
                    val clip = ClipData.newPlainText("label", textToCopy)
                    clipMan?.setPrimaryClip(clip)
                    getString(R.string.copied).showToast(requireContext())
                }

                ivTransactionHash -> {
                    var textToCopy = ""
                    textToCopy = transactionData.txId
                    val clipMan =
                        requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
                    val clip = ClipData.newPlainText("label", textToCopy)
                    clipMan?.setPrimaryClip(clip)
                    getString(R.string.copied).showToast(requireContext())
                }

                ivTopAction -> {
                    dismiss()
                }
            }
        }
    }

}