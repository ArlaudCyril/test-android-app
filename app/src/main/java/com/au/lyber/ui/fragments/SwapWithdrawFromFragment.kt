package com.au.lyber.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.au.lyber.R
import com.au.lyber.databinding.FragmentSwapFromBinding
import com.au.lyber.databinding.ItemMyAssetBinding
import com.au.lyber.models.AssetBaseData
import com.au.lyber.ui.adapters.BaseAdapter
import com.au.lyber.utils.CommonMethods.Companion.checkInternet
import com.au.lyber.utils.CommonMethods.Companion.commaFormatted
import com.au.lyber.utils.CommonMethods.Companion.dismissProgressDialog
import com.au.lyber.utils.CommonMethods.Companion.getViewModel
import com.au.lyber.utils.CommonMethods.Companion.gone
import com.au.lyber.utils.CommonMethods.Companion.loadCircleCrop
import com.au.lyber.utils.CommonMethods.Companion.replaceFragment
import com.au.lyber.utils.CommonMethods.Companion.roundFloat
import com.au.lyber.utils.CommonMethods.Companion.showProgressDialog
import com.au.lyber.utils.CommonMethods.Companion.visible
import com.au.lyber.utils.Constants
import com.au.lyber.ui.portfolio.viewModel.PortfolioViewModel
import java.util.*

class SwapWithdrawFromFragment : BaseFragment<FragmentSwapFromBinding>(), View.OnClickListener {

    private lateinit var adapter: SwapFromAdapter
    private lateinit var layoutManager: LinearLayoutManager

    private lateinit var viewModel: PortfolioViewModel

    override fun bind() = FragmentSwapFromBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /* view model */

        viewModel = getViewModel(requireActivity())
        viewModel.allMyPortfolio = ""
        viewModel.listener = this

        viewModel.getAssetResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                dismissProgressDialog()
                if (it.assets.isNotEmpty()) {
                    viewModel.exchangeAssetTo = it.assets[0]
                }
                adapter.addList(it.assets)
            }
        }


        /* recycler view */
        adapter =
            SwapFromAdapter(::itemClicked, viewModel.selectedOption == Constants.USING_EXCHANGE)
        layoutManager = LinearLayoutManager(requireContext())
        binding.rvRecyclerView.let {
            it.adapter = adapter
            it.layoutManager = layoutManager
        }

        binding.ivTopAction.setOnClickListener(this)
        binding.rlAllPortfolio.setOnClickListener(this)
        binding.includedAsset.root.setOnClickListener(this)
        prepareUi()
        getData()
    }

    private fun getData() {
        checkInternet(requireContext()) {
            showProgressDialog(requireContext())
            viewModel.getAssets()
        }
    }

    private fun prepareUi() {
        if (viewModel.selectedOption == Constants.USING_EXCHANGE) {
            binding.rlAllPortfolio.gone()
            binding.tvTitle.text = "Exchange → From"
            binding.includedAsset.root.gone()
        } else {
            binding.tvLyberPortfolio.text = "A precise asset"
            binding.tvTitle.text = "I want to withdraw"
            binding.rlAllPortfolio.visible()
            binding.includedAsset.root.visible()
            binding.includedAsset.llFiatWallet.visible()
            binding.includedAsset.ivAssetIcon.setImageResource(R.drawable.ic_euro)
            binding.includedAsset.ivDropIcon.setImageResource(R.drawable.ic_right_arrow_grey)
            binding.includedAsset.tvAssetName.text = "Euro"
//            binding.includedAsset.tvAssetAmountCenter.text =
//                "${App.prefsManager.getBalance()}${Constants.EURO}"
            binding.tvAmountAllPortfolio.text =
                "${viewModel.totalPortfolio.commaFormatted}${Constants.EURO}"
        }
    }

    private fun itemClicked(myAsset: AssetBaseData, position: Int) {

        if (viewModel.selectedOption == Constants.USING_EXCHANGE)
            viewModel.exchangeAssetFrom = myAsset
        else
            viewModel.withdrawAsset = myAsset

        requireActivity().replaceFragment(R.id.flSplashActivity, AddAmountFragment())
    }

    class SwapFromAdapter(
        private val itemClicked: (AssetBaseData, Int) -> Unit = { _, _ -> },
        private val toExchange: Boolean = false
    ) :
        BaseAdapter<AssetBaseData>() {


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SwapViewHolder {
            return SwapViewHolder(
                ItemMyAssetBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            (holder as SwapViewHolder).binding.apply {
                itemList[position]?.let { it ->

                    //val value = it.total_balance * it.euro_amount TODO
                    val value = 0 // TODO : Remove this line
                    if (toExchange) {

                        if (!it.image.isNullOrEmpty())
                             ivAssetIcon.loadCircleCrop(it.image)


                        tvAssetAmount.text = "${value.commaFormatted}${Constants.EURO}"
                       /* tvAssetAmountInCrypto.text = "${
                            it.total_balance.toString().commaFormatted
                        }${it.id.uppercase()}" TODO*/

                    } else {
//                            if (position == itemList.count() - 1) {
//
//                                llFiatWallet.visible()
//                                tvAssetAmount.gone()
//                                tvAssetAmountInCrypto.gone()
//                                tvAssetAmountCenter.visible()
//
//                                tvAssetNameCenter.text = it.name
//
//                                if (it.symbol == Constants.EURO) {
//                                    tvAssetAmountCenter.text =
//                                        "${
//                                            asset.total_balance.toString().roundFloat()
//                                        }${Constants.EURO}"
//                                    ivAssetIcon.setImageResource(R.drawable.ic_euro)
//                                } else {
//                                    if (it.image.endsWith("svg"))
//                                        GlideToVectorYou.init().with(ivAssetIcon.context)
//                                            .load(Uri.parse(it.image), ivAssetIcon)
//                                    else ivAssetIcon.loadCircleCrop(it.image)
//                                    tvAssetAmountCenter.text = value.toString().roundFloat()
//                                }
//
//                            } else {

                        if (!it.image.isNullOrEmpty())
                            ivAssetIcon.loadCircleCrop(it.image)

                        tvAssetAmount.text =
                            "${value.toString().roundFloat()}${Constants.EURO}"

                        /*tvAssetAmountInCrypto.text = "${
                            it.total_balance.toString().roundFloat()
                        }${it.id.uppercase()}" TODO*/

                    }
                }

            }
        }


        inner class SwapViewHolder(val binding: ItemMyAssetBinding) :
            RecyclerView.ViewHolder(binding.root) {

            init {
                binding.apply {
                    // view preparation
                    tvAssetName.gone()
                    ivDropIcon.setImageResource(R.drawable.ic_right_arrow_grey)

                    root.setOnClickListener {
                        itemList[adapterPosition]?.let {
                            itemClicked(it, adapterPosition)
                        }

                    }
                }
            }

        }
    }

    override fun onClick(v: View?) {
        binding.apply {
            when (v!!) {

                rlAllPortfolio -> {
                    viewModel.allMyPortfolio = "${viewModel.totalPortfolio}"
                    requireActivity().replaceFragment(R.id.flSplashActivity, AddAmountFragment())
                }

                ivTopAction -> requireActivity().onBackPressed()

                includedAsset.root -> {
                    viewModel.selectedOption = Constants.USING_WITHDRAW_FIAT
                    requireActivity().replaceFragment(R.id.flSplashActivity, AddAmountFragment())
                }

            }
        }
    }
}