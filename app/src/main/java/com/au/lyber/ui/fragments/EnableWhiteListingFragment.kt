package com.au.lyber.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getColor
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.au.lyber.R
import com.au.lyber.databinding.FragmentManageWhitelistingBinding
import com.au.lyber.databinding.ItemExtraSecurityBinding
import com.au.lyber.ui.adapters.BaseAdapter
import com.au.lyber.utils.App
import com.au.lyber.utils.CommonMethods.Companion.checkInternet
import com.au.lyber.utils.CommonMethods.Companion.dismissProgressDialog
import com.au.lyber.utils.CommonMethods.Companion.getViewModel
import com.au.lyber.utils.CommonMethods.Companion.setBackgroundTint
import com.au.lyber.utils.CommonMethods.Companion.showProgressDialog
import com.au.lyber.utils.Constants
import com.au.lyber.viewmodels.NetworkViewModel

class EnableWhiteListingFragment : BaseFragment<FragmentManageWhitelistingBinding>() {

    private lateinit var viewModel: NetworkViewModel
    private lateinit var adapter: SecurityAdapter
    private var selectedPosition: Int = 0

    override fun bind() = FragmentManageWhitelistingBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = getViewModel(this)
        viewModel.listener = this

        viewModel.updateUserInfoResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                dismissProgressDialog()

                val security: String = when (selectedPosition) {
                    0 -> Constants.HOURS_72
                    1 -> Constants.HOURS_24
                    else -> Constants.NO_EXTRA_SECURITY
                }
               App.prefsManager.withdrawalLockSecurity = security
                requireActivity().onBackPressed()
            }
        }

        /* if whitelisting is enabled */

        selectedPosition = when (App.prefsManager.withdrawalLockSecurity) {
            Constants.HOURS_72 -> 0
            Constants.HOURS_24 -> 1
            else -> 2
        }


        binding.rvExtraSecurity.let {
            adapter = SecurityAdapter(::itemClicked)
            it.adapter = adapter
            it.layoutManager = LinearLayoutManager(requireContext())
            adapter.setList(getList())
//            it.addItemDecoration(ItemOffsetDecoration(12))
        }

        binding.ivtopAction.setOnClickListener { requireActivity().onBackPressed() }

        binding.btnEnableWhitelisting.setOnClickListener {

            checkInternet(requireContext()) {
                showProgressDialog(requireContext())
                viewModel.updateWithdrawalLock(
                    when (selectedPosition) {
                        0 -> Constants.HOURS_72
                        1 -> Constants.HOURS_24
                        else -> Constants.NO_EXTRA_SECURITY
                    }
                )
            }
        }
    }


    private fun getList(): List<ExtraSecurity> {
        val list = mutableListOf<ExtraSecurity>()
        list.add(ExtraSecurity("72 Hours", false))
        list.add(ExtraSecurity("24 Hours", false))
        list.add(ExtraSecurity("No Extra Security", false))
        list[selectedPosition].isSelected = true
        return list
    }

    private fun itemClicked(position: Int) {

        if (selectedPosition != position) {

            adapter.getItem(selectedPosition)?.let {
                it.isSelected = false
                adapter.notifyItemChanged(selectedPosition)
            }

            adapter.getItem(position)?.let {
                it.isSelected = true
                selectedPosition = position
            }

            adapter.notifyItemChanged(position)

        }

    }


    class SecurityAdapter(private val itemClicked: (Int) -> Unit = { _ -> }) :
        BaseAdapter<ExtraSecurity>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return SecurityViewHolder(
                ItemExtraSecurityBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }


        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            itemList[position]?.let {
                (holder as SecurityViewHolder).binding.apply {

                    if (it.isSelected) {
                        root.background = ContextCompat.getDrawable(
                            root.context,
                            R.drawable.round_stroke_purple_500
                        )
                        tvTitle.setTextColor(getColor(tvTitle.context, R.color.purple_500))
                        ivRadio.setImageResource(R.drawable.radio_select)
                    } else {

                        root.background = ContextCompat.getDrawable(
                            root.context,
                            R.drawable.round_stroke_gray_100
                        )

                        ivRadio.setImageResource(R.drawable.radio_unselect)
//                        tvTitle.setTextColor(
//                            getColor(
//                                tvTitle.context,
//                                if (App.prefsManager.isWhitelisting()) R.color.purple_gray_500 else R.color.purple_gray_700
//                            )
//                        )


                    }

                    tvTitle.text = it.title
                }
            }
        }

        inner class SecurityViewHolder(val binding: ItemExtraSecurityBinding) :
            RecyclerView.ViewHolder(binding.root) {
            init {
                binding.root.setOnClickListener {
//                    if (!App.prefsManager.isWhitelisting())
                        itemClicked(adapterPosition)
                }
            }
        }

    }

    data class ExtraSecurity(val title: String, var isSelected: Boolean)
}