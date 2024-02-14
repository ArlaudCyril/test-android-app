package com.Lyber.ui.fragments.bottomsheetfragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.Lyber.R
import com.Lyber.databinding.BottomSheetPortfolioBinding
import com.Lyber.databinding.BottomSheetRecyclerViewBinding
import com.Lyber.databinding.BottomSheetWithdrawExchangeBinding
import com.Lyber.models.AssetBaseData
import com.Lyber.models.DataBottomSheet
import com.Lyber.ui.adapters.BottomSheetAdapter
import com.Lyber.ui.portfolio.viewModel.PortfolioViewModel
import com.Lyber.utils.CommonMethods.Companion.getViewModel
import com.Lyber.utils.CommonMethods.Companion.gone
import com.Lyber.utils.CommonMethods.Companion.toPx
import com.Lyber.utils.Constants.LYBER_ASSETS
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

open class BottomSheetDialog(
    private val listenItemClicked: (tag: String, item: DataBottomSheet) -> Unit = { _, _ -> },
    private val selectedAsset: AssetBaseData? = null,
    private val fromDetailScreen: Boolean = false
) :
    BottomSheetDialogFragment(), BottomSheetAdapter.ItemListener {

    private var _binding: BottomSheetPortfolioBinding? = null
    private var _recyclerBinding: BottomSheetRecyclerViewBinding? = null

    private val rvBinding get() = _recyclerBinding!!
    private val binding get() = _binding!!

    private var _withdrawBinding: BottomSheetWithdrawExchangeBinding? = null
    private val withdrawBinding get() = _withdrawBinding!!

    private lateinit var viewModel: PortfolioViewModel

    private lateinit var bottomSheetAdapter: BottomSheetAdapter

    private val behaviour get() = BottomSheetBehavior.from(requireView().parent as View)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomDialogBottomSheet)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return when (tag) {
            SheetType.COMPLETE_ACCOUNT.title(requireContext()) -> {
                _binding = BottomSheetPortfolioBinding.inflate(layoutInflater)
                binding.ivDropDown.setOnClickListener {
                    if (behaviour.state != BottomSheetBehavior.STATE_EXPANDED)
                        behaviour.state = BottomSheetBehavior.STATE_EXPANDED
                    else behaviour.state = BottomSheetBehavior.STATE_COLLAPSED
                }
                binding.root
            }

            SheetType.WITHDRAW_EXCHANGE.title(requireContext()) -> {
                _withdrawBinding = BottomSheetWithdrawExchangeBinding.inflate(layoutInflater)

                withdrawBinding.root
            }

            else -> {
                bottomSheetAdapter = when (tag) {
                    SheetType.CRYPTO_EXP.title(requireContext()) -> BottomSheetAdapter(
                        this,
                        SheetType.CRYPTO_EXP,
                        requireActivity()
                    )

                    SheetType.SOURCE_OF_INCOME.title(requireContext()) -> BottomSheetAdapter(
                        this,
                        SheetType.SOURCE_OF_INCOME, requireActivity()
                    )

                    SheetType.WORK_INDUSTRY.title(requireContext()) -> BottomSheetAdapter(
                        this,
                        SheetType.WORK_INDUSTRY, requireActivity()
                    )

                    SheetType.ANNUAL_INCOME.title(requireContext()) -> BottomSheetAdapter(
                        this,
                        SheetType.ANNUAL_INCOME, requireActivity()
                    )

                    else -> BottomSheetAdapter(
                        this,
                        SheetType.YOUR_ACTIVITY_ON_LYBER,
                        requireActivity()
                    )
                }

                _recyclerBinding = BottomSheetRecyclerViewBinding.inflate(layoutInflater)
                rvBinding.rvBottomSheet.let {
                    it.adapter = bottomSheetAdapter
                    it.layoutManager = LinearLayoutManager(requireContext())
                }

                rvBinding.ivTopAction.setOnClickListener { dismiss() }
                rvBinding.tvTitleBottomSheet.text = tag

                rvBinding.root
            }
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (tag != SheetType.COMPLETE_ACCOUNT.title(requireContext()))
            behaviour.state = BottomSheetBehavior.STATE_EXPANDED

        when (tag) {

            SheetType.COMPLETE_ACCOUNT.title(requireContext()) -> {}

            SheetType.CRYPTO_EXP.title(requireContext()) -> bottomSheetAdapter.setList(
                SheetType.CRYPTO_EXP.getData(
                    requireContext()
                )
            )

            SheetType.SOURCE_OF_INCOME.title(requireContext()) -> bottomSheetAdapter.setList(
                SheetType.SOURCE_OF_INCOME.getData(requireContext())
            )

            SheetType.WORK_INDUSTRY.title(requireContext()) -> bottomSheetAdapter.setList(
                SheetType.WORK_INDUSTRY.getData(
                    requireContext()
                )
            )

            SheetType.ANNUAL_INCOME.title(requireContext()) -> bottomSheetAdapter.setList(
                SheetType.ANNUAL_INCOME.getData(
                    requireContext()
                )
            )

            SheetType.ADD_AN_ASSET.title(requireContext()) -> {}

            SheetType.WITHDRAW_EXCHANGE.title(requireContext()) -> {

                viewModel = getViewModel(requireActivity())

                if (fromDetailScreen)

                    selectedAsset?.let {

                        val isLyberAsset =
                            LYBER_ASSETS.contains(it.id) || LYBER_ASSETS.contains(it.id.uppercase()) || it.fullName in LYBER_ASSETS

                        if (!isLyberAsset) {
                            withdrawBinding.apply {
                                ivWithdraw.gone()
                                tvSubTitleWithdraw.gone()
                                tvTitleWithdraw.gone()
                                ivRightArrowWithdraw.gone()
                                ivDeposit.gone()
                                tvSubTitleDeposit.gone()
                                tvTitleDeposit.gone()
                                ivRightArrowDeposit.gone()
                            }
                        }

                        withdrawBinding.apply {
                            tvTitleDepositAsset.text =
                                "${requireContext().getString(R.string.sell)} ${it.id.uppercase()}"
//                        tvSubTitleDepositAsset.text =
//                            "Sell ${it.asset_id.uppercase()} from your wallet"
                        }

                    }

                withdrawBinding.ivTopAction.setOnClickListener { dismiss() }

                val withdraw = View.OnClickListener {
                    listenItemClicked(
                        tag ?: "", DataBottomSheet(1, requireContext().getString(R.string.withdraw))
                    )
                    dismiss()
                }

                val exchange = View.OnClickListener {
                    listenItemClicked(
                        tag ?: "",
                        DataBottomSheet(1, requireContext().getString(R.string.exchange))
                    )
                    dismiss()
                }

                val deposit = View.OnClickListener {
                    listenItemClicked(
                        tag ?: "",
                        DataBottomSheet(1, requireContext().getString(R.string.deposit))
                    )
                    dismiss()
                }

                val depositAsset = View.OnClickListener {
                    listenItemClicked(
                        tag ?: "",
                        DataBottomSheet(1, requireContext().getString(R.string.deposit_an_asset))
                    )
                    dismiss()
                }

                withdrawBinding.let {

                    if (fromDetailScreen) {
                        it.ivDepositAsset.gone()
                        it.tvSubTitleDepositAsset.gone()
                        it.tvTitleDepositAsset.gone()
                        it.ivRightArrowDepositAsset.gone()
                    }

                    it.ivExchange.setOnClickListener(exchange)
                    it.tvSubTitleExchange.setOnClickListener(exchange)
                    it.tvTitleExchange.setOnClickListener(exchange)
                    it.ivRightArrowExchange.setOnClickListener(exchange)

                    it.ivWithdraw.setOnClickListener(withdraw)
                    it.tvSubTitleWithdraw.setOnClickListener(withdraw)
                    it.tvTitleWithdraw.setOnClickListener(withdraw)
                    it.ivRightArrowWithdraw.setOnClickListener(withdraw)

                    it.ivDeposit.setOnClickListener(deposit)
                    it.tvSubTitleDeposit.setOnClickListener(deposit)
                    it.tvTitleDeposit.setOnClickListener(deposit)
                    it.ivRightArrowDeposit.setOnClickListener(deposit)

                    it.ivDepositAsset.setOnClickListener(depositAsset)
                    it.tvSubTitleDepositAsset.setOnClickListener(depositAsset)
                    it.tvTitleDepositAsset.setOnClickListener(depositAsset)
                    it.ivRightArrowDepositAsset.setOnClickListener(depositAsset)
                }


            }

            SheetType.YOUR_ACTIVITY_ON_LYBER.title(requireContext()) -> bottomSheetAdapter.setList(
                SheetType.YOUR_ACTIVITY_ON_LYBER.getData(requireContext())
            )

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        _recyclerBinding = null
    }

    override fun onStart() {
        super.onStart()
        if (tag == SheetType.COMPLETE_ACCOUNT.title(requireContext()))
            behaviour.peekHeight = 100F.toPx(requireContext())
    }

    enum class SheetType {
        COMPLETE_ACCOUNT {
            override fun getData(context: Context) = emptyList<DataBottomSheet>()
            override fun title(context: Context): String {
                return context.getString(R.string.complete_your_account)
            }
//            override val title: String
//                get() ="Complete your account"
        },
        CRYPTO_EXP {
            override fun getData(context: Context) = mutableListOf<DataBottomSheet>().apply {
                add(
                    DataBottomSheet(
                        R.string.i_have_never_invested,
                        context.getString(R.string.i_have_never_invested)
                    )
                )
                add(
                    DataBottomSheet(
                        R.string.less_than_1000,
                        context.getString(R.string.less_than_1000)
                    )
                )
                add(
                    DataBottomSheet(
                        R.string.between_1000_and_9999,
                        context.getString(R.string.between_1000_and_9999)
                    )
                )
                add(
                    DataBottomSheet(
                        R.string.between_10000_and_99999,
                        context.getString(R.string.between_10000_and_99999)
                    )
                )
                add(
                    DataBottomSheet(
                        R.string.greater_than_100000,
                        context.getString(R.string.greater_than_100000)
                    )
                )
            }

            override fun title(context: Context): String {
                return context.getString(R.string.bottom_sheet_cypto_exp_title)
            }
//            override val title: String
//                get() = "What’s your investment experience with cryptos ?"
        },
        SOURCE_OF_INCOME {
            override fun getData(context: Context) = mutableListOf<DataBottomSheet>().apply {
                add(DataBottomSheet(R.string.salary, context.getString(R.string.salary)))
                add(DataBottomSheet(R.string.investments, context.getString(R.string.investments)))
                add(DataBottomSheet(R.string.savings, context.getString(R.string.savings)))
                add(DataBottomSheet(R.string.inheritance, context.getString(R.string.inheritance)))
                add(DataBottomSheet(R.string.credit_loan, context.getString(R.string.credit_loan)))
                add(
                    DataBottomSheet(
                        R.string.family_others,
                        context.getString(R.string.family_others)
                    )
                )
            }

            override fun title(context: Context): String {
                return context.getString(R.string.what_your_source_of_income)
            }
//            override val title: String
//                get() = "What’s your source of income ?"
        },
        WORK_INDUSTRY {
            override fun getData(context: Context) = mutableListOf<DataBottomSheet>().apply {
                add(DataBottomSheet(R.string.agriculture, context.getString(R.string.agriculture)))
                add(DataBottomSheet(R.string.arts_media, context.getString(R.string.arts_media)))
                add(
                    DataBottomSheet(
                        R.string.banking_finance_insurance,
                        context.getString(R.string.banking_finance_insurance)
                    )
                )
                add(
                    DataBottomSheet(
                        R.string.business_services_consulting,
                        context.getString(R.string.business_services_consulting)
                    )
                )
                add(DataBottomSheet(R.string.building, context.getString(R.string.building)))
                add(
                    DataBottomSheet(
                        R.string.education_training_research,
                        context.getString(R.string.education_training_research)
                    )
                )
                add(
                    DataBottomSheet(
                        R.string.energy_environment,
                        context.getString(R.string.energy_environment)
                    )
                )
                add(
                    DataBottomSheet(
                        (R.string.government_administration_social),
                        context.getString(R.string.government_administration_social)
                    )
                )
                add(
                    DataBottomSheet(
                        R.string.health_medical_pharmaceutical,
                        context.getString(R.string.health_medical_pharmaceutical)
                    )
                )
                add(
                    DataBottomSheet(
                        R.string.hospitality_tourism_catering,
                        context.getString(R.string.hospitality_tourism_catering)
                    )
                )
                add(DataBottomSheet(R.string.it, context.getString(R.string.it)))
                add(
                    DataBottomSheet(
                        R.string.manufacturing_mettalurgy,
                        context.getString(R.string.manufacturing_mettalurgy)
                    )
                )
                add(
                    DataBottomSheet(
                        24,
                        context.getString(R.string.marketing_advertising_public_relations)
                    )
                )
                add(
                    DataBottomSheet(
                        25,
                        context.getString(R.string.real_estate_property_management)
                    )
                )
                add(
                    DataBottomSheet(
                        R.string.retail_ecommerce,
                        context.getString(R.string.retail_ecommerce)
                    )
                )
                add(
                    DataBottomSheet(
                        R.string.sports_leisure_entertainment,
                        context.getString(R.string.sports_leisure_entertainment)
                    )
                )
                add(
                    DataBottomSheet(
                        R.string.textile_fashion_apparel,
                        context.getString(R.string.textile_fashion_apparel)
                    )
                )
                add(
                    DataBottomSheet(
                        R.string.transport_logistics_wholesale,
                        context.getString(R.string.transport_logistics_wholesale)
                    )
                )
            }

            override fun title(context: Context): String {
                return context.getString(R.string.whats_your_work_industry)
            }
//            override val title: String
//                get() = "What’s your work industry ?"
        },
        ANNUAL_INCOME {
            override fun getData(context: Context) = mutableListOf<DataBottomSheet>().apply {
                add(
                    DataBottomSheet(
                        R.string.less_than_500,
                        context.getString(R.string.less_than_500)
                    )
                )
                add(DataBottomSheet(R.string.five00_1000, context.getString(R.string.five00_1000)))
                add(DataBottomSheet(R.string.one001_1500, context.getString(R.string.one001_1500)))
                add(DataBottomSheet(R.string.one501_2000, context.getString(R.string.one501_2000)))
                add(DataBottomSheet(R.string.two001_3000, context.getString(R.string.two001_3000)))
                add(DataBottomSheet(R.string.over_3001, context.getString(R.string.over_3001)))
            }

            override fun title(context: Context): String {
                return context.getString(R.string.what_salary_range_you_fall_into)
            }
//            override val title: String
//                get() = "What salary range do you fall into ?"
        },
        ADD_AN_ASSET {
            override fun getData(context: Context): List<DataBottomSheet> = emptyList()

            //            override val title: String = "Add an Asset"
            override fun title(context: Context): String {
                return context.getString(R.string.add_an_asset)
            }
        },
        WITHDRAW_EXCHANGE {
            override fun getData(context: Context): List<DataBottomSheet> = emptyList()

            //            override val title: String = "Withdraw or exchange"
            override fun title(context: Context): String {
                return context.getString(R.string.withdraw_or_exchange)
            }
        },
        PERSONAL_ASSETS {
            override fun getData(context: Context): List<DataBottomSheet> =
                mutableListOf<DataBottomSheet>().apply {
                    add(
                        DataBottomSheet(
                            R.string.two_assets,
                            context.getString(R.string.two_assets)
                        )
                    )
                    add(
                        DataBottomSheet(
                            R.string.two2_assets,
                            context.getString(R.string.two2_assets)
                        )
                    )
                    add(
                        DataBottomSheet(
                            R.string.one28_assets,
                            context.getString(R.string.one28_assets)
                        )
                    )
                    add(
                        DataBottomSheet(
                            R.string.three19_assets,
                            context.getString(R.string.three19_assets)
                        )
                    )
                    add(
                        DataBottomSheet(
                            R.string.four64_assets,
                            context.getString(R.string.four64_assets)
                        )
                    )
                    add(
                        DataBottomSheet(
                            R.string.four65_assets,
                            context.getString(R.string.four65_assets)
                        )
                    )
                }

            override fun title(context: Context): String {
                return context.getString(R.string.how_many_personal_asset_you_have)
            }
//            override val title: String
//                get() = "How many personal asset you have ?"
        },
        YOUR_ACTIVITY_ON_LYBER {
            override fun getData(context: Context): List<DataBottomSheet> =
                mutableListOf<DataBottomSheet>().apply {
                    add(
                        DataBottomSheet(
                            R.string.buy_sell_digital_assets,
                            context.getString(R.string.buy_sell_digital_assets)
                        )
                    )
                    add(
                        DataBottomSheet(
                            R.string.save_money,
                            context.getString(R.string.save_money)
                        )
                    )
                    add(
                        DataBottomSheet(
                            R.string.store_my_digital_assets,
                            context.getString(R.string.store_my_digital_assets)
                        )
                    )
                }

            override fun title(context: Context): String {
                return context.getString(R.string.what_do_you_plan_to_do)
            }
//            override val title: String
//            get() = "What do you plan to mainly do?"
        };

        abstract fun getData(context: Context): List<DataBottomSheet>

        //        abstract val title: String
        abstract fun title(context: Context): String
    }


    /*    private val cryptoExp = mutableListOf<DataBottomSheet>().apply {
            add(DataBottomSheet("I have never invested"))
            add(DataBottomSheet("< 1 000€"))
            add(DataBottomSheet("Between 1 000€ and 9 999€"))
            add(DataBottomSheet("Between 10 000€ and 99 999€"))
            add(DataBottomSheet("> 100 000€"))
        }

        private val sourceOfIncome = mutableListOf<DataBottomSheet>().apply {
            add(DataBottomSheet("Salary"))
            add(DataBottomSheet("Investments"))
            add(DataBottomSheet("Savings"))
            add(DataBottomSheet("Inheritance"))
            add(DataBottomSheet("Credit/loan"))
            add(DataBottomSheet("Family or others"))
        }

        private val workIndustry = mutableListOf<DataBottomSheet>().apply {
            add(DataBottomSheet("Agriculture"))
            add(DataBottomSheet("Arts & Media"))
            add(DataBottomSheet("Casinos & games"))
            add(DataBottomSheet("Building"))
            add(DataBottomSheet("Defense"))
            add(DataBottomSheet("Entertainment"))
            add(DataBottomSheet("Education"))
            add(DataBottomSheet("Energy"))
            add(DataBottomSheet("Media & TV"))
            add(DataBottomSheet("New technologies"))
        }

        private val annualIncomeData = mutableListOf<DataBottomSheet>().apply {
            add(DataBottomSheet("Less than 800€/month"))
            add(DataBottomSheet("801 - 1100€/month"))
            add(DataBottomSheet("1101 - 2000€/month"))
            add(DataBottomSheet("2001 - 4000€/month"))
            add(DataBottomSheet("4000 - 10 000€/month"))
            add(DataBottomSheet("Over 10 001€/month"))
        }*/


    override fun itemClicked(item: DataBottomSheet) {
        listenItemClicked(tag.toString(), item)
        dismiss()
    }

}