package com.au.lyber.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.au.lyber.R
import com.au.lyber.databinding.FragmentSwapFromBinding
import com.au.lyber.models.Balance
import com.au.lyber.ui.activities.BaseActivity
import com.au.lyber.ui.adapters.BalanceAdapter
import com.au.lyber.ui.portfolio.viewModel.PortfolioViewModel
import com.au.lyber.utils.CommonMethods
import com.au.lyber.utils.CommonMethods.Companion.checkInternet
import com.au.lyber.utils.CommonMethods.Companion.commaFormatted
import com.au.lyber.utils.CommonMethods.Companion.currencyFormatted
import com.au.lyber.utils.CommonMethods.Companion.dismissProgressDialog
import com.au.lyber.utils.CommonMethods.Companion.formattedAsset
import com.au.lyber.utils.CommonMethods.Companion.replaceFragment
import com.au.lyber.utils.CommonMethods.Companion.showProgressDialog
import com.au.lyber.utils.CommonMethods.Companion.visible
import com.au.lyber.utils.Constants

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
                BaseActivity.balances = balances
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
        binding.tvLyberPortfolio.text = getString(R.string.a_precise_asset)
        binding.tvTitle.text = getString(R.string.i_want_to_withdraw)
        binding.rlAllPortfolio.visible()
        binding.includedAsset.root.visible()
        binding.includedAsset.llFiatWallet.visible()
        binding.includedAsset.ivAssetIcon.setImageResource(R.drawable.ic_euro)
        binding.includedAsset.ivDropIcon.setImageResource(R.drawable.ic_right_arrow_grey)
        binding.includedAsset.tvAssetName.text = getString(R.string.euro)
        val balances = BaseActivity.balances
        var totalPrice = 0.0
        for (balance in balances){
            totalPrice += balance.balanceData.euroBalance.toDouble()
        }
        binding.tvAmountAllPortfolio.text =
            "${totalPrice.commaFormatted.currencyFormatted}"

    }

    private fun itemClicked(myAsset: Balance) {
        val currency = BaseActivity.assets.find { it.id == myAsset.id }
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