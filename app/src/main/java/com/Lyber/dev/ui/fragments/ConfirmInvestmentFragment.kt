package com.Lyber.dev.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.Lyber.dev.R
import com.Lyber.dev.databinding.FragmentConfirmInvestmentBinding
import com.Lyber.dev.ui.activities.SplashActivity
import com.Lyber.dev.ui.portfolio.fragment.PortfolioHomeFragment
import com.Lyber.dev.utils.CommonMethods
import com.Lyber.dev.viewmodels.PortfolioViewModel
import com.Lyber.dev.utils.CommonMethods.Companion.addFragment
import com.Lyber.dev.utils.CommonMethods.Companion.checkInternet
import com.Lyber.dev.utils.CommonMethods.Companion.clearBackStack
import com.Lyber.dev.utils.CommonMethods.Companion.commaFormatted
import com.Lyber.dev.utils.CommonMethods.Companion.commaFormattedDecimal
import com.Lyber.dev.utils.CommonMethods.Companion.decimalPoint
import com.Lyber.dev.utils.CommonMethods.Companion.dismissProgressDialog
import com.Lyber.dev.utils.CommonMethods.Companion.getViewModel
import com.Lyber.dev.utils.CommonMethods.Companion.gone
import com.Lyber.dev.utils.CommonMethods.Companion.showProgressDialog
import com.Lyber.dev.utils.CommonMethods.Companion.visible
import com.Lyber.dev.utils.Constants
import com.appsflyer.AFInAppEventParameterName
import com.appsflyer.AFInAppEventType
import com.appsflyer.AppsFlyerLib
import com.appsflyer.attribution.AppsFlyerRequestListener
import com.google.android.gms.tasks.Task
import com.google.android.play.core.integrity.StandardIntegrityManager
import okhttp3.ResponseBody
import org.json.JSONObject
import java.util.*

//TODO from invest add money : IAM
class ConfirmInvestmentFragment : BaseFragment<FragmentConfirmInvestmentBinding>(),
    View.OnClickListener {

    private lateinit var viewModel: PortfolioViewModel
    override fun bind() = FragmentConfirmInvestmentBinding.inflate(layoutInflater)
    private var decimal = 2
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = getViewModel(requireActivity())
        viewModel.listener = this
        binding.ivTopAction.setOnClickListener(this)
        binding.btnConfirmInvestment.setOnClickListener(this)
        binding.allocationView.rvAllocation.isNestedScrollingEnabled = false
        if (arguments != null && requireArguments().containsKey(Constants.DECIMAL))
            decimal = requireArguments().getInt(Constants.DECIMAL)
        prepareView()


        viewModel.investStrategyResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                dismissProgressDialog()
                val eventValues = HashMap<String, Any>()
                eventValues[AFInAppEventParameterName.CONTENT_ID] = viewModel.amount.toDouble()
                eventValues[AFInAppEventParameterName.CONTENT_TYPE] =
                    Constants.APP_FLYER_TYPE_ACTIVATE_STRATEGY
                AppsFlyerLib.getInstance().logEvent(requireContext().applicationContext,
                    AFInAppEventType.PURCHASE, eventValues,
                    object : AppsFlyerRequestListener {
                        override fun onSuccess() {
                            Log.d("LOG_TAG", "Event Activate Strategy  sent successfully")
                        }

                        override fun onError(errorCode: Int, errorDesc: String) {
                            Log.d(
                                "LOG_TAG", "Event Activate Strategy  failed to be sent:\n" +
                                        "Error code: " + errorCode + "\n"
                                        + "Error description: " + errorDesc
                            )
                        }
                    })
                findNavController().popBackStack(R.id.pickYourStrategyFragment, false)
            }
        }

        viewModel.oneTimeStrategyDataResponse.observe(viewLifecycleOwner) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                dismissProgressDialog()
                val eventValues = HashMap<String, Any>()
                eventValues[AFInAppEventParameterName.CONTENT_ID] = viewModel.amount.toDouble()
                eventValues[AFInAppEventParameterName.CONTENT_TYPE] =
                    Constants.APP_FLYER_TYPE_STRATEGY_EXECUTION
                AppsFlyerLib.getInstance().logEvent(requireContext().applicationContext,
                    AFInAppEventType.PURCHASE, eventValues,
                    object : AppsFlyerRequestListener {
                        override fun onSuccess() {
                            Log.d("LOG_TAG", "Event Strategy execution sent successfully")
                        }

                        override fun onError(errorCode: Int, errorDesc: String) {
                            Log.d(
                                "LOG_TAG", "Event Strategy execution failed to be sent:\n" +
                                        "Error code: " + errorCode + "\n"
                                        + "Error description: " + errorDesc
                            )
                        }
                    })
                Log.d("dataa", "${it.data.id}")
                val bundle = Bundle()
                bundle.putString("executionId", it.data.id)
                findNavController().navigate(R.id.orderStrategyExecutionFragment, bundle)
            }
        }
    }

    override fun onClick(v: View?) {
        binding.apply {
            when (v!!) {

                ivTopAction -> {
                    requireActivity().onBackPressed()
                }

                btnConfirmInvestment -> {

                    when (viewModel.selectedOption) {
                        Constants.USING_STRATEGY -> {
                            viewModel.selectedStrategy?.let {
                                checkInternet(binding.root, requireContext()) {
                                    /*frequency = "now" || "1d" || "1w" || "1m"*/
                                    val freq = when (viewModel.selectedFrequency) {
                                        "Once" -> null     //"now"
                                        "Daily" -> "1d"
                                        "Weekly" -> "1w"
                                        "none" -> "none"
                                        else -> "1m"
                                    }
                                    showProgressDialog(requireContext())
                                    if (freq == "none") {
                                        val jsonObject = JSONObject()
                                        jsonObject.put("ownerUuid", it.ownerUuid)
                                        jsonObject.put("strategyName", viewModel.selectedStrategy!!.name)
                                        jsonObject.put("amount",  viewModel.amount.toDouble() )
                                        val jsonString = jsonObject.toString()
                                        // Generate the request hash
                                        val requestHash =
                                            CommonMethods.generateRequestHash(jsonString)
                                        val integrityTokenResponse1: Task<StandardIntegrityManager.StandardIntegrityToken>? =
                                            SplashActivity.integrityTokenProvider?.request(
                                                StandardIntegrityManager.StandardIntegrityTokenRequest.builder()
                                                    .setRequestHash(requestHash)
                                                    .build()
                                            )
                                        integrityTokenResponse1?.addOnSuccessListener { response ->
                                            Log.d("token", "${response.token()}")
                                            viewModel.oneTimeOrderStrategy( viewModel.selectedStrategy!!.name,
                                               viewModel.amount.toDouble(), it.ownerUuid,
                                                response.token()
                                            )

                                        }?.addOnFailureListener { exception ->
                                            Log.d("token", "${exception}")
                                        }
                                    } else {
                                        if (arguments != null && requireArguments().getBoolean(
                                                Constants.EDIT_ACTIVE_STRATEGY
                                            )
                                        ) {
                                            val jsonObject = JSONObject()
                                            jsonObject.put("ownerUuid", it.ownerUuid)
                                            jsonObject.put("strategyName", viewModel.selectedStrategy!!.name)
                                            jsonObject.put("amount",  viewModel.amount.toDouble() )
                                            if (freq != null)
                                                jsonObject.put("frequency",  freq )

                                            val jsonString = jsonObject.toString()
                                            // Generate the request hash
                                            val requestHash =
                                                CommonMethods.generateRequestHash(jsonString)
                                            val integrityTokenResponse1: Task<StandardIntegrityManager.StandardIntegrityToken>? =
                                                SplashActivity.integrityTokenProvider?.request(
                                                    StandardIntegrityManager.StandardIntegrityTokenRequest.builder()
                                                        .setRequestHash(requestHash)
                                                        .build()
                                                )
                                            integrityTokenResponse1?.addOnSuccessListener { response ->
                                                Log.d("token", "${response.token()}")
                                                viewModel.editEnabledStrategy(
                                                    it.ownerUuid,
                                                    freq,
                                                    viewModel.amount.toDouble(),
                                                    viewModel.selectedStrategy!!.name,response.token()
                                                )

                                            }?.addOnFailureListener { exception ->
                                                Log.d("token", "${exception}")
                                            }
                                        }
                                        else {
                                            val jsonObject = JSONObject()
                                            jsonObject.put("ownerUuid", it.ownerUuid)
                                            jsonObject.put("strategyName", viewModel.selectedStrategy!!.name)
                                            jsonObject.put("amount",  viewModel.amount.toDouble() )
                                            if (freq != null)
                                                jsonObject.put("frequency",  freq )
                                            val jsonString = jsonObject.toString()
                                            // Generate the request hash
                                            val requestHash =
                                                CommonMethods.generateRequestHash(jsonString)
                                            val integrityTokenResponse1: Task<StandardIntegrityManager.StandardIntegrityToken>? =
                                                SplashActivity.integrityTokenProvider?.request(
                                                    StandardIntegrityManager.StandardIntegrityTokenRequest.builder()
                                                        .setRequestHash(requestHash)
                                                        .build()
                                                )
                                            integrityTokenResponse1?.addOnSuccessListener { response ->
                                                Log.d("token", "${response.token()}")
                                                viewModel.investStrategy(
                                                    it.ownerUuid,
                                                    freq,
                                                    viewModel.amount.toDouble(),
                                                    viewModel.selectedStrategy!!.name,
                                                    response.token()
                                                )

                                            }?.addOnFailureListener { exception ->
                                                Log.d("token", "${exception}")
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        else -> {
                            requireActivity().clearBackStack()
                            requireActivity().addFragment(
                                R.id.flSplashActivity,
                                PortfolioHomeFragment()
                            )
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun prepareView() {
        binding.apply {
            val buyValue = (viewModel.amount.toDouble() * (0.08)).toDouble()
            tvNestedAmountValue.text =
                viewModel.amount.decimalPoint()
                    .commaFormattedDecimal(decimal) + " ${Constants.MAIN_ASSET_UPPER}"
            tvValueTotal.text =
                (viewModel.amount.toFloat() + buyValue).toString()
                    .decimalPoint()
                    .commaFormattedDecimal(decimal) + " ${Constants.MAIN_ASSET_UPPER}"
            tvValueLyberFee.text =
                buyValue.toString().decimalPoint()
                    .commaFormattedDecimal(decimal) + " ${Constants.MAIN_ASSET_UPPER}"

            when (viewModel.selectedOption) {

                Constants.USING_STRATEGY -> {

                    listOf(
                        zzInfor,
                        tvFrequency,
                        tvValueFrequency,
                        tvAllocation,
                        allocationView,
                        tvLyberFee,
                        tvValueLyberFee
                    ).visible()

                    viewModel.selectedStrategy?.bundle?.let {
                        binding.allocationView.setAssetsList(it)
                    }

                    listOf(
                        ivSingleAsset,
                        tvTotalAmount,
                        tvMoreDetails,
                        tvAssetPrice,
                        tvValueAssetPrice,
                        tvDeposit,
                        tvDepositFee,
                        tvValueDeposit,
                        tvValueDepositFee
                    ).gone()
                    //changed fee to 1 percent of the amount
                    var fee = ((viewModel.amount.toDouble() * 0.5f) / 100.0f)
                    fee = String.format(Locale.US, "%.${decimal}f", fee).toDouble()

                    tvNestedAmount.text = getString(R.string.invest)

                    if (viewModel.selectedFrequency == "none")
                        tvValueFrequency.text = getString(R.string.immediate)
                    else
                        tvValueFrequency.text = viewModel.selectedFrequency

                    tvNestedAmountValue.text = (viewModel.amount.toDouble() - fee).toString()
                        .decimalPoint()
                        .commaFormattedDecimal(decimal) + " ${Constants.MAIN_ASSET_UPPER}"


//                    viewModel.amount.decimalPoint().commaFormatted + " ${Constants.MAIN_ASSET_UPPER}"
                    tvValueTotal.text =
                        (viewModel.amount.toDouble()).toString()
                            .decimalPoint().commaFormatted + " ${Constants.MAIN_ASSET_UPPER}"
                    tvLyberFee.text = getString(R.string.fee)
                    tvValueLyberFee.text =
                        "~" + fee.toString().decimalPoint() + " ${Constants.MAIN_ASSET_UPPER}"


                    tvAmount.text =
                        (viewModel.amount.toDouble()).toString() + " ${Constants.MAIN_ASSET_UPPER}"

                }


            }

        }
    }

    override fun onRetrofitError(errorCode: Int, msg: String) {
        dismissProgressDialog()
        when (errorCode) {
            13006 -> {
                var minInvest = ""
                try {
                    minInvest = viewModel.selectedStrategy?.minAmount?.toFloat()!!.toString()

                } catch (_: Exception) {
                }
                CommonMethods.showSnack(
                    binding.root,
                    requireContext(),
                    getString(R.string.error_code_13006, minInvest)
                )
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }

            7024 -> {
                //IAM
                CommonMethods.showSnack(
                    binding.root,
                    requireContext(),
                    getString(R.string.error_code_7024)
                )
                findNavController().navigate(R.id.action_confirmInvestment_to_investment_strategies)

            }

            13001 -> {
                //IAM
                CommonMethods.showSnack(
                    binding.root,
                    requireContext(),
                    getString(R.string.error_code_13001)
                )
                findNavController().navigate(R.id.action_confirmInvestment_to_investment_strategies)
            }

            13003 -> {
                //IAM
                CommonMethods.showSnack(
                    binding.root,
                    requireContext(),
                    getString(R.string.error_code_13003)
                )
                findNavController().navigate(R.id.action_confirmInvestment_to_investment_strategies)
            }

            13016 -> {
                //IAM
                CommonMethods.showSnack(
                    binding.root,
                    requireContext(),
                    getString(R.string.error_code_13016)
                )
                findNavController().navigate(R.id.action_confirmInvestment_to_investment_strategies)
            }

            13017 -> {
                //IAM
                CommonMethods.showSnack(
                    binding.root,
                    requireContext(),
                    getString(R.string.error_code_13017)
                )
                findNavController().navigate(R.id.action_confirmInvestment_to_investment_strategies)
            }

            13018 -> {
                //IAM
                CommonMethods.showSnack(
                    binding.root,
                    requireContext(),
                    getString(R.string.error_code_13018)
                )
                findNavController().navigate(R.id.action_confirmInvestment_to_investment_strategies)
            }

            13019 -> {
                //IAM
                CommonMethods.showSnack(
                    binding.root,
                    requireContext(),
                    getString(R.string.error_code_13019)
                )
                findNavController().navigate(R.id.action_confirmInvestment_to_investment_strategies)

            }

            13020 -> {
                //IAM
                CommonMethods.showSnack(
                    binding.root,
                    requireContext(),
                    getString(R.string.error_code_13020)
                )
                findNavController().navigate(R.id.action_confirmInvestment_to_investment_strategies)

            }

            else -> super.onRetrofitError(errorCode, msg)

        }
    }
}