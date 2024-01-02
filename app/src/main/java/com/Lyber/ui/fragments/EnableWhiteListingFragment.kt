package com.Lyber.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getColor
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.Lyber.R
import com.Lyber.databinding.FragmentManageWhitelistingBinding
import com.Lyber.databinding.ItemExtraSecurityBinding
import com.Lyber.ui.adapters.BaseAdapter
import com.Lyber.utils.App
import com.Lyber.utils.CommonMethods.Companion.checkInternet
import com.Lyber.utils.CommonMethods.Companion.dismissProgressDialog
import com.Lyber.utils.CommonMethods.Companion.getViewModel
import com.Lyber.utils.CommonMethods.Companion.setBackgroundTint
import com.Lyber.utils.CommonMethods.Companion.showProgressDialog
import com.Lyber.utils.Constants
import com.Lyber.viewmodels.NetworkViewModel

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
        when(selectedPosition){
            1->{binding.tvSecurityText.text =
                getString(R.string.a_24h_delay_is_required_before_you_can_withdraw_to_any_address_newly_added_to_your_address_book).also { binding.tvSecurityText.text = it }
            }
            0->{
                binding.tvSecurityText.text =
                    getString(R.string.a_delay_of_72_hours_will_be_required_before_you_can_withdraw_to_any_address_newly_added_to_your_address_book)
            }
            2->{
                binding.tvSecurityText.text = getString(R.string.you_can_immediately_withdraw_to_any_address_newly_added_to_your_address_book)
            }
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
        list.add(ExtraSecurity(requireContext().getString(R.string.hours_72), false))
        list.add(ExtraSecurity(requireContext().getString(R.string.hours_24), false))
        list.add(ExtraSecurity(requireContext().getString(R.string.no_extra_security), false))
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
                when(selectedPosition){
                    1->{
                        binding.tvSecurityText.text =
                            getString(R.string.a_24h_delay_is_required_before_you_can_withdraw_to_any_address_newly_added_to_your_address_book) }
                    0->{
                        binding.tvSecurityText.text =
                            getString(R.string.a_delay_of_72_hours_will_be_required_before_you_can_withdraw_to_any_address_newly_added_to_your_address_book)
                    }
                    2->{
                        binding.tvSecurityText.text = getString(R.string.shifting_no_security)
                    }
                    else->{
                        binding.tvSecurityText.text = getString(R.string.shifting)

                    }
                }
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
                        ivRadio.setImageResource(R.drawable.radio_select)
                    } else {

                        root.background = ContextCompat.getDrawable(
                            root.context,
                            R.drawable.round_stroke_gray_100
                        )

                        ivRadio.setImageResource(R.drawable.radio_unselect)
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