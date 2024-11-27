package com.Lyber.dev.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.Lyber.dev.models.ActiveStrategyResponse
import com.Lyber.dev.models.ActivityLogs
import com.Lyber.dev.models.AddedAsset
import com.Lyber.dev.models.AssetBaseDataResponse
import com.Lyber.dev.models.AssetDetailBaseDataResponse
import com.Lyber.dev.models.BalanceResponse
import com.Lyber.dev.models.BooleanResponse
import com.Lyber.dev.models.ChangePasswordData
import com.Lyber.dev.models.ChooseAssets
import com.Lyber.dev.models.CoinsResponse
import com.Lyber.dev.models.CommonResponse
import com.Lyber.dev.models.CurrentPriceResponse
import com.Lyber.dev.models.Duration
import com.Lyber.dev.models.ExchangeListingResponse
import com.Lyber.dev.models.ExportResponse
import com.Lyber.dev.models.GetAddress
import com.Lyber.dev.models.GetAssetsResponse
import com.Lyber.dev.models.GetQuoteResponse
import com.Lyber.dev.models.GetUserResponse
import com.Lyber.dev.models.KYCResponse
import com.Lyber.dev.models.MessageResponse
import com.Lyber.dev.models.MessageResponsePause
import com.Lyber.dev.models.NetworkResponse
import com.Lyber.dev.models.NetworksResponse
import com.Lyber.dev.models.NewsResponse
import com.Lyber.dev.models.OneTimeStrategyData
import com.Lyber.dev.models.OrderResponseData
import com.Lyber.dev.models.PriceGraphResponse
import com.Lyber.dev.models.PriceResponse
import com.Lyber.dev.models.PriceResumeByIdResponse
import com.Lyber.dev.models.PriceServiceResume
import com.Lyber.dev.models.QrCodeResponse
import com.Lyber.dev.models.RIBResponse
import com.Lyber.dev.models.RecurringInvestmentDetailResponse
import com.Lyber.dev.models.SetPhoneResponse
import com.Lyber.dev.models.SignURlResponse
import com.Lyber.dev.models.StrategiesResponse
import com.Lyber.dev.models.Strategy
import com.Lyber.dev.models.StrategyExecution
import com.Lyber.dev.models.TransactionList
import com.Lyber.dev.models.TransactionResponse
import com.Lyber.dev.models.UpdateAuthenticateResponse
import com.Lyber.dev.models.UploadResponse
import com.Lyber.dev.models.UserByPhoneResponse
import com.Lyber.dev.models.UserChallengeResponse
import com.Lyber.dev.models.UserLoginResponse
import com.Lyber.dev.models.WalletHistoryResponse
import com.Lyber.dev.models.WhitelistingResponse
import com.Lyber.dev.models.WithdrawEuroFee
import com.Lyber.dev.models.WithdrawalAddress
import com.Lyber.dev.models.res
import com.Lyber.dev.network.RestClient
import com.Lyber.dev.utils.App
import com.Lyber.dev.utils.CommonMethods
import com.Lyber.dev.utils.Constants
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.ResponseBody
import java.io.File

open class NetworkViewModel : ViewModel() {

    private var _listener: RestClient.OnRetrofitError? = null
    var listener
        get() = _listener
        set(value) {
            _listener = value
        }

    data class ErrorResponse(val errorCode: Int?, val responseBody: ResponseBody?)

    // In your ViewModel
    private val _errorResponse = MutableLiveData<ErrorResponse?>()
    val errorResponse: LiveData<ErrorResponse?> get() = _errorResponse

    private var _commonResponse = MutableLiveData<CommonResponse>()
    val commonResponse get() = _commonResponse


    private val exceptionHandler = CoroutineExceptionHandler { coroutineContext, exception ->
        Log.d("exceptionHandler", exception.message ?: "")

        /* handle exception */
    }

    private var _educationStrategyResponse = MutableLiveData<MessageResponse>()
    val educationStrategyResponse get() = _educationStrategyResponse

    private var _getStrategiesResponse = MutableLiveData<StrategiesResponse>()
    val getStrategiesResponse get() = _getStrategiesResponse

    private var _selectedStrategyResponse = MutableLiveData<MessageResponse>()
    val selectedStrategyResponse get() = _selectedStrategyResponse

    private var _buildStrategyResponse = MutableLiveData<MessageResponse>()
    val buildStrategyResponse get() = _buildStrategyResponse

    private var _trendingCoinResponse = MutableLiveData<CoinsResponse>()
    val trendingCoinResponse get() = _trendingCoinResponse

    private var _investSingleAssetResponse = MutableLiveData<MessageResponse>()
    val investSingleAssetResponse get() = _investSingleAssetResponse

    private var _investStrategyResponse = MutableLiveData<MessageResponse>()
    val investStrategyResponse get() = _investStrategyResponse
    private var _pauseStrategyResponse = MutableLiveData<MessageResponsePause>()
    val pauseStrategyResponse get() = _pauseStrategyResponse

    private var _withdrawResponse = MutableLiveData<MessageResponse>()
    val withdrawResponse get() = _withdrawResponse

    private var _exchangeResponse = MutableLiveData<MessageResponse>()
    val exchangeResponse get() = _exchangeResponse

    private var _getQuoteResponse = MutableLiveData<GetQuoteResponse>()
    val getQuoteResponse get() = _getQuoteResponse

    private var _logoutResponse = MutableLiveData<BooleanResponse>()
    val logoutResponse get() = _logoutResponse

    private var _transactionResponse = MutableLiveData<TransactionResponse>()
    val transactionResponse get() = _transactionResponse

    private var _verifyPhoneForPinResponse = MutableLiveData<MessageResponse>()
    val verifyPhoneForPinResponse get() = _verifyPhoneForPinResponse

    private var _updatePinResponse = MutableLiveData<MessageResponse>()
    val updatePinResponse get() = _updatePinResponse

     private var _addBankResponse = MutableLiveData<MessageResponse>()
    val addBankAccount get() = _addBankResponse

    private var _verifyStrongAuthentication = MutableLiveData<MessageResponse>()

    private var _networksResponse = MutableLiveData<NetworksResponse>()
    val networkResponse get() = _networksResponse

    private var _networksResponseSingle = MutableLiveData<NetworkResponse>()
    val singleNetworkResponse get() = _networksResponseSingle

    private var _exchangeListingResponse = MutableLiveData<ExchangeListingResponse>()

    private var _addWhitelistResponse = MutableLiveData<MessageResponse>()
    val addWhitelistResponse get() = _addWhitelistResponse
    private var _updateUserInfoResponse = MutableLiveData<MessageResponse>()
    val updateUserInfoResponse get() = _updateUserInfoResponse

    private var _getWhiteListing = MutableLiveData<WhitelistingResponse>()
    val getWhiteListing get() = _getWhiteListing

    private var _searchWhitelisting = MutableLiveData<WhitelistingResponse>()

    private var _uploadResponse = MutableLiveData<UploadResponse>()
    val uploadResponse get() = _uploadResponse

    private var _deleteWhiteListResponse = MutableLiveData<MessageResponse>()
    val deleteWhiteList get() = _deleteWhiteListResponse

    private var _updateWhiteListResponse = MutableLiveData<MessageResponse>()
    val updateWhiteList get() = _updateWhiteListResponse

    private var _updateUserResponse = MutableLiveData<MessageResponse>()
    val updateUser get() = _updateUserResponse

    private val _assetsToChoose = MutableLiveData<GetAssetsResponse>()
    val assetsToChoose get() = _assetsToChoose

    private val _priceServiceResumes = MutableLiveData<ArrayList<PriceServiceResume>>()
    val priceServiceResumes get() = _priceServiceResumes

    private val _allAssets = MutableLiveData<AssetBaseDataResponse>()
    val allAssets get() = _allAssets
    private val _withdrawalAddresses = MutableLiveData<WithdrawalAddress>()
    val withdrawalAddresses get() = _withdrawalAddresses

    private val _getAssetDetail = MutableLiveData<AssetDetailBaseDataResponse>()
    private val _getAddress = MutableLiveData<GetAddress>()
    val getAddress get() = _getAddress
    val getAssetDetail get() = _getAssetDetail

    private val _recurringInvestmentDetail =
        MutableLiveData<RecurringInvestmentDetailResponse>()
    val recurringInvestmentDetail get() = _recurringInvestmentDetail

    private val _cancelRecurringInvestment = MutableLiveData<MessageResponse>()
    val cancelRecurringInvestment get() = _cancelRecurringInvestment

    private val _priceGraphResponse = MutableLiveData<PriceGraphResponse>()

    private val _withdrawFiatResponse = MutableLiveData<MessageResponse>()
    val withdrawFiatResponse get() = _withdrawFiatResponse


    private val _userChallengeResponse = MutableLiveData<UserChallengeResponse>()
    val userChallengeResponse get() = _userChallengeResponse

    private val _userLoginResponse = MutableLiveData<UserLoginResponse>()
    val userLoginResponse get() = _userLoginResponse

    private val _setPhoneResponse = MutableLiveData<SetPhoneResponse>()
    val setPhoneResponse get() = _setPhoneResponse

    private val _verifyPhoneResponse = MutableLiveData<MessageResponse>()
    val verifyPhoneResponse get() = _verifyPhoneResponse

    private val _setUserInfoResponse = MutableLiveData<MessageResponse>()
    val setUserInfoResponse get() = _setUserInfoResponse

    private val _setUpEmailResponse = MutableLiveData<MessageResponse>()
    val setUpEmailResponse get() = _setUpEmailResponse

    private val _verifyEmailResponse = MutableLiveData<Any>()
    val verifyEmailResponse get() = _verifyEmailResponse

    private val _getUserResponse = MutableLiveData<GetUserResponse>()
    val getUserResponse get() = _getUserResponse

    private val _setUserAddressResponse = MutableLiveData<MessageResponse>()
    val setUserAddressResponse get() = _setUserAddressResponse

    private val _finishRegistrationResponse = MutableLiveData<UserLoginResponse>()
    val finishRegistrationResponse get() = _finishRegistrationResponse

    private val _setInvestmentExpResponse = MutableLiveData<MessageResponse>()
    val setInvestmentExpResponse get() = _setInvestmentExpResponse

    private val _newsResponse = MutableLiveData<NewsResponse>()
    val newsResponse get() = _newsResponse

    private val _priceResponse = MutableLiveData<PriceResponse>()
    val priceResponse get() = _priceResponse

    private val _balanceResponse = MutableLiveData<BalanceResponse>()
    val balanceResponse get() = _balanceResponse
    private val _kycResponse = MutableLiveData<KYCResponse>()
    val kycResponse get() = _kycResponse

    private val _kycResponseIdentity = MutableLiveData<KYCResponse>()
    val kycResponseIdentity get() = _kycResponseIdentity


    private var _msgResponse: MutableLiveData<BooleanResponse> = MutableLiveData()
    val msgResponse get() = _msgResponse

    private val _getOrderResponse = MutableLiveData<OrderResponseData>()
    val orderResponse get() = _getOrderResponse

    private var _oneTimeStrategyDataResponse = MutableLiveData<OneTimeStrategyData>()
    val oneTimeStrategyDataResponse get() = _oneTimeStrategyDataResponse
    private var _strategyExecutionResponse = MutableLiveData<StrategyExecution>()
    val strategyExecutionResponse get() = _strategyExecutionResponse

    private var _booleanResponse: MutableLiveData<BooleanResponse> = MutableLiveData()
    val booleanResponse get() = _booleanResponse
    val notificationResponse get() = _booleanResponse

    private var _updateAuthenticateResponse: MutableLiveData<UpdateAuthenticateResponse> =
        MutableLiveData()
    val updateAuthenticateResponse get() = _updateAuthenticateResponse
    private var _qrCodeResponse: MutableLiveData<QrCodeResponse> = MutableLiveData()
    val qrCodeResponse get() = _qrCodeResponse

    private var _getTransactionListing = MutableLiveData<TransactionList>()
    val getTransactionListingResponse get() = _getTransactionListing


    private var _exportOperationResponse: MutableLiveData<ExportResponse> = MutableLiveData()
    val exportOperationResponse get() = _exportOperationResponse

    private var _resetPassResponse: MutableLiveData<res> = MutableLiveData()
    val resetPasswordResponse get() = _resetPassResponse
    private var _changePassResponse: MutableLiveData<ChangePasswordData> = MutableLiveData()
    val changePasswordData get() = _changePassResponse

    private var _getActivityLogsListing = MutableLiveData<ActivityLogs>()
    val getActivityLogsListingResponse get() = _getActivityLogsListing

    private val _signUrlResponse = MutableLiveData<SignURlResponse>()
    val signUrlResponse get() = _signUrlResponse

    private val _walletHistoryResponse = MutableLiveData<WalletHistoryResponse>()
    val walletHistoryResponse get() = _walletHistoryResponse

    private val _activeStrategyResponse = MutableLiveData<ActiveStrategyResponse>()
    val activeStrategyResponse get() = _activeStrategyResponse
    private val _priceResumeIdResponse = MutableLiveData<PriceResumeByIdResponse>()
    val priceResumeIdResponse get() = _priceResumeIdResponse

    private val _getWalletRibResponse = MutableLiveData<RIBResponse>()
    val walletRibResponse get() = _getWalletRibResponse

    private val _getWithdrawEuroFeeResponse = MutableLiveData<WithdrawEuroFee>()
    val withdrawEuroFeeResponse get() = _getWithdrawEuroFeeResponse
    private val _getUserByPhoneResponse = MutableLiveData<UserByPhoneResponse>()
    val userByPhoneResponse get() = _getUserByPhoneResponse
    private val _getCurrentPriceResponse = MutableLiveData<CurrentPriceResponse>()
    val currentPriceResponse get() = _getCurrentPriceResponse
    fun cancelJob() {

    }

    //API HITTING
    fun educationStrategy() {
        viewModelScope.launch(exceptionHandler) {
            val res = RestClient.get().educationStrategy()
            if (res.isSuccessful)
                _educationStrategyResponse.postValue(res.body())
            else {
                val errorBody = res.errorBody()
                val errorCode =
                    CommonMethods.returnErrorCode(errorBody) // Extract the code from the body if needed
                _listener?.onRetrofitError(errorCode.code, errorCode.error)
            }
        }
    }

    fun getStrategies() {
        viewModelScope.launch(exceptionHandler) {
            val map = HashMap<String, Any>()
            map.put("type", "all")
            val res = RestClient.get().getStrategies(map)
            if (res.isSuccessful)
                _getStrategiesResponse.postValue(res.body())
            else {
                val errorBody = res.errorBody()
                val errorCode =
                    CommonMethods.returnErrorCode(errorBody) // Extract the code from the body if needed
                _listener?.onRetrofitError(errorCode.code, errorCode.error)
            }
        }
    }

    //TODO check it if it is right
    fun chooseStrategy(strategy: Strategy?) {
        strategy?.let {
            val hashMap = hashMapOf<String, Any>()
//            hashMap["assets"] = list
            hashMap["is_own_strategy"] = 0
            hashMap["investment_strategy_id"] = it.ownerUuid
            viewModelScope.launch(exceptionHandler) {
                val res = RestClient.get().chooseStrategy(hashMap)
                if (res.isSuccessful)
                    _selectedStrategyResponse.postValue(res.body())
                else {
                    val errorBody = res.errorBody()
                    val errorCode =
                        CommonMethods.returnErrorCode(errorBody) // Extract the code from the body if needed
                    _listener?.onRetrofitError(errorCode.code, errorCode.error)
                }
            }
        }
    }

    fun editOwnStrategy(strategyName: String, addedAsset: List<AddedAsset>) {
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
        val hashMap = hashMapOf<String, Any>()
//        if (total < 100) {
//            list[0].share = list[0].share + (100 - total)
//        }
        hashMap["bundle"] = list
        hashMap["strategyName"] = strategyName
        hashMap["strategyType"] = "MultiAsset"
//        hashMap["strategyType"] = "SingleAsset"

        viewModelScope.launch(exceptionHandler) {
            val res = RestClient.get().editStrategy(hashMap)
            if (res.isSuccessful)
                _buildStrategyResponse.postValue(res.body())
            else {
                val errorBody = res.errorBody()
                val errorCode =
                    CommonMethods.returnErrorCode(errorBody) // Extract the code from the body if needed
                _listener?.onRetrofitError(errorCode.code, errorCode.error)
            }
        }

    }

    fun buildOwnStrategy(strategyName: String, addedAsset: List<AddedAsset>) {
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
//        if (total < 100) {
//            list[0].share = list[0].share + (100 - total)
//        }
        val hashMap = hashMapOf<String, Any>()
        hashMap["bundle"] = list
        hashMap["strategyType"] = "MultiAsset"
        hashMap["strategyName"] = strategyName

        viewModelScope.launch(exceptionHandler) {
            val res = RestClient.get().chooseStrategy(hashMap)
            if (res.isSuccessful)
                _buildStrategyResponse.postValue(res.body())
            else {
                val errorBody = res.errorBody()
                val errorCode =
                    CommonMethods.returnErrorCode(errorBody) // Extract the code from the body if needed
                _listener?.onRetrofitError(errorCode.code, errorCode.error)
            }
        }

    }

    fun getCoins(
        page: Int = 1,
        limit: Int = 100,
        order: String = Constants.VOLUME_DESC,
        keyword: String = ""
    ) {
        viewModelScope.launch(exceptionHandler) {
            val res = if (keyword.isNotEmpty()) RestClient.get()
                .trendingCoin(order = order, keyword = keyword, limit = limit)
            else RestClient.get().trendingCoins(order = order, limit = limit)

            if (res.isSuccessful)
                _trendingCoinResponse.postValue(res.body())
            else {
                val errorBody = res.errorBody()
                val errorCode =
                    CommonMethods.returnErrorCode(errorBody) // Extract the code from the body if needed
                _listener?.onRetrofitError(errorCode.code, errorCode.error)
            }
        }
    }

    fun investStrategy(
        strategyId: String,
        frequency: String?,
        amount: Number,
        strategyName: String, token: String
    ) {
        viewModelScope.launch(exceptionHandler) {
            val hash = hashMapOf<String, Any>()
            hash["strategyName"] = strategyName
            hash["ownerUuid"] = strategyId
            if (frequency != null)
                hash["frequency"] = frequency
            hash["amount"] = amount
            Log.d("logData", "$hash")
            val res = RestClient.getRetrofitInstanceSecure(token).investOnStrategy(hash)
            if (res.isSuccessful)
                _investStrategyResponse.postValue(res.body())
            else {
                val errorBody = res.errorBody()
                val errorCode =
                    CommonMethods.returnErrorCode(errorBody) // Extract the code from the body if needed
                _listener?.onRetrofitError(errorCode.code, errorCode.error)
            }
        }
    }

    fun pauseStrategy(strategyId: String, strateggyName: String) {
        viewModelScope.launch(exceptionHandler) {
            val res = RestClient.get().pauseStrategy(
                hashMapOf(
                    "strategyName" to strateggyName,
                    "ownerUuid" to strategyId
                )
            )
            if (res.isSuccessful)
                pauseStrategyResponse.postValue(res.body())
            else {
                val errorBody = res.errorBody()
                val errorCode =
                    CommonMethods.returnErrorCode(errorBody) // Extract the code from the body if needed
                _listener?.onRetrofitError(errorCode.code, errorCode.error)
            }
        }
    }

    fun deleteStrategy(strateggyName: String) {
        viewModelScope.launch(exceptionHandler) {
            val res = RestClient.get().deleteStrategy(
                hashMapOf(
                    "strategyName" to strateggyName
                )
            )
            if (res.isSuccessful)
                pauseStrategyResponse.postValue(res.body())
            else {
                val errorBody = res.errorBody()
                val errorCode =
                    CommonMethods.returnErrorCode(errorBody) // Extract the code from the body if needed
                _listener?.onRetrofitError(errorCode.code, errorCode.error)
            }
        }
    }

    fun withdraw(assetId: String, amount: String, assetAmount: Float, wallet_address: String) {
        viewModelScope.launch(exceptionHandler) {
            val hash = hashMapOf<String, Any>()
            hash["asset_id"] = assetId
            hash["amount"] = amount
            hash["asset_amount"] = assetAmount
            hash["wallet_address"] = wallet_address
            val res = RestClient.get().withdrawCryptos(hash)
            if (res.isSuccessful)
                _withdrawResponse.postValue(res.body())
            else {
                val errorBody = res.errorBody()
                val errorCode =
                    CommonMethods.returnErrorCode(errorBody) // Extract the code from the body if needed
                _listener?.onRetrofitError(errorCode.code, errorCode.error)
            }
        }
    }

    fun confirmOrder(order: String,token: String) {
        viewModelScope.launch(exceptionHandler) {
            val hashMap = hashMapOf<String, Any>()
            hashMap["orderId"] = order
            val res = RestClient.getRetrofitInstanceSecure(token).acceptQuote(hashMap)
            if (res.isSuccessful)
                exchangeResponse.postValue(res.body())
            else {
                val errorBody = res.errorBody()
                val errorCode =
                    CommonMethods.returnErrorCode(errorBody) // Extract the code from the body if needed
                _listener?.onRetrofitError(errorCode.code, errorCode.error)
            }
        }
    }

    fun getQuote(
        assetIdFrom: String,
        assetIdTo: String,
        exchangeFromAmount: String
    ) {
        viewModelScope.launch(exceptionHandler) {
            val hashMap = hashMapOf<String, Any>()
            hashMap["fromAsset"] = assetIdFrom.trim().lowercase()
            hashMap["toAsset"] = assetIdTo.trim().lowercase()
            hashMap["fromAmount"] = exchangeFromAmount
            val res = RestClient.get().getQuote(hashMap)
            if (res.isSuccessful)
                getQuoteResponse.postValue(res.body())
            else {
                val errorBody = res.errorBody()
                val errorCode =
                    CommonMethods.returnErrorCode(errorBody) // Extract the code from the body if needed
                _listener?.onRetrofitError(errorCode.code, errorCode.error)
            }
        }
    }

    fun getTransactions(page: Int = 1, limit: Int = 10, assetId: String = "") {
        viewModelScope.launch(exceptionHandler) {
            val res = if (assetId.isEmpty()) RestClient.get()
                .getTransactions(page, limit)
            else RestClient.get()
                .getTransactions(assetId, page, limit)
            if (res.isSuccessful)
                _transactionResponse.postValue(res.body())
            else {
                val errorBody = res.errorBody()
                val errorCode =
                    CommonMethods.returnErrorCode(errorBody) // Extract the code from the body if needed
                _listener?.onRetrofitError(errorCode.code, errorCode.error)
            }
        }
    }

    fun logout() {
        viewModelScope.launch(exceptionHandler) {
            val res = RestClient.get().logout()
            if (res.isSuccessful)
                _logoutResponse.postValue(res.body())
            else {
                val errorBody = res.errorBody()
                val errorCode =
                    CommonMethods.returnErrorCode(errorBody) // Extract the code from the body if needed
                _listener?.onRetrofitError(errorCode.code, errorCode.error)
            }
        }
    }



    fun verifyPhoneForPin(otp: String) {
        viewModelScope.launch(exceptionHandler) {
            val res = RestClient.get().verifyPhoneForPinChange(hashMapOf("otp" to otp.toInt()))
            if (res.isSuccessful)
                _verifyPhoneForPinResponse.postValue(res.body())
            else {
                val errorBody = res.errorBody()
                val errorCode =
                    CommonMethods.returnErrorCode(errorBody) // Extract the code from the body if needed
                _listener?.onRetrofitError(errorCode.code, errorCode.error)
            }
        }
    }

    fun updatePin(pin: String) {
        viewModelScope.launch(exceptionHandler) {
            val res = RestClient.get().updatePin(hashMapOf("newPin" to pin.toInt()))
            if (res.isSuccessful)
                _updatePinResponse.postValue(res.body())
            else {
                val errorBody = res.errorBody()
                val errorCode =
                    CommonMethods.returnErrorCode(errorBody) // Extract the code from the body if needed
                _listener?.onRetrofitError(errorCode.code, errorCode.error)
            }
        }
    }

    fun addBankInfo(iban: String, bic: String) {

        viewModelScope.launch(exceptionHandler) {
            val hashMap = hashMapOf<String, Any>()
            hashMap["iban"] = iban
            hashMap["bic"] = bic
            val res = RestClient.get().addBank(hashMap)
            if (res.isSuccessful)
                _addBankResponse.postValue(res.body())
            else {
                val errorBody = res.errorBody()
                val errorCode =
                    CommonMethods.returnErrorCode(errorBody) // Extract the code from the body if needed
                _listener?.onRetrofitError(errorCode.code, errorCode.error)
            }
        }
    }



    fun verifyStrongAuthentication(otp: String) {
        viewModelScope.launch(exceptionHandler) {
            val res =
                RestClient.get().verifyStrongAuthentication(hashMapOf("otp" to otp.toInt()))
            if (res.isSuccessful)
                _verifyStrongAuthentication.postValue(res.body())
            else {
                val errorBody = res.errorBody()
                val errorCode =
                    CommonMethods.returnErrorCode(errorBody) // Extract the code from the body if needed
                _listener?.onRetrofitError(errorCode.code, errorCode.error)
            }
        }
    }


    fun getNetwork(id: String) {
        viewModelScope.launch(exceptionHandler) {
            val res = RestClient.get().getNetworkById(id)
            if (res.isSuccessful)
                _networksResponseSingle.postValue(res.body())
            else {
                val errorBody = res.errorBody()
                val errorCode =
                    CommonMethods.returnErrorCode(errorBody) // Extract the code from the body if needed
                _listener?.onRetrofitError(errorCode.code, errorCode.error)
            }
        }
    }

    fun getNetworks() {
        viewModelScope.launch(exceptionHandler) {
            val res = RestClient.get().getNetworks()
            if (res.isSuccessful)
                _networksResponse.postValue(res.body())
            else {
                val errorBody = res.errorBody()
                val errorCode =
                    CommonMethods.returnErrorCode(errorBody) // Extract the code from the body if needed
                _listener?.onRetrofitError(errorCode.code, errorCode.error)
            }
        }
    }

    fun addAddress(
        name: String,
        network: String,
        address: String,
        origin: String,
        exchange: String
    ) {
        viewModelScope.launch(exceptionHandler) {
            val hashMap: HashMap<String, Any> = hashMapOf()
            hashMap["name"] = name
            hashMap["network"] = network
            hashMap["address"] = address
            hashMap["origin"] = origin.lowercase()
            if (origin.lowercase() == "exchange")
                hashMap["exchange"] = exchange
            val res = RestClient.get().addWhitelistingAddress(hashMap)
            if (res.isSuccessful)
                _addWhitelistResponse.postValue(res.body())
            else {
                val errorBody = res.errorBody()
                val errorCode =
                    CommonMethods.returnErrorCode(errorBody) // Extract the code from the body if needed
                _listener?.onRetrofitError(errorCode.code, errorCode.error)
            }
        }
    }

    fun updateUserInfo(
        hashMap: HashMap<String, Any>
    ) {
        viewModelScope.launch(exceptionHandler) {
//     val res = RestClient.getRetrofitInstanceSecure(token).updateUserInfo(hashMap)
     val res = RestClient.get().updateUserInfo(hashMap)
            if (res.isSuccessful)
                _updateUserInfoResponse.postValue(res.body())
            else {
                val errorBody = res.errorBody()
                val errorCode =
                    CommonMethods.returnErrorCode(errorBody) // Extract the code from the body if needed
                _listener?.onRetrofitError(errorCode.code, errorCode.error)
            }
        }
    }

    fun updateAvtaar(
        lock: String
    ) {
        viewModelScope.launch(exceptionHandler) {
            val hashMap: HashMap<String, Any> = hashMapOf()
            hashMap["avatar"] = lock
//            val res = RestClient.getRetrofitInstanceSecure(token).updateUserInfo(hashMap)
            val res = RestClient.get().updateUserInfo(hashMap)
            if (res.isSuccessful)
                _updateUserInfoResponse.postValue(res.body())
            else {
                val errorBody = res.errorBody()
                val errorCode =
                    CommonMethods.returnErrorCode(errorBody) // Extract the code from the body if needed
                _listener?.onRetrofitError(errorCode.code, errorCode.error)
            }
        }
    }

    fun getWhiteListings() {
        viewModelScope.launch(exceptionHandler) {
            val res = RestClient.get().getWhitelistedAddress()
            if (res.isSuccessful)
                _getWhiteListing.postValue(res.body())
            else {
                val errorBody = res.errorBody()
                val errorCode =
                    CommonMethods.returnErrorCode(errorBody) // Extract the code from the body if needed
                _listener?.onRetrofitError(errorCode.code, errorCode.error)
            }
        }
    }


    fun searchWhitelist(keyword: String) {
        viewModelScope.launch(exceptionHandler) {
            val res = RestClient.get().getWhitelistedAddress(keyword = keyword)
            if (res.isSuccessful)
                _searchWhitelisting.postValue(res.body())
            else {
                val errorBody = res.errorBody()
                val errorCode =
                    CommonMethods.returnErrorCode(errorBody) // Extract the code from the body if needed
                _listener?.onRetrofitError(errorCode.code, errorCode.error)
            }
        }
    }

    fun upload(file: File) {
        viewModelScope.launch(exceptionHandler) {
            val fileRequestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
            val multiPart = MultipartBody.Part.createFormData(
                "file",
                file.name,
                fileRequestBody
            )
            val res = RestClient.get().upload(multiPart)
            if (res.isSuccessful)
                _uploadResponse.postValue(res.body())
            else {
                val errorBody = res.errorBody()
                val errorCode =
                    CommonMethods.returnErrorCode(errorBody) // Extract the code from the body if needed
                _listener?.onRetrofitError(errorCode.code, errorCode.error)
            }
        }
    }

    fun deleteWhiteList(address: String, network: String) {
        viewModelScope.launch(exceptionHandler) {
            val map = HashMap<String, Any>()
            map["network"] = network
            map["address"] = address
            val res = RestClient.get().deleteWhiteListing(map)
            if (res.isSuccessful) _deleteWhiteListResponse.postValue(res.body())
            else {
                val errorBody = res.errorBody()
                val errorCode =
                    CommonMethods.returnErrorCode(errorBody) // Extract the code from the body if needed
                _listener?.onRetrofitError(errorCode.code, errorCode.error)
            }
        }
    }


    fun updateUser(hashMap: HashMap<String, Any>) {
        viewModelScope.launch(exceptionHandler) {
            val res = RestClient.get().updateUser(hashMap)
            if (res.isSuccessful)
                _updateUserResponse.postValue(res.body())
            else {
                val errorBody = res.errorBody()
                val errorCode =
                    CommonMethods.returnErrorCode(errorBody) // Extract the code from the body if needed
                _listener?.onRetrofitError(errorCode.code, errorCode.error)
            }
        }
    }

    fun assetsToChoose(keyword: String = "") {
        viewModelScope.launch(exceptionHandler) {
            val res = if (keyword.isEmpty()) RestClient.get().getAssetsToChoose()
            else RestClient.get().getAssetsToChoose(keyword)
            if (res.isSuccessful) _assetsToChoose.postValue(res.body())
            else {
                val errorBody = res.errorBody()
                val errorCode =
                    CommonMethods.returnErrorCode(errorBody) // Extract the code from the body if needed
                _listener?.onRetrofitError(errorCode.code, errorCode.error)
            }
        }
    }

    fun getAllPriceResume() {
        viewModelScope.launch(exceptionHandler) {
            val res = RestClient.get().getAllPriceResume()
            if (res.isSuccessful) {
                val priceServiceResumeDict = res.body()?.data
                val priceServiceResumeArray = ArrayList<PriceServiceResume>()
                priceServiceResumeDict?.forEach {
                    val priceServiceResume =
                        PriceServiceResume(id = it.key, priceServiceResumeData = it.value)
                    priceServiceResumeArray.add(priceServiceResume)
                }
                _priceServiceResumes.postValue(priceServiceResumeArray)
            } else {
                val errorBody = res.errorBody()
                val errorCode =
                    CommonMethods.returnErrorCode(errorBody) // Extract the code from the body if needed
                _listener?.onRetrofitError(errorCode.code, errorCode.error)
            }
        }
    }

    fun getAllAssets() {
        viewModelScope.launch(exceptionHandler) {
            val res = RestClient.get().getAllAssets()
            if (res.isSuccessful) _allAssets.postValue(res.body())
            else {
                val errorBody = res.errorBody()
                val errorCode =
                    CommonMethods.returnErrorCode(errorBody) // Extract the code from the body if needed
                _listener?.onRetrofitError(errorCode.code, errorCode.error)
            }
        }
    }

    fun getWithdrawalAddresses() {
        viewModelScope.launch(exceptionHandler) {
            val res = RestClient.get().getWithdrawalAddress()
            if (res.isSuccessful) _withdrawalAddresses.postValue(res.body())
            else {
                val errorBody = res.errorBody()
                val errorCode =
                    CommonMethods.returnErrorCode(errorBody) // Extract the code from the body if needed
                _listener?.onRetrofitError(errorCode.code, errorCode.error)
            }
        }
    }

    fun getAssetDetail(assetId: String) {
        viewModelScope.launch(exceptionHandler) {
            val res = RestClient.get().getAssetDetail(assetId)
            if (res.isSuccessful) _getAssetDetail.postValue(res.body())
            else {
                val errorBody = res.errorBody()
                val errorCode =
                    CommonMethods.returnErrorCode(errorBody) // Extract the code from the body if needed
                _listener?.onRetrofitError(errorCode.code, errorCode.error)
            }
        }
    }

    fun getAssetDetailIncludeNetworks(assetId: String) {
        viewModelScope.launch(exceptionHandler) {
            val res = RestClient.get().getAssetDetail(assetId, "true")
            if (res.isSuccessful) _getAssetDetail.postValue(res.body())
            else {
                val errorBody = res.errorBody()
                val errorCode =
                    CommonMethods.returnErrorCode(errorBody) // Extract the code from the body if needed
                _listener?.onRetrofitError(errorCode.code, errorCode.error)
            }
        }
    }

    fun getAddress(network: String, assetId: String) {
        viewModelScope.launch(exceptionHandler) {
            val res = RestClient.get().getAddress(network, assetId)
            if (res.isSuccessful) _getAddress.postValue(res.body())
            else {
                val errorBody = res.errorBody()
                val errorCode =
                    CommonMethods.returnErrorCode(errorBody) // Extract the code from the body if needed
                _listener?.onRetrofitError(errorCode.code, errorCode.error)
            }
        }
    }

    fun getRecurringInvestmentDetail(investmentId: String) {
        viewModelScope.launch(exceptionHandler) {
            val res = RestClient.get().getRecurringInvestmentDetail(investmentId)
            if (res.isSuccessful)
                _recurringInvestmentDetail.postValue(res.body())
            else {
                val errorBody = res.errorBody()
                val errorCode =
                    CommonMethods.returnErrorCode(errorBody) // Extract the code from the body if needed
                _listener?.onRetrofitError(errorCode.code, errorCode.error)
            }
        }
    }

    fun cancelRecurringInvestment(investmentId: String) {
        viewModelScope.launch(exceptionHandler) {
            val res =
                RestClient.get().cancelRecurringInvestment(hashMapOf("id" to investmentId))
            if (res.isSuccessful) _cancelRecurringInvestment.postValue(res.body())
            else {
                val errorBody = res.errorBody()
                val errorCode =
                    CommonMethods.returnErrorCode(errorBody) // Extract the code from the body if needed
                _listener?.onRetrofitError(errorCode.code, errorCode.error)
            }
        }
    }

    fun getPriceGraph(assetId: String, duration: Duration) {
        viewModelScope.launch(exceptionHandler) {
            val res = RestClient.get().getPriceGraph(assetId, duration.duration)
            if (res.isSuccessful)
                _priceGraphResponse.postValue(res.body())
            else {
                val errorBody = res.errorBody()
                val errorCode =
                    CommonMethods.returnErrorCode(errorBody) // Extract the code from the body if needed
                _listener?.onRetrofitError(errorCode.code, errorCode.error)
            }
        }
    }

    fun withdrawFiat(amount: String) {
        viewModelScope.launch(exceptionHandler) {
            val res = RestClient.get().withdrawFiat(hashMapOf("amount" to amount))
            if (res.isSuccessful)
                _withdrawFiatResponse.postValue(res.body())
            else {
                val errorBody = res.errorBody()
                val errorCode =
                    CommonMethods.returnErrorCode(errorBody) // Extract the code from the body if needed
                _listener?.onRetrofitError(errorCode.code, errorCode.error)
            }
        }
    }

    fun userChallenge(phone: String = "", email: String = "") {
        viewModelScope.launch(exceptionHandler) {
            var phoneNumber = ""
            if (phone.isNotEmpty()) phoneNumber = phone.substring(1)//remove the "+"
            val param = phoneNumber.ifEmpty { email }
            val key = if (phone.isEmpty()) "email" else "phoneNo"

            val res = RestClient.get().userChallenge(hashMapOf(key to param))
            if (res.isSuccessful)
                _userChallengeResponse.postValue(res.body())
            else {
                val errorBody = res.errorBody()
                val errorCode =
                    CommonMethods.returnErrorCode(errorBody) // Extract the code from the body if needed
                _listener?.onRetrofitError(errorCode.code, errorCode.error)
            }
        }
    }

    fun authenticateUser(a: String, m1: String,token: String) {
        viewModelScope.launch(exceptionHandler) {

            val param = hashMapOf<String, Any>()
            param["method"] = "srp"
            param["A"] = a
            param["M1"] = m1

            val res = RestClient.getRetrofitInstanceSecure(token).userLogin(param)
            if (res.isSuccessful)
                _userLoginResponse.postValue(res.body())
            else {
                val errorBody = res.errorBody()
                val errorCode =
                    CommonMethods.returnErrorCode(errorBody) // Extract the code from the body if needed
                _listener?.onRetrofitError(errorCode.code, errorCode.error)
            }

        }
    }

    fun verify2FA(code: String) {
        viewModelScope.launch(exceptionHandler) {
            val hash = hashMapOf<String, Any>()
            hash["code"] = code
            val res = RestClient.get().verify2FA(hash)
            if (res.isSuccessful)
                _userLoginResponse.postValue(res.body())
            else {
                val errorBody = res.errorBody()
                val errorCode =
                    CommonMethods.returnErrorCode(errorBody) // Extract the code from the body if needed
                _listener?.onRetrofitError(errorCode.code, errorCode.error)
            }
        }
    }


    fun refreshToken(token: String) {
        viewModelScope.launch(exceptionHandler) {
            val hashMap = hashMapOf<String, Any>()
            hashMap["method"] = "refresh_token"
            hashMap["refresh_token"] = App.prefsManager.refreshToken
            val res = RestClient.getRetrofitInstanceSecure(token).userLogin(hashMap)
            if (res.isSuccessful)
                _userLoginResponse.postValue(res.body())
            else {
                val errorBody = res.errorBody()
                val errorCode =
                    CommonMethods.returnErrorCode(errorBody) // Extract the code from the body if needed
                _listener?.onRetrofitError(errorCode.code, errorCode.error)
            }
        }
    }

    fun setPhone(
        countryCode: Int, phone: String,
        signature: String,
        timestamp: String,token: String
    ) {
        viewModelScope.launch(exceptionHandler) {
            val res = RestClient.setPhoneInstance(token, signature, timestamp)
                .setPhone(hashMapOf("countryCode" to countryCode, "phoneNo" to phone))
            if (res.isSuccessful)
                _setPhoneResponse.postValue(res.body())
            else {
                val errorBody = res.errorBody()
                val errorCode =
                    CommonMethods.returnErrorCode(errorBody) // Extract the code from the body if needed
                _listener?.onRetrofitError(errorCode.code, errorCode.error)
            }
        }
    }

    fun verifyPhone(code: String) {
        viewModelScope.launch(exceptionHandler) {
            val res = RestClient.get()
                .verifyPhone(hashMapOf("code" to code.toString()))
            if (res.isSuccessful)
                _verifyPhoneResponse.postValue(res.body())
            else {
                val errorBody = res.errorBody()
                val errorCode =
                    CommonMethods.returnErrorCode(errorBody) // Extract the code from the body if needed
                _listener?.onRetrofitError(errorCode.code, errorCode.error)
            }
        }
    }

    fun setUserInfo(
        hashMap: HashMap<String, Any>
    ) {
        viewModelScope.launch(exceptionHandler) {
            val res = RestClient.get().setUserInfo(hashMap)
            if (res.isSuccessful)
                _setUserInfoResponse.postValue(res.body())
            else {
                val errorBody = res.errorBody()
                val errorCode =
                    CommonMethods.returnErrorCode(errorBody) // Extract the code from the body if needed
                _listener?.onRetrofitError(errorCode.code, errorCode.error)
            }
        }
    }

    fun setEmail(
        email: String,
        emailSalt: String,
        emailVerifier: String,
        phoneSalt: String,
        phoneVerifier: String
    ) {
        viewModelScope.launch(exceptionHandler) {
            val hashMap = hashMapOf<String, Any>()
            hashMap["email"] = email
            hashMap["emailSalt"] = emailSalt
            hashMap["emailVerifier"] = emailVerifier
            hashMap["phoneSalt"] = phoneSalt
            hashMap["phoneVerifier"] = phoneVerifier

            val res = RestClient.get().setEmail(hashMap)
            if (res.isSuccessful)
                _setUpEmailResponse.postValue(res.body())
            else {
                val errorBody = res.errorBody()
                val errorCode =
                    CommonMethods.returnErrorCode(errorBody) // Extract the code from the body if needed
                _listener?.onRetrofitError(errorCode.code, errorCode.error)
            }
        }
    }


    fun verifyEmail( code: String) {
        viewModelScope.launch(exceptionHandler) {

            val hash = hashMapOf<String, Any>()
            hash["code"] = code
            try {
                val res = RestClient.get().verifyEmail(hash)
                if (res.isSuccessful)
                    _verifyEmailResponse.postValue(res.body())
                else {
                    val errorBody = res.errorBody()
                    val errorCode =
                        CommonMethods.returnErrorCode(errorBody) // Extract the code from the body if needed
                    _listener?.onRetrofitError(errorCode.code, errorCode.error)
                }
            } catch (e: Exception) {
                _verifyEmailResponse.postValue("Email verified")
            }

        }
    }


    fun setUserAddress(
       hashMap: HashMap<String, Any>
    ) {
        viewModelScope.launch(exceptionHandler) {
            val res = RestClient.get().setUserAddress(hashMap)
            if (res.isSuccessful)
                _setUserAddressResponse.postValue(res.body())
            else {
                val errorBody = res.errorBody()
                val errorCode =
                    CommonMethods.returnErrorCode(errorBody) // Extract the code from the body if needed
                _listener?.onRetrofitError(errorCode.code, errorCode.error)
            }
        }
    }

    fun finishRegistration() {
        viewModelScope.launch(exceptionHandler) {
            val res = RestClient.get().finishRegistration()
            if (res.isSuccessful)
                _finishRegistrationResponse.postValue(res.body())
            else {
                val errorBody = res.errorBody()
                val errorCode =
                    CommonMethods.returnErrorCode(errorBody) // Extract the code from the body if needed
                _listener?.onRetrofitError(errorCode.code, errorCode.error)
            }
        }
    }

    fun setInvestmentExp(
        investmentExperience: String,
        incomeSource: String,
        occupation: String,
        incomeRange: String,
        personalAssets: String
    ) {
        viewModelScope.launch(exceptionHandler) {
            val hash = hashMapOf<String, Any>()
            hash["investmentExperience"] = investmentExperience
            hash["incomeSource"] = incomeSource
            hash["occupation"] = occupation
            hash["incomeRange"] = incomeRange
            hash["mainUse"] = personalAssets
            val res = RestClient.get().setInvestmentExp(hash)
            if (res.isSuccessful)
                _setInvestmentExpResponse.postValue(res.body())
            else {
                val errorBody = res.errorBody()
                val errorCode =
                    CommonMethods.returnErrorCode(errorBody) // Extract the code from the body if needed
                _listener?.onRetrofitError(errorCode.code, errorCode.error)
            }
        }

    }

    fun getUser() {
        viewModelScope.launch(exceptionHandler) {
            val res = RestClient.get().getUser()
            if (res.isSuccessful)
                _getUserResponse.postValue(res.body())
            else {
                val errorBody = res.errorBody()
                val errorCode =
                    CommonMethods.returnErrorCode(errorBody) // Extract the code from the body if needed
                _listener?.onRetrofitError(errorCode.code, errorCode.error)
            }
        }
    }

    fun getNews(id: String = "btc") {
        viewModelScope.launch(exceptionHandler) {
            val res = RestClient.get().getNews(id)
            if (res.isSuccessful)
                _newsResponse.postValue(res.body())
            else {
                val errorBody = res.errorBody()
                val errorCode =
                    CommonMethods.returnErrorCode(errorBody) // Extract the code from the body if needed
                _listener?.onRetrofitError(errorCode.code, errorCode.error)
            }
        }
    }

    fun getPrice(id: String = "btc", tf: String = "1h") {
        viewModelScope.launch(exceptionHandler) {
            val res = RestClient.get().getPrice(id, tf)
            if (res.isSuccessful)
                _priceResponse.postValue(res.body())
            else {
                val errorBody = res.errorBody()
                val errorCode =
                    CommonMethods.returnErrorCode(errorBody) // Extract the code from the body if needed
                _listener?.onRetrofitError(errorCode.code, errorCode.error)
            }
        }
    }

    fun createWithdrawalRequest(
        assetId: String,
        amount: Double,
        destination: String,
        network: String,
        code: String,token: String
    ) {
        viewModelScope.launch(exceptionHandler) {
            val map = HashMap<String, Any>()
            map["asset"] = assetId
            map["amount"] = amount
            map["destination"] = destination
            map["network"] = network
            map["otp"] = code
            val res = RestClient.getRetrofitInstanceSecure(token).createWithdrawalRequest(map)
            if (res.isSuccessful) {
                _commonResponse.postValue(res.body())
            } else {
                val errorBody = res.errorBody()
                val errorCode =
                    CommonMethods.returnErrorCode(errorBody) // Extract the code from the body if needed
                listener!!.onRetrofitError(errorCode.code, errorCode.error)
            }
        }
    }

    fun getOtpForWithdraw(action: String, details: String?) {
        viewModelScope.launch(exceptionHandler) {
            val map = HashMap<String, Any>()
            map["action"] = action
            if (details != null)
                map["details"] = details
            val res = RestClient.get().getOtpForWithdraw(map)
            if (res.isSuccessful) {
                _commonResponse.postValue(res.body())
            } else {
                 val errorBody = res.errorBody()
                val errorCode = CommonMethods.returnErrorCode(errorBody) // Extract the code from the body if needed
                                listener!!.onRetrofitError(errorCode.code, errorCode.error)
            }
        }
    }

    fun startKYC() {
        viewModelScope.launch(exceptionHandler) {
            val res = RestClient.get().startKyc()
            if (res.isSuccessful) {
                _kycResponse.postValue(res.body())
            } else {
                val errorBody = res.errorBody()
                val errorCode =
                    CommonMethods.returnErrorCode(errorBody) // Extract the code from the body if needed
                                listener!!.onRetrofitError(errorCode.code, errorCode.error)

            }
        }
    }

    fun getBalanceApi() {
        viewModelScope.launch(exceptionHandler) {
            val res = RestClient.get().getBalance()
            if (res.isSuccessful)
                _balanceResponse.postValue(res.body())
            else {
                val errorBody = res.errorBody()
                val errorCode =
                    CommonMethods.returnErrorCode(errorBody) // Extract the code from the body if needed
                _listener?.onRetrofitError(errorCode.code, errorCode.error)
            }
        }
    }

    fun getOrderApi(id: String) {
        viewModelScope.launch(exceptionHandler) {
            val res = RestClient.get().getOrder(id)
            if (res.isSuccessful)
                _getOrderResponse.postValue(res.body())
            else {
                val errorBody = res.errorBody()
                val errorCode =
                    CommonMethods.returnErrorCode(errorBody) // Extract the code from the body if needed
                _listener?.onRetrofitError(errorCode.code, errorCode.error)
            }
        }
    }

    fun updateAuthentication(hash: HashMap<String, Any>) {
        viewModelScope.launch(exceptionHandler) {

            val res = RestClient.get().updateUserAuthentication(hash)
            if (res.isSuccessful)
                _updateAuthenticateResponse.postValue(res.body())
            else {
                val errorBody = res.errorBody()
                val errorCode =
                    CommonMethods.returnErrorCode(errorBody) // Extract the code from the body if needed
                _listener?.onRetrofitError(errorCode.code, errorCode.error)
            }
        }
    }

    fun switchOffAuthentication(detail: String, action: String) {
        viewModelScope.launch(exceptionHandler) {
            val res =
                RestClient.get().switchOffAuthentication(detail, action)
            if (res.isSuccessful)
                _booleanResponse.postValue(res.body())
            else {
                val errorBody = res.errorBody()
                val errorCode =
                    CommonMethods.returnErrorCode(errorBody) // Extract the code from the body if needed
                _listener?.onRetrofitError(errorCode.code, errorCode.error)
            }
        }
    }

    fun getExportOperation(date: String) {
        try {
            viewModelScope.launch(exceptionHandler) {
                val res = RestClient.get().getOperationExport(date)
                if (res.isSuccessful)
                    _exportOperationResponse.postValue(res.body())
                else {
                    val errorBody = res.errorBody()
                    val errorCode =
                        CommonMethods.returnErrorCode(errorBody) // Extract the code from the body if needed
                    _listener?.onRetrofitError(errorCode.code, errorCode.error)
                }
            }
        } catch (ex: Exception) {
        }
    }

    fun qrCodeUrl() {
        viewModelScope.launch(exceptionHandler) {
            val res = RestClient.get().getQrUrl()
            if (res.isSuccessful)
                _qrCodeResponse.postValue(res.body())
            else {
            val errorBody = res.errorBody()
            val errorCode =
                CommonMethods.returnErrorCode(errorBody) // Extract the code from the body if needed
            _listener?.onRetrofitError(errorCode.code, errorCode.error)
        }
        }
    }

    fun sendMsgToSupport(msg: String) {
        try {
            viewModelScope.launch(exceptionHandler) {
                val hash = hashMapOf<String, Any>()
                hash["message"] = msg
                val res = RestClient.get().contactSupport(hash)
                if (res.isSuccessful)
                    _msgResponse.postValue(res.body())
                else {
                    val errorBody = res.errorBody()
                    val errorCode =
                        CommonMethods.returnErrorCode(errorBody) // Extract the code from the body if needed
                    _listener?.onRetrofitError(errorCode.code, errorCode.error)
                }
            }
        } catch (e: Exception) {
            listener?.onError()

        }
    }

    fun getTransactionsListing(limit: Int, offset: Int) {
        viewModelScope.launch(exceptionHandler) {
            val res = RestClient.get().getTransactionsList(limit, offset)
            if (res.isSuccessful)
                _getTransactionListing.postValue(res.body())
            else {
                val errorBody = res.errorBody()
                val errorCode =
                    CommonMethods.returnErrorCode(errorBody) // Extract the code from the body if needed
                _listener?.onRetrofitError(errorCode.code, errorCode.error)
            }
        }
    }

    fun forgotPass(email: String,token: String) {
        try {
            viewModelScope.launch(exceptionHandler) {
                val hash = hashMapOf<String, Any>()
                hash["email"] = email
                val res = RestClient.getRetrofitInstanceSecure(token).forgotPassword(hash)
                if (res.isSuccessful)
                    _booleanResponse.postValue(res.body())
                else {
                    val errorBody = res.errorBody()
                    val errorCode =
                        CommonMethods.returnErrorCode(errorBody) // Extract the code from the body if needed
                    _listener?.onRetrofitError(errorCode.code, errorCode.error)
                }
            }
        } catch (e: Exception) {
            listener?.onError()
        }

    }

    fun resetNewPass(
        emailSalt: String,
        emailVerifier: String,
        phoneSalt: String,
        phoneVerifier: String,token: String
    ) {
        try {
            viewModelScope.launch(exceptionHandler) {
                val hashMap = hashMapOf<String, Any>()
                hashMap["emailSalt"] = emailSalt
                hashMap["emailVerifier"] = emailVerifier
                hashMap["phoneSalt"] = phoneSalt
                hashMap["phoneVerifier"] = phoneVerifier
                val res = RestClient.getRetrofitInstanceSecure(token).resetNewPassword(hashMap)
                if (res.isSuccessful)
                    _booleanResponse.postValue(res.body())
                else {
                    val errorBody = res.errorBody()
                    val errorCode =
                        CommonMethods.returnErrorCode(errorBody) // Extract the code from the body if needed
                    _listener?.onRetrofitError(errorCode.code, errorCode.error)
                }
            }
        } catch (e: Exception) {
            listener?.onError()
        }
    }

    fun getResetPass() {
        try {
            viewModelScope.launch(exceptionHandler) {
                val res = RestClient.get().getResetPassword()
                if (res.isSuccessful)
                    _resetPassResponse.postValue(res.body())
                else {
                    val errorBody = res.errorBody()
                    val errorCode =
                        CommonMethods.returnErrorCode(errorBody) // Extract the code from the body if needed
                    _listener?.onRetrofitError(errorCode.code, errorCode.error)
                }
            }
        } catch (e: Exception) {
            listener?.onError()

        }
    }

    fun getPasswordChangeChallenge() {
        try {
            viewModelScope.launch(exceptionHandler) {
                val res = RestClient.get().getPasswordChangeChallenge()
                if (res.isSuccessful)
                    _changePassResponse.postValue(res.body())
                else {
                    val errorBody = res.errorBody()
                    val errorCode =
                        CommonMethods.returnErrorCode(errorBody) // Extract the code from the body if needed
                    _listener?.onRetrofitError(errorCode.code, errorCode.error)
                }
            }
        } catch (e: Exception) {
            listener?.onError()

        }
    }

    fun changePassword(hash: HashMap<String, Any>,token: String) {
        try {
            viewModelScope.launch(exceptionHandler) {
                val res = RestClient.getRetrofitInstanceSecure(token).changePassword(hash)
                if (res.isSuccessful)
                    _booleanResponse.postValue(res.body())
                else {
                    val errorBody = res.errorBody()
                    val errorCode =
                        CommonMethods.returnErrorCode(errorBody) // Extract the code from the body if needed
                    _listener?.onRetrofitError(errorCode.code, errorCode.error)
                }
            }
        } catch (e: Exception) {
            listener?.onError()

        }
    }

    fun verifyPasswordChange(code: String) {
        try {
            viewModelScope.launch(exceptionHandler) {
                val hash = hashMapOf<String, Any>()
                hash["code"] = code
                val res = RestClient.get().verifyPasswordChange(hash)
                if (res.isSuccessful)
                    _exportOperationResponse.postValue(res.body())
                else {
                    val errorBody = res.errorBody()
                    val errorCode =
                        CommonMethods.returnErrorCode(errorBody) // Extract the code from the body if needed
                    _listener?.onRetrofitError(errorCode.code, errorCode.error)
                }
            }
        } catch (e: Exception) {
            listener?.onError()
        }
    }

    fun oneTimeOrderStrategy(strategyName: String, amount: Number, ownerUuid: String, token: String) {
        viewModelScope.launch(exceptionHandler) {
            val map = HashMap<String, Any>()
            map["strategyName"] = strategyName
            map["amount"] = amount
            map["ownerUuid"] = ownerUuid
            val res =
                RestClient.getRetrofitInstanceSecure(token).oneTimeStrategyExecution(map)
            if (res.isSuccessful) {
                _oneTimeStrategyDataResponse.postValue(res.body())
            } else {
                 val errorBody = res.errorBody()
                val errorCode =
                    CommonMethods.returnErrorCode(errorBody) // Extract the code from the body if needed
                                listener!!.onRetrofitError(errorCode.code, errorCode.error)

            
            }
        }
    }

    fun strategyStatus(executionId: String) {
        viewModelScope.launch(exceptionHandler) {
            val res =
                RestClient.get().checkStrategyStatus(executionId)
            if (res.isSuccessful) {
                _strategyExecutionResponse.postValue(res.body())
            } else {
                 val errorBody = res.errorBody()
                val errorCode =
                    CommonMethods.returnErrorCode(errorBody) // Extract the code from the body if needed
                                listener!!.onRetrofitError(errorCode.code, errorCode.error)

            
            }
        }
    }

    fun cancelQuote(hashMap: HashMap<String, Any>) {
        viewModelScope.launch(exceptionHandler) {
            val res = RestClient.get().cancelQuote(hashMap)
            if (res.isSuccessful) {
                _booleanResponse.postValue(res.body())
            } else {
                 val errorBody = res.errorBody()
                val errorCode =
                    CommonMethods.returnErrorCode(errorBody) // Extract the code from the body if needed
                                listener!!.onRetrofitError(errorCode.code, errorCode.error)

            
            }
        }
    }

    fun getActivityLogs(limit: Int, offset: Int) {
        viewModelScope.launch(exceptionHandler) {
            val res = RestClient.get().getActivityLogsList(limit, offset)
            if (res.isSuccessful)
                _getActivityLogsListing.postValue(res.body())
            else {
                val errorBody = res.errorBody()
                val errorCode =
                    CommonMethods.returnErrorCode(errorBody) // Extract the code from the body if needed
                _listener?.onRetrofitError(errorCode.code, errorCode.error)
            }
        }
    }

    fun closeAccount(hash: HashMap<String, Any>,token: String) {
        viewModelScope.launch(exceptionHandler) {

            val res = RestClient.getRetrofitInstanceSecure(token).closeAccount(hash)
            if (res.isSuccessful)
                _booleanResponse.postValue(res.body())
            else {
                val errorBody = res.errorBody()
                val errorCode =
                    CommonMethods.returnErrorCode(errorBody) // Extract the code from the body if needed
                _listener?.onRetrofitError(errorCode.code, errorCode.error)
            }

        }
    }

    fun startSignUrl() {
        viewModelScope.launch(exceptionHandler) {
            val res = RestClient.get().startSignUrl()
            if (res.isSuccessful) {
                _signUrlResponse.postValue(res.body())
            } else {
                val errorBody = res.errorBody()
                val errorCode =
                    CommonMethods.returnErrorCode(errorBody) // Extract the code from the body if needed
                                listener!!.onRetrofitError(errorCode.code, errorCode.error)

            }
        }
    }

    fun startKYCIdentity() {
        viewModelScope.launch(exceptionHandler) {
            val res = RestClient.get().startKyc()
            if (res.isSuccessful) {
                _kycResponseIdentity.postValue(res.body())
            } else {
                val errorBody = res.errorBody()
                val errorCode =
                    CommonMethods.returnErrorCode(errorBody) // Extract the code from the body if needed
                                listener!!.onRetrofitError(errorCode.code, errorCode.error)

            }
        }
    }

    fun getWalletHistoryPrice(daily: Boolean = true, limit: Int = 500) {
        viewModelScope.launch(exceptionHandler) {
            val res = RestClient.get().getWalletHistory(limit, daily)
            if (res.isSuccessful)
                _walletHistoryResponse.postValue(res.body())
            else {
                val errorBody = res.errorBody()
                val errorCode =
                    CommonMethods.returnErrorCode(errorBody) // Extract the code from the body if needed
                _listener?.onRetrofitError(errorCode.code, errorCode.error)
            }
        }
    }

    fun getActiveStrategies() {
        viewModelScope.launch(exceptionHandler) {
            val res = RestClient.get().activeStrategies()
            if (res.isSuccessful)
                _activeStrategyResponse.postValue(res.body())
            else {
                val errorBody = res.errorBody()
                val errorCode =
                    CommonMethods.returnErrorCode(errorBody) // Extract the code from the body if needed
                _listener?.onRetrofitError(errorCode.code, errorCode.error)
            }
        }
    }

    fun getPriceResumeById(id: String) {
        viewModelScope.launch(exceptionHandler) {
            val res = RestClient.get().getPriceResumeId(id)
            if (res.isSuccessful)
                _priceResumeIdResponse.postValue(res.body())
            else {
                val errorBody = res.errorBody()
                val errorCode =
                    CommonMethods.returnErrorCode(errorBody) // Extract the code from the body if needed
                _listener?.onRetrofitError(errorCode.code, errorCode.error)
            }
        }
    }

    fun editEnabledStrategy(
        strategyId: String,
        frequency: String?,
        amount: Number,
        strategyName: String
    ) {
        viewModelScope.launch(exceptionHandler) {
            val hash = hashMapOf<String, Any>()
            hash["strategyName"] = strategyName
            hash["ownerUuid"] = strategyId
            if (frequency != null)
                hash["frequency"] = frequency
            hash["amount"] = amount
            Log.d("logData", "$hash")
            val res = RestClient.get().editEnabledStrategy(hash)
            if (res.isSuccessful)
                _investStrategyResponse.postValue(res.body())
            else {
                val errorBody = res.errorBody()
                val errorCode =
                    CommonMethods.returnErrorCode(errorBody) // Extract the code from the body if needed
                _listener?.onRetrofitError(errorCode.code, errorCode.error)
            }
        }
    }

    fun enableNotification(
        token: String
    ) {
        viewModelScope.launch(exceptionHandler) {
            val hash = hashMapOf<String, Any>()
            hash["token"] = token
            hash["device"] = "ANDROID"
            val res = RestClient.get().enableNotification(hash)
            if (res.isSuccessful)
                _booleanResponse.postValue(res.body())
            else {
                val errorBody = res.errorBody()
                val errorCode =
                    CommonMethods.returnErrorCode(errorBody) // Extract the code from the body if needed
                _listener?.onRetrofitError(errorCode.code, errorCode.error)
            }
        }
    }


    fun getWalletRib() {
        viewModelScope.launch(exceptionHandler) {
            val res = RestClient.get().getWalletServices()
            if (res.isSuccessful)
                _getWalletRibResponse.postValue(res.body())
            else {
                val errorBody = res.errorBody()
                val errorCode =
                    CommonMethods.returnErrorCode(errorBody) // Extract the code from the body if needed
                _listener?.onRetrofitError(errorCode.code, errorCode.error)
            }
        }
    }

    fun addRib(
        hash: HashMap<String, Any>
    ) {
        viewModelScope.launch(exceptionHandler) {
            val res = RestClient.get().addRib(hash)
            if (res.isSuccessful)
                _booleanResponse.postValue(res.body())
            else {
                val errorBody = res.errorBody()
                val errorCode =
                    CommonMethods.returnErrorCode(errorBody) // Extract the code from the body if needed
                _listener?.onRetrofitError(errorCode.code, errorCode.error)
            }
        }
    }

    fun deleteRIB(
        ribID: String
    ) {
        viewModelScope.launch(exceptionHandler) {
            val hash = HashMap<String, Any>()
            hash["ribId"] = ribID
            val res = RestClient.get().deleteRIB(hash)
            if (res.isSuccessful)
                _exportOperationResponse.postValue(res.body())
            else {
                val errorBody = res.errorBody()
                val errorCode =
                    CommonMethods.returnErrorCode(errorBody) // Extract the code from the body if needed
                _listener?.onRetrofitError(errorCode.code, errorCode.error)
            }
        }
    }

    fun createWithdrawalEuroRequest(
        ribId: String,
        iban: String,
        bic: String,
        amount: Double,
        code: String, token: String
    ) {
        viewModelScope.launch(exceptionHandler) {
            val map = HashMap<String, Any>()
            map["ribId"] = ribId
            map["iban"] = iban
            map["bic"] = bic
            map["amount"] = amount
            map["otp"] = code
            val res = RestClient.getRetrofitInstanceSecure(token).withdrawEuroRequest(map)
            if (res.isSuccessful) {
                _commonResponse.postValue(res.body())
            } else {
                 val errorBody = res.errorBody()
                val errorCode =
                    CommonMethods.returnErrorCode(errorBody) // Extract the code from the body if needed
                                listener!!.onRetrofitError(errorCode.code, errorCode.error)

            }
        }
    }

    fun getWithdrawEuroFee() {
        viewModelScope.launch(exceptionHandler) {
            val res = RestClient.get().getWithdrawEuroFee()
            if (res.isSuccessful)
                _getWithdrawEuroFeeResponse.postValue(res.body())
            else {
                val errorBody = res.errorBody()
                val errorCode =
                    CommonMethods.returnErrorCode(errorBody) // Extract the code from the body if needed
                _listener?.onRetrofitError(errorCode.code, errorCode.error)
            }
        }
    }

    fun getUserNameByPhone(phone: String) {
        viewModelScope.launch(exceptionHandler) {
            val res = RestClient.get().getUserNameByPhone(phone)
            if (res.isSuccessful)
                _getUserByPhoneResponse.postValue(res.body())
            else {
                val errorBody = res.errorBody()
                val errorCode =
                    CommonMethods.returnErrorCode(errorBody) // Extract the code from the body if needed
                _listener?.onRetrofitError(errorCode.code, errorCode.error)
            }
        }
    }
    fun transferToFriend(hashMap: HashMap<String,Any>,token: String) {
        viewModelScope.launch(exceptionHandler) {
            val res = RestClient.getRetrofitInstanceSecure(token).transferToFriend(hashMap)
//            val res = RestClient.get().transferToFriend(hashMap)
            if (res.isSuccessful)
                _booleanResponse.postValue(res.body())
            else {
                val errorBody = res.errorBody()
                val errorCode =
                    CommonMethods.returnErrorCode(errorBody) // Extract the code from the body if needed
                _listener?.onRetrofitError(errorCode.code, errorCode.error)
            }
        }
    }
    fun getCurrentPrice(id: String) {
        viewModelScope.launch(exceptionHandler) {
            val res = RestClient.get().getCurrentPrice(id)
            if (res.isSuccessful)
                _getCurrentPriceResponse.postValue(res.body())
            else {
                val errorBody = res.errorBody()
                val errorCode =
                    CommonMethods.returnErrorCode(errorBody) // Extract the code from the body if needed
                _listener?.onRetrofitError(errorCode.code, errorCode.error)
            }
        }
    }
}
