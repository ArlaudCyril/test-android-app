
package com.Lyber.ui.fragments.bottomsheetfragments

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.View
import com.Lyber.R
import com.Lyber.databinding.BottomSheetAddressBookBinding
import com.Lyber.models.RIBData
import com.Lyber.models.WithdrawAddress
import com.Lyber.utils.CommonMethods
import com.Lyber.utils.CommonMethods.Companion.gone
import com.Lyber.utils.CommonMethods.Companion.showToast
import com.Lyber.utils.CommonMethods.Companion.toFormat
import com.Lyber.utils.CommonMethods.Companion.visible

class AddAddressInfoBottomSheet(
    private val toDelete: Boolean, private val context: Context,
    private val clickListener: (Int) -> Unit = { _ -> }
) :
    BaseBottomSheet<BottomSheetAddressBookBinding>(), View.OnClickListener {

    override fun bind() = BottomSheetAddressBookBinding.inflate(layoutInflater)

    private var whitelisting: WithdrawAddress? = null
    private var ribData: RIBData? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (whitelisting != null) {
            binding.tvDelete.text =
                if (toDelete) context.getString(R.string.delete) else context.getString(R.string.confirm)
            binding.tvDelete.setCompoundDrawablesRelativeWithIntrinsicBounds(
                if (toDelete) R.drawable.delete_new else 0,
                0,
                0,
                0
            )
            binding.btnUseThis.gone()
        } else if (ribData != null) {
            binding.btnUseThis.visible()
            binding.tvDelete.text = context.getString(R.string.delete)
            binding.tvDelete.setCompoundDrawablesRelativeWithIntrinsicBounds(
                if (toDelete) R.drawable.delete_new else 0,
                0,
                0,
                0
            )
        }

        /* click listeners */
        binding.ivTopAction.setOnClickListener(this)
        binding.tvCopy.setOnClickListener(this)
        binding.llDelete.setOnClickListener(this)
        binding.llEdit.setOnClickListener(this)
        binding.btnUseThis.setOnClickListener(this)

    }


    fun setWhiteListing(whitelistings: WithdrawAddress): AddAddressInfoBottomSheet {
        whitelisting = whitelistings
        return this
    }

    fun setRibData(rib: RIBData): AddAddressInfoBottomSheet {
        ribData = rib
        return this
    }

    override fun onResume() {
        super.onResume()
        if (whitelisting != null)
            whitelisting?.let {
                binding.tvTitle.text = it.name
                binding.tvAddress.text = it.address
                binding.tvValueNetwork.text = it.network!!.uppercase()
                binding.tvValueAddressOrigin.text =
                    it.origin!!.substring(0, 1).uppercase() + it.origin!!.substring(1).lowercase()
                binding.tvValueDateAdded.text = it.creationDate!!.toFormat(
                    "yyyy-MM-dd'T'hh:mm:ss",
                    "dd MMM yyyy hh:mm"
                )/*2023-09-05T11:04:31.348Z*/
            }
        else if (ribData != null)
            ribData?.let {
                binding.tvTitle.text = it.name
                binding.tvAddress.text = getString(R.string.iban)
                binding.tvCopy.gone()
                binding.tvIbanNo.visible()
                val maxLength = 20 // Adjust this value as needed
                val truncatedText = CommonMethods.getTruncatedText(it.iban, maxLength)
                binding.tvIbanNo.text = truncatedText
//
//                binding.tvIbanNo.text = it.iban

                val truncatedTextBic = CommonMethods.getTruncatedText(it.bic, maxLength)
                binding.tvValueNetwork.text = truncatedTextBic
//                                     binding.tvValueNetwork.text = it.bic
                binding.tvNetwork.text = getString(R.string.bic)
                binding.tvAddressOrigin.text = getString(R.string.owner_name)
                binding.tvValueAddressOrigin.text = it.userName
                binding.tvDateAdded.text = getString((R.string.bank_country))
                binding.tvValueDateAdded.text = it.bankCountry
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
                    getString(R.string.adress_copied).showToast(binding.root,requireContext())
                }

                llDelete -> {
                    clickListener(1)
                    dismiss()
                }

                llEdit -> {
                    clickListener(2)
                    dismiss()
                }

                btnUseThis -> {
                    clickListener(3)
                    dismiss()
                }
            }
        }
    }


}
