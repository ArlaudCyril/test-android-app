package com.Lyber.ui.fragments

import android.annotation.SuppressLint
import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.Lyber.R
import com.Lyber.databinding.FragmentActivityLogsBinding
import com.Lyber.databinding.ItemActivityLogsBinding
import com.Lyber.databinding.ItemTransactionBinding
import com.Lyber.databinding.LoaderViewBinding
import com.Lyber.models.ActivityLogsData
import com.Lyber.models.TransactionData
import com.Lyber.ui.adapters.BaseAdapter
import com.Lyber.ui.fragments.bottomsheetfragments.TransactionDetailsBottomSheetFragment
import com.Lyber.ui.portfolio.viewModel.PortfolioViewModel
import com.Lyber.utils.CommonMethods
import com.Lyber.utils.CommonMethods.Companion.gone
import com.Lyber.utils.CommonMethods.Companion.px
import com.Lyber.utils.CommonMethods.Companion.toDateFormat
import com.Lyber.utils.CommonMethods.Companion.toDateFormatTwo
import com.Lyber.utils.CommonMethods.Companion.toFormat
import com.Lyber.utils.CommonMethods.Companion.visible
import com.Lyber.utils.PaginationListener
import com.google.gson.GsonBuilder
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone


class ActivityLogsFragment : BaseFragment<FragmentActivityLogsBinding>() {
    private lateinit var adapter: ActivityLogAdapter
    private val viewModel: PortfolioViewModel by viewModels()
    val limit = 50
    var offset = 0

    private var isLoading = true
    private var isLastPage = false
    override fun bind() = FragmentActivityLogsBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.listener = this
        CommonMethods.checkInternet(requireContext()) {
            CommonMethods.showProgressDialog(requireContext())
            viewModel.getActivityLogs(limit, offset)
        }
        val mLayoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        binding.rvActivityLogs.let {
            adapter = ActivityLogAdapter()
            it.layoutManager = mLayoutManager
            it.adapter = adapter
            binding.nsv.setOnScrollChangeListener(object :
                PaginationListener.NestedScrollPaginationListener() {
                override fun loadMoreItems() {
                    binding.rvActivityLogs.post {
                        offset = offset + limit + 1
                        isLoading = true
                        adapter.addProgress()
                        CommonMethods.checkInternet(requireContext()) {
                            viewModel.getActivityLogs(limit, offset)
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
            viewModel.getActivityLogs(limit, offset)
        }
        viewModel.getActivityLogsListingResponse.observe(viewLifecycleOwner) { response ->
            response?.let {
                // Process the response here
                binding.rvRefresh.isRefreshing = false
                CommonMethods.dismissProgressDialog()
                adapter.calculatePositions(it.data)
                if (offset == 0)
                    binding.rvActivityLogs.startLayoutAnimation()
                isLoading = false
                isLastPage = it.data.count() < limit

                if (offset == 0) {
                    Log.d(ContentValues.TAG, "notificationResponse: Success  page == 1")
                    if (it.data.isNotEmpty()) {
                        Log.d(ContentValues.TAG, "notificationResponse: page == 1 setList")
                        adapter.setList(it.data)

                    }
                } else {
                    adapter.removeProgress()
                    adapter.addList(it.data)
                }
            }
        }
    }

    private val positionList = mutableListOf<String>()

    inner class ActivityLogAdapter : BaseAdapter<ActivityLogsData>() {
        private var date = ""

//        fun String.toMillis(): Long {
//            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
//            dateFormat.timeZone = TimeZone.getTimeZone("UTC")
//
//            return try {
//                dateFormat.parse(this)?.time ?: 0
//            } catch (e: Exception) {
//                e.printStackTrace()
//                0
//            }
//        }
//
//
//        fun calculatePositions(list: List<ActivityLogsData>) {
//            if (positionList.isEmpty()) {
//                date = list[0].date.toMillis().toLong().toDateFormatTwo()
//                positionList.add(list[0].date)
//            }
//            for (i in 0 until list.count()) {
//                val currentValue = list[i].date.toMillis().toLong().toDateFormatTwo()
//                if (date.split("/")[0] != currentValue.split("/")[0]) {
//                    positionList.add(list[i].date)
//                    date = currentValue
//                }
//            }
//        }

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

        fun calculatePositions(list: List<ActivityLogsData>) {
            if (positionList.isEmpty()) {
                date = list[0].date.toMillis().toLong().toDateFormatTwo()
                positionList.add(list[0].date)
            }
            for (i in 0 until list.count()) {
                val currentValue = list[i].date.toMillis().toLong().toDateFormatTwo()
                if (date.split("/")[0] != currentValue.split("/")[0]) {
                    positionList.add(list[i].date)
                    date = currentValue
                }
            }
        }

        fun Long.toDateFormatWithTodayYesterday(): String {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = this

            val todayCalendar = Calendar.getInstance()
            todayCalendar.set(Calendar.HOUR_OF_DAY, 0)
            todayCalendar.set(Calendar.MINUTE, 0)
            todayCalendar.set(Calendar.SECOND, 0)
            todayCalendar.set(Calendar.MILLISECOND, 0)

            val yesterdayCalendar = Calendar.getInstance()
            yesterdayCalendar.add(Calendar.DAY_OF_YEAR, -1)
            yesterdayCalendar.set(Calendar.HOUR_OF_DAY, 0)
            yesterdayCalendar.set(Calendar.MINUTE, 0)
            yesterdayCalendar.set(Calendar.SECOND, 0)
            yesterdayCalendar.set(Calendar.MILLISECOND, 0)

            return when {
                calendar.after(todayCalendar) -> "Today"
                calendar.after(yesterdayCalendar) -> "Yesterday"
                else -> SimpleDateFormat("dd/MM/yyyy", Locale.US).format(this)
            }
        }



        override fun getItemViewType(position: Int) =
            if (itemList[position] == null) LOADER_VIEW else ORDINARY_VIEW

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return when (viewType) {
                ORDINARY_VIEW -> ActivityViewHolder(
                    ItemActivityLogsBinding.inflate(
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
                    (holder as ActivityViewHolder).binding.apply {

                        if (itemList[position]!!.date in positionList) {
                            tvDate.visible()
                            tvDate.text = it.date.toMillis().toLong().toDateFormatWithTodayYesterday()
                        } else
                            tvDate.gone()
                        tvTime.text=
                        it.date.toFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", "HH:mm")

                        tvLog.text = it.log
                    }
                }
        }

        inner class ActivityViewHolder(val binding: ItemActivityLogsBinding) :
            RecyclerView.ViewHolder(binding.root) {

        }
    }

}