package com.Lyber.dev.ui.portfolio.bottomSheetFragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.Lyber.dev.R
import com.Lyber.dev.databinding.BottomSheetBalanceDetailBinding
import com.Lyber.dev.databinding.ItemBalanceDetailPortfolioBinding
import com.Lyber.dev.databinding.ItemBalancePortfolioHistoryBinding
import com.Lyber.dev.databinding.LoaderViewBinding
import com.Lyber.dev.models.Transaction
import com.Lyber.dev.ui.adapters.BaseAdapter
import com.Lyber.dev.ui.fragments.bottomsheetfragments.BaseBottomSheet
import com.Lyber.dev.utils.App
import com.Lyber.dev.utils.CommonMethods
import com.Lyber.dev.viewmodels.PortfolioViewModel
import com.Lyber.dev.utils.CommonMethods.Companion.checkInternet
import com.Lyber.dev.utils.CommonMethods.Companion.decimalPoints
import com.Lyber.dev.utils.CommonMethods.Companion.dismissAlertDialog
import com.Lyber.dev.utils.CommonMethods.Companion.getViewModel
import com.Lyber.dev.utils.CommonMethods.Companion.gone
import com.Lyber.dev.utils.CommonMethods.Companion.visible
import com.Lyber.dev.utils.CommonMethods.Companion.zoomIn
import com.Lyber.dev.utils.Constants
import com.Lyber.dev.utils.ItemOffsetDecoration
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
//        viewModel.logoutResponse.observe(viewLifecycleOwner){
//            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
//                App.prefsManager.logout()
//                findNavController().popBackStack()
//                findNavController().navigate(R.id.discoveryFragment)
//            }
//        }
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
            checkInternet(binding.root,requireContext()) {
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
        list.add(BalanceInfo(getString(R.string.total_earnings), "13,2229€", "+0.00034 BTC"))
        list.add(BalanceInfo("ROI", "~5,01%*", getString(R.string.annual_percentage)))
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

  inner  class HistoryAdapter :
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
                                    "${requireContext().getString(R.string.exchange)}. ${it.exchange_from.uppercase()} ${requireContext().getString(R.string.to_)} ${it.exchange_to.uppercase()}"
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
                                tvTitle.text = getString(R.string.withdrawal)
                                tvPriceTitle.text = "-${it.amount}${Constants.EURO}"
                                tvPriceSubTitle.text =
                                    "${
                                        it.asset_amount.toString().decimalPoints(5)
                                    }${it.asset_id.uppercase()}"
                            }
                            4 -> { // single asset
//                                ivItem.setImageResource(R.drawable.ic_deposit)
                                tvTitle.text = "${getString(R.string.bought)} ${it.asset_id.uppercase()}"
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

    override fun onRetrofitError(responseBody: ResponseBody?) {
        super.onRetrofitError(responseBody)
        dismissProgress()
        dismissAlertDialog()
    }
}