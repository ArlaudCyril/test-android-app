package com.Lyber.dev.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.Lyber.dev.R
import com.Lyber.dev.databinding.FragmentSwapFromBinding
import com.Lyber.dev.models.Balance
import com.Lyber.dev.ui.activities.BaseActivity
import com.Lyber.dev.ui.adapters.BalanceAdapter
import com.Lyber.dev.viewmodels.PortfolioViewModel
import com.Lyber.dev.utils.CommonMethods
import com.Lyber.dev.utils.CommonMethods.Companion.checkInternet
import com.Lyber.dev.utils.CommonMethods.Companion.commaFormatted
import com.Lyber.dev.utils.CommonMethods.Companion.currencyFormatted
import com.Lyber.dev.utils.CommonMethods.Companion.dismissProgressDialog
import com.Lyber.dev.utils.CommonMethods.Companion.formattedAsset
import com.Lyber.dev.utils.CommonMethods.Companion.gone
import com.Lyber.dev.utils.CommonMethods.Companion.loadCircleCrop
import com.Lyber.dev.utils.CommonMethods.Companion.replaceFragment
import com.Lyber.dev.utils.CommonMethods.Companion.showProgressDialog
import com.Lyber.dev.utils.CommonMethods.Companion.visible
import com.Lyber.dev.utils.Constants
import java.math.RoundingMode

class SwapWithdrawFromFragment : BaseFragment<FragmentSwapFromBinding>(), View.OnClickListener {

    private lateinit var adapter: BalanceAdapter
    private lateinit var layoutManager: LinearLayoutManager

    private lateinit var viewModel: PortfolioViewModel

    override fun bind() = FragmentSwapFromBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = CommonMethods.getViewModel(requireActivity())
        viewModel.allMyPortfolio = ""
        viewModel.listener = this
        viewModel.balanceResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                dismissProgressDialog()
                val balanceDataDict = it.data
                val balances = ArrayList<Balance>()
                balanceDataDict.forEach { it1 ->
                    val balance = Balance(id = it1.key, balanceData = it1.value)
                    balances.add(balance)
                    if (it1.key == Constants.MAIN_ASSET)
                        setBankAccount(balance)

                }
                com.Lyber.dev.ui.activities.BaseActivity.balances = balances
                balances.sortByDescending { it.balanceData.euroBalance.toDoubleOrNull() }
                adapter.setList(balances)
            }
        }
        /* recycler view */
        adapter = BalanceAdapter(true, ::itemClicked)
        layoutManager = LinearLayoutManager(requireContext())
        binding.rvRecyclerView.let {
            it.adapter = adapter
            it.layoutManager = layoutManager
        }

        binding.ivTopAction.setOnClickListener(this)
        binding.includedAsset.root.setOnClickListener(this)
        prepareUi()
        getData()
    }

    private fun setBankAccount(balance: Balance) {
        val priceCoin = balance.balanceData.euroBalance.toDouble()
            .div(balance.balanceData.balance.toDouble() ?: 1.0)
        binding.includedAsset.tvAssetAmount.text =
            balance.balanceData.euroBalance.commaFormatted.currencyFormatted
        binding.includedAsset.tvAssetAmountInCrypto.text =
            balance.balanceData.balance.formattedAsset(
                priceCoin,
                rounding = RoundingMode.DOWN
            )
//            ) + " USDT"

    }

    private fun getData() {
        checkInternet(binding.root,requireContext()) {
            showProgressDialog(requireContext())
            viewModel.getBalance()
        }
    }

    private fun prepareUi() {
//        binding.tvLyberPortfolio.text = getString(R.string.a_precise_asset)
        binding.tvTitle.text = getString(R.string.i_want_to_withdraw)
        binding.rlAllPortfolio.gone()
        binding.tvOnMyBank.visible()
        binding.includedAsset.root.visible()
        binding.includedAsset.llFiatWallet.gone()
        val currency =
            com.Lyber.dev.ui.activities.BaseActivity.assets.find { it.id == Constants.MAIN_ASSET }
        binding.includedAsset.ivAssetIcon.loadCircleCrop(currency?.imageUrl ?: "")
        binding.includedAsset.ivDropIcon.setImageResource(R.drawable.ic_right_arrow_grey)
        binding.includedAsset.tvAssetName.text = "USDC"

    }


    private fun itemClicked(myAsset: Balance) {
        val currency = com.Lyber.dev.ui.activities.BaseActivity.assets.find { it.id == myAsset.id }
        if (currency!!.isWithdrawalActive) {
            val bundle = Bundle()
            bundle.putString(Constants.ID, myAsset.id)
            findNavController().navigate(R.id.withdrawlNetworksFragment, bundle)
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
                    if (BaseActivity.ribWalletList.isEmpty()) {
                        findNavController().navigate(R.id.addRibFragment)
                    } else {
                        findNavController().navigate(R.id.ribListingFragment)

                    }

                }

            }
        }
    }
}