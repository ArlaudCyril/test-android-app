package com.au.lyber.ui.fragments.bottomsheetfragments

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import com.au.lyber.R
import com.au.lyber.databinding.BottomSheetAddressBookBinding
import com.au.lyber.databinding.FragmentTransactionDetailsBottomSheetBinding
import com.au.lyber.models.TransactionData
import com.au.lyber.utils.CommonMethods
import com.au.lyber.utils.CommonMethods.Companion.gone
import com.au.lyber.utils.CommonMethods.Companion.showToast
import com.au.lyber.utils.CommonMethods.Companion.toFormat
import com.au.lyber.utils.Constants
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class TransactionDetailsBottomSheetFragment :
    BaseBottomSheet<FragmentTransactionDetailsBottomSheetBinding>(),OnClickListener {
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
                binding.tvFromValue.text = "${transactionData.fromAmount} ${transactionData.fromAsset.uppercase()}"
                binding.ivCopyFrom.visibility=View.GONE
                binding.ivTransactionHash.visibility=View.GONE
                binding.tvToValue.text = "${transactionData.toAmount} ${transactionData.toAsset.uppercase()}"
                binding.tvFeePaid.text = "${transactionData.fees} ${transactionData.fromAsset.uppercase()}"
                binding.tvDateValue.text = transactionData.date.toFormat("yyyy-MM-dd'T'hh:mm:ss","dd MMMM yyyy")

            }

            Constants.STRATEGY -> {
                binding.tvTitle.text = transactionData.type.replaceFirstChar(Char::uppercase)
                binding.tvOrder.text = getString(R.string.execution_id)
                binding.tvOrderId.text = transactionData.executionId
                binding.tvStatus.text =getString(R.string.nameOrder)
                binding.tvStatusValue.text =
                    transactionData.strategyName.lowercase().replaceFirstChar(Char::uppercase)
                binding.tvFrom.text = getString(R.string.type)
                if(transactionData.nextExecution!=null)
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
                binding.tvToValue.text = "${transactionData.amount} ${transactionData.asset.uppercase()}"
                binding.tvFee.text = getString(R.string.network)
                binding.tvFeePaid.text = transactionData.network
                binding.tvDate.text = getString(R.string.transaction_hash)
                binding.tvDateValue.text = transactionData.txId
            }

            Constants.WITHDRAW -> { // single asset
               binding.tvTitle.text = getString(R.string.withdrawal)
//                   transactionData.type.replaceFirstChar(Char::uppercase)
                binding.tvOrder.text = getString(R.string.transaction_id)
                binding.tvOrderId.text = transactionData.id
                binding.tvStatusValue.text =
                    transactionData.status.lowercase().replaceFirstChar(Char::uppercase)
                binding.tvFrom.text=getString(R.string.to)
                binding.tvFromValue.text = transactionData.toAddress
                binding.tvTo.text = getString(R.string.amount)
                binding.tvToValue.text = "${transactionData.amount} ${transactionData.asset.uppercase()}"
                binding.tvFee.text = getString(R.string.date)
                binding.tvFeePaid.text =transactionData.date.toFormat("yyyy-MM-dd'T'hh:mm:ss","dd MMMM yyyy")
                binding.tvDate.visibility=View.GONE
                binding.tvDateValue.visibility=View.GONE
                binding.ivTransactionHash.visibility=View.GONE
            }

        }
        binding.ivCopyId.setOnClickListener(this)
        binding.ivCopyFrom.setOnClickListener(this)
        binding.ivTransactionHash.setOnClickListener(this)
        binding.ivTopAction.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        binding.apply {
            when(v!!){
                ivCopyId->{
                    var textToCopy=""
                    when(transactionData.type){
                        Constants.ORDER -> textToCopy=transactionData.id
                       Constants.STRATEGY ->textToCopy=transactionData.executionId
                        Constants.DEPOSIT ->textToCopy=transactionData.id
                        Constants.WITHDRAW ->textToCopy=transactionData.id
                    }
                    val clipMan =
                        requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
                    val clip = ClipData.newPlainText("label", textToCopy)
                    clipMan?.setPrimaryClip(clip)
                    "Copied".showToast(requireContext())
                }
                ivCopyFrom->{
                    var textToCopy=""
                    when(transactionData.type){
                        Constants.DEPOSIT ->textToCopy=transactionData.fromAddress
                        Constants.WITHDRAW ->textToCopy=transactionData.toAddress
                    }
                    val clipMan =
                        requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
                    val clip = ClipData.newPlainText("label", textToCopy)
                    clipMan?.setPrimaryClip(clip)
                    "Copied".showToast(requireContext())
                }
                ivTransactionHash->{
                    var textToCopy=""
                    textToCopy=transactionData.txId
                    val clipMan =
                        requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
                    val clip = ClipData.newPlainText("label", textToCopy)
                    clipMan?.setPrimaryClip(clip)
                    "Copied".showToast(requireContext())
                }
                ivTopAction->{ dismiss() }
            }
        }
    }

}