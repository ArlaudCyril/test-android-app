package com.Lyber.dev.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.Lyber.dev.models.AddedAsset
import com.Lyber.dev.models.AssetBaseData
import com.Lyber.dev.models.AssetDetailBaseData
import com.Lyber.dev.models.Balance
import com.Lyber.dev.models.ChooseAssets
import com.Lyber.dev.models.NetworkDeposit
import com.Lyber.dev.models.PersonalDataResponse
import com.Lyber.dev.models.PriceServiceResume
import com.Lyber.dev.models.RIBData
import com.Lyber.dev.models.Strategy
import com.Lyber.dev.models.WithdrawAddress
import com.Lyber.dev.ui.activities.SplashActivity
import com.Lyber.dev.utils.CommonMethods
import com.google.android.gms.tasks.Task
import com.google.android.play.core.integrity.StandardIntegrityManager
import org.json.JSONObject

class PortfolioViewModel : NetworkViewModel() {

    private var _selectedAsset: AssetBaseData? = null
    var selectedAsset
        get() = _selectedAsset
        set(value) {
            _selectedAsset = value
        }
    private var _withdrawAddress: WithdrawAddress? = null
    var withdrawAddress
        get() = _withdrawAddress
        set(value) {
            _withdrawAddress = value
        }
    private var _ribAddress: RIBData? = null

    var ribDataAddress
        get() = _ribAddress
        set(value) {
            _ribAddress = value
        }

    private var _selectedAssetPriceResume: PriceServiceResume? = null
    var selectedAssetPriceResume
        get() = _selectedAssetPriceResume
        set(value) {
            _selectedAssetPriceResume = value
        }

    private var _selectedAssetDetail: AssetDetailBaseData? = null
    var selectedAssetDetail
        get() = _selectedAssetDetail
        set(value) {
            _selectedAssetDetail = value
        }
    private var _selectedNetworkDeposit: NetworkDeposit? = null
    var selectedNetworkDeposit
        get() = _selectedNetworkDeposit
        set(value) {
            _selectedNetworkDeposit = value
        }

    var selectedBalance: Balance? = null

    private var _personalData: PersonalDataResponse? = null
    var personalData
        get() = _personalData
        set(value) {
            _personalData = value
        }

    private var _identityDocumentImage: String? = null
    var identityDocument
        get() = _identityDocumentImage
        set(value) {
            _identityDocumentImage = value
        }

    private var _selfieImage: String? = null
    var selfieImage
        get() = _selfieImage
        set(value) {
            _selfieImage = value
        }


    private var _identityVerificationInitiated: Boolean = false
    var verificationInitiated
        get() = _identityVerificationInitiated
        set(value) {
            _identityVerificationInitiated = value
        }

    private var _verifyIdentity: Boolean = false
    var identityVerified
        get() = _verifyIdentity
        set(value) {
            _verifyIdentity = value
        }


    private var _portfolioBuildUpState: Int = 0
    var portfolioState
        get() = when {
            personalData == null -> 0
            !verificationInitiated -> 1
            verificationInitiated -> if (identityVerified) 3 else 2
            else -> -1
        }
        set(value) {
            _portfolioBuildUpState = value
        }

    /* screen count */
    // this fragment manages multiple screens
    // this variable helps to identify the particular screen
    // 0-> Home Screen, 1-> Asset Detail Screen 2-> Asset Breakdown screen

    var chosenAssets: PriceServiceResume? = null

    private var _screenCount: Int = 0
    var screenCount
        get() = _screenCount
        set(value) {
            _screenCount = value
        }

    /* to check whether the live data instance contains data or not */


    private var _totalPortfolio: Double = 0.0
    var totalPortfolio
        get() = _totalPortfolio
        set(value) {
            _totalPortfolio = value
        }


    /* strategy viewModel migration */

    private var _selectedStrategy: Strategy? = null
    var selectedStrategy
        get() = _selectedStrategy
        set(value) {
            _selectedStrategy = value
        }

    private var _selectedOption: String = ""
    var selectedOption
        get() = _selectedOption
        set(value) {
            _selectedOption = value
        }

    private var _selectedFrequency: String = ""
    var selectedFrequency
        get() = _selectedFrequency
        set(value) {
            _selectedFrequency = value
        }

    private var _amount: String = ""
    var amount
        get() = _amount
        set(value) {
            _amount = value
        }

    private var _assetAmount: String = ""
    var assetAmount: String
        get() = _assetAmount
        set(value) {
            _assetAmount = value
        }


    private var _exchangeAssetFrom: String? = null
    var exchangeAssetFrom
        get() = _exchangeAssetFrom
        set(value) {
            _exchangeAssetFrom = value
        }

    private var _exchangeAssetTo: String? = null
    var exchangeAssetTo
        get() = _exchangeAssetTo
        set(value) {
            _exchangeAssetTo = value
        }


    var allMyPortfolio: String = ""

    private var _withdrawAsset: AssetBaseData? = null
    var withdrawAsset
        get() = _withdrawAsset
        set(value) {
            _withdrawAsset = value
        }


    /* investment strategy view model migration */

    private val _strategyPosition = MutableLiveData<Int>()
    val strategyPositionSelected
        get() = _strategyPosition

    private val _addedAssets: MutableList<AddedAsset> = mutableListOf()
    val addedAsset get() = _addedAssets


    /* apis */

    //    fun chooseStrategy() {
//        selectedStrategy?.let {
//            chooseStrategy(it)
//        }
//    }
    fun startKyc(token: String) {
        startKYC(token)
    }

    fun buildOwnStrategy(strategyName: String) {
        val jsonObject = JSONObject()
        val list = arrayListOf<ChooseAssets>()
        var total = 0
        for (i in addedAsset) {
            total += i.allocation.toInt()
            list.add(
                ChooseAssets(
                    i.addAsset.id,
                    i.allocation.toInt()
                )
            )
        }
        jsonObject.put("bundle", list)
        jsonObject.put("strategyType", "MultiAsset")
        jsonObject.put("strategyName", strategyName)
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
            buildOwnStrategy(strategyName, addedAsset, response.token())

        }?.addOnFailureListener { exception ->
            Log.d("token", "${exception}")
        }
    }

    fun editOwnStrategy(strategyName: String) {
        val jsonObject = JSONObject()
        val list = arrayListOf<ChooseAssets>()
        var total = 0
        for (i in addedAsset) {
            total += i.allocation.toInt()
            list.add(
                ChooseAssets(
                    i.addAsset.id,
                    i.allocation.toInt()
                )
            )
        }
        jsonObject.put("bundle", list)
        jsonObject.put("strategyType", "MultiAsset")
        jsonObject.put("strategyName", strategyName)
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
            editOwnStrategy(strategyName, addedAsset, response.token())

        }?.addOnFailureListener { exception ->
            Log.d("token", "${exception}")
        }
    }

    fun getBalance(token: String) {
        getBalanceApi(token)
    }

    fun getExportOperations(date: String, token: String) {
        getExportOperation(date, token)
    }


    fun contactSupport(msg: String, token: String) {
        sendMsgToSupport(msg, token)
    }


}