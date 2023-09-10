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
import com.au.lyber.utils.CommonMethods.Companion.commaFormatted
import com.au.lyber.utils.CommonMethods.Companion.gone
import com.au.lyber.utils.CommonMethods.Companion.visible
import com.au.lyber.utils.Constants

class ExchangeFromFragment : BaseFragment<FragmentSwapFromBinding>(), View.OnClickListener {

    private lateinit var adapter: BalanceAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var viewModel: PortfolioViewModel

    override fun bind() = FragmentSwapFromBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /* view model */

        viewModel = CommonMethods.getViewModel(requireActivity())
        viewModel.allMyPortfolio = ""
        viewModel.listener = this
        viewModel.balanceResponse.observe(viewLifecycleOwner){
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                CommonMethods.dismissProgressDialog()
                val balanceDataDict = it.data
                val balances = ArrayList<Balance>()
                balanceDataDict.forEach { it1 ->
                    val balance = Balance(id = it1.key, balanceData = it1.value)
                    if (arguments != null && requireArguments().getString(Constants.TYPE)
                        == Constants.FROM_SWAP
                    ) {
                        if (viewModel.exchangeAssetTo!!.id != balance.id){
                            balances.add(balance)
                        }
                    } else {
                        balances.add(balance)
                    }
                }
                BaseActivity.balances = balances
                adapter.setList(balances)
            }
        }


        /* recycler view */
        adapter = BalanceAdapter(::itemClicked)
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
        CommonMethods.checkInternet(requireContext()) {
            CommonMethods.showProgressDialog(requireContext())
            viewModel.getBalance()
        }
    }

    private fun prepareUi() {

        binding.rlAllPortfolio.gone()
        binding.tvTitle.text = getString(R.string.exchange_from)
        binding.rlAllPortfolio.gone()
        binding.includedAsset.root.visible()
        binding.includedAsset.llFiatWallet.visible()
        binding.includedAsset.ivAssetIcon.setImageResource(R.drawable.ic_euro)
        binding.includedAsset.ivDropIcon.setImageResource(R.drawable.ic_right_arrow_grey)
        binding.includedAsset.tvAssetName.text = getString(R.string.euro)
        "${viewModel.totalPortfolio.commaFormatted}${Constants.EURO}".also { binding.tvAmountAllPortfolio.text = it }
    }

    private fun itemClicked(myAsset: Balance) {
        viewModel.exchangeAssetFromResume = (myAsset.balanceData.euroBalance.toDouble()/myAsset.balanceData.balance
            .toDouble()).toString()
        viewModel.exchangeAssetFrom = myAsset
        if (arguments!=null&& requireArguments().containsKey(Constants.TYPE)
            && requireArguments().getString(Constants.TYPE) == Constants.FROM_SWAP
        ) {
            requireActivity().onBackPressed()
        } else {
            val bundle = Bundle()
            bundle.putString(Constants.TYPE, Constants.Exchange)
            findNavController().navigate(R.id.allAssetFragment, bundle)
        }
    }



    override fun onClick(v: View?) {
        binding.apply {
            when (v!!) {
                ivTopAction -> requireActivity().onBackPressed()
            }
        }
    }
}