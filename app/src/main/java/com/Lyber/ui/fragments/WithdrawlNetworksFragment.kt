package com.Lyber.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.Lyber.R
import com.Lyber.databinding.FragmentListingBinding
import com.Lyber.databinding.ItemNetworkBinding
import com.Lyber.models.NetworkDeposit
import com.Lyber.ui.adapters.BaseAdapter
import com.Lyber.utils.AppLifeCycleObserver
import com.Lyber.viewmodels.PortfolioViewModel
import com.Lyber.utils.CommonMethods
import com.Lyber.utils.CommonMethods.Companion.gone
import com.Lyber.utils.CommonMethods.Companion.loadCircleCrop
import com.Lyber.utils.CommonMethods.Companion.visible
import com.Lyber.utils.Constants

class WithdrawlNetworksFragment : BaseFragment<FragmentListingBinding>() {
    private lateinit var viewModel: PortfolioViewModel
    private var assetId = ""
    private lateinit var adapter: NetworkAdapter
    override fun bind() = FragmentListingBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        assetId = requireArguments().getString(Constants.ID, "")
        viewModel = CommonMethods.getViewModel(requireActivity())
        setObservers()
        adapter = NetworkAdapter(::itemClicked)
        val layoutManager = LinearLayoutManager(requireContext())
        binding.rvAddAsset.let {
            it.adapter = adapter
            it.layoutManager = layoutManager
        }
        binding.ivTopAction.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        hitApi()
        binding.rvRefresh.setOnRefreshListener {
            binding.rvRefresh.isRefreshing = true
           hitApi()
        }

    }

    private fun hitApi() {
        CommonMethods.checkInternet(binding.root,requireActivity()) {
            if(!binding.rvRefresh.isRefreshing)
            CommonMethods.showProgressDialog(requireActivity())
            viewModel.getAssetDetailIncludeNetworks(assetId)
        }
    }

    override fun onResume() {
        super.onResume()
        if (AppLifeCycleObserver.fromBack) {
            AppLifeCycleObserver.fromBack = false
            hitApi()
        }
    }

    private fun setObservers() {
        viewModel.getAssetDetail.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                binding.rvRefresh.isRefreshing=false
                CommonMethods.dismissProgressDialog()
                viewModel.selectedAssetDetail = it.data
                adapter.setList(it.data.networks)

            }
        }
    }

    private fun itemClicked(myAsset: NetworkDeposit) {
        if (myAsset.isWithdrawalActive) {
            val bundle = Bundle()
            viewModel.selectedNetworkDeposit = myAsset
            bundle.putString(Constants.ID, myAsset.id)
            bundle.putString("assetIdWithdraw", assetId)
            findNavController().navigate(R.id.withdrawAmountFragment, bundle)
        }
    }

    class NetworkAdapter(
        private val handle: (NetworkDeposit) -> Unit
    ) :
        BaseAdapter<NetworkDeposit>() {
        override fun getItemCount(): Int {
            return itemList.size
        }

        inner class NetworkHolder(val binding: ItemNetworkBinding) :
            RecyclerView.ViewHolder(binding.root) {
            init {
                binding.root.setOnClickListener {
                    handle(itemList[absoluteAdapterPosition]!!)
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return NetworkHolder(
                ItemNetworkBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            (holder as NetworkHolder).binding.apply {
                val data = itemList[position]
                ivAssetIcon.loadCircleCrop(data!!.imageUrl)
//                "${context.getString(R.string.withdraw_on)} ${data.fullName}".also { tvAssetName.text = it }
                "${data.fullName}".also { tvAssetName.text = it }
                if (data.isWithdrawalActive) {
                    tvAssetNameCode.gone()
                } else {
                    tvAssetNameCode.visible()
                }
            }
        }
    }
}