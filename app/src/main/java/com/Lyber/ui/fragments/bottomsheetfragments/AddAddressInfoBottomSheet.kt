package com.Lyber.ui.fragments.bottomsheetfragments

import android.content.Context
import android.os.Bundle
import android.view.View
import com.Lyber.R
import com.Lyber.databinding.BottomSheetAddressBookBinding
import com.Lyber.models.Whitelistings
import com.Lyber.utils.CommonMethods.Companion.gone
import com.Lyber.utils.CommonMethods.Companion.toDateFormat

class AddAddressInfoBottomSheet(
    private val toDelete: Boolean,private val context: Context,
    private val clickListener: (Int) -> Unit = { _ -> }) :
    BaseBottomSheet<BottomSheetAddressBookBinding>(), View.OnClickListener {

    override fun bind() = BottomSheetAddressBookBinding.inflate(layoutInflater)

    private var whitelisting: Whitelistings? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvDelete.text = if (toDelete) context.getString(R.string.delete) else context.getString(R.string.confirm)
        binding.tvDelete.setCompoundDrawablesRelativeWithIntrinsicBounds(
            if (toDelete) R.drawable.ic_delete else 0,
            0,
            0,
            0
        )

        /* click listeners */
        binding.ivTopAction.setOnClickListener(this)
        binding.tvCopy.setOnClickListener(this)
        binding.llDelete.setOnClickListener(this)
        binding.llEdit.setOnClickListener(this)

    }


    fun setWhiteListing(whitelistings: Whitelistings): AddAddressInfoBottomSheet {
        whitelisting = whitelistings
        return this
    }

    override fun onResume() {
        super.onResume()

        whitelisting?.let {
            binding.tvTitle.text = it.name
            binding.tvAddress.text = it.name
            binding.tvValueNetwork.text = it.network
            if (!it.exchange.isNullOrEmpty())
                binding.tvValueAddressOrigin.text = it.exchange
            else{
                binding.tvAddressOrigin.gone()
                binding.tvValueAddressOrigin.gone()
            }
            binding.tvValueDateAdded.text = it.created_at.toLong().toDateFormat()
        }
    }


    override fun onClick(v: View?) {
        binding.apply {
            when (v!!) {
                ivTopAction -> dismiss()
                tvCopy -> {}
                llDelete -> {
                    clickListener(1)
                    dismiss()
                }
                llEdit -> {
                    clickListener(2)
                    dismiss()
                }
            }
        }
    }


}