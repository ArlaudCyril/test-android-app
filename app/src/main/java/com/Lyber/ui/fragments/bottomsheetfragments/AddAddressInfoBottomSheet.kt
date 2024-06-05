package com.Lyber.ui.fragments.bottomsheetfragments

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.Lyber.R
import com.Lyber.databinding.BottomSheetAddressBookBinding
import com.Lyber.models.WithdrawAddress
import com.Lyber.utils.CommonMethods.Companion.toFormat

class AddAddressInfoBottomSheet(
    private val toDelete: Boolean, private val context: Context,
    private val clickListener: (Int) -> Unit = { _ -> }
) :
    BaseBottomSheet<BottomSheetAddressBookBinding>(), View.OnClickListener {

    override fun bind() = BottomSheetAddressBookBinding.inflate(layoutInflater)

    private var whitelisting: WithdrawAddress? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvDelete.text =
            if (toDelete) context.getString(R.string.delete) else context.getString(R.string.confirm)
        binding.tvDelete.setCompoundDrawablesRelativeWithIntrinsicBounds(
            if (toDelete) R.drawable.delete_new else 0,
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


    fun setWhiteListing(whitelistings: WithdrawAddress): AddAddressInfoBottomSheet {
        whitelisting = whitelistings
        return this
    }

    override fun onResume() {
        super.onResume()

        whitelisting?.let {
            binding.tvTitle.text = it.name
            binding.tvAddress.text = it.address
            binding.tvValueNetwork.text = it.network!!.uppercase()
            binding.tvValueAddressOrigin.text = it.origin!!.substring(0, 1).uppercase() + it.origin!!.substring(1).lowercase()
            binding.tvValueDateAdded.text = it.creationDate!!.toFormat(
                "yyyy-MM-dd'T'hh:mm:ss",
                "dd MMM yyyy hh:mm"
            )/*2023-09-05T11:04:31.348Z*/
        }
    }


    override fun onClick(v: View?) {
        binding.apply {
            when (v!!) {
                ivTopAction -> dismiss()
                tvCopy -> {
                    val clipboard =
                        requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip =
                        ClipData.newPlainText(
                            getString(R.string.deposit_adress),
                            binding.tvAddress.text.toString()
                        )
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(
                        requireActivity(),
                        getString(R.string.adress_copied), Toast.LENGTH_SHORT
                    ).show()
                }

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