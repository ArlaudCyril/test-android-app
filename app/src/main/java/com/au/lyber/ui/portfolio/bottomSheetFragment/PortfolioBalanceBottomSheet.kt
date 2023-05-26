package com.au.lyber.ui.portfolio.bottomSheetFragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.au.lyber.R
import com.au.lyber.databinding.BottomSheetBalanceDetailBinding
import com.au.lyber.databinding.ItemBalanceDetailPortfolioBinding
import com.au.lyber.databinding.ItemBalancePortfolioHistoryBinding
import com.au.lyber.databinding.LoaderViewBinding
import com.au.lyber.models.Transaction
import com.au.lyber.ui.adapters.BaseAdapter
import com.au.lyber.ui.fragments.bottomsheetfragments.BaseBottomSheet
import com.au.lyber.ui.portfolio.viewModel.PortfolioViewModel
import com.au.lyber.utils.CommonMethods.Companion.checkInternet
import com.au.lyber.utils.CommonMethods.Companion.commaFormatted
import com.au.lyber.utils.CommonMethods.Companion.decimalPoints
import com.au.lyber.utils.CommonMethods.Companion.getViewModel
import com.au.lyber.utils.CommonMethods.Companion.gone
import com.au.lyber.utils.CommonMethods.Companion.visible
import com.au.lyber.utils.CommonMethods.Companion.zoomIn
import com.au.lyber.utils.Constants
import com.au.lyber.utils.ItemOffsetDecoration
import okhttp3.ResponseBody

class PortfolioBalanceBottomSheet(private val clickListener: (BalanceInfo) -> Unit = { _ -> }) :
    BaseBottomSheet<BottomSheetBalanceDetailBinding>() {


    private lateinit var viewModel: PortfolioViewModel
    private lateinit var infoAdapter: BalanceInfoAdapter
    private lateinit var historyAdapter: HistoryAdapter

    private var page: Int = 1
    private var limit: Int = 10

    override fun bind() = BottomSheetBalanceDetailBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = getViewModel(requireActivity())
        viewModel.listener = this

        viewModel.transactionResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {

                dismissProgress()

                viewModel.selectedAsset?.let {
                    binding.tvSubTitle.text = "${it.fullName} (${it.id.uppercase()})"
                    /*binding.tvAssetValueCrypto.text =
                        "${it.total_balance} ${it.id.uppercase()}"
                    binding.tvValueAmount.text =
                        "${(it.total_balance * it.euro_amount).commaFormatted}${Constants.EURO}" TODO*/
                }

                binding.llContent.visible()
                zoomIn(binding.llContent)

                if (it.transactions.isEmpty()) {
                    binding.rvBalanceInfo.gone()
                    binding.tvHistory.gone()
                } else {
                    binding.rvBalanceInfo.startLayoutAnimation()
                    historyAdapter.addList(it.transactions)
                    binding.rvHistory.startLayoutAnimation()
                }

            }
        }

        infoAdapter = BalanceInfoAdapter(clickListener)
        historyAdapter = HistoryAdapter()

        binding.apply {

            rvBalanceInfo.let {
                it.adapter = infoAdapter
                it.layoutManager = GridLayoutManager(requireContext(), 2)
                it.addItemDecoration(ItemOffsetDecoration(8))
                infoAdapter.addList(getBalanceInfo())
            }

            rvHistory.let {
                it.adapter = historyAdapter
                it.layoutManager = LinearLayoutManager(requireContext())
            }
        }

        binding.ivTopAction.setOnClickListener { dismiss() }
        getTransactions()
    }

    private fun getTransactions() {
        viewModel.selectedAsset?.let {
            checkInternet(requireContext()) {
                showProgress()
                viewModel.getTransactions(page, limit, it.id)
            }
        }
    }


    /* managing progress bar */

    private fun showProgress() {
        binding.ivProgress.visible()
        binding.ivProgress.animation =
            AnimationUtils.loadAnimation(requireContext(), R.anim.rotate_drawable)
    }

    private fun dismissProgress() {
        binding.ivProgress.gone()
        binding.ivProgress.clearAnimation()
    }

    // Balance info Adapter

    private fun getBalanceInfo(): List<BalanceInfo> {
        val list = mutableListOf<BalanceInfo>()
        list.add(BalanceInfo("Total earnings", "13,2229€", "+0.00034 BTC"))
        list.add(BalanceInfo("ROI", "~5,01%*", "*Annual Percentage"))
        return list
    }

    inner class BalanceInfoAdapter(private val clickListener: (BalanceInfo) -> Unit = { _ -> }) :
        RecyclerView.Adapter<BalanceInfoAdapter.BalanceInfoViewHolder>() {

        private var balanceList = mutableListOf<BalanceInfo>()

        fun addList(list: List<BalanceInfo>) {
            val start = balanceList.count()
            balanceList.addAll(list)
            notifyItemRangeInserted(start, list.count())
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BalanceInfoViewHolder {
            return BalanceInfoViewHolder(
                ItemBalanceDetailPortfolioBinding.inflate(
                    LayoutInflater.from(
                        parent.context
                    ), parent, false
                )
            )
        }

        override fun onBindViewHolder(holder: BalanceInfoViewHolder, position: Int) {
            holder.binding.apply {
                balanceList[position].let {
                    tvTitle.text = it.title
                    tvSubTitle.text = it.subTitle
                    tvBottomText.text = it.bottomText
                }
            }
        }

        override fun getItemCount() = balanceList.count()

        inner class BalanceInfoViewHolder(val binding: ItemBalanceDetailPortfolioBinding) :
            RecyclerView.ViewHolder(binding.root) {
            init {
                binding.root.setOnClickListener {
                    clickListener(balanceList[adapterPosition])
                    dismiss()
                }
            }
        }
    }

    data class BalanceInfo(val title: String, val subTitle: String, val bottomText: String)


    /* history adapter */

    class HistoryAdapter :
        BaseAdapter<Transaction>() {

        override fun getItemViewType(position: Int) =
            if (itemList[position] == null) LOADER_VIEW else ORDINARY_VIEW

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): RecyclerView.ViewHolder {
            return when (viewType) {
                LOADER_VIEW -> LoaderViewHolder(
                    LoaderViewBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
                else -> BalanceHistoryViewHolder(
                    ItemBalancePortfolioHistoryBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false
                    )
                )
            }

        }

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (itemList[position] == null) {

            } else {
                itemList[position]?.let {
                    (holder as BalanceHistoryViewHolder).binding.apply {

                        when (it.type) {
                            1 -> { //exchange

                                tvTitle.text =
                                    "Exch. ${it.exchange_from.uppercase()} to ${it.exchange_to.uppercase()}"
                                tvPriceTitle.text =
                                    "-${
                                        it.exchange_from_amount.toString().decimalPoints(6)
                                    }${it.exchange_from.uppercase()}"
                                tvPriceSubTitle.text =
                                    "${
                                        it.exchange_to_amount.toString().decimalPoints(5)
                                    }${it.exchange_to.uppercase()}"
                            }
                            2 -> { // deposit
                                root.gone()
                            }
                            3 -> { // withdraw
//                                ivItem.setImageResource(R.drawable.ic_withdraw)
                                tvTitle.text = "Withdrawal"
                                tvPriceTitle.text = "-${it.amount}${Constants.EURO}"
                                tvPriceSubTitle.text =
                                    "${
                                        it.asset_amount.toString().decimalPoints(5)
                                    }${it.asset_id.uppercase()}"
                            }
                            4 -> { // single asset
//                                ivItem.setImageResource(R.drawable.ic_deposit)
                                tvTitle.text = "Bought ${it.asset_id.uppercase()}"
                                tvPriceTitle.text =
                                    "+${it.amount.toFloat().toInt()}${Constants.EURO}"
                                tvPriceSubTitle.text =
                                    "${
                                        it.asset_amount.toString().decimalPoints(5)
                                    }${it.asset_id.uppercase()}"
                            }
                            else -> root.gone()
                        }

                    }
                }
            }
        }

        inner class BalanceHistoryViewHolder(val binding: ItemBalancePortfolioHistoryBinding) :
            RecyclerView.ViewHolder(binding.root) {
            init {
            }
        }
    }

    data class BalanceHistory(
        val title: String,
        val titlePrice: String,
        val subTitle: String,
        val subTitlePrice: String
    )

    override fun onRetrofitError(responseBody: ResponseBody?) {
        super.onRetrofitError(responseBody)
        dismissProgress()
    }
}