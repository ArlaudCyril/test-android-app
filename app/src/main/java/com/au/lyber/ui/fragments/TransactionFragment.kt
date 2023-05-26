package com.au.lyber.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.getColor
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.au.lyber.R
import com.au.lyber.databinding.FragmentTransactionBinding
import com.au.lyber.databinding.ItemTransactionBinding
import com.au.lyber.databinding.LoaderViewBinding
import com.au.lyber.models.Transaction
import com.au.lyber.ui.adapters.BaseAdapter
import com.au.lyber.utils.CommonMethods.Companion.decimalPoints
import com.au.lyber.utils.CommonMethods.Companion.getViewModel
import com.au.lyber.utils.CommonMethods.Companion.gone
import com.au.lyber.utils.CommonMethods.Companion.px
import com.au.lyber.utils.CommonMethods.Companion.toDateFormat
import com.au.lyber.utils.CommonMethods.Companion.toDateFormatTwo
import com.au.lyber.utils.CommonMethods.Companion.visible
import com.au.lyber.utils.Constants
import com.au.lyber.ui.portfolio.viewModel.PortfolioViewModel

class TransactionFragment : BaseFragment<FragmentTransactionBinding>() {

    private lateinit var adapter: TransactionAdapter
    private lateinit var viewModel: PortfolioViewModel

    override fun bind() = FragmentTransactionBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = getViewModel(requireActivity())

        binding.rvTransactions.let {
            adapter = TransactionAdapter()
            it.adapter = adapter
            it.layoutManager = LinearLayoutManager(requireContext())
            it.isNestedScrollingEnabled = false
        }

        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }

        viewModel.transactionResponse.value?.let {
            adapter.calculatePositions(it.transactions)
            adapter.addList(it.transactions)
            binding.rvTransactions.startLayoutAnimation()
        }

    }

    class TransactionAdapter : BaseAdapter<Transaction>() {

        private val positionList = mutableListOf<Int>()

        fun calculatePositions(list: List<Transaction>) {
            var date = list[0].created_at.toLong().toDateFormatTwo()
            positionList.add(0)
            for (i in 0 until list.count()) {
                val currentValue = list[i].created_at.toLong().toDateFormatTwo()
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
                            tvDate.text = it.created_at.toLong().toDateFormat()
                        }

                        when (it.type) {
                            1 -> { //exchange
                                ivItem.setImageResource(R.drawable.ic_exchange)
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
                                ivItem.setImageResource(R.drawable.ic_withdraw)
                                tvStartTitleCenter.text = "Withdrawal"
                                tvEndTitle.text = "-${it.amount}${Constants.EURO}"
                                tvEndSubTitle.text =
                                    "${
                                        it.asset_amount.toString().decimalPoints(5)
                                    }${it.asset_id.uppercase()}"
                            }
                            4 -> { // single asset
                                ivItem.setImageResource(R.drawable.ic_deposit)
                                tvStartTitleCenter.text = "Bought ${it.asset_id.uppercase()}"
                                tvEndTitle.text = "+${it.amount.toFloat().toInt()}${Constants.EURO}"
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


        inner class TransactionViewHolder(val binding: ItemTransactionBinding) :
            RecyclerView.ViewHolder(binding.root) {
            init {
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

