package com.Lyber.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updatePadding
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.Lyber.R
import com.Lyber.databinding.AppItemLayoutBinding
import com.Lyber.databinding.FragmentInvestmentDetailBinding
import com.Lyber.databinding.LoaderViewBinding
import com.Lyber.models.InvestmentStrategyAsset
import com.Lyber.models.Transaction
import com.Lyber.ui.adapters.BaseAdapter
import com.Lyber.utils.CommonMethods.Companion.checkInternet
import com.Lyber.utils.CommonMethods.Companion.commaFormatted
import com.Lyber.utils.CommonMethods.Companion.decimalPoints
import com.Lyber.utils.CommonMethods.Companion.dismissProgressDialog
import com.Lyber.utils.CommonMethods.Companion.getViewModel
import com.Lyber.utils.CommonMethods.Companion.gone
import com.Lyber.utils.CommonMethods.Companion.showProgressDialog
import com.Lyber.utils.CommonMethods.Companion.visible
import com.Lyber.utils.Constants
import com.Lyber.viewmodels.NetworkViewModel

class InvestmentDetailFragment : BaseFragment<FragmentInvestmentDetailBinding>() {

    private lateinit var viewModel: NetworkViewModel

    private lateinit var adapter: HistoryAdapter
    private var investmentId: String = ""
    override fun bind() = FragmentInvestmentDetailBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            investmentId = it.getString(Constants.INVESTMENT_ID, "") ?: ""
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = getViewModel(this)
        viewModel.listener = this

        adapter = HistoryAdapter()

        viewModel.recurringInvestmentDetail.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {

                dismissProgressDialog()

                binding.tvHistory.visible()
                adapter.addList(it.history)

                if (it.type == Constants.SINGULAR) {

                    binding.tvTitle.text = "${it.asset_id}"
                    binding.rlAssetDetail.visible()
                    binding.tvAssetFrequency.text = it.frequency.uppercase()
                    binding.tvAssetPrice.text = it.asset_amount.commaFormatted + Constants.EURO

                } else {

                    binding.strategyView.visible()
                    binding.strategyView.yeild = getString(R.string._5_roi)
                    binding.strategyView.risk = getString(R.string.medium)
                    binding.strategyView.topText = it.strategy_name
                    binding.strategyView.strategyPrice = "${it.amount} ${Constants.EURO}"
                    binding.strategyView.binding.tvPriceStrategy.visible()
                    //make text visible

                    binding.tvTitle.text = "${it.strategy_name} (${it.frequency.lowercase()})"
                    binding.strategyView.radioButton.gone()

                    val list = mutableListOf<InvestmentStrategyAsset>()

                    for (i in it.strategy_assets) {
                        list.add(
                            InvestmentStrategyAsset(
                                allocation = i.allocation,
                                _id = i._id,
                                asset_id = i.asset_id
                            )
                        )
                    }

                    binding.strategyView.allocationView.setAssetsList(list)

                }
            }
        }

        viewModel.cancelRecurringInvestment.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                dismissProgressDialog()
                requireActivity().onBackPressed()
            }
        }

        binding.ivTopAction.setOnClickListener {
            requireActivity().onBackPressed()
        }

        checkInternet(requireContext()) {
            showProgressDialog(requireContext())
            viewModel.getRecurringInvestmentDetail(investmentId)
        }

        binding.btnCancelInvestment.setOnClickListener {
            checkInternet(requireContext()) {
                showProgressDialog(requireContext())
                viewModel.cancelRecurringInvestment(investmentId)
            }
        }

        binding.rvHistory.let {
            it.adapter = adapter
            it.layoutManager = LinearLayoutManager(requireContext())
        }

    }

    // adapter for history items

    class HistoryAdapter : BaseAdapter<Transaction>() {


        inner class TransactionViewHolder(val binding: AppItemLayoutBinding) :
            RecyclerView.ViewHolder(binding.root) {
            init {
                binding.root.updatePadding(12, 12, 12, 12)
                binding.ivItem.gone()
            }
        }

        override fun getItemViewType(position: Int): Int {
            return if (itemList[position] == null) LOADER_VIEW else ORDINARY_VIEW
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return when (viewType) {
                ORDINARY_VIEW -> {
                    TransactionViewHolder(
                        AppItemLayoutBinding.inflate(
                            LayoutInflater.from(parent.context),
                            parent,
                            false
                        )
                    )
                }
                else -> {
                    LoaderViewHolder(
                        LoaderViewBinding.inflate(
                            LayoutInflater.from(parent.context),
                            parent,
                            false
                        )
                    )
                }
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when (holder.itemViewType) {
                ORDINARY_VIEW -> {
                    itemList[position]?.let {
                        (holder as TransactionViewHolder).binding.apply {

                            when (it.type) {
                                1 -> { //exchange

                                    tvStartTitleCenter.text =
                                        "Exch. ${it.exchange_from.uppercase()} to ${it.exchange_to.uppercase()}"
                                    tvEndTitle.text =
                                        "-${
                                            it.exchange_from_amount.toString().decimalPoints(6)
                                        }${it.exchange_from.uppercase()}"
                                    tvEndSubTitle.text =
                                        "${
                                            it.exchange_to_amount.toString().decimalPoints(5)
                                        }${it.exchange_to.uppercase()}"
                                }
                                2 -> { // deposit
                                    root.gone()
                                }
                                3 -> { // withdraw
//                                ivItem.setImageResource(R.drawable.ic_withdraw)
                                    tvStartTitleCenter.text = "Withdrawal"
                                    tvEndTitle.text = "-${it.amount}${Constants.EURO}"
                                    tvEndSubTitle.text =
                                        "${
                                            it.asset_amount.toString().decimalPoints(5)
                                        }${it.asset_id.uppercase()}"
                                }
                                4 -> { // single asset
//                                ivItem.setImageResource(R.drawable.ic_deposit)
                                    tvStartTitleCenter.text = "Bought ${it.asset_id.uppercase()}"
                                    tvEndTitle.text =
                                        "+${it.amount.toFloat().toInt()}${Constants.EURO}"
                                    tvEndSubTitle.text =
                                        "${
                                            it.asset_amount.toString().decimalPoints(5)
                                        }${it.asset_id.uppercase()}"
                                }
                                else -> root.gone()
                            }

                        }
                    }
                }
                LOADER_VIEW -> {}
            }
        }

    }

    companion object {
        fun get(investmentId: String): InvestmentDetailFragment {
            return InvestmentDetailFragment().apply {
                arguments = Bundle().apply {
                    putString(Constants.INVESTMENT_ID, investmentId)
                }
            }
        }
    }

}