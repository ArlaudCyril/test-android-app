package com.Lyber.dev.ui.fragments

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.getColor
import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.Lyber.dev.R
import com.Lyber.dev.databinding.FragmentTransactionBinding
import com.Lyber.dev.databinding.ItemTransactionBinding
import com.Lyber.dev.databinding.ItemTransactionNewBinding
import com.Lyber.dev.databinding.LoaderViewBinding
import com.Lyber.dev.models.TransactionData
import com.Lyber.dev.ui.activities.SplashActivity
import com.Lyber.dev.ui.adapters.BaseAdapter
import com.Lyber.dev.ui.fragments.bottomsheetfragments.TransactionDetailsBottomSheetFragment
import com.Lyber.dev.utils.AppLifeCycleObserver
import com.Lyber.dev.utils.CommonMethods
import com.Lyber.dev.utils.CommonMethods.Companion.formattedAsset
import com.Lyber.dev.utils.CommonMethods.Companion.gone
import com.Lyber.dev.utils.CommonMethods.Companion.px
import com.Lyber.dev.utils.CommonMethods.Companion.toDateFormat
import com.Lyber.dev.utils.CommonMethods.Companion.toDateFormatTwo
import com.Lyber.dev.utils.CommonMethods.Companion.visible
import com.Lyber.dev.utils.Constants
import com.Lyber.dev.utils.PaginationListener
import com.Lyber.dev.viewmodels.PortfolioViewModel
import com.google.android.gms.tasks.Task
import com.google.android.play.core.integrity.StandardIntegrityManager
import com.google.gson.GsonBuilder
import org.json.JSONObject
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class TransactionFragment : BaseFragment<FragmentTransactionBinding>() {

    private lateinit var adapter: TransactionAdapter
    private val viewModel: PortfolioViewModel by viewModels()
    val limit = 50
    var offset = 0

    private var isLoading = true
    private var isLastPage = false

    override fun bind() = FragmentTransactionBinding.inflate(layoutInflater)

    private fun hitApi() {
        CommonMethods.checkInternet(binding.root,requireContext()) {
            CommonMethods.showProgressDialog(requireContext())
                viewModel.getTransactionsListing(
                    limit,
                    offset
                )
        }
    }

    override fun onResume() {
        super.onResume()
        if (AppLifeCycleObserver.fromBack) {
            AppLifeCycleObserver.fromBack = false
            hitApi()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        viewModel = getViewModel(requireActivity())
        viewModel.listener = this
        hitApi()

        val mLayoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        binding.rvTransactions.let {
            adapter = TransactionAdapter()
            it.layoutManager = mLayoutManager
            it.adapter = adapter
            binding.nsv.setOnScrollChangeListener(object :
                PaginationListener.NestedScrollPaginationListener() {
                override fun loadMoreItems() {
                    binding.rvTransactions.post {
                        offset = offset + limit + 1
                        isLoading = true
                        adapter.addProgress()
                        CommonMethods.checkInternet(binding.root,requireContext()) {
                          viewModel.getTransactionsListing(
                                    limit,
                                    offset
                                )
                        }

                    }
                }

                override fun isLastPage() = isLastPage
                override fun isLoading() = isLoading

            })

        }


        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }
        binding.rvRefresh.setOnRefreshListener {
            binding.rvRefresh.isRefreshing = true
            offset = 0
            positionList.clear()
            CommonMethods.checkInternet(binding.root,requireContext()) {
                viewModel.getTransactionsListing(
                        limit,
                        offset
                    )
            }

        }
        viewModel.getTransactionListingResponse.observe(viewLifecycleOwner) { response ->
            response?.let {
                // Process the response here
                //for now
//                val transactionList = mutableListOf<TransactionData>()
//                for (i in it.data) {
//                    if (i.type != Constants.WITHDRAW_EURO) {
//                        transactionList.add(i)
//                    }
//                }
                binding.rvRefresh.isRefreshing = false
                CommonMethods.dismissProgressDialog()
//                adapter.calculatePositions(transactionList)
                adapter.calculatePositions(it.data)
                if (offset == 0) binding.rvTransactions.startLayoutAnimation()
                isLoading = false
                isLastPage = it.data.count() < limit

                if (offset == 0) {
                    Log.d(TAG, "notificationResponse: Success  page == 1")
                    if (it.data.isNotEmpty()) {
                        Log.d(TAG, "notificationResponse: page == 1 setList")

//                        for (i in it.data) {
//                            if (i.type != Constants.WITHDRAW_EURO) {
//                                transactionList.add(i)
//                            }
//                        }
                        adapter.setList(it.data)
//                        adapter.setList(transactionList)

                    }
                } else {
                    adapter.removeProgress()
                    adapter.addList(it.data)
//                    adapter.addList(transactionList)
                }
            }
        }

    }

    private val positionList = mutableListOf<String>()

    inner class TransactionAdapter : BaseAdapter<TransactionData>() {
        private var date = ""

        fun String.toMillis(): Long {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            dateFormat.timeZone = TimeZone.getTimeZone("UTC")

            return try {
                dateFormat.parse(this)?.time ?: 0
            } catch (e: Exception) {
                e.printStackTrace()
                0
            }
        }


        fun calculatePositions(list: List<TransactionData>) {
            if (positionList.isEmpty()) {
                date = list[0].date.toMillis().toLong().toDateFormatTwo()
                positionList.add(list[0].id)
            }
            for (i in 0 until list.count()) {
                val currentValue = list[i].date.toMillis().toLong().toDateFormatTwo()
                if (date.split("/")[0] != currentValue.split("/")[0]) {
                    positionList.add(list[i].id)
                    date = currentValue
                }
            }
        }

        override fun getItemViewType(position: Int) =
            if (itemList[position] == null) LOADER_VIEW else ORDINARY_VIEW

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return when (viewType) {
                ORDINARY_VIEW -> TransactionViewHolder(
                    ItemTransactionNewBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false
                    )
                )

                else -> LoaderViewHolder(
                    LoaderViewBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false
                    )
                )
            }
        }


        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (itemList[position] == null) {

            } else itemList[position]?.let {
                (holder as TransactionViewHolder).binding.apply {

                    if (itemList[position]!!.id in positionList) {
                        tvDate.visible()
                        tvDate.text = it.date.toMillis().toLong().toDateFormat()
                    } else tvDate.gone()
                    when (it.type) {
                        Constants.ORDER -> { //exchange
                            ivItem.setImageResource(R.drawable.ic_exchange)
                            tvStartTitle.text = getString(R.string.exchange)
                            tvStartSubTitle.text =
                                "${it.fromAsset.uppercase()} -> ${it.toAsset.uppercase()}"

                            var roundedNumber = BigDecimal(it.fromAmount)
                            try {
                                val originalNumber = BigDecimal(it.fromAmount)
                                val scale = originalNumber.scale()
                                roundedNumber = if (scale > 8) {
                                    originalNumber.setScale(8, RoundingMode.HALF_UP)
                                } else originalNumber
                            } catch (_: Exception) {

                            }
//                                tvEndTitle.text = "-${roundedNumber} ${it.fromAsset.uppercase()}"
                            tvEndTitle.text = "-${it.fromAmount} ${it.fromAsset.uppercase()}"
//                                var amount = it.toAmount
//                                try {
//                                    amount = String.format(Locale.US, "%.8f", it.toAmount.toFloat())
//                                    try {
//                                        amount = CommonMethods.trimTrailingZeros(amount.toDouble())
//                                    } catch (_: Exception) {
//
//                                    }
//
//                                } catch (ex: Exception) {
//
//                                }
                            tvEndSubTitle.text = "+${
                                it.toAmount.formattedAsset(
                                    0.0, rounding = RoundingMode.DOWN, 8
                                )
                            } ${it.toAsset.uppercase()}"
                            tvFailed.visibility = View.GONE
                            tvStartTitleCenter.visibility = View.GONE
                            tvEndTitleCenter.visibility = View.GONE
                        }

                        Constants.STRATEGY -> {
                            ivItem.setImageResource(R.drawable.strategy)
                            tvStartTitleCenter.text =
                                "${it.strategyName.replaceFirstChar(Char::uppercase)}"
                            if (it.status == Constants.FAILURE) tvFailed.visibility = View.VISIBLE
                            else {
                                if (it.successfulBundleEntries.isNotEmpty()) {
                                    try {
                                        tvEndTitleCenter.text =
                                            "${it.successfulBundleEntries[0].assetAmount} ${
                                                it.successfulBundleEntries[0].asset.uppercase(
                                                    Locale.US
                                                )
                                            }"

                                    } catch (ex: Exception) {

                                    }
                                }
                            }
                        }

                        Constants.DEPOSIT -> {
                            ivItem.setImageResource(R.drawable.ic_deposit)
                            tvStartTitle.text =
                                "${it.asset.uppercase()} ${getString(R.string.deposit_)}"
                            tvStartSubTitle.text =
                                it.status.lowercase().replaceFirstChar(Char::uppercase)
                            tvEndTitleCenter.text = "+${it.amount} ${it.asset.uppercase()}"
                            tvFailed.visibility = View.GONE
                            tvStartTitleCenter.visibility = View.GONE
                        }

                        Constants.WITHDRAW -> { // single asset
                            ivItem.setImageResource(R.drawable.ic_withdraw)
                            tvFailed.visibility = View.GONE
                            tvStartTitle.text =
                                "${it.asset.uppercase()} ${getString(R.string.withdrawal)}"
                            tvStartSubTitle.text =
                                it.status.lowercase().replaceFirstChar(Char::uppercase)
                            tvEndTitleCenter.text = "-${it.amount} ${it.asset.uppercase()}"
                            tvStartTitleCenter.visibility = View.GONE
                        }

                        Constants.WITHDRAW_EURO -> { // single asset

                                ivItem.setImageResource(R.drawable.ic_withdraw)
                                tvFailed.visibility = View.GONE
                                tvStartTitle.text =
                                    "EUR ${getString(R.string.withdrawal)}"
                                tvStartSubTitle.text =
                                    it.status.lowercase().replaceFirstChar(Char::uppercase)
                                tvEndTitleCenter.text = "-${it.amount} ${it.asset.uppercase()}"
                                tvStartTitleCenter.visibility = View.GONE

 }

                        else -> root.gone()
                    }
                }
            }
        }


        inner class TransactionViewHolder(val binding: ItemTransactionNewBinding) :
            RecyclerView.ViewHolder(binding.root) {
            init {
                binding.root.setOnClickListener {

                    val detailBottomSheet = TransactionDetailsBottomSheetFragment()
                    val gson = GsonBuilder().create()
                    var data = ""
                    data = gson.toJson(itemList[absoluteAdapterPosition])
                    detailBottomSheet.arguments = Bundle().apply {
                        putString("data", data)
                    }
                    detailBottomSheet.show(childFragmentManager, "")
                }
                binding.root.setBackgroundColor(
                    getColor(
                        binding.root.context, R.color.purple_gray_00
                    )
                )
                binding.root.updatePadding(left = 0.px, right = 0.px)
            }
        }
    }

}

