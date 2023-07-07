package com.au.lyber.network

import com.au.lyber.models.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface Api {


    @POST("user/signup")
    suspend fun enterPhone(@Body hashMap: HashMap<String, Any>): Response<EnterPhoneResponse>

    @POST("user/verify/phone")
    suspend fun enterOtp(@Body hashMap: HashMap<String, Any>): Response<MessageResponse>

    @POST("user/set_login_pin")
    suspend fun setPin(@Body hashMap: HashMap<String, Any>): Response<MessageResponse>

    @POST("user/resend/phone-verification-otp")
    suspend fun resendOtp(): Response<MessageResponse>

    @POST("user/activate/face-id")
    suspend fun setFaceId(@Body hashMap: HashMap<String, Any>): Response<MessageResponse>

    @PUT("user/reset/notification")
    suspend fun enableNotification(@Body hashMap: HashMap<String, Any>): Response<MessageResponse>

    @POST("/user/resend/email")
    suspend fun sendEmail(@Body hashMap: HashMap<String, Any>): Response<MessageResponse>

    @GET("user/check_email_verification")
    suspend fun checkEmailVerification(): Response<MessageResponse>

    @POST("user/personal-info")
    suspend fun fillPersonalData(@Body hashMap: HashMap<String, Any>): Response<MessageResponse>

    @POST("user/login")
    suspend fun login(@Body hashMap: HashMap<String, Any>): Response<EnterPhoneResponse>

    @Multipart
    @POST("upload")
    suspend fun upload(@Part filePart: MultipartBody.Part): Response<UploadResponse>

    @GET("user/personal_info")
    suspend fun getPersonalInfo(): Response<User>

    @GET("treezor/kyc-liveness")
    suspend fun initiateKyc(): Response<InitiateKycResponse>

    @GET("treezor/kyc-status")
    suspend fun kycStatus(): Response<KycStatusResponse>

    @PUT("treezor/user")
    suspend fun updatePersonalInfo(@Body hashMap: HashMap<String, Any>): Response<MessageResponse>

    @POST("user/invest/education")
    suspend fun educationStrategy(): Response<MessageResponse>

    @GET("investment-strategies")
    suspend fun getStrategies(): Response<StrategiesResponse>

    @POST("user/investment-strategy")
    suspend fun chooseStrategy(@Body hashMap: HashMap<String, Any>): Response<MessageResponse>

    @GET("coingecko/coins")
    suspend fun trendingCoins(
        @Query("page") page: Int,
        @Query("limit") limit: Int,
        @Query("order") order: String
    ): Response<CoinsResponse>

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

    @PUT("user/treezor-status")
    suspend fun treezorStatus(@Body hashMap: HashMap<String, Any>): Response<MessageResponse>

    @POST("user/verify/pin")
    suspend fun verifyPin(@Body hashMap: HashMap<String, Any>): Response<MessageResponse>

    @POST("user/logout")
    suspend fun logout(@Body hashMap: HashMap<String, Any>): Response<MessageResponse>

    @POST("user/personal-info")
    suspend fun personalInfo(@Body hashMap: HashMap<String, Any>): Response<MessageResponse>

    @POST("treezor/user")
    suspend fun createUser(): Response<User>

    @POST("user/invest-on-asset")
    suspend fun investOnSingleAsset(@Body hashMap: HashMap<String, Any>): Response<MessageResponse>

    @POST("user/invest-on-strategy")
    suspend fun investOnStrategy(@Body hashMap: HashMap<String, Any>): Response<MessageResponse>

    @POST("user/swap-crypto")
    suspend fun swapCrypto(@Body hashMap: HashMap<String, Any>): Response<MessageResponse>

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


    @GET("user/assets")
    suspend fun getAssets(): Response<MyAssetResponse>

    /* change pin */

    @POST("user/pin/otp")
    suspend fun sendOtpForChangePin(): Response<MessageResponse>

    @POST("user/pin/verify-phone")
    suspend fun verifyPhoneForPinChange(@Body hashMap: HashMap<String, Any>): Response<MessageResponse>

    @PUT("user/pin")
    suspend fun updatePin(@Body hashMap: HashMap<String, Any>): Response<MessageResponse>

    @GET("user/investments")
    suspend fun getRecurringInvestments(): Response<RecurringInvestmentResponse>

    @GET("user/investments")
    suspend fun getRecurringInvestments(@Query("id") id: String): Response<RecurringInvestmentResponse>

    @POST("user/bank-info")
    suspend fun addBank(@Body hashMap: HashMap<String, Any>): Response<MessageResponse>


    /* strong authentication */

    @POST("user/enable/strong-auth")
    suspend fun enableStrongAuthentication(@Body hashMap: HashMap<String, Any>): Response<MessageResponse>

    @POST("user/verify/strong-auth")
    suspend fun verifyStrongAuthentication(@Body hashMap: HashMap<String, Any>): Response<MessageResponse>

    @POST("user/enable/whitelisting")
    suspend fun enableWhitelisting(@Body hashMap: HashMap<String, Any>): Response<MessageResponse>


    @GET("aplo/venues")
    suspend fun getExchangeListing(): Response<ExchangeListingResponse>

    @GET("user/networks")
    suspend fun getNetworks(): Response<NetworksResponse>

    @GET("user/whitelisted-addresses")
    suspend fun getWhitelistedAddress(): Response<WhitelistingResponse>

    @GET("user/whitelisted-addresses")
    suspend fun getWhitelistsAddress(@Query("asset") assetId: String): Response<WhitelistingResponse>

    @GET("user/whitelisted-addresses")
    suspend fun getWhitelistedAddress(@Query("keyword") keyword: String): Response<WhitelistingResponse>

    @POST("user/whitelist-address")
    suspend fun addWhitelistingAddress(@Body hashMap: HashMap<String, Any>): Response<MessageResponse>

    @HTTP(method = "DELETE", path = "user/whitelist-address", hasBody = true)
    suspend fun deleteWhiteListing(@Body hashMap: HashMap<String, Any>): Response<MessageResponse>

    @PUT("user/whitelist-address")
    suspend fun updateWhiteList(@Body hashMap: HashMap<String, Any>): Response<MessageResponse>

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

    @GET("news-service/news")
    suspend fun getNews(@Query("id") id: String): Response<NewsResponse>

    @GET("price-service/price")
    suspend fun getPrice(
        @Query("id") id: String,
        @Query("tf") timeFrame: String
    ): Response<PriceResponse>

    @GET("wallet-service/balance")
    suspend fun getBalance(): Response<BalanceResponse>

    @POST("user-service/verify-2FA")
    suspend fun verify2FA(@Body hashMap: HashMap<String, Any>): Response<UserLoginResponse>
}