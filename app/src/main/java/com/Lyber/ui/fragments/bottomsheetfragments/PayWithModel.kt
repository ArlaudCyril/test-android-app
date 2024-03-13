package com.Lyber.ui.fragments.bottomsheetfragments

import android.graphics.drawable.Icon
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat.getColor
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.Lyber.R
import com.Lyber.databinding.AppItemLayoutBinding
import com.Lyber.databinding.LoaderViewBinding
import com.Lyber.databinding.PayWithModelBottomSheetBinding
import com.Lyber.models.AssetBaseData
import com.Lyber.models.WhitelistingResponse
import com.Lyber.models.Whitelistings
import com.Lyber.ui.adapters.BaseAdapter
import com.Lyber.ui.fragments.AddBankInfoFragment
import com.Lyber.ui.fragments.AddCryptoAddress
import com.Lyber.utils.CommonMethods.Companion.getViewModel
import com.Lyber.utils.CommonMethods.Companion.gone
import com.Lyber.utils.CommonMethods.Companion.loadCircleCrop
import com.Lyber.utils.CommonMethods.Companion.replaceFragment
import com.Lyber.utils.CommonMethods.Companion.visible
import com.Lyber.viewmodels.PortfolioViewModel
import com.google.gson.Gson

class PayWithModel(
    private val handle: (Whitelistings?, String?) -> Unit = { _, _ -> }
) : BaseBottomSheet<PayWithModelBottomSheetBinding>(), View.OnClickListener {

    private var selectedAsset: AssetBaseData? = null
    private var withdrawTo: Boolean = false
    private var withdrawAllAsset: Boolean = false

    private lateinit var adapter: PayAdapter
    private lateinit var viewModel: PortfolioViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = getViewModel(requireActivity())
        adapter = PayAdapter(handle)
        arguments?.let {
            val data = it.getString(SELECTED_ASSET, "") ?: ""
            selectedAsset = Gson().fromJson(data, AssetBaseData::class.java)
            withdrawTo = it.getBoolean(WITHDRAW, false)
            withdrawAllAsset = it.getBoolean(ALL_PORTFOLIO, false)
        }

    }

    override fun bind() = PayWithModelBottomSheetBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startProgress()

        binding.includeAddedBank.root.gone()

        binding.ivTopAction.setOnClickListener(this)
        binding.includeAddBank.root.setOnClickListener(this)
        binding.includeAddCryptoAddress.root.setOnClickListener(this)

        viewModel.getWhiteListing.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                dismissProgress()
                setAddress(it)
            }
        }

        viewModel.getWhiteListing.value?.let {
            dismissProgress()
            setAddress(it)
        }

        binding.rvPayOptions.adapter = adapter
        binding.rvPayOptions.layoutManager = LinearLayoutManager(requireContext())


        prepareView()

    }


    private fun setAddress(whitelists: WhitelistingResponse) {
        adapter.setList(whitelists.addresses)
    }

    private fun startProgress() {
        binding.ivProgress.visible()
        binding.ivProgress.animation =
            AnimationUtils.loadAnimation(requireContext(), R.anim.rotate_drawable)
    }

    private fun dismissProgress() {
        binding.ivProgress.gone()
        binding.ivProgress.clearAnimation()
    }

    private fun prepareView() {
        binding.includeAddBank.let {
            it.ivItem.setImageIcon(
                Icon.createWithResource(
                    requireContext(),
                    R.drawable.ic_bank_fill
                ).apply {
                    setTint(getColor(requireContext(), R.color.purple_500))
                })
            it.ivEndIcon.setImageResource(R.drawable.ic_right_arrow_grey)
            it.tvStartTitle.text =requireContext().getString(R.string.add_a_bank_account)
            it.tvStartSubTitle.text = requireContext().getString(R.string.limited_to_25_000_per_week)
        }
        binding.includeAddCryptoAddress.let {
            it.ivEndIcon.setImageResource(R.drawable.ic_right_arrow_grey)
            it.ivItem.setImageResource(R.drawable.ic_add_btc_address)
            it.tvStartTitle.text = "${getString(R.string.add)} ${viewModel.withdrawAsset?.id?.uppercase()} ${getString(R.string.address_)}"
            it.tvStartSubTitle.text = getString(R.string.unlimited_withdrawl)

        }
    }


    override fun onClick(v: View?) {
        binding.apply {
            when (v!!) {

                ivTopAction -> dismiss()

                includeAddBank.root -> {
                    dismiss()
                    requireActivity().replaceFragment(R.id.flSplashActivity, AddBankInfoFragment())
                }
                includeAddCryptoAddress.root -> {
                    dismiss()
                    requireActivity().replaceFragment(R.id.flSplashActivity, AddCryptoAddress())
                }

            }
        }
    }

    companion object {

        private const val SELECTED_ASSET: String = "selectedAsset"
        private const val WITHDRAW: String = "withdraw"
        private const val ALL_PORTFOLIO = "allPortfolio"

        fun getToWithdraw(
            asset: AssetBaseData?,
            handle: (Whitelistings?, String?) -> Unit,
            withdraw: Boolean = false,
            allPortfolio: Boolean = false
        ): PayWithModel {

            return PayWithModel(handle).apply {
                arguments = Bundle().apply {
                    asset?.let {
                        putString(SELECTED_ASSET, Gson().toJson(it))
                    }
                    putBoolean(WITHDRAW, withdraw)
                    putBoolean(ALL_PORTFOLIO, allPortfolio)
                }
            }
        }
    }


    private class PayAdapter(
        private val clickListener: (Whitelistings?, String?) -> Unit =
            { _, _ -> }
    ) : BaseAdapter<Whitelistings>() {


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
                            ivItem.loadCircleCrop(it.logo)

                            tvStartTitle.text = it.name
                            tvStartSubTitle.text = it.address

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
                        clickListener(it, null)
                    }
                }
            }

        }
    }


}