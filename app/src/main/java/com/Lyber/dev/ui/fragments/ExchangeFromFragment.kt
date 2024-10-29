package com.Lyber.dev.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.Lyber.dev.R
import com.Lyber.dev.databinding.FragmentSwapFromBinding
import com.Lyber.dev.models.Balance
import com.Lyber.dev.ui.activities.SplashActivity
import com.Lyber.dev.ui.adapters.BalanceAdapter
import com.Lyber.dev.utils.AppLifeCycleObserver
import com.Lyber.dev.viewmodels.PortfolioViewModel
import com.Lyber.dev.utils.CommonMethods
import com.Lyber.dev.utils.CommonMethods.Companion.commaFormatted
import com.Lyber.dev.utils.CommonMethods.Companion.gone
import com.Lyber.dev.utils.CommonMethods.Companion.visible
import com.Lyber.dev.utils.Constants
import com.google.android.gms.tasks.Task
import com.google.android.play.core.integrity.StandardIntegrityManager

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
                val balancesDummy = ArrayList<Balance>()
                balanceDataDict.forEach { it1 ->
                    val balance = Balance(id = it1.key, balanceData = it1.value)
                    if (arguments != null && requireArguments().getString(Constants.TYPE)
                        == Constants.FROM_SWAP
                    ) {
                        if (viewModel.exchangeAssetTo != balance.id){
                            balances.add(balance)
                        }
                    }else if (arguments != null && requireArguments().getString(Constants.TYPE)
                        == Constants.TO_SWAP
                    ) {
                        if (viewModel.exchangeAssetFrom != balance.id){
                            balances.add(balance)
                        }
                    } else {
                        balances.add(balance)
                    }
                    balancesDummy.add(balance)
                }
                com.Lyber.dev.ui.activities.BaseActivity.balances = balancesDummy
                if(balances.isNotEmpty()) {
                    binding.tvNoAssets.gone()
                    adapter.setList(balances)
                }
                else
                    binding.tvNoAssets.visible()
            }
        }


        /* recycler view */
        adapter = BalanceAdapter(false,::itemClicked)
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
        CommonMethods.checkInternet(binding.root,requireContext()) {
            CommonMethods.showProgressDialog(requireContext())
                viewModel.getBalance()
        }
    }

    override fun onResume() {
        super.onResume()
        if(AppLifeCycleObserver.fromBack){
            AppLifeCycleObserver.fromBack=false
            getData()
        }
    }

    private fun prepareUi() {

        binding.rlAllPortfolio.gone()
        binding.tvTitle.text = getString(R.string.exchange_from)
        binding.rlAllPortfolio.gone()
        binding.tvLyberPortfolio.text=getString(R.string.lyber_portfolio)
        binding.tvOnMyBank.gone()
        binding.includedAsset.root.gone()
        binding.includedAsset.llFiatWallet.gone()
        binding.includedAsset.ivAssetIcon.setImageResource(R.drawable.ic_euro)
        binding.includedAsset.ivDropIcon.setImageResource(R.drawable.ic_right_arrow_grey)
        binding.includedAsset.tvAssetName.text = getString(R.string.euro)
        "${viewModel.totalPortfolio.commaFormatted}${Constants.EURO}".also { binding.tvAmountAllPortfolio.text = it }
    }

    private fun itemClicked(myAsset: Balance) {
//        viewModel.exchangeAssetFrom = myAsset.id
        if (arguments!=null&& requireArguments().containsKey(Constants.TYPE)
            && requireArguments().getString(Constants.TYPE) == Constants.FROM_SWAP
        ) {
            viewModel.exchangeAssetFrom = myAsset.id
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        else if (arguments!=null&& requireArguments().containsKey(Constants.TYPE)
            && requireArguments().getString(Constants.TYPE) == Constants.TO_SWAP
        ) {
            viewModel.exchangeAssetTo = myAsset.id
            requireActivity().onBackPressedDispatcher.onBackPressed()
        } else {
            viewModel.exchangeAssetFrom = myAsset.id
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