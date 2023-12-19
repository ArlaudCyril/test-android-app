package com.au.lyber.ui.fragments

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
import com.au.lyber.R
import com.au.lyber.databinding.FragmentTransactionBinding
import com.au.lyber.databinding.ItemTransactionBinding
import com.au.lyber.databinding.LoaderViewBinding
import com.au.lyber.models.TransactionData
import com.au.lyber.ui.adapters.BaseAdapter
import com.au.lyber.ui.fragments.bottomsheetfragments.TransactionDetailsBottomSheetFragment
import com.au.lyber.ui.portfolio.viewModel.PortfolioViewModel
import com.au.lyber.utils.CommonMethods
import com.au.lyber.utils.CommonMethods.Companion.gone
import com.au.lyber.utils.CommonMethods.Companion.px
import com.au.lyber.utils.CommonMethods.Companion.toDateFormat
import com.au.lyber.utils.CommonMethods.Companion.toDateFormatTwo
import com.au.lyber.utils.CommonMethods.Companion.visible
import com.au.lyber.utils.Constants
import com.au.lyber.utils.PaginationListener
import com.google.gson.GsonBuilder
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class TransactionFragment : BaseFragment<FragmentTransactionBinding>() {

    private lateinit var adapter: TransactionAdapter
    private val viewModel: PortfolioViewModel by viewModels()
    val limit = 10
    var offset = 0

    private var isLoading = true
    private var isLastPage = false

    override fun bind() = FragmentTransactionBinding.inflate(layoutInflater)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        viewModel = getViewModel(requireActivity())
        viewModel.listener = this
        CommonMethods.checkInternet(requireContext()) {
            CommonMethods.showProgressDialog(requireContext())
            viewModel.getTransactions(limit, offset)
        }

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
                        offset++
                        isLoading = true
                        adapter.addProgress()
                        CommonMethods.checkInternet(requireContext()) {
                            viewModel.getTransactions(limit, offset)
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
        viewModel.getTransactionListingResponse.observe(viewLifecycleOwner) { response ->
            response?.let {
                // Process the response here
                CommonMethods.dismissProgressDialog()
                adapter.calculatePositions(it.data)
                if (offset == 0)
                    binding.rvTransactions.startLayoutAnimation()
                isLoading = false
                isLastPage = it.data.count() < limit

                if (offset == 0) {

                    Log.d(TAG, "notificationResponse: Success  page == 1")
                    if (it.data.isNotEmpty()) {
                        Log.d(TAG, "notificationResponse: page == 1 setList")
                        adapter.setList(it.data)

                    }
                } else {
                    adapter.removeProgress()
                    adapter.addList(it.data)
                }
            }
        }

    }

    inner class TransactionAdapter : BaseAdapter<TransactionData>() {

        private val positionList = mutableListOf<Int>()
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
            var date = list[0].date.toMillis().toLong().toDateFormatTwo()
            positionList.add(0)
            for (i in 0 until list.count()) {
                val currentValue = list[i].date.toMillis().toLong().toDateFormatTwo()
                if (date.split("/")[0] != currentValue.split("/")[0]) {
                    positionList.add(i)
                    date = currentValue
                }
            }
        }

        override fun getItemViewType(position: Int) =
            if (itemList[position] == null) LOADER_VIEW else ORDINARY_VIEW

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return when (viewType) {
                ORDINARY_VIEW -> TransactionViewHolder(
                    ItemTransactionBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )

                else -> LoaderViewHolder(
                    LoaderViewBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
        }


        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (itemList[position] == null) {

            } else
                itemList[position]?.let {
                    (holder as TransactionViewHolder).binding.apply {

                        if (position in positionList) {
                            tvDate.visible()
                            tvDate.text = it.date.toMillis().toLong().toDateFormat()
                        }
                        when (it.type) {
                            Constants.ORDER -> { //exchange
                                ivItem.setImageResource(R.drawable.ic_exchange)
                                tvStartTitle.text = getString(R.string.exchange)
                                tvStartSubTitle.text =
                                    "${it.fromAsset.uppercase()} to ${it.toAsset.uppercase()}"

                                var roundedNumber=BigDecimal(it.fromAmount)
                                try {
                                    val originalNumber = BigDecimal(it.fromAmount)
                                    val scale = originalNumber.scale()
                                     roundedNumber = if (scale > 8) {
                                        originalNumber.setScale(8, RoundingMode.HALF_UP)
                                    } else
                                        originalNumber
                                }catch (_:Exception){

                                }
                                 tvEndTitle.text = "-${roundedNumber}${it.fromAsset.uppercase()}"
//                                tvEndTitle.text = "-${it.fromAmount} ${it.fromAsset.uppercase()}"
                                var amount = it.toAmount
                                try {
                                    amount = String.format("%.10f", it.toAmount.toFloat())
                                } catch (ex: Exception) {

                                }
                                tvEndSubTitle.text = "+${amount} ${it.toAsset.uppercase()}"
                                tvFailed.visibility = View.GONE
                                tvStartTitleCenter.visibility = View.GONE
                                tvEndTitleCenter.visibility = View.GONE
                            }

                            Constants.STRATEGY -> {
                                ivItem.setImageResource(R.drawable.strategy)
                                tvStartTitleCenter.text =
                                    "${it.strategyName.replaceFirstChar(Char::uppercase)}"
                                if (it.status == Constants.FAILURE)
                                    tvFailed.visibility = View.VISIBLE
                                else {
                                    if (it.successfulBundleEntries.isNotEmpty()) {
                                        try {
                                            tvEndTitleCenter.text =
                                                "${it.successfulBundleEntries[0].assetAmount} ${it.successfulBundleEntries[0].asset.uppercase(Locale.US)}"

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

                            else -> root.gone()
                        }
                    }
                }
        }


        inner class TransactionViewHolder(val binding: ItemTransactionBinding) :
            RecyclerView.ViewHolder(binding.root) {
            init {
                binding.root.setOnClickListener {

                    val detailBottomSheet = TransactionDetailsBottomSheetFragment()
                    val gson = GsonBuilder().create()
                    var data = ""
                    data = gson.toJson(itemList[adapterPosition])
                    detailBottomSheet.arguments = Bundle().apply {
                        putString("data", data)
                    }
                    detailBottomSheet.show(childFragmentManager, "")
                }
                binding.root.setBackgroundColor(
                    getColor(
                        binding.root.context,
                        R.color.purple_gray_00
                    )
                )
                binding.root.updatePadding(left = 0.px, right = 0.px)
            }
        }
    }

}

