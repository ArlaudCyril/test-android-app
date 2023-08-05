package com.au.lyber.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.au.lyber.R
import com.au.lyber.databinding.FragmentSwapFromBinding
import com.au.lyber.databinding.ItemMyAssetBinding
import com.au.lyber.models.AssetBaseData
import com.au.lyber.ui.activities.BaseActivity
import com.au.lyber.ui.adapters.BaseAdapter
import com.au.lyber.ui.portfolio.viewModel.PortfolioViewModel
import com.au.lyber.utils.CommonMethods
import com.au.lyber.utils.CommonMethods.Companion.checkInternet
import com.au.lyber.utils.CommonMethods.Companion.commaFormatted
import com.au.lyber.utils.CommonMethods.Companion.currencyFormatted
import com.au.lyber.utils.CommonMethods.Companion.dismissProgressDialog
import com.au.lyber.utils.CommonMethods.Companion.formattedAsset
import com.au.lyber.utils.CommonMethods.Companion.gone
import com.au.lyber.utils.CommonMethods.Companion.loadCircleCrop
import com.au.lyber.utils.CommonMethods.Companion.replaceFragment
import com.au.lyber.utils.CommonMethods.Companion.showProgressDialog
import com.au.lyber.utils.CommonMethods.Companion.visible
import com.au.lyber.utils.Constants
import java.math.RoundingMode
import java.util.*

class SwapWithdrawFromFragment : BaseFragment<FragmentSwapFromBinding>(), View.OnClickListener {

    private lateinit var adapter: SwapFromAdapter
    private lateinit var layoutManager: LinearLayoutManager

    private lateinit var viewModel: PortfolioViewModel

    override fun bind() = FragmentSwapFromBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /* view model */

        viewModel = CommonMethods.getViewModel(requireActivity())
        viewModel.allMyPortfolio = ""
        viewModel.listener = this

        viewModel.allAssets.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                dismissProgressDialog()
                if (it.data.isNotEmpty()) {
                  //  viewModel.exchangeAssetTo = it.data[0]
                }
                adapter.addList(it.data)
            }
        }


        /* recycler view */
        adapter =
            SwapFromAdapter(::itemClicked)
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
            viewModel.getBalance()
        }
    }

    private fun prepareUi() {
        if (viewModel.selectedOption == Constants.USING_EXCHANGE) {
            binding.rlAllPortfolio.gone()
            binding.tvTitle.text = getString(R.string.exchange_from)
            binding.rlAllPortfolio.gone()
            binding.includedAsset.root.visible()
            binding.includedAsset.llFiatWallet.visible()
            binding.includedAsset.ivAssetIcon.setImageResource(R.drawable.ic_euro)
            binding.includedAsset.ivDropIcon.setImageResource(R.drawable.ic_right_arrow_grey)
            binding.includedAsset.tvAssetName.text = getString(R.string.euro)
            binding.tvAmountAllPortfolio.text =
                "${viewModel.totalPortfolio.commaFormatted}${Constants.EURO}"
        } else {
            binding.tvLyberPortfolio.text = getString(R.string.a_precise_asset)
            binding.tvTitle.text = getString(R.string.i_want_to_withdraw)
            binding.rlAllPortfolio.visible()
            binding.includedAsset.root.visible()
            binding.includedAsset.llFiatWallet.visible()
            binding.includedAsset.ivAssetIcon.setImageResource(R.drawable.ic_euro)
            binding.includedAsset.ivDropIcon.setImageResource(R.drawable.ic_right_arrow_grey)
            binding.includedAsset.tvAssetName.text = getString(R.string.euro)
//            binding.includedAsset.tvAssetAmountCenter.text =
//                "${App.prefsManager.getBalance()}${Constants.EURO}"
            binding.tvAmountAllPortfolio.text =
                "${viewModel.totalPortfolio.commaFormatted}${Constants.EURO}"
        }
    }

    private fun itemClicked(myAsset: AssetBaseData, position: Int) {

/*        if (viewModel.selectedOption == Constants.USING_EXCHANGE)
            viewModel.exchangeAssetFrom = myAsset
        else*/
            viewModel.withdrawAsset = myAsset

        findNavController().navigate(R.id.allAssetFragment)
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

                    if (!it.imageUrl.isNullOrEmpty())
                        ivAssetIcon.loadCircleCrop(it.imageUrl)
                    tvAssetName.text = it.fullName
                    if (it.isTradeActive){
                        tvAssetNameCode.gone()
                    }else{
                        tvAssetNameCode.visible()
                    }
                    for (balance in BaseActivity.balances){
                        if (balance.id == it.id){
                            val priceCoin = balance.balanceData.euroBalance.toDouble()
                                .div(balance.balanceData.balance.toDouble() ?: 1.0)
                            tvAssetAmount.text= balance.balanceData.euroBalance.commaFormatted.currencyFormatted
                            tvAssetAmountInCrypto.text =balance.balanceData.balance.formattedAsset(
                                price = priceCoin,
                                rounding = RoundingMode.DOWN
                            )
                        }
                    }

                }

            }
        }


        inner class SwapViewHolder(val binding: ItemMyAssetBinding) :
            RecyclerView.ViewHolder(binding.root) {

            init {
                binding.apply {
                    // view preparation
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