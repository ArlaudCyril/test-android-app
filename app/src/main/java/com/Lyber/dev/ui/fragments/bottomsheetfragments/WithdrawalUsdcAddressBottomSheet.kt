package com.Lyber.dev.ui.fragments.bottomsheetfragments

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.Lyber.dev.R
import com.Lyber.dev.databinding.AppItemLayoutBinding
import com.Lyber.dev.databinding.BottomsheetWithdrawalAddressesBinding
import com.Lyber.dev.databinding.LoaderViewBinding
import com.Lyber.dev.models.RIBData
import com.Lyber.dev.models.WithdrawAddress
import com.Lyber.dev.ui.activities.BaseActivity
import com.Lyber.dev.ui.adapters.BaseAdapter
import com.Lyber.dev.ui.fragments.AddAddressBookFragment
import com.Lyber.dev.utils.CommonMethods.Companion.gone
import com.Lyber.dev.utils.CommonMethods.Companion.loadCircleCrop
import com.Lyber.dev.utils.CommonMethods.Companion.visible
import com.Lyber.dev.utils.Constants

class WithdrawalUsdcAddressBottomSheet(
    private val addresses: MutableList<RIBData>,
    private val selectedNetwork: String,
    private val handle: (RIBData?, String?) -> Unit = { _, _ -> }
) : BaseBottomSheet<BottomsheetWithdrawalAddressesBinding>(), View.OnClickListener {
    private lateinit var adapter: AddressesAdapter
    override fun bind() = BottomsheetWithdrawalAddressesBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter = AddressesAdapter(this, handle)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvPayOptions.adapter = adapter
        binding.rvPayOptions.layoutManager = LinearLayoutManager(requireContext())
        prepareView()
        adapter.setList(addresses)
        binding.ivTopAction.setOnClickListener(this)
    }

    private fun prepareView() {
        binding.apply {
            tvTitle.text = getString(R.string.withdraw_to)
        }
        binding.includeAddCryptoAddress.let {
            it.ivEndIcon.setImageResource(R.drawable.ic_right_arrow_grey)
            it.ivItem.setImageResource(R.drawable.addaddressicon)
            it.tvStartTitle.text = getString(R.string.add_address)
            it.tvStartSubTitle.text = getString(R.string.unlimited_withdrawl)
        }
        binding.includeAddCryptoAddress.root.setOnClickListener {
            val bundle = Bundle().apply {
                putString(Constants.FROM,WithdrawalUsdcAddressBottomSheet::class.java.name)
            }
            dismiss()
            findNavController().navigate(R.id.addRibFragment,bundle)
        }
    }


    override fun onClick(v: View?) {
        when (v) {
            binding.ivTopAction -> {
                dismiss()
            }
        }
    }

    private class AddressesAdapter(
        private val clickListener1: WithdrawalUsdcAddressBottomSheet,
        private val clickListener: (RIBData?, String?) -> Unit =
            { _, _ -> }
    ) : BaseAdapter<RIBData>() {


        override fun getItemViewType(position: Int): Int {
            return if (itemList[position] != null) ORDINARY_VIEW else LOADER_VIEW
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return when (viewType) {
                ORDINARY_VIEW -> AddressViewHolder(
                    AppItemLayoutBinding.inflate(
                        LayoutInflater.from(
                            parent.context
                        ), parent, false
                    )
                )

                else -> LoaderViewHolder(
                    LoaderViewBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when (holder.itemViewType) {
                ORDINARY_VIEW -> {
                    itemList[position]?.let {
                        (holder as AddressViewHolder).binding.apply {
                            tvStartTitle.text = it.name
                            tvStartSubTitle.gone()
                            tvAddress.visible()
                            tvAddress.text = it.iban
                            ivItem.setImageResource(R.drawable.ic_euro)
                        }
                    }
                }

                else -> {
                    (holder as BaseAdapter<*>.LoaderViewHolder).bind.apply {

                    }
                }
            }
        }

        inner class AddressViewHolder(val binding: AppItemLayoutBinding) :
            RecyclerView.ViewHolder(binding.root) {

            init {

                binding.tvStartSubTitle.ellipsize = TextUtils.TruncateAt.END
                binding.tvStartSubTitle.maxLines = 1
                binding.ivEndIcon.setImageResource(R.drawable.ic_right_arrow_grey)

                binding.root.setOnClickListener {
                    itemList[absoluteAdapterPosition]?.let {
                        clickListener1.dismiss()
                        clickListener(it, null)
                    }
                }
            }

        }
    }
}