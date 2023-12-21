package com.Lyber.models

import com.Lyber.R
import com.github.mikephil.charting.data.Entry


/* api responses */

data class ErrorResponse(
    val success: String,
    val error: String,
    val message: String,
    val type: String
)

data class CommonResponse(val success: String, val message: String)

data class MessageResponse(
    val message: String,
    val msg: String
)


data class KycStatusResponse(
    val bic: String,
    val comment: Any,
    val iban: String,
    val is_liveness_initiated: Boolean,
    val is_payin_done: Boolean,
    val kyc_level: Int,
    val kyc_review: Int,
    val kyc_status: Any,
    val score: Int,
    val ubble_link: String,
    val user_status: String,
    val wallet_status: String
)


data class InitiateKycResponse(
    val identification: Identification,
    val statusCode: Int
)

data class Identification(
    val identification_id: String,
    val identification_url: String
)


data class EnterPhoneResponse(
    val message: String,
    val token: String,
)


data class TreezorCreateUserResponse(
    val statusCode: Int,
    val users: List<UserTreezor>
)

data class UserTreezor(
    val activityOutsideEu: Any,
    val address1: String,
    val address2: String,
    val address3: Any,
    val birthCountry: String,
    val birthday: String,
    val city: String,
    val controllingPersonType: Int,
    val country: String,
    val countryName: String,
    val createdDate: String,
    val economicSanctions: Any,
    val effectiveBeneficiary: Int,
    val email: String,
    val employeeType: Int,
    val entityType: Int,
    val firstname: String,
    val incomeRange: String,
    val involvedSanctions: Any,
    val isFreezed: Int,
    val isFrozen: Any,
    val kycLevel: Int,
    val kycReview: Int,
    val kycReviewComment: String,
    val language: String,
    val lastname: String,
    val legalAnnualTurnOver: String,
    val legalForm: String,
    val legalName: String,
    val legalNameEmbossed: String,
    val legalNetIncomeRange: String,
    val legalNumberOfEmployeeRange: String,
    val legalRegistrationDate: String,
    val legalRegistrationNumber: String,
    val legalSector: String,
    val legalShareCapital: Int,
    val legalTvaNumber: String,
    val middleNames: String,
    val mobile: String,
    val modifiedDate: String,
    val nationality: String,
    val nationalityOther: String,
    val occupation: String,
    val optInMailing: Any,
    val parentType: String,
    val parentUserId: Int,
    val payinCount: Int,
    val personalAssets: String,
    val phone: String,
    val placeOfBirth: String,
    val position: String,
    val postcode: String,
    val residentCountriesSanctions: Any,
    val sepaCreditorIdentifier: String,
    val specifiedUSPerson: Int,
    val state: String,
    val taxNumber: String,
    val taxResidence: String,
    val timezone: Any,
    val title: String,
    val totalRows: String,
    val userId: Int,
    val userStatus: String,
    val userTag: String,
    val userTypeId: Int,
    val walletCount: Int
)


data class StrategiesResponse(
    val strategies: List<Strategy>
)

data class Strategy(
    val __v: Int,
    val _id: String,
    val is_own_strategy: Int = 0,
    val created_at: String,
    val investment_strategy_assets: List<InvestmentStrategyAsset>,
    val risk: String,
    val status: String? = "",
    val updated_at: Any,
    val yield: Int,
    val is_chosen: Int
)

data class InvestmentStrategyAsset(
    val __v: Int = 0,
    val _id: String,
    val allocation: Float,
    val asset_id: String,
    val created_at: String = System.currentTimeMillis().toString(),
    val investment_strategy_id: String = "",
    val updated_at: Any = ""
)


data class AddedAsset(val addAsset: Data, var allocation: Float)

data class AddAsset(
    val asset: Asset,
    val assetValue: String,
    val assetVariation: Float
)

enum class Asset(val image: Int, val nameCode: String) {
    Bitcoin(R.drawable.ic_bitcoin, "BTC"),
    Ether(R.drawable.ic_ether, "ETH"),
    Dogecoin(R.drawable.ic_dog, "DOGE"),
    Solana(R.drawable.ic_sol, "SOL"),
    USDC(R.drawable.ic_usdc, "ETH"),
    Euro(R.drawable.ic_euro, "Euro"),
    Ada(R.drawable.ic_ada, "ADA"),
    Luna(R.drawable.ic_bitcoin, "LUNA"),
    Xnd(R.drawable.ic_sol, "XND"),
    Matic(R.drawable.ic_matic, "MATIC")
}


data class DataBottomSheet(val title: String)


/* choose your strategy */
//data class Strategy(
//    val strategyTitle: String,
//    val yield: String,
//    val risk: String,
//    val assetItem: List<AssetItems>
//)

data class AssetItems(val assetName: String, val assetPercent: Int, val colorRes: Int)


data class MyAsset(
    val asset: Asset,
    val purchasedAmount: String,
    val purchasedAmountCrypto: String,
    val assetVariation: String
)

data class AnalyticsData(val title: String, val figure: String, val list: List<Entry>)

data class RecurringInvestmentData(
    val image: Int,
    val title: String,
    val amount: String,
    val frequency: String,
    val upcomingPayment: String
)

data class Resources(val imageRes: Int, val text: String)

data class PayModel(val title: String, val subtitle: String, val max: String, val image: Int)

data class Transactions(
    val image: Int = 0,
    val title: String = "",
    val subtitle: String = "",
    val amount: String = "",
    val amountInAssetValue: String = "",
    val type: Int = 1
)

data class UploadResponse(
    val file_name: String,
    val s3Url: String
)

data class PersonalDataResponse(
    val _id: String,
    val address1: String,
    val bic: String,
    val birth_country: String,
    val birth_place: String,
    val citizenship: Any,
    val city: String,
    val comment: Any,
    val country: String,
    val countryName: Any,
    val country_code: String,
    val dob: String,
    val doc_status: Any,
    val email: String,
    val first_name: String,
    val iban: String,
    val incomeRange: String,
    val income_source: Any,
    val investment_experience: Any,
    val kyc_level: Int,
    val kyc_review: Int,
    val kyc_status: String,
    val last_name: String,
    val nationality: String,
    val occupation: String,
    val personalAssets: String,
    val phone_no: Long,
    val score: Int,
    val specifiedUSPerson: Boolean,
    val state: String,
    val treezor_user_id: Any,
    val user_status: String,
    val wallet_id: Int,
    val wallet_status: String,
    val zip_code: Int
)

data class CoinResponse(
    val count: Int,
    val trending: List<CoinDetail>
)

data class CoinDetail(
    val name: String,
    val price: String,
    val currency: String,
    val logo_url: String,
    val price_change: String
)

data class CoinsResponse(
    val code: Int,
    val `data`: List<Data>?,
    val message: String,
    val success: Boolean
)
data class GetQuoteResponse(
    val code: Int,
    val data: DataQuote?,
    val message: String,
    val success: Boolean
)
data class DataQuote(val quoteId:String,
    val ratio:String,
    val inverseRatio:String,
    val validTimestamp:String,
    val toAmount:String,
    val fromAmount:String,
    val fromAmountDeductedFees:String,
    val fees :String,
    val orderId:String,
    val fromAsset:String,
    val toAsset:String)

data class Data(
    val ath: Double,
    val ath_change_percentage: Double,
    val ath_date: String,
    val atl: Double,
    val atl_change_percentage: Double,
    val atl_date: String,
    val circulating_supply: Double,
    val current_price: Double,
    val fully_diluted_valuation: Long,
    val high_24h: Double,
    val id: String,
    val image: String,
    val last_updated: String,
    val low_24h: Double,
    val market_cap: Long,
    val market_cap_change_24h: Double,
    val market_cap_change_percentage_24h: Double,
    val market_cap_rank: Int,
    val max_supply: Double,
    val name: String,
    val price_change_24h: Double,
    val price_change_percentage_24h: Double,
    val roi: Roi,
    val description: String,
    val total_balance: Double,
    val sparkline_in_7d: SparklineIn7d,
    val symbol: String?,
    val total_supply: Double,
    val total_volume: Double
)

data class Roi(
    val currency: String,
    val percentage: Double,
    val times: Double
)

data class SparklineIn7d(
    val price: List<Double>
)

data class TransactionResponse(
    val transactions: List<Transaction>
)

data class Transaction(
    val __v: Int,
    val _id: String,
    val asset_id: String,
    val created_at: String,
    val exchange_from: String,
    val exchange_to: String,
    val amount: Double,
    val asset_amount: Double,
    val exchange_to_amount: Double,
    val exchange_from_amount: Double,
    val status: Int,
    val type: Int,
    val user_id: String,
    val withdrawal_wallet_address: String
)

data class MyAssetResponse(
    val total_euros_available: Double,
    val assets: List<AssetBaseData>,
    val count: Int
)

data class RecurringInvestmentResponse(
    val investments: List<Investment>
)

data class Investment(
    val _id: String,
    val amount: Double,
    val asset_amount: Double,
    val asset_id: Any,
    val created_at: String,
    val frequency: String,
    val is_cancelled: Boolean,
    val logo: Any,
    val type: String,
    val upcoming_investment: Any,
    val updated_at: Any,
    val user_id: String,
    val user_investment_strategy_id: UserInvestmentStrategyId?
)

data class UserInvestmentStrategyId(
    val _id: String,
    val is_own_strategy: Int,
    val strategy_name: String
)

data class WhitelistingResponse(
    val addresses: List<Whitelistings>,
    val count: Int
)

data class Whitelistings(
    val __v: Int = 0,
    val _id: String = "",
    val logo: String,
    val address: String,
    val created_at: String = System.currentTimeMillis().toString(),
    val name: String,
    val network: String,
    val origin: String,
    val exchange: String = "",
    val tag: Any = "",
    val updated_at: Any = "",
    val user_id: String = ""
)

data class NetworksResponse(
    val networks: List<Network>
)

data class Network(
    val __v: Int = 1100,
    val _id: String = "",
    val asset_id: String,
    val createdAt: Long = System.currentTimeMillis(),
    val is_deleted: Boolean = false,
    val logo: String,
    val name: String
)

data class ExchangeListingResponse(
    val assets: List<ExchangeAsset>
)

data class ExchangeAsset(
    val code: String,
    val date: String,
    val name: String,
    val vid: Int
)

class GetAssetsResponse : ArrayList<GetAssetsResponseItem>()

data class GetAssetsResponseItem(
    val _id: String,
    val asset_id: String,
    val asset_name: String,
    val symbol: String,
    val image: String
)

class PriceServiceResumeResponse(
    val data: Map<String, PriceServiceResumeData>
)

data class PriceServiceResumeData(
    val lastPrice: String,
    val change: String,
    val squiggleURL: String,
    val isAuto: Boolean,
)

data class PriceServiceResume(
    val id: String,
    val priceServiceResumeData: PriceServiceResumeData
)

class AssetBaseDataResponse(
    val data: List<AssetBaseData>
)

data class AssetBaseData(
    val id: String,
    val fullName: String,
    val imageUrl: String,
    val isUIActive: Boolean,
    val isTradeActive: Boolean,
    val isStablecoin: Boolean,
    val isDepositActive: Boolean,
    val isWithdrawalActive: Boolean,
)

class AssetDetailBaseDataResponse(
    val data: AssetDetailBaseData
)

data class AssetDetailBaseData(
    val id: String,
    val fullName: String,
    val image: String,
    val about: Description,
    val marketRank: Int,
    val isUIActive: Boolean,
    val isTradeActive: Boolean,
    val isStablecoin: Boolean,
    val isDepositActive: Boolean,
    val isWithdrawalActive: Boolean,
    val networks:MutableList<NetworkDeposit>
)

data class NetworkDeposit(
    val id: String,
    val fullName: String,
    val addressRegex: String,
    val imageUrl: String,
    val isUIActive: Boolean,
    val binanceId: String,
    val withdrawMin: Float,
    val withdrawFee: Float,
    val isDepositActive: Boolean,
    val isWithdrawalActive: Boolean
)

data class Description(
    val fr: String,
    val en: String
)

data class RecurringInvestmentDetailResponse(
    val _id: String,
    val amount: Float,
    val asset_amount: Float,
    val asset_id: Any,
    val created_at: String,
    val frequency: String,
    val history: List<Transaction>,
    val is_cancelled: Boolean,
    val is_chosen: Any,
    val is_own_strategy: Int,
    val strategy_assets: List<StrategyAsset>,
    val strategy_name: String,
    val type: String,
    val user_id: String,
    val user_investment_strategy_id: String
)


data class StrategyAsset(
    val _id: String,
    val allocation: Float,
    val asset_id: String,
    val asset_name: Any
)


data class PriceGraphResponse(
    val stats: List<List<Double>>,
    val total_volumes: List<List<Double>>
)

enum class Duration(val duration: Any) {
    ONE_HOUR(1),
    FOUR_HOUR(1),
    DAY(1),
    WEEK(7),
    MONTH(30),
    YEAR(365),
    MAX("max")
}

/* new routes */

//class UserChallengeResponse : ArrayList<UserChallengeResponseItem>()

data class UserChallengeResponse(val data: ChallengeResponse)
data class ChallengeResponse(
    val B: String,
    val phoneNo: String,
    val salt: String,
    val token: String
)

data class UserLoginResponse(
    val data: LoginData
)
data class GetAddress(val data: Address)

data class Address(val address:String)
data class LoginData(
    val access_token: String,
    val refresh_token: String,
    val type2FA: String
)

data class SetPhoneResponse(val data: DataNew)

data class DataNew(val token: String)

data class GetUserResponse(val data: User)

data class User(
    val email: String,
    val firstName: String,
    val lastName: String,
    val nationality: String,
    val phoneNo: String,
    val profilePic: String,
    val profilePicType: String,
    val strongAuthentification: Boolean,
    val uuid: String
)

data class JWTPayload(
    val authorizationType: String,
    val exp: Int,
    val iat: Int,
    val uuid: String
)

data class NewsResponse(
    val data: List<News>
)

data class News(
    val url: String,
    val date: String,
    val image_url: String,
    val title: String
)

data class PriceResponse(
    val data: PriceData
)

data class PriceData(
    val lastUpdate: String,
    val prices: List<String>
)

data class BalanceResponse(
    val data: Map<String, BalanceData>
)

data class BalanceData(
    val balance: String,
    val euroBalance: String
)

data class Balance(
    val id: String,
    val balanceData: BalanceData
)

data class BooleanResponse(
    val success:Boolean
)



data class ResetPasswordResponse(
    val email: String,
    val phoneNo: String
)
data class res(
    val `data`: ResetPasswordResponse
)

