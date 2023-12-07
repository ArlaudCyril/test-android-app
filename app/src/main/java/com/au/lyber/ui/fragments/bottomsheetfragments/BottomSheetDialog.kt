package com.au.lyber.ui.fragments.bottomsheetfragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.au.lyber.R
import com.au.lyber.databinding.BottomSheetPortfolioBinding
import com.au.lyber.databinding.BottomSheetRecyclerViewBinding
import com.au.lyber.databinding.BottomSheetWithdrawExchangeBinding
import com.au.lyber.models.AssetBaseData
import com.au.lyber.models.DataBottomSheet
import com.au.lyber.ui.adapters.BottomSheetAdapter
import com.au.lyber.utils.CommonMethods.Companion.getViewModel
import com.au.lyber.utils.CommonMethods.Companion.gone
import com.au.lyber.utils.CommonMethods.Companion.toPx
import com.au.lyber.utils.Constants.LYBER_ASSETS
import com.au.lyber.ui.portfolio.viewModel.PortfolioViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

open class BottomSheetDialog(
    private val listenItemClicked: (tag: String, item: String) -> Unit = { _, _ -> },
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
            SheetType.COMPLETE_ACCOUNT.title -> {
                _binding = BottomSheetPortfolioBinding.inflate(layoutInflater)
                binding.ivDropDown.setOnClickListener {
                    if (behaviour.state != BottomSheetBehavior.STATE_EXPANDED)
                        behaviour.state = BottomSheetBehavior.STATE_EXPANDED
                    else behaviour.state = BottomSheetBehavior.STATE_COLLAPSED
                }
                binding.root
            }
            SheetType.WITHDRAW_EXCHANGE.title -> {
                _withdrawBinding = BottomSheetWithdrawExchangeBinding.inflate(layoutInflater)

                withdrawBinding.root
            }
            else -> {
                bottomSheetAdapter = when (tag) {
                    SheetType.CRYPTO_EXP.title -> BottomSheetAdapter(this, SheetType.CRYPTO_EXP ,requireActivity())
                    SheetType.SOURCE_OF_INCOME.title -> BottomSheetAdapter(
                        this,
                        SheetType.SOURCE_OF_INCOME
                    ,requireActivity())
                    SheetType.WORK_INDUSTRY.title -> BottomSheetAdapter(
                        this,
                        SheetType.WORK_INDUSTRY
                        ,requireActivity())
                    SheetType.ANNUAL_INCOME.title -> BottomSheetAdapter(
                        this,
                        SheetType.ANNUAL_INCOME
                        ,requireActivity())
                    else -> BottomSheetAdapter(this, SheetType.YOUR_ACTIVITY_ON_LYBER ,requireActivity())
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

        if (tag != SheetType.COMPLETE_ACCOUNT.title)
            behaviour.state = BottomSheetBehavior.STATE_EXPANDED

        when (tag) {

            SheetType.COMPLETE_ACCOUNT.title -> {}

            SheetType.CRYPTO_EXP.title -> bottomSheetAdapter.setList(SheetType.CRYPTO_EXP.getData())

            SheetType.SOURCE_OF_INCOME.title -> bottomSheetAdapter.setList(SheetType.SOURCE_OF_INCOME.getData())

            SheetType.WORK_INDUSTRY.title -> bottomSheetAdapter.setList(SheetType.WORK_INDUSTRY.getData())

            SheetType.ANNUAL_INCOME.title -> bottomSheetAdapter.setList(SheetType.ANNUAL_INCOME.getData())

            SheetType.ADD_AN_ASSET.title -> {}

            SheetType.WITHDRAW_EXCHANGE.title -> {

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
                            tvTitleDepositAsset.text = "Sell ${it.id.uppercase()}"
//                        tvSubTitleDepositAsset.text =
//                            "Sell ${it.asset_id.uppercase()} from your wallet"
                        }

                    }

                withdrawBinding.ivTopAction.setOnClickListener { dismiss() }

                val withdraw = View.OnClickListener {
                    listenItemClicked(tag ?: "", "withdraw")
                    dismiss()
                }

                val exchange = View.OnClickListener {
                    listenItemClicked(tag ?: "", "exchange")
                    dismiss()
                }

                val deposit = View.OnClickListener {
                    listenItemClicked(tag ?: "", "deposit")
                    dismiss()
                }

                val depositAsset = View.OnClickListener {
                    listenItemClicked(tag ?: "", "depositAnAsset")
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

            SheetType.YOUR_ACTIVITY_ON_LYBER.title -> bottomSheetAdapter.setList(SheetType.YOUR_ACTIVITY_ON_LYBER.getData())

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        _recyclerBinding = null
    }

    override fun onStart() {
        super.onStart()
        if (tag == SheetType.COMPLETE_ACCOUNT.title)
            behaviour.peekHeight = 100F.toPx(requireContext())
    }

    enum class SheetType {
        COMPLETE_ACCOUNT {
            override fun getData() = emptyList<DataBottomSheet>()
            override val title: String
                get() = "Complete your account"
        },
        CRYPTO_EXP {
            override fun getData() = mutableListOf<DataBottomSheet>().apply {
                add(DataBottomSheet("I have never invested"))
                add(DataBottomSheet("< 1 000€"))
                add(DataBottomSheet("Between 1 000€ and 9 999€"))
                add(DataBottomSheet("Between 10 000€ and 99 999€"))
                add(DataBottomSheet("> 100 000€"))
            }

            override val title: String
                get() = "What’s your investment experience with cryptos ?"
        },
        SOURCE_OF_INCOME {
            override fun getData() = mutableListOf<DataBottomSheet>().apply {
                add(DataBottomSheet("Salary"))
                add(DataBottomSheet("Investments"))
                add(DataBottomSheet("Savings"))
                add(DataBottomSheet("Inheritance"))
                add(DataBottomSheet("Credit/loan"))
                add(DataBottomSheet("Family or others"))
            }

            override val title: String
                get() = "What’s your source of income ?"
        },
        WORK_INDUSTRY {
            override fun getData() = mutableListOf<DataBottomSheet>().apply {
                add(DataBottomSheet("Agriculture, Agribusiness & Natural Resources"))
                add(DataBottomSheet("Art, Entertainment & Media"))
                add(DataBottomSheet("Banking, Finance & Insurance"))
                add(DataBottomSheet("Business Services & Consulting"))
                add(DataBottomSheet("Construction, Engineering & Public Works"))
                add(DataBottomSheet("Education, Training & Research"))
                add(DataBottomSheet("Energy & Environment"))
                add(DataBottomSheet("Government, Public Administration & Social Services"))
                add(DataBottomSheet("Health, Medical & Pharmaceutical"))
                add(DataBottomSheet("Hospitality, Tourism & Catering"))
                add(DataBottomSheet("IT"))
                add(DataBottomSheet("Manufacturing & Metallurgy Industry"))
                add(DataBottomSheet("Marketing, Advertising & Public Relations"))
                add(DataBottomSheet("Real Estate & Property Management"))
                add(DataBottomSheet("Retail & E-commerce"))
                add(DataBottomSheet("Sports, Leisure & Interactive Entertainment"))
                add(DataBottomSheet("Textile, Fashion & Apparel"))
                add(DataBottomSheet("Transport, Logistics & Wholesale"))
            }

            override val title: String
                get() = "What’s your work industry ?"
        },
        ANNUAL_INCOME {
            override fun getData() = mutableListOf<DataBottomSheet>().apply {
                add(DataBottomSheet("Less than 500"))
                add(DataBottomSheet("500-1000"))
                add(DataBottomSheet("1001-1500"))
                add(DataBottomSheet("1501-2000"))
                add(DataBottomSheet("2001-3000"))
                add(DataBottomSheet("Over3001"))
            }

            override val title: String
                get() = "What salary range do you fall into ?"
        },
        ADD_AN_ASSET {
            override fun getData(): List<DataBottomSheet> = emptyList()
            override val title: String = "Add an Asset"
        },
        WITHDRAW_EXCHANGE {
            override fun getData(): List<DataBottomSheet> = emptyList()
            override val title: String = "Withdraw or exchange"
        },
        PERSONAL_ASSETS {
            override fun getData(): List<DataBottomSheet> = mutableListOf<DataBottomSheet>().apply {
                add(DataBottomSheet("0-2"))
                add(DataBottomSheet("3-22"))
                add(DataBottomSheet("23-128"))
                add(DataBottomSheet("129-319"))
                add(DataBottomSheet("320-464"))
                add(DataBottomSheet("465+"))
            }

            override val title: String
                get() = "How many personal asset you have ?"
        },
        YOUR_ACTIVITY_ON_LYBER {
            override fun getData(): List<DataBottomSheet> = mutableListOf<DataBottomSheet>().apply {
                add(DataBottomSheet("Buy and sell digital assets"))
                add(DataBottomSheet("Save money"))
                add(DataBottomSheet("Store my digital assets"))
            }

            override val title: String
            get() = "What do you plan to mainly do?"
        };

        abstract fun getData(): List<DataBottomSheet>
        abstract val title: String
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
        listenItemClicked(tag.toString(), item.title)
        dismiss()
    }

}