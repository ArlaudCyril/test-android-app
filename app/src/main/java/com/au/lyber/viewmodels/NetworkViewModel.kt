package com.au.lyber.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.au.lyber.models.*
import com.au.lyber.network.RestClient
import com.au.lyber.ui.portfolio.viewModel.PortfolioViewModel
import com.au.lyber.utils.App
import com.au.lyber.utils.Constants
import com.google.android.datatransport.runtime.Destination
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

open class NetworkViewModel : ViewModel() {

    private var _listener: RestClient.OnRetrofitError? = null
    var listener
        get() = _listener
        set(value) {
            _listener = value
        }

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

    /*    private var _topGainerResponse = MutableLiveData<CoinsResponse>()
        val topGainerResponse get() = _topGainerResponse

        private var _topLooserResponse = MutableLiveData<CoinsResponse>()
        val topLooserResponse get() = _topLooserResponse

        private var _stableResponse = MutableLiveData<CoinsResponse>()
        val stableResponse get() = _stableResponse*/

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

    private var _logoutResponse = MutableLiveData<MessageResponse>()
    val logoutResponse get() = _logoutResponse

    private var _transactionResponse = MutableLiveData<TransactionResponse>()
    val transactionResponse get() = _transactionResponse

    private var _otpForPinChangeResponse = MutableLiveData<MessageResponse>()
    val otpPinChangeResponse get() = _otpForPinChangeResponse

    private var _verifyPhoneForPinResponse = MutableLiveData<MessageResponse>()
    val verifyPhoneForPinResponse get() = _verifyPhoneForPinResponse

    private var _updatePinResponse = MutableLiveData<MessageResponse>()
    val updatePinResponse get() = _updatePinResponse

    private var _getAssetsResponse = MutableLiveData<MyAssetResponse>()
    val getAssetResponse get() = _getAssetsResponse

    private var _recurringInvestmentResponse = MutableLiveData<RecurringInvestmentResponse>()
    val recurringInvestmentResponse get() = _recurringInvestmentResponse

    private var _faceIdResponse = MutableLiveData<MessageResponse>()
    val faceIdResponse get() = _faceIdResponse

    private var _addBankResponse = MutableLiveData<MessageResponse>()
    val addBankAccount get() = _addBankResponse

    private var _enableStrongAuthentication = MutableLiveData<MessageResponse>()
    val enableStrongAuthentication get() = _enableStrongAuthentication

    private var _verifyStrongAuthentication = MutableLiveData<MessageResponse>()
    val verifyStrongAuthentication get() = _verifyStrongAuthentication

    private var _enableWhitelisting = MutableLiveData<MessageResponse>()
    val enableWhitelisting get() = _enableWhitelisting

    private var _networksResponse = MutableLiveData<NetworksResponse>()
    val networkResponse get() = _networksResponse

    private var _networksResponseSingle = MutableLiveData<NetworkResponse>()
    val singleNetworkResponse get() = _networksResponseSingle

    private var _exchangeListingResponse = MutableLiveData<ExchangeListingResponse>()
    val exchangeListingResponse get() = _exchangeListingResponse

    private var _addWhitelistResponse = MutableLiveData<MessageResponse>()
    val addWhitelistResponse get() = _addWhitelistResponse
    private var _updateUserInfoResponse = MutableLiveData<MessageResponse>()
    val updateUserInfoResponse get() = _updateUserInfoResponse

    private var _getWhiteListing = MutableLiveData<WhitelistingResponse>()
    val getWhiteListing get() = _getWhiteListing

    private var _searchWhitelisting = MutableLiveData<WhitelistingResponse>()
    val searchWhitelisting get() = _searchWhitelisting

    private var _assetsWhitelisting = MutableLiveData<WhitelistingResponse>()
    val assetsWhitelisting get() = _assetsWhitelisting

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
    val priceGraphResponse get() = _priceGraphResponse

    private val _withdrawFiatResponse = MutableLiveData<MessageResponse>()
    val withdrawFiatResponse get() = _withdrawFiatResponse


    private val _userChallengeResponse = MutableLiveData<UserChallengeResponse>()
    val userChallengeResponse get() = _userChallengeResponse

    private val _userLoginResponse = MutableLiveData<UserLoginResponse>()
    val commonResponseWithdraw = MutableLiveData<CommonResponseVerfiy>()
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

    fun cancelJob() {

    }

    fun educationStrategy() {
        viewModelScope.launch(exceptionHandler) {
            val res = RestClient.get().educationStrategy()
            if (res.isSuccessful)
                _educationStrategyResponse.postValue(res.body())
            else listener?.onRetrofitError(res.errorBody())
        }
    }

    fun getStrategies() {
        viewModelScope.launch(exceptionHandler) {
            val map = HashMap<String,Any>()
            map.put("type","all")
            val res = RestClient.get().getStrategies(map)
            if (res.isSuccessful)
                _getStrategiesResponse.postValue(res.body())
            else listener?.onRetrofitError(res.errorBody())
        }
    }

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
                else listener?.onRetrofitError(res.errorBody())
            }
        }
    }
    fun editOwnStrategy(strategyName: String, addedAsset: List<AddedAsset>) {

        val list = arrayListOf<PortfolioViewModel.ChooseAssets>()
        for (i in addedAsset)
            list.add(
                PortfolioViewModel.ChooseAssets(
                    i.addAsset.id,
                    i.allocation.toInt()
                )
            )
        val hashMap = hashMapOf<String, Any>()
        hashMap["bundle"] = list
        hashMap["strategyName"] = strategyName

        viewModelScope.launch(exceptionHandler) {
            val res = RestClient.get().editStrategy(hashMap)
            if (res.isSuccessful)
                _buildStrategyResponse.postValue(res.body())
            else listener?.onRetrofitError(res.errorBody())
        }

    }
    fun buildOwnStrategy(strategyName: String, addedAsset: List<AddedAsset>) {

        val list = arrayListOf<PortfolioViewModel.ChooseAssets>()
        var total = 0
        for (i in addedAsset) {
            total = total + i.allocation.toInt()
            list.add(
                PortfolioViewModel.ChooseAssets(
                    i.addAsset.id,
                    i.allocation.toInt()
                )
            )
        }
        if (total<100){
            list[0].share = list[0].share+(100 - total)
        }
        val hashMap = hashMapOf<String, Any>()
        hashMap["bundle"] = list
        hashMap["strategyType"] = "SingleAsset"
        hashMap["strategyName"] = strategyName

        viewModelScope.launch(exceptionHandler) {
            val res = RestClient.get().chooseStrategy(hashMap)
            if (res.isSuccessful)
                _buildStrategyResponse.postValue(res.body())
            else listener?.onRetrofitError(res.errorBody())
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
            else listener?.onRetrofitError(res.errorBody())
        }
    }

    fun investSingleAsset(
        coinDetail: AssetBaseData?,
        amount: Int,
        assetAmount: Float,
        frequency: String
    ) {
        coinDetail?.let {
            val hashMap: HashMap<String, Any> = hashMapOf()
            hashMap["asset_id"] = it.id
            hashMap["amount"] = amount
            hashMap["asset_name"] = it.fullName
            hashMap["asset_amount"] = assetAmount
            if (frequency.isNotEmpty())
                hashMap["frequency"] = frequency
            viewModelScope.launch(exceptionHandler) {
                val res = RestClient.get().investOnSingleAsset(hashMap)
                if (res.isSuccessful)
                    _investSingleAssetResponse.postValue(res.body())
                else listener?.onRetrofitError(res.errorBody())
            }
        }
    }

    fun investStrategy(strategyId: String, frequency: String, amount: Int,strateggyName:String) {
        viewModelScope.launch(exceptionHandler) {
            val res = RestClient.get().investOnStrategy(
                hashMapOf(
                    "strategyName" to strateggyName,
                    "ownerUuid" to strategyId,
                    "frequency" to frequency,
                    "amount" to amount
                )
            )
            if (res.isSuccessful)
                _investStrategyResponse.postValue(res.body())
            else listener?.onRetrofitError(res.errorBody())
        }
    }
    fun pauseStrategy(strategyId: String,strateggyName:String) {
        viewModelScope.launch(exceptionHandler) {
            val res = RestClient.get().pauseStrategy(
                hashMapOf(
                    "strategyName" to strateggyName,
                    "ownerUuid" to strategyId
                )
            )
            if (res.isSuccessful)
                pauseStrategyResponse.postValue(res.body())
            else listener?.onRetrofitError(res.errorBody())
        }
    }
    fun deleteStrategy(strateggyName:String) {
        viewModelScope.launch(exceptionHandler) {
            val res = RestClient.get().deleteStrategy(
                hashMapOf(
                    "strategyName" to strateggyName
                )
            )
            if (res.isSuccessful)
                pauseStrategyResponse.postValue(res.body())
            else listener?.onRetrofitError(res.errorBody())
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
            else listener?.onRetrofitError(res.errorBody())
        }
    }

    fun confirmOrder(order: String) {
        viewModelScope.launch(exceptionHandler) {
            val hashMap = hashMapOf<String, Any>()
            hashMap["orderId"] = order
            val res = RestClient.get().acceptQuote(hashMap)
            if (res.isSuccessful)
                exchangeResponse.postValue(res.body())
            else listener?.onRetrofitError(res.errorBody())
        }
    }

    fun exchange(
        assetIdFrom: String,
        assetIdTo: String,
        exchangeFromAmount: String,
        exchangeToAmount: String
    ) {
        viewModelScope.launch(exceptionHandler) {
            val hashMap = hashMapOf<String, Any>()
            hashMap["exchange_from"] = assetIdFrom.lowercase()
            hashMap["exchange_to"] = assetIdTo.lowercase()
            hashMap["exchange_from_amount"] = exchangeFromAmount
            hashMap["exchange_to_amount"] = exchangeToAmount
            val res = RestClient.get().swapCrypto(hashMap)
            if (res.isSuccessful)
                exchangeResponse.postValue(res.body())
            else listener?.onRetrofitError(res.errorBody())
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
            else listener?.onRetrofitError(res.errorBody())
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
            else listener?.onRetrofitError(res.errorBody())
        }
    }

    fun logout(deviceId: String) {
        viewModelScope.launch(exceptionHandler) {
            val res = RestClient.get().logout(hashMapOf("device_id" to deviceId))
            if (res.isSuccessful)
                _logoutResponse.postValue(res.body())
            else listener?.onRetrofitError(res.errorBody())
        }
    }

    fun sendOtpPinChange() {
        viewModelScope.launch(exceptionHandler) {
            val res = RestClient.get().sendOtpForChangePin()
            if (res.isSuccessful)
                _otpForPinChangeResponse.postValue(res.body())
            else listener?.onRetrofitError(res.errorBody())
        }
    }

    fun verifyPhoneForPin(otp: String) {
        viewModelScope.launch(exceptionHandler) {
            val res = RestClient.get().verifyPhoneForPinChange(hashMapOf("otp" to otp.toInt()))
            if (res.isSuccessful)
                _verifyPhoneForPinResponse.postValue(res.body())
            else listener?.onRetrofitError(res.errorBody())
        }
    }

    fun updatePin(pin: String) {
        viewModelScope.launch(exceptionHandler) {
            val res = RestClient.get().updatePin(hashMapOf("newPin" to pin.toInt()))
            if (res.isSuccessful)
                _updatePinResponse.postValue(res.body())
            else listener?.onRetrofitError(res.errorBody())
        }
    }

    fun getAssets() {
        viewModelScope.launch(exceptionHandler) {
            val res = RestClient.get().getAssets()
            if (res.isSuccessful)
                _getAssetsResponse.postValue(res.body())
            else listener?.onRetrofitError(res.errorBody())
        }
    }

    fun getRecurringInvestments() {
        viewModelScope.launch(exceptionHandler) {
            val res = RestClient.get().getRecurringInvestments()
            if (res.isSuccessful) _recurringInvestmentResponse.postValue(res.body())
            else listener?.onRetrofitError(res.errorBody())
        }
    }

    fun setFaceId(faceId: String, enableFaceId: Boolean) {
        val hashMap = hashMapOf<String, Any>()
        hashMap["face_id"] = faceId
        hashMap["enable_face_id"] = if (enableFaceId) 1 else 0
        viewModelScope.launch(exceptionHandler) {
            val res = RestClient.get().setFaceId(hashMap)
            if (res.isSuccessful)
                _faceIdResponse.postValue(res.body())
            else listener?.onRetrofitError(res.errorBody())
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
            else listener?.onRetrofitError(res.errorBody())
        }
    }

    fun enableStrongAuthentication(enable: Boolean) {
        viewModelScope.launch(exceptionHandler) {
            val res = RestClient.get().enableStrongAuthentication(hashMapOf("enable" to enable))
            if (res.isSuccessful)
                _enableStrongAuthentication.postValue(res.body())
            else listener?.onRetrofitError(res.errorBody())
        }
    }

    fun verifyStrongAuthentication(otp: String) {
        viewModelScope.launch(exceptionHandler) {
            val res = RestClient.get().verifyStrongAuthentication(hashMapOf("otp" to otp.toInt()))
            if (res.isSuccessful)
                _verifyStrongAuthentication.postValue(res.body())
            else listener?.onRetrofitError(res.errorBody())
        }
    }

    fun enableWhitelisting(enable: Boolean, extraSecurity: String) {
        viewModelScope.launch(exceptionHandler) {
            val res = RestClient.get().enableWhitelisting(
                hashMapOf(
                    "enable" to enable,
                    "extra_security" to extraSecurity
                )
            )
            if (res.isSuccessful)
                _enableWhitelisting.postValue(res.body())
            else listener?.onRetrofitError(res.errorBody())

        }
    }


    fun getNetwork(id: String) {
        viewModelScope.launch(exceptionHandler) {
            val res = RestClient.get().getNetworkById(id)
            if (res.isSuccessful)
                _networksResponseSingle.postValue(res.body())
            else listener?.onRetrofitError(res.errorBody())
        }
    }

    fun getNetworks() {
        viewModelScope.launch(exceptionHandler) {
            val res = RestClient.get().getNetworks()
            if (res.isSuccessful)
                _networksResponse.postValue(res.body())
            else listener?.onRetrofitError(res.errorBody())
        }
    }

    fun getExchangeListing() {
        viewModelScope.launch(exceptionHandler) {
            val res = RestClient.get().getExchangeListing()
            if (res.isSuccessful)
                _exchangeListingResponse.postValue(res.body())
            else listener?.onRetrofitError(res.errorBody())
        }
    }

    fun addAddress(
        name: String,
        network: String,
        address: String,
        origin: String,
        exchange: String,
        logo: String
    ) {
        viewModelScope.launch(exceptionHandler) {
            val hashMap: HashMap<String, Any> = hashMapOf()
            hashMap["name"] = name
            hashMap["network"] = network
            hashMap["address"] = address
            hashMap["origin"] = origin.lowercase()
            if (exchange.isNotEmpty())
                hashMap["exchange"] = exchange
            val res = RestClient.get().addWhitelistingAddress(hashMap)
            if (res.isSuccessful)
                _addWhitelistResponse.postValue(res.body())
            else listener?.onRetrofitError(res.errorBody())
        }
    }
    fun updateWithdrawalLock(
        lock: String
    ) {
        viewModelScope.launch(exceptionHandler) {
            val hashMap: HashMap<String, Any> = hashMapOf()
            hashMap["withdrawalLock"] = lock
            val res = RestClient.get().updateUserInfo(hashMap)
            if (res.isSuccessful)
                _updateUserInfoResponse.postValue(res.body())
            else listener?.onRetrofitError(res.errorBody())
        }
    }
    fun updateAvtaar(
        lock: String
    ) {
        viewModelScope.launch(exceptionHandler) {
            val hashMap: HashMap<String, Any> = hashMapOf()
            hashMap["avatar"] = lock
            val res = RestClient.get().updateUserInfo(hashMap)
            if (res.isSuccessful)
                _updateUserInfoResponse.postValue(res.body())
            else listener?.onRetrofitError(res.errorBody())
        }
    }

    fun getWhiteListings() {
        viewModelScope.launch(exceptionHandler) {
            val res = RestClient.get().getWhitelistedAddress()
            if (res.isSuccessful)
                _getWhiteListing.postValue(res.body())
            else listener?.onRetrofitError(res.errorBody())
        }
    }


    fun searchWhitelist(keyword: String) {
        viewModelScope.launch(exceptionHandler) {
            val res = RestClient.get().getWhitelistedAddress(keyword = keyword)
            if (res.isSuccessful)
                _searchWhitelisting.postValue(res.body())
            else listener?.onRetrofitError(res.errorBody())
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
            else listener?.onRetrofitError(res.errorBody())
        }
    }

    fun deleteWhiteList(address: String, network: String) {
        viewModelScope.launch(exceptionHandler) {
            val map = HashMap<String, Any>()
            map["network"] = network
            map["address"] = address
            val res = RestClient.get().deleteWhiteListing(map)
            if (res.isSuccessful) _deleteWhiteListResponse.postValue(res.body())
            else listener?.onRetrofitError(res.errorBody())
        }
    }

    fun updateWhiteList(hashMap: HashMap<String, Any>) {
        viewModelScope.launch(exceptionHandler) {
            val res = RestClient.get().updateWhiteList(hashMap)
            if (res.isSuccessful)
                _updateWhiteListResponse.postValue(res.body())
            else listener?.onRetrofitError(res.errorBody())
        }
    }

    fun updateUser(hashMap: HashMap<String, Any>) {
        viewModelScope.launch(exceptionHandler) {
            val res = RestClient.get().updateUser(hashMap)
            if (res.isSuccessful)
                _updateUserResponse.postValue(res.body())
            else listener?.onRetrofitError(res.errorBody())
        }
    }

    fun assetsToChoose(keyword: String = "") {
        viewModelScope.launch(exceptionHandler) {
            val res = if (keyword.isEmpty()) RestClient.get().getAssetsToChoose()
            else RestClient.get().getAssetsToChoose(keyword)
            if (res.isSuccessful) _assetsToChoose.postValue(res.body())
            else listener?.onRetrofitError(res.errorBody())
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
            } else listener?.onRetrofitError(res.errorBody())
        }
    }

    fun getAllAssets() {
        viewModelScope.launch(exceptionHandler) {
            val res = RestClient.get().getAllAssets()
            if (res.isSuccessful) _allAssets.postValue(res.body())
            else listener?.onRetrofitError(res.errorBody())
        }
    }

    fun getWithdrawalAddresses() {
        viewModelScope.launch(exceptionHandler) {
            val res = RestClient.get().getWithdrawalAddress()
            if (res.isSuccessful) _withdrawalAddresses.postValue(res.body())
            else listener?.onRetrofitError(res.errorBody())
        }
    }

    fun getAssetDetail(assetId: String) {
        viewModelScope.launch(exceptionHandler) {
            val res = RestClient.get().getAssetDetail(assetId)
            if (res.isSuccessful) _getAssetDetail.postValue(res.body())
            else listener?.onRetrofitError(res.errorBody())
        }
    }

    fun getAssetDetailIncludeNetworks(assetId: String) {
        viewModelScope.launch(exceptionHandler) {
            val res = RestClient.get().getAssetDetail(assetId, "true")
            if (res.isSuccessful) _getAssetDetail.postValue(res.body())
            else listener?.onRetrofitError(res.errorBody())
        }
    }

    fun getAddress(network: String, assetId: String) {
        viewModelScope.launch(exceptionHandler) {
            val res = RestClient.get().getAddress(network, assetId)
            if (res.isSuccessful) _getAddress.postValue(res.body())
            else listener?.onRetrofitError(res.errorBody())
        }
    }

    fun getRecurringInvestmentDetail(investmentId: String) {
        viewModelScope.launch(exceptionHandler) {
            val res = RestClient.get().getRecurringInvestmentDetail(investmentId)
            if (res.isSuccessful)
                _recurringInvestmentDetail.postValue(res.body())
            else listener?.onRetrofitError(res.errorBody())
        }
    }

    fun cancelRecurringInvestment(investmentId: String) {
        viewModelScope.launch(exceptionHandler) {
            val res = RestClient.get().cancelRecurringInvestment(hashMapOf("id" to investmentId))
            if (res.isSuccessful) _cancelRecurringInvestment.postValue(res.body())
            else listener?.onRetrofitError(res.errorBody())
        }
    }

    fun getPriceGraph(assetId: String, duration: Duration) {
        viewModelScope.launch(exceptionHandler) {
            val res = RestClient.get().getPriceGraph(assetId, duration.duration)
            if (res.isSuccessful)
                _priceGraphResponse.postValue(res.body())
            else listener?.onRetrofitError(res.errorBody())
        }
    }

    fun withdrawFiat(amount: String) {
        viewModelScope.launch(exceptionHandler) {
            val res = RestClient.get().withdrawFiat(hashMapOf("amount" to amount))
            if (res.isSuccessful)
                _withdrawFiatResponse.postValue(res.body())
            else listener?.onRetrofitError(res.errorBody())
        }
    }

    fun userChallenge(phone: String = "", email: String = "") {
        viewModelScope.launch(exceptionHandler) {
            var phoneNumber = ""
            if (phone.isNotEmpty()) phoneNumber = phone.substring(1)//remove the "+"
            val param = phoneNumber.ifEmpty { email }
            val key = if (phone.isEmpty()) "email" else "phoneNo"
            val res = RestClient.get(Constants.NEW_BASE_URL).userChallenge(hashMapOf(key to param))
            if (res.isSuccessful)
                _userChallengeResponse.postValue(res.body())
            else listener?.onRetrofitError(res.errorBody())
        }
    }

    fun authenticateUser(a: String, m1: String) {
        viewModelScope.launch(exceptionHandler) {

            val param = hashMapOf<String, Any>()
            param["method"] = "srp"
            param["A"] = a
            param["M1"] = m1

            val res = RestClient.get(Constants.NEW_BASE_URL).userLogin(param)
            if (res.isSuccessful)
                _userLoginResponse.postValue(res.body())
            else listener?.onRetrofitError(res.errorBody())

        }
    }

    fun verify2FA(code: String) {
        viewModelScope.launch(exceptionHandler) {
            val hash = hashMapOf<String, Any>()
            hash["code"] = code
            val res = RestClient.get(Constants.NEW_BASE_URL).verify2FA(hash)
            if (res.isSuccessful)
                _userLoginResponse.postValue(res.body())
            else listener?.onRetrofitError(res.errorBody())
        }
    }
    fun verify2FAWithdraw(code: String){
        viewModelScope.launch(exceptionHandler) {
            val hash = hashMapOf<String, Any>()
            hash["code"] = code
            val res = RestClient.get(Constants.NEW_BASE_URL).verify2FAWithdraw(hash)
            if (res.isSuccessful)
                commonResponseWithdraw.postValue(res.body())

            else listener?.onRetrofitError(res.errorBody())
        }
    }


    fun refreshToken() {
        viewModelScope.launch(exceptionHandler) {
            val hashMap = hashMapOf<String, Any>()
            hashMap["method"] = "refresh_token"
            hashMap["refresh_token"] = App.prefsManager.refreshToken
            val res = RestClient.get(Constants.NEW_BASE_URL).userLogin(hashMap)
            if (res.isSuccessful)
                _userLoginResponse.postValue(res.body())
            else listener?.onRetrofitError(res.errorBody())
        }
    }

    fun setPhone(countryCode: String, phone: String) {
        viewModelScope.launch(exceptionHandler) {
            val res = RestClient.get(Constants.NEW_BASE_URL)
                .setPhone(hashMapOf("countryCode" to countryCode.toInt(), "phoneNo" to phone))
            if (res.isSuccessful)
                _setPhoneResponse.postValue(res.body())
            else listener?.onRetrofitError(res.errorBody())
        }
    }

    fun verifyPhone(code: String) {
        viewModelScope.launch(exceptionHandler) {
            val res = RestClient.get(Constants.NEW_BASE_URL)
                .verifyPhone(hashMapOf("code" to code.toString()))
            if (res.isSuccessful)
                _verifyPhoneResponse.postValue(res.body())
            else listener?.onRetrofitError(res.errorBody())
        }
    }

    fun setUserInfo(
        firstName: String,
        lastName: String,
        birthPlace: String,
        birthDate: String,
        birthCountry: String,
        nationality: String,
        isUSCitizen: Boolean
    ) {
        viewModelScope.launch(exceptionHandler) {

            val hashMap = hashMapOf<String, Any>()
            hashMap["firstName"] = firstName
            hashMap["lastName"] = lastName
            hashMap["birthPlace"] = birthPlace
            hashMap["birthDate"] = birthDate
            hashMap["birthCountry"] = birthCountry
            hashMap["nationality"] = nationality
            hashMap["language"] = "EN"
            hashMap["isUSCitizen"] = isUSCitizen


            val res = RestClient.get(Constants.NEW_BASE_URL).setUserInfo(hashMap)
            if (res.isSuccessful)
                _setUserInfoResponse.postValue(res.body())
            else listener?.onRetrofitError(res.errorBody())
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

            val res = RestClient.get(Constants.NEW_BASE_URL).setEmail(hashMap)
            if (res.isSuccessful)
                _setUpEmailResponse.postValue(res.body())
            else listener?.onRetrofitError(res.errorBody())
        }
    }


    fun verifyEmail(/*uuid: String,*/ code: String) {
        viewModelScope.launch(exceptionHandler) {

            val hash = hashMapOf<String, Any>()
//            hash["uuid"] = uuid
            hash["code"] = code
            try {
                val res = RestClient.get(Constants.NEW_BASE_URL).verifyEmail(hash)
                if (res.isSuccessful)
                    _verifyEmailResponse.postValue(res.body())
                else listener?.onRetrofitError(res.errorBody())
            } catch (e: Exception) {
                _verifyEmailResponse.postValue("Email verified")
            }

        }
    }


    fun setUserAddress(
        streetNumber: String,
        street: String,
        city: String,
        stateOrProvince: String,
        zipCode: String,
        country: String
    ) {
        viewModelScope.launch(exceptionHandler) {
            val hash = hashMapOf<String, Any>()
            if (streetNumber.isNotEmpty())
            hash["streetNumber"] = streetNumber
            if (street.isNotEmpty())
            hash["street"] = street.toString()
            hash["city"] = city
            hash["stateOrProvince"] = stateOrProvince
            hash["zipCode"] = zipCode
            hash["country"] = country

            val res = RestClient.get(Constants.NEW_BASE_URL).setUserAddress(hash)
            if (res.isSuccessful)
                _setUserAddressResponse.postValue(res.body())
            else listener?.onRetrofitError(res.errorBody())
        }
    }

    fun finishRegistration() {
        viewModelScope.launch(exceptionHandler) {
            val res = RestClient.get(Constants.NEW_BASE_URL).finishRegistration()
            if (res.isSuccessful)
                _finishRegistrationResponse.postValue(res.body())
            else listener?.onRetrofitError(res.errorBody())
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
            val res = RestClient.get(Constants.NEW_BASE_URL).setInvestmentExp(hash)
            if (res.isSuccessful)
                _setInvestmentExpResponse.postValue(res.body())
            else listener?.onRetrofitError(res.errorBody())
        }

    }

    fun getUser() {
        viewModelScope.launch(exceptionHandler) {
            val res = RestClient.get(Constants.NEW_BASE_URL).getUser()
            if (res.isSuccessful)
                _getUserResponse.postValue(res.body())
            else listener?.onRetrofitError(res.errorBody())
        }
    }

    fun getNews(id: String = "btc") {
        viewModelScope.launch(exceptionHandler) {
            val res = RestClient.get(Constants.NEW_BASE_URL).getNews(id)
            if (res.isSuccessful)
                _newsResponse.postValue(res.body())
            else listener?.onRetrofitError(res.errorBody())
        }
    }

    fun getPrice(id: String = "btc", tf: String = "1h") {
        viewModelScope.launch(exceptionHandler) {
            val res = RestClient.get(Constants.NEW_BASE_URL).getPrice(id, tf)
            if (res.isSuccessful)
                _priceResponse.postValue(res.body())
            else listener?.onRetrofitError(res.errorBody())
        }
    }

    fun createWithdrawalRequest(assetId: String,amount: Double,destination: String,network: String,code: String){
        viewModelScope.launch(exceptionHandler){
            val map = HashMap<String,Any>()
            map["asset"] = assetId
            map["amount"] = amount
            map["destination"] = destination
            map["network"] = network
            map["otp"] = code
            val res = RestClient.get(Constants.NEW_BASE_URL).createWithdrawalRequest(map)
            if (res.isSuccessful){
                _commonResponse.postValue(res.body())
            }else{
                listener!!.onRetrofitError(res.errorBody())
            }
        }
    }

    fun getOtpForWithdraw(action: String, details: String) {
        viewModelScope.launch(exceptionHandler) {
            val map = HashMap<String, Any>()
            map["action"] = action
            map["details"] = details
            val res = RestClient.get(Constants.NEW_BASE_URL).getOtpForWithdraw(map)
            if (res.isSuccessful) {
                _commonResponse.postValue(res.body())
            } else {
                listener!!.onRetrofitError(res.errorBody())
            }
        }
    }

    fun startKYC(){
        viewModelScope.launch(exceptionHandler) {
            val res = RestClient.get(Constants.NEW_BASE_URL).startKyc()
            if (res.isSuccessful){
                _kycResponse.postValue(res.body())
            }else{
                _listener?.onRetrofitError(res.errorBody())
            }
        }
    }
    fun getBalanceApi() {
        viewModelScope.launch(exceptionHandler) {
            val res = RestClient.get(Constants.NEW_BASE_URL).getBalance()
            if (res.isSuccessful)
                _balanceResponse.postValue(res.body())
            else listener?.onRetrofitError(res.errorBody())
        }
    }

}