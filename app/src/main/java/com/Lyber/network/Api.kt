package com.Lyber.network


import com.Lyber.models.AssetBaseDataResponse
import com.Lyber.models.AssetDetailBaseDataResponse
import com.Lyber.models.BalanceResponse
import com.Lyber.models.CoinsResponse
import com.Lyber.models.CommonResponse
import com.Lyber.models.CommonResponseVerfiy
import com.Lyber.models.EnterPhoneResponse
import com.Lyber.models.ExchangeListingResponse
import com.Lyber.models.GetAddress
import com.Lyber.models.GetAssetsResponse
import com.Lyber.models.GetQuoteResponse
import com.Lyber.models.GetUserResponse
import com.Lyber.models.KYCResponse
import com.Lyber.models.KycStatusResponse
import com.Lyber.models.MessageResponse
import com.Lyber.models.MessageResponsePause
import com.Lyber.models.MyAssetResponse
import com.Lyber.models.NetworkResponse
import com.Lyber.models.NetworksResponse
import com.Lyber.models.NewsResponse
import com.Lyber.models.OrderResponseData
import com.Lyber.models.PriceGraphResponse
import com.Lyber.models.PriceResponse
import com.Lyber.models.PriceServiceResumeResponse
import com.Lyber.models.RecurringInvestmentDetailResponse
import com.Lyber.models.RecurringInvestmentResponse
import com.Lyber.models.SetPhoneResponse
import com.Lyber.models.StrategiesResponse
import com.Lyber.models.TransactionResponse
import com.Lyber.models.UploadResponse
import com.Lyber.models.User
import com.Lyber.models.UserChallengeResponse
import com.Lyber.models.UserLoginResponse
import com.Lyber.models.WhitelistingResponse
import com.Lyber.models.WithdrawalAddress
import com.Lyber.models.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface Api {
    @Multipart
    @POST("upload")
    suspend fun upload(@Part filePart: MultipartBody.Part): Response<UploadResponse>

    @POST("kyc-service/kyc")
    suspend fun startKyc(): Response<KYCResponse>


    @POST("user/invest/education") // may be not in use
    suspend fun educationStrategy(): Response<MessageResponse>

    @GET("strategy-service/strategies")
    suspend fun getStrategies(@QueryMap hashMap: HashMap<String, Any>): Response<StrategiesResponse>

    @POST("strategy-service/strategy")
    suspend fun chooseStrategy(@Body hashMap: HashMap<String, Any>): Response<MessageResponse>

    @GET("coingecko/coins")
    suspend fun trendingCoins(
        @Query("order") order: String,
        @Query("limit") limit: Int = 100
    ): Response<CoinsResponse>

    @GET("coingecko/coins")
    suspend fun trendingCoin(
        @Query("order") order: String,
        @Query("keyword") keyword: String,
        @Query("limit") limit: Int = 100
    ): Response<CoinsResponse>

    @POST("order-service/quote")
    suspend fun getQuote(@Body hashMap: HashMap<String, Any>): Response<GetQuoteResponse>

    @POST("user-service/logout")
    suspend fun logout(): Response<BooleanResponse>

    @PATCH("strategy-service/strategy")
    suspend fun editStrategy(@Body hashMap: HashMap<String, Any>): Response<MessageResponse>

    @POST("strategy-service/active-strategy")
    suspend fun investOnStrategy(@Body hashMap: HashMap<String, Any>): Response<MessageResponse>

    @HTTP(method = "DELETE", path = "strategy-service/active-strategy", hasBody = true)
    suspend fun pauseStrategy(@Body hashMap: HashMap<String, Any>): Response<MessageResponsePause>

    @HTTP(method = "DELETE", path = "strategy-service/strategy", hasBody = true)
    suspend fun deleteStrategy(@Body hashMap: HashMap<String, Any>): Response<MessageResponsePause>

    @POST("order-service/accept-quote")
    suspend fun acceptQuote(@Body hashMap: HashMap<String, Any>): Response<MessageResponse>

    @POST("user/withdraw-crypto")
    suspend fun withdrawCryptos(@Body hashMap: HashMap<String, Any>): Response<MessageResponse>

    @GET("user/transactions")
    suspend fun getTransactions(
        @Query("asset_id") assetId: String = "",
        @Query("page") page: Int,
        @Query("limit") limit: Int
    ): Response<TransactionResponse>


    @GET("user/transactions")
    suspend fun getTransactions(
        @Query("page") page: Int,
        @Query("limit") limit: Int
    ): Response<TransactionResponse>


    @POST("user/pin/verify-phone") // not in use
    suspend fun verifyPhoneForPinChange(@Body hashMap: HashMap<String, Any>): Response<MessageResponse>

    @PUT("user/pin") // not in use
    suspend fun updatePin(@Body hashMap: HashMap<String, Any>): Response<MessageResponse>


    @POST("user/bank-info")  // to confirm
    suspend fun addBank(@Body hashMap: HashMap<String, Any>): Response<MessageResponse>


    /* strong authentication */

    @POST("user/verify/strong-auth") // not in use
    suspend fun verifyStrongAuthentication(@Body hashMap: HashMap<String, Any>): Response<MessageResponse>

    @GET("aplo/venues")
    suspend fun getExchangeListing(): Response<ExchangeListingResponse>

    @GET("network-service/networks")
    suspend fun getNetworks(): Response<NetworksResponse>

    @GET("user/whitelisted-addresses")
    suspend fun getWhitelistedAddress(): Response<WhitelistingResponse>

    @GET("user/whitelisted-addresses")
    suspend fun getWhitelistedAddress(@Query("keyword") keyword: String): Response<WhitelistingResponse>

    @POST("wallet-service/withdrawal-address")
    suspend fun addWhitelistingAddress(@Body hashMap: HashMap<String, Any>): Response<MessageResponse>

    @PATCH("user-service/user")
    suspend fun updateUserInfo(@Body hashMap: HashMap<String, Any>): Response<MessageResponse>

    @HTTP(method = "DELETE", path = "wallet-service/withdrawal-address", hasBody = true)
    suspend fun deleteWhiteListing(@Body hashMap: HashMap<String, Any>): Response<MessageResponse>

    @PUT("user")
    suspend fun updateUser(@Body hashMap: HashMap<String, Any>): Response<MessageResponse>

    @GET("assets")
    suspend fun getAssetsToChoose(@Query("keyword") keyword: String): Response<GetAssetsResponse>

    @GET("assets")
    suspend fun getAssetsToChoose(): Response<GetAssetsResponse>

    @GET("price-service/resume")
    suspend fun getAllPriceResume(): Response<PriceServiceResumeResponse>

    @GET("asset-service/assets")
    suspend fun getAllAssets(): Response<AssetBaseDataResponse>

    @GET("asset-service/asset")
    suspend fun getAssetDetail(@Query("id") id: String): Response<AssetDetailBaseDataResponse>

    @GET("asset-service/asset")
    suspend fun getAssetDetail(
        @Query("id") id: String,
        @Query("include_networks") include_networks: String
    ): Response<AssetDetailBaseDataResponse>

    @GET("wallet-service/address")
    suspend fun getAddress(
        @Query("network") network: String,
        @Query("asset") asset: String
    ): Response<GetAddress>

    @GET("user/investment")
    suspend fun getRecurringInvestmentDetail(@Query("id") id: String): Response<RecurringInvestmentDetailResponse>

    @HTTP(method = "DELETE", path = "user/investment", hasBody = true)
    suspend fun cancelRecurringInvestment(@Body hashMap: HashMap<String, Any>): Response<MessageResponse>

    @GET("coingecko/price-chart")
    suspend fun getPriceGraph(
        @Query("asset_symbol") assetId: String,
        @Query("duration") duration: Any
    ): Response<PriceGraphResponse>

    @POST("treezor/withdraw-fiat")
    suspend fun withdrawFiat(@Body hashMap: HashMap<String, Any>): Response<MessageResponse>

    @POST("user-service/challenge")
    suspend fun userChallenge(@Body hashMap: HashMap<String, Any>): Response<UserChallengeResponse>

    @POST("user-service/login")
    suspend fun userLogin(@Body hashMap: HashMap<String, Any>): Response<UserLoginResponse>

    @POST("user-service/set-user-info")
    suspend fun setUserInfo(@Body hashMap: HashMap<String, Any>): Response<MessageResponse>

    @POST("user-service/set-phone")
    suspend fun setPhone(@Body hashMap: HashMap<String, Any>): Response<SetPhoneResponse>

    @POST("user-service/verify-phone")
    suspend fun verifyPhone(@Body hashMap: HashMap<String, Any>): Response<MessageResponse>

    @POST("user-service/set-email-and-password")
    suspend fun setEmail(@Body hashMap: HashMap<String, Any>): Response<MessageResponse>

    @POST("user-service/verify-email")
    suspend fun verifyEmail(@Body hashMap: HashMap<String, Any>/*, @Query("uuid") uuid: String*/): Response<Any>


    @GET("user-service/user")
    suspend fun getUser(): Response<GetUserResponse>

    @POST("user-service/set-user-address")
    suspend fun setUserAddress(@Body hashMap: HashMap<String, Any>): Response<MessageResponse>

    @POST("user-service/set-user-investment-experience")
    suspend fun setInvestmentExp(@Body hashMap: HashMap<String, Any>): Response<MessageResponse>

    @POST("user-service/finish-registration")
    suspend fun finishRegistration(): Response<UserLoginResponse>

    @GET("user-service/2fa-otp")
    suspend fun getOtpForWithdraw(@QueryMap hashMap: HashMap<String, Any>): Response<CommonResponse>

    @GET("news-service/news")
    suspend fun getNews(@Query("id") id: String): Response<NewsResponse>

    @GET("price-service/price")
    suspend fun getPrice(
        @Query("id") id: String,
        @Query("tf") timeFrame: String
    ): Response<PriceResponse>

    @GET("wallet-service/balance")
    suspend fun getBalance(): Response<BalanceResponse>

    @GET("wallet-service/withdrawal-address")
    suspend fun getWithdrawalAddress(): Response<WithdrawalAddress>

    @POST("user-service/verify-2FA")
    suspend fun verify2FA(@Body hashMap: HashMap<String, Any>): Response<UserLoginResponse>


    @GET("user-service/export")
    suspend fun getOperationExport(
        @Query("date") date: String = ""
    ): Response<ExportResponse>

    @POST("user-service/contact-support")
    suspend fun contactSupport(@Body hashMap: HashMap<String, Any>): Response<BooleanResponse>

    @PATCH("user-service/user")
    suspend fun updateUserAuthentication(@Body hashMap: HashMap<String, Any>): Response<UpdateAuthenticateResponse>

    @GET("user-service/2fa-otp")
    suspend fun switchOffAuthentication(
        @Query("details") details: String,
        @Query("action") action: String
    ): Response<BooleanResponse>

    @GET("user-service/google-otp")
    suspend fun getQrUrl(): Response<QrCodeResponse>


    @POST("wallet-service/withdraw")
    suspend fun createWithdrawalRequest(@Body hashMap: HashMap<String, Any>): Response<CommonResponse>

    @GET("network-service/network")
    suspend fun getNetworkById(@Query("id") id: String): Response<NetworkResponse>


    @GET("user-service/transactions")
    suspend fun getTransactionsList(
        @Query("limit") limit: Int,
        @Query("offset") page: Int
    ): Response<TransactionList>

    @POST("user-service/reset-password")
    suspend fun resetNewPassword(@Body hashMap: HashMap<String, Any>): Response<BooleanResponse>

    @POST("user-service/forgot")
    suspend fun forgotPassword(@Body hashMap: HashMap<String, Any>): Response<BooleanResponse>

    @GET("user-service/reset-password-identifiers")
    suspend fun getResetPassword(): Response<res>


    @GET("user-service/password-change-challenge")
    suspend fun getPasswordChangeChallenge(): Response<ChangePasswordData>

    @POST("user-service/password")
    suspend fun changePassword(@Body hashMap: HashMap<String, Any>): Response<BooleanResponse>

    @POST("user-service/verify-password-change")
    suspend fun verifyPasswordChange(@Body hashMap: HashMap<String, Any>): Response<ExportResponse>

    @GET("order-service/order")
    suspend fun getOrder(
        @Query("orderId") orderId: String
    ): Response<OrderResponseData>

    @POST("strategy-service/strategy-execution")
    suspend fun oneTimeStrategyExecution(@Body hashMap: HashMap<String, Any>): Response<OneTimeStrategyData>

    @GET("strategy-service/strategy-execution")
    suspend fun checkStrategyStatus(@Query("executionId") executionId: String): Response<StrategyExecution>

    @POST("order-service/cancel-quote")
    suspend fun cancelQuote(@Body hashMap: HashMap<String, Any>): Response<BooleanResponse>

    @GET("notification-service/notifications")
    suspend fun getActivityLogsList(
        @Query("limit") limit: Int,
        @Query("offset") page: Int
    ): Response<ActivityLogs>

    @PATCH("user-service/close-account")
    suspend fun closeAccount(@Body hashMap: HashMap<String, Any>): Response<BooleanResponse>

    @GET("kyc-service/sign-url")
    suspend fun startSignUrl(): Response<SignURlResponse>

    @GET("wallet-service/history")
    suspend fun getWalletHistory(
        @Query("limit") limit: Int,
        @Query("daily") daily: Boolean
    ): Response<WalletHistoryResponse>

    @GET("strategy-service/active-strategy")
    suspend fun activeStrategies(): Response<ActiveStrategyResponse>

    @GET("price-service/resume")
    suspend fun getPriceResumeId(@Query("id") id: String): Response<PriceResumeByIdResponse>

    @PATCH("strategy-service/active-strategy")
    suspend fun editEnabledStrategy(@Body hashMap: HashMap<String, Any>): Response<MessageResponse>

    @POST("notification-service/register")
    suspend fun enableNotification(@Body hashMap: HashMap<String, Any>): Response<BooleanResponse>

    @GET("wallet-service/ribs")
    suspend fun getWalletServices(): Response<RIBResponse>

    @POST("wallet-service/rib")
    suspend fun addRib(@Body hashMap: HashMap<String, Any>): Response<BooleanResponse>

    @HTTP(method = "DELETE", path = "wallet-service/rib", hasBody = true)
    suspend fun deleteRIB(@Body hashMap: HashMap<String, Any>): Response<ExportResponse>

    @POST("wallet-service/withdraw-euro")
    suspend fun withdrawEuroRequest(@Body hashMap: HashMap<String, Any>): Response<CommonResponse>

    @GET("wallet-service/withdraw-euro-info")
    suspend fun getWithdrawEuroFee(): Response<WithdrawEuroFee>

    @GET("user-service/user-by-phone")
    suspend fun getUserNameByPhone(@Query("phone") phone: String): Response<UserByPhoneResponse>

    @POST("wallet-service/transfer-to-friend")
    suspend fun transferToFriend(@Body hashMap: HashMap<String, Any>): Response<BooleanResponse>

    @GET("price-service/lastPrice")
    suspend fun getCurrentPrice(@Query("id") id: String): Response<CurrentPriceResponse>
}
