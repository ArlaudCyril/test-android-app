package com.Lyber.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.getColor
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.Lyber.R
import com.Lyber.databinding.FragmentNotificationBinding
import com.Lyber.databinding.ItemNotificationBinding
import com.Lyber.utils.CommonMethods.Companion.gone
import com.Lyber.utils.CommonMethods.Companion.visible
//not in use
class NotificationFragment : BaseFragment<FragmentNotificationBinding>() {

    private lateinit var adapter: NotificationAdapter
    override fun bind() = FragmentNotificationBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = NotificationAdapter()
        binding.rvNotification.layoutManager = LinearLayoutManager(requireContext())
        binding.rvNotification.adapter = adapter
        adapter.addList(getList())

        binding.ivTopAction.setOnClickListener { requireActivity().onBackPressed() }
    }

    private fun getList(): List<String> {
        val list = mutableListOf<String>()
        for (i in 0..10)
            list.add("Lorem Ipsum is simply dummy text of the printing and typesetting industry.30s")
        return list
    }

    inner class NotificationAdapter :
        RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {


        private val list = mutableListOf<String>()

        fun addList(items: List<String>) {
            val startCount = list.count()
            list.addAll(items)
            notifyItemRangeInserted(startCount, items.count())
        }

        inner class NotificationViewHolder(val binding: ItemNotificationBinding) :
            RecyclerView.ViewHolder(binding.root) {
            init {
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
            return NotificationViewHolder(
                ItemNotificationBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }

        override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
            holder.binding.apply {
                list[position].let {
                    when (position) {
                        0 -> {
                            root.setBackgroundColor(
                                getColor(
                                    requireContext(),
                                    R.color.purple_gray_50
                                )
                            )
                            tvNotification.text = getString(R.string.today)
                            viewDivider.gone()
                        }
                        5 -> {
                            root.setBackgroundColor(
                                getColor(
                                    requireContext(),
                                    R.color.purple_gray_50
                                )
                            )
                            tvNotification.text = getString(R.string.yesterday)
                            viewDivider.gone()
                        }
                        else -> {
                            tvNotification.setTextColor(
                                getColor(
                                    requireContext(),
                                    R.color.purple_gray_600
                                )
                            )
                            tvNotification.text = it
                            viewDivider.visible()
                        }
                    }
                }
            }
        }

        override fun getItemCount() = list.count()

    }
}