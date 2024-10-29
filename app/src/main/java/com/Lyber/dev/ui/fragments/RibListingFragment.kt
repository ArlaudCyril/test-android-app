package com.Lyber.dev.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.Lyber.dev.R
import com.Lyber.dev.databinding.FragmentRibListingBinding
import com.Lyber.dev.databinding.ItemMyAssetBinding
import com.Lyber.dev.models.RIBData
import com.Lyber.dev.ui.activities.SplashActivity
import com.Lyber.dev.ui.adapters.BaseAdapter
import com.Lyber.dev.ui.fragments.bottomsheetfragments.AddAddressInfoBottomSheet
import com.Lyber.dev.utils.CommonMethods
import com.Lyber.dev.utils.CommonMethods.Companion.gone
import com.Lyber.dev.utils.CommonMethods.Companion.visible
import com.Lyber.dev.utils.Constants
import com.Lyber.dev.viewmodels.PortfolioViewModel
import com.google.android.gms.tasks.Task
import com.google.android.play.core.integrity.StandardIntegrityManager
import com.google.gson.GsonBuilder
import org.json.JSONObject

class RibListingFragment : BaseFragment<FragmentRibListingBinding>(), OnClickListener {
    override fun bind() = FragmentRibListingBinding.inflate(layoutInflater)
    private lateinit var adapter: ListingAdapter

    private lateinit var layoutManager: LinearLayoutManager

    private lateinit var viewModel: PortfolioViewModel
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = CommonMethods.getViewModel(requireActivity())
        viewModel.listener = this
        adapter = ListingAdapter(true, ::itemClicked)
        layoutManager = LinearLayoutManager(requireContext())
        binding.rvRib.let {
            it.adapter = adapter
            it.layoutManager = layoutManager
        }
        val list = com.Lyber.dev.ui.activities.BaseActivity.ribWalletList
        adapter.setList(list)
        binding.ivBack.setOnClickListener(this)
        binding.btnAdd.setOnClickListener(this)
        CommonMethods.checkInternet(binding.root, requireActivity()) {
            viewModel.getAssetDetailIncludeNetworks(Constants.MAIN_ASSET)
        }
        viewModel.getAssetDetail.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                CommonMethods.dismissProgressDialog()
                viewModel.selectedAssetDetail = it.data
            }
        }
        viewModel.exportOperationResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                  viewModel.getWalletRib()
            }
        }
        viewModel.walletRibResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                CommonMethods.dismissProgressDialog()
                com.Lyber.dev.ui.activities.BaseActivity.ribWalletList =
                    it.data as ArrayList<RIBData>
                adapter.clearList()
                adapter.setList(it.data)
            }
        }
    }

    private fun itemClicked(ribData: RIBData) {
        AddAddressInfoBottomSheet(true, requireActivity()) {
            if (it == 1) {
                // delete
                CommonMethods.checkInternet(binding.root, requireContext()) {
                    CommonMethods.showProgressDialog(requireContext())
                    val jsonObject = JSONObject()
                    jsonObject.put("ribId", ribData.ribId)
                    val jsonString = jsonObject.toString()
                    // Generate the request hash
                    val requestHash = CommonMethods.generateRequestHash(jsonString)

                    val integrityTokenResponse1: Task<StandardIntegrityManager.StandardIntegrityToken>? =
                        SplashActivity.integrityTokenProvider?.request(
                            StandardIntegrityManager.StandardIntegrityTokenRequest.builder()
                                .setRequestHash(requestHash)
                                .build()
                        )
                    integrityTokenResponse1?.addOnSuccessListener { response ->
                        Log.d("token", "${response.token()}")
                        viewModel.deleteRIB(ribData.ribId, response.token())

                    }?.addOnFailureListener { exception ->
                        Log.d("token", "${exception}")

                    }
                }
            } else if (it == 3) {
                when (ribData.ribStatus.lowercase()) {
                    "pending" -> {
                        CommonMethods.showSnack(
                            binding.root,
                            requireContext(),
                            getString(R.string.under_validation)
                        )
                    }

                    "rejected" -> {
                        CommonMethods.showSnack(
                            binding.root,
                            requireContext(),
                            getString(R.string.rejected_rib)
                        )
                    }

                    "validated" -> {
                        findNavController().navigate(R.id.withdrawUsdcFragment)
                    }
                }

            } else {
                val gson = GsonBuilder().create()
                var data = ""
                data = gson.toJson(ribData)
                //edit
                val bundle = Bundle().apply {
                    putString(Constants.TO_EDIT, data)
                }
                findNavController().navigate(R.id.addRibFragment, bundle)
            }
        }
            .setRibData(ribData)
            .show(childFragmentManager, "")
    }

    inner class ListingAdapter(
        private val isFromWithdraw: Boolean = false,
        private val listener: (RIBData) -> Unit = { _ ->
        },
    ) :
        BaseAdapter<RIBData>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return AssetViewHolder(
                ItemMyAssetBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

            if (itemList[position] != null)
                (holder as AssetViewHolder).binding.apply {
                    itemList[position]?.let {
                        when (it.ribStatus.lowercase()) {
                            "pending" -> ivAssetIcon.setImageResource(R.drawable.pending_indicator)
                            "validated" -> ivAssetIcon.setImageResource(R.drawable.accepted_indicator)
                            else -> ivAssetIcon.setImageResource(R.drawable.red_rejected_indicator)
                        }
                        tvAssetName.visible()
                        tvAssetName.text = it.name

                        tvAssetNameCode.gone()

                        tvAssetAmount.text = it.userName
                        val maxLength = 20 // Adjust this value as needed
                        val truncatedText = CommonMethods.getTruncatedText(it.iban, maxLength)
                        tvAssetAmountInCrypto.text = truncatedText
//                        tvAssetAmountInCrypto.text = it.iban


                    }
                }
        }

        inner class AssetViewHolder(val binding: ItemMyAssetBinding) :
            RecyclerView.ViewHolder(binding.root) {
            init {

                binding.ivDropIcon.setImageResource(R.drawable.ic_right_arrow_grey)
                binding.ivDropIcon.visible()
                binding.root.setOnClickListener {
                    itemList[absoluteAdapterPosition]?.let { it1 ->
                        listener(it1)
                    }
                }
            }
        }
    }

    override fun onClick(p0: View?) {
        binding.apply {
            when (p0) {
                ivBack -> requireActivity().onBackPressedDispatcher.onBackPressed()
                btnAdd -> {
                    findNavController().navigate(R.id.addRibFragment)
                }
            }
        }
    }


}