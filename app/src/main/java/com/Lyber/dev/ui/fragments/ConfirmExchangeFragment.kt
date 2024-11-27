package com.Lyber.dev.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.Lyber.dev.R
import com.Lyber.dev.databinding.FragmentConfirmInvestmentBinding
import com.Lyber.dev.models.AssetBaseData
import com.Lyber.dev.models.DataQuote
import com.Lyber.dev.viewmodels.PortfolioViewModel
import com.Lyber.dev.utils.CommonMethods
import com.Lyber.dev.utils.CommonMethods.Companion.formattedAsset
import com.Lyber.dev.utils.CommonMethods.Companion.gone
import com.Lyber.dev.utils.CommonMethods.Companion.visible
import com.Lyber.dev.utils.Constants
import com.google.gson.Gson
import okhttp3.ResponseBody
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Locale

class ConfirmExchangeFragment : BaseFragment<FragmentConfirmInvestmentBinding>(),
    View.OnClickListener {
    private var timer = 25

    private var isExpand = false
    private var orderId: String = ""
    private lateinit var viewModel: PortfolioViewModel
    private lateinit var fromAsset : AssetBaseData
    private lateinit var toAsset : AssetBaseData
    private var exchangeFromPrice: Double = 0.0
    private var exchangeToPrice: Double = 0.0
    override fun bind() = FragmentConfirmInvestmentBinding.inflate(layoutInflater)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = CommonMethods.getViewModel(requireActivity())
        binding.ivTopAction.setOnClickListener(this)
        binding.tvMoreDetails.setOnClickListener(this)
        binding.btnConfirmInvestment.setOnClickListener(this)
        binding.allocationView.rvAllocation.isNestedScrollingEnabled = false

    }

    override fun onResume() {
        super.onResume()
        getData()
    }


    private fun getData() {
        binding.apply {
            listOf(
                tvNestedAmount,
                tvNestedAmountValue,
                tvExchangeFrom, tvExchangeFromValue, tvExchangeTo, tvExchangeToValue,
                tvLyberFee,
                tvValueLyberFee
            ).visible()

            listOf(
                tvFrequency,
                tvValueFrequency,
                tvAllocation,
                allocationView,
                tvAssetPrice,
                tvValueAssetPrice,
                tvDeposit,
                tvDepositFee,
                tvValueDeposit,
                tvValueDepositFee, tvLyberFee,
                tvValueLyberFee
            ).gone()
        }

        if (arguments != null && requireArguments().containsKey(Constants.DATA_SELECTED)) {
            val data = Gson().fromJson(
                requireArguments().getString(Constants.DATA_SELECTED),
                DataQuote::class.java
            )
            prepareView(data)
            exchangeFromPrice=requireArguments().getDouble("FromPrice")
            exchangeToPrice=requireArguments().getDouble("ToPrice")
        }

    }

    override fun onClick(v: View?) {
        binding.apply {
            when (v!!) {
                ivTopAction -> {
                    requireActivity().onBackPressedDispatcher.onBackPressed()
//                    CommonMethods.showSnack(
//                        binding.root,
//                        requireContext(),
//                        getString(R.string.error_code_7015)
//                    )
//                    viewModel.exchangeAssetFrom = fromAsset.id
//                    val bundle = Bundle()
//                    bundle.putString(Constants.TYPE, Constants.Exchange)
//                    findNavController().navigate(R.id.action_confirmExchangeFragment_to_all_asset_fragment,bundle)

                }

                btnConfirmInvestment -> {
                    viewModel.selectedAsset = CommonMethods.getAsset(viewModel.exchangeAssetTo!!)
                    val bundle = Bundle().apply {
                        putString(Constants.ORDER_ID, orderId)
                    }
                    findNavController().navigate(
                        R.id.action_confirmExchangeFragment_to_deatil_fragment, bundle
                    )
                }

                tvMoreDetails -> {
                    if (isExpand) {
                        zzInfor.gone()
                        isExpand = false
                        binding.tvMoreDetails.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            R.drawable.ic_right_arrow_grey,
                            0,
                            0,
                            0
                        )
                    } else {
                        zzInfor.visible()
                        isExpand = true
                        binding.tvMoreDetails.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            R.drawable.ic_drop_down,
                            0,
                            0,
                            0
                        )
                    }
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun prepareView(data: DataQuote?) {
        binding.apply {
            val assetTo =
                com.Lyber.dev.ui.activities.BaseActivity.assets.find { it1 -> it1.id == data!!.toAsset }
            val assetFrom =
                com.Lyber.dev.ui.activities.BaseActivity.assets.find { it1 -> it1.id == data!!.fromAsset }
            fromAsset = assetFrom!!
//            toAsset = assetTo!!
            tvNestedAmount.text = getString(R.string.ratio)
            val balance =
                com.Lyber.dev.ui.activities.BaseActivity.balances.find { it1 -> it1.id == viewModel.exchangeAssetFrom }
            var priceCoin = balance!!.balanceData.euroBalance.toDouble()
                .div(balance.balanceData.balance.toDouble())
            tvNestedAmountValue.text = "1 : " + data!!.ratio
            tvValueLyberFee.text =
                data.fees.formattedAsset(
                    priceCoin,
                    rounding = RoundingMode.DOWN
                ) + " " + data.fromAsset.uppercase()
            tvExchangeTo.text = getString(R.string.lyber_fees)

            tvExchangeToValue.text = "~" +
                    data.fees.formattedAsset(
                        priceCoin,
                        rounding = RoundingMode.DOWN, assetTo!!.decimals
                    ) + " " + data.fromAsset.uppercase()


            orderId = data.orderId
//            tvExchangeFromValue.text =
//                "${data.fromAmount} ${data.fromAsset.uppercase()}"
            tvExchangeFromValue.text =
                "~${
                    (data.fromAmount.toDouble() - data.fees.toDouble()).toString()
                        .formattedAsset(priceCoin, RoundingMode.DOWN, 8)
                } ${data.fromAsset.uppercase()}"

//            val balanceFrom =
//                com.Lyber.dev.ui.activities.BaseActivity.balanceResume.find { it1 -> it1.id == viewModel.exchangeAssetFrom }
//            val balanceTo =
//                com.Lyber.dev.ui.activities.BaseActivity.balanceResume.find { it1 -> it1.id == viewModel.exchangeAssetTo }
//            val balanceFromPrice = balanceFrom!!.priceServiceResumeData.lastPrice
//            val balanceToPrice = balanceTo!!.priceServiceResumeData.lastPrice
            val valuesInEurosToAsset =
                (if (data.fromAsset == viewModel.exchangeAssetTo!!) exchangeToPrice else exchangeFromPrice).toDouble()
            priceCoin = valuesInEurosToAsset
                .div(data.toAmount.toDouble())
//            tvExchangeTo.text=getString(R.string.lyber_fees)
//
//            tvExchangeToValue.text =
//                "${data.toAmount.formattedAsset(
//                    price = priceCoin,
//                    rounding = RoundingMode.DOWN
//                )} ${data.toAsset.uppercase()}"
            Log.d("dataa", "$data")

            if (data.fromAmount.contains(".")) {
                val number = BigDecimal(data.fromAmount)
                val trimmedNumber = number.stripTrailingZeros()
                tvTotalAmount.text =
                    "${trimmedNumber} ${data.fromAsset.uppercase()}"
            } else
                tvTotalAmount.text =
                    "${
                        String.format(Locale.US, "%f", data.fromAmount.toFloat()).trimEnd('0')
                            .trimEnd('.')
                    } ${data.fromAsset.uppercase()}"

            tvAmount.text =
                "${
                    data.toAmount.formattedAsset(
                        price = priceCoin,
                        rounding = RoundingMode.DOWN, assetTo!!.decimals
                    )
                } ${data.toAsset.uppercase()}"
//            val valueTotal = data.fees.toDouble()+data.fromAmount.toDouble()
            val valueTotal = data.fromAmount.toDouble()
            tvValueTotal.text =
                "${
                    valueTotal.toString().formattedAsset(
                        price = priceCoin,
                        rounding = RoundingMode.DOWN,
                        assetFrom!!.decimals
                    )
                } ${data.fromAsset.uppercase()}"
            btnConfirmInvestment.isEnabled = true
            startTimer()
            btnConfirmInvestment.text =
                getString(R.string._25_sec, getString(R.string.confirm_exchange), "25")
            title.text = getString(R.string.confirm_exchange)


        }
    }


    private fun startTimer() {
        try {
            Handler(Looper.getMainLooper()).postDelayed({
                if (lifecycle.currentState == Lifecycle.State.RESUMED)
                    if (timer == 0) {
                        binding.btnConfirmInvestment.isEnabled = true
                        binding.btnConfirmInvestment.text = getString(
                            R.string._25_sec, getString(R.string.confirm_exchange), timer.toString()
                        )
                        binding.btnConfirmInvestment.isEnabled = false
                        binding.btnConfirmInvestment.background = ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.button_purple_400
                        )
                    } else {
                        timer -= 1
                        binding.btnConfirmInvestment.text = getString(
                            R.string._25_sec, getString(R.string.confirm_exchange), timer.toString()
                        )
                        binding.btnConfirmInvestment.background = ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.button_purple_500
                        )
                        startTimer()
                    }
            }, 1000)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onRetrofitError(errorCode: Int, msg: String) {
        when (errorCode) {
            7006 -> {
                CommonMethods.showSnack(
                    binding.root,
                    requireContext(),
                    getString(R.string.error_code_7006)
                )
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }

            7010 -> {
                CommonMethods.showSnack(
                    binding.root,
                    requireContext(),
                    getString(R.string.error_code_7010)
                )
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }

            7007 -> {
                //FromExch
                viewModel.selectedAsset = CommonMethods.getAsset(viewModel.exchangeAssetTo!!)
                CommonMethods.showSnack(
                    binding.root,
                    requireContext(),
                    getString(R.string.error_code_7007)
                )
                findNavController().navigate(R.id.action_confirmExchangeFragment_to_deatil_fragment)
            }

            7008 -> {
                //FromExch
                viewModel.selectedAsset = CommonMethods.getAsset(viewModel.exchangeAssetTo!!)
                CommonMethods.showSnack(
                    binding.root,
                    requireContext(),
                    getString(R.string.error_code_7008)
                )
                findNavController().navigate(R.id.action_confirmExchangeFragment_to_deatil_fragment)
            }

            7009 -> {
                //FromExch
                viewModel.selectedAsset = CommonMethods.getAsset(viewModel.exchangeAssetTo!!)
                CommonMethods.showSnack(
                    binding.root,
                    requireContext(),
                    getString(R.string.error_code_7009)
                )
                findNavController().navigate(R.id.action_confirmExchangeFragment_to_deatil_fragment)
            }

            7000 -> {
                val data1 =
                    com.Lyber.dev.ui.activities.BaseActivity.assets.firstNotNullOfOrNull { item -> item.takeIf { item.id == viewModel.exchangeAssetFrom } }

                CommonMethods.showSnack(
                    binding.root,
                    requireContext(),
                    getString(R.string.error_code_7000, data1!!.fullName)
                )
                findNavController().navigate(R.id.action_confirmExchangeFragment_to_home_fragment)

            }

            7001 -> {
                val data1 =
                    com.Lyber.dev.ui.activities.BaseActivity.assets.firstNotNullOfOrNull { item -> item.takeIf { item.id == viewModel.exchangeAssetTo } }

                CommonMethods.showSnack(
                    binding.root,
                    requireContext(),
                    getString(R.string.error_code_7001, data1!!.fullName)
                )
                findNavController().navigate(R.id.action_confirmExchangeFragment_to_home_fragment)

            }

            7002 -> {
                CommonMethods.showSnack(
                    binding.root,
                    requireContext(),
                    getString(R.string.error_code_7002)
                )
                findNavController().navigate(R.id.action_confirmExchangeFragment_to_home_fragment)
            }

            7018 -> {
                CommonMethods.showSnack(
                    binding.root,
                    requireContext(),
                    getString(R.string.error_code_7018)
                )
                findNavController().navigate(R.id.action_confirmExchangeFragment_to_home_fragment)
            }

            7019 -> {
                CommonMethods.showSnack(
                    binding.root,
                    requireContext(),
                    getString(R.string.error_code_7019)
                )
                findNavController().navigate(R.id.action_confirmExchangeFragment_to_home_fragment)
            }

            7020 -> {
                CommonMethods.showSnack(
                    binding.root,
                    requireContext(),
                    getString(R.string.error_code_7020)
                )
                findNavController().navigate(R.id.action_confirmExchangeFragment_to_home_fragment)
            }

            7021 -> {
                CommonMethods.showSnack(
                    binding.root,
                    requireContext(),
                    getString(R.string.error_code_7021)
                )
                findNavController().navigate(R.id.action_confirmExchangeFragment_to_home_fragment)
            }

            7022 -> {
                CommonMethods.showSnack(
                    binding.root,
                    requireContext(),
                    getString(R.string.error_code_7022)
                )
                findNavController().navigate(R.id.action_confirmExchangeFragment_to_home_fragment)

            }

            7024 -> {
                CommonMethods.showSnack(
                    binding.root,
                    requireContext(),
                    getString(R.string.error_code_7024)
                )
                findNavController().navigate(R.id.action_confirmExchangeFragment_to_home_fragment)

            }
            7015 -> {
                CommonMethods.showSnack(
                    binding.root,
                    requireContext(),
                    getString(R.string.error_code_7015)
                )
                findNavController().navigate(R.id.action_confirmExchangeFragment_to_all_asset_fragment)
            }
            else ->   super.onRetrofitError(errorCode, msg)
        }
    }
}