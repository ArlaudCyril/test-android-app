package com.Lyber.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.Lyber.R
import com.Lyber.databinding.FragmentSwapFromBinding
import com.Lyber.models.Balance
import com.Lyber.ui.activities.BaseActivity
import com.Lyber.ui.adapters.BalanceAdapter
import com.Lyber.ui.portfolio.viewModel.PortfolioViewModel
import com.Lyber.utils.CommonMethods
import com.Lyber.utils.CommonMethods.Companion.checkInternet
import com.Lyber.utils.CommonMethods.Companion.commaFormatted
import com.Lyber.utils.CommonMethods.Companion.currencyFormatted
import com.Lyber.utils.CommonMethods.Companion.dismissProgressDialog
import com.Lyber.utils.CommonMethods.Companion.formattedAsset
import com.Lyber.utils.CommonMethods.Companion.replaceFragment
import com.Lyber.utils.CommonMethods.Companion.showProgressDialog
import com.Lyber.utils.CommonMethods.Companion.visible
import com.Lyber.utils.Constants

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
        viewModel.balanceResponse.observe(viewLifecycleOwner){
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                dismissProgressDialog()
                val balanceDataDict = it.data
                val balances = ArrayList<Balance>()
                balanceDataDict.forEach { it1 ->
                    val balance = Balance(id = it1.key, balanceData = it1.value)
                    balances.add(balance)
                }
                com.Lyber.ui.activities.BaseActivity.balances = balances
                adapter.setList(balances)
            }
        }
        /* recycler view */
        adapter = BalanceAdapter(true,::itemClicked)
        layoutManager = LinearLayoutManager(requireContext())
        binding.rvRecyclerView.let {
            it.adapter = adapter
            it.layoutManager = layoutManager
        }

        binding.ivTopAction.setOnClickListener(this)
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
        binding.tvLyberPortfolio.text = getString(R.string.a_precise_asset)
        binding.tvTitle.text = getString(R.string.i_want_to_withdraw)
        binding.rlAllPortfolio.visible()
        binding.includedAsset.root.visible()
        binding.includedAsset.llFiatWallet.visible()
        binding.includedAsset.ivAssetIcon.setImageResource(R.drawable.ic_euro)
        binding.includedAsset.ivDropIcon.setImageResource(R.drawable.ic_right_arrow_grey)
        binding.includedAsset.tvAssetName.text = getString(R.string.euro)
        val balances = com.Lyber.ui.activities.BaseActivity.balances
        var totalPrice = 0.0
        for (balance in balances){
            totalPrice += balance.balanceData.euroBalance.toDouble()
        }
        binding.tvAmountAllPortfolio.text =
            "${totalPrice.commaFormatted.currencyFormatted}"

    }

    private fun itemClicked(myAsset: Balance) {
        val currency = com.Lyber.ui.activities.BaseActivity.assets.find { it.id == myAsset.id }
        if (currency!!.isWithdrawalActive){
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
                    viewModel.selectedOption = Constants.USING_WITHDRAW_FIAT
                    requireActivity().replaceFragment(R.id.flSplashActivity, AddAmountFragment())
                }

            }
        }
    }
}