package com.Lyber.ui.fragments.bottomsheetfragments

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.Lyber.R
import com.Lyber.databinding.AppItemLayoutBinding
import com.Lyber.databinding.BottomsheetWithdrawalAddressesBinding
import com.Lyber.databinding.LoaderViewBinding
import com.Lyber.models.WithdrawAddress
import com.Lyber.ui.activities.BaseActivity
import com.Lyber.ui.adapters.BaseAdapter
import com.Lyber.utils.CommonMethods.Companion.gone
import com.Lyber.utils.CommonMethods.Companion.loadCircleCrop
import com.Lyber.utils.CommonMethods.Companion.visible
import com.Lyber.utils.Constants

class WithdrawalAddressBottomSheet (private val addresses: MutableList<WithdrawAddress>, private val selectedNetwork:String, private val handle: (WithdrawAddress?, String?) -> Unit = { _, _ -> }
) : BaseBottomSheet<BottomsheetWithdrawalAddressesBinding>(), View.OnClickListener {
    private lateinit var adapter: PayAdapter
    override fun bind()= BottomsheetWithdrawalAddressesBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter = PayAdapter(this,handle)
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
            val bundle =Bundle().apply {
                putBoolean(Constants.ACTION_WITHDRAW,true)
                putString(Constants.ID,selectedNetwork)
            }
            dismiss()
            findNavController().navigate(R.id.addCryptoAddress,bundle)
        }
    }


    override fun onClick(v: View?) {
      when(v){
          binding.ivTopAction->{
              dismiss()
          }
      }
    }

    private class PayAdapter(
        private val clickListener1: WithdrawalAddressBottomSheet,
        private val clickListener: (WithdrawAddress?, String?) -> Unit =
            { _, _ -> }
    ) : BaseAdapter<WithdrawAddress>() {


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
                            val data = it
                            val assest = com.Lyber.ui.activities.BaseActivity.networkAddress.firstNotNullOfOrNull{ item -> item.takeIf {item.id == data.network}}
                            ivItem.loadCircleCrop(assest!!.imageUrl)
                            tvStartTitle.text = it.name
                            tvStartSubTitle.gone()
                            tvAddress.visible()
                            tvAddress.text = it.address

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
                    itemList[adapterPosition]?.let {
                        clickListener1.dismiss()
                        clickListener(it, null)
                    }
                }
            }

        }
    }
}