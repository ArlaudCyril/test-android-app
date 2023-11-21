package com.au.lyber.utils

import com.au.lyber.R
import okhttp3.internal.connection.Exchange

object Constants {
    const val ORDER_ID:String = "order_id"
    const val LOADING: Int = 0
    const val LOADING_SUCCESS: Int = 1
    const val LOADING_FAILURE: Int = 2
    const val FROM_SWAP: String = "from_swap"

    /* steps fill personal data */
    const val ACCOUNT_INITIALIZATION = 0
    const val PERSONAL_DATA = 1
    const val EMAIL_ADDRESS = 2
    const val EMAIL_VERIFIED = 3
    const val ADDRESS = 4
    const val INVESTMENT_EXP = 5

    const val ACCOUNT_CREATED = 1
    const val PERSONAL_DATA_FILLED = 2
    const val KYC_COMPLETED = 3
    const val PROFILE_COMPLETED = 4
    const val DONE_EDUCATION = 5


    const val IS_LOGOUT: String = "is_logout"


    //    const val BASE_URL = "http://104.211.21.101:3000/"
    const val BASE_URL = "https://staging.lyber.com/"
    const val NEW_BASE_URL = "https://staging.lyber.com/"
    const val SOCKET_BASE_URL = "ws://ws.lyber.com:80/websocket/"
    const val PICTURE_DIRECTORY = "Lyber/Images"

    const val FOR_LOGIN = "for_login"
    const val FOR_LOGOUT = "fromLogout"
    const val ADDRESS_ID = "address_id"
    const val INVESTMENT_ID = "investmentId"
    const val TYPE = "type"
    const val DATA_SELECTED = "dataSelected"
    const val NETWORK = "network"

    /* screen navigation for specific uses */
    const val USING_STRATEGY = "using_strategy"
    const val USING_SINGULAR_ASSET = "using_singular_asset"
    const val USING_DEPOSIT = "using_deposit"
    const val USING_WITHDRAW = "using_withdraw"
    const val SINGULAR = "SINGULAR"
    const val TO_EDIT = "toEdit"
    const val USING_EXCHANGE = "using_exchange"
    const val USING_SELL = "using_sell"
    const val NAME = "name"
    const val USING_ALL_PORTFOLIO = "using_all_portfolio"
    const val USING_WITHDRAW_FIAT = "using_withdraw_fiat"
    const val EMAIL_SENT = "emailSent"


    /* icons */
    const val EURO = "€"
    const val UP_ICON = "▲"
    const val DOWN_ICON = "\u25BC"

    /* coingecho api query params   */
    const val VOLUME_DESC = "volume_desc"
    const val HOURS_24_DESC = "hour_24_desc"
    const val HOURS_24_ASC = "hour_24_asc"
    const val STABLE_COINS = "stable_coins"

    /*  whitelisting duartion  */
    const val HOURS_72 = "72_HOURS"
    const val HOURS_24 = "24_HOURS"
    const val NO_EXTRA_SECURITY = "NO_EXTRA_SECURITY"

    const val JPEG_FILE_PREFIX: String = "IMG_"
    const val JPEG_FILE_SUFFIX: String = ".jpg"
    const val ADDRESS_STR = "address"
    const val ORIGIN = "origin"
    const val Exchange = "exchange"
    const val LOGO = "logo"

    const val BASE_IMAGE_URL_ORIGINAL: String = "https://lyberblob.blob.core.windows.net/original/"
    const val BASE_IMAGE_URL_SMALL: String = "https://lyberblob.blob.core.windows.net/small/"
    const val BASE_IMAGE_URL_MEDIUM: String = "https://lyberblob.blob.core.windows.net/medium/"

    const val SCAN_COMPLETE = "scan-complete"
    const val SCANNED_ADDRESS = "scanned-address"


    val defaults = mutableListOf<Int>().apply {
        add(R.drawable.one)
        add(R.drawable.two)
        add(R.drawable.three)
        add(R.drawable.four)
        add(R.drawable.five)
        add(R.drawable.six)
        add(R.drawable.seven)
        add(R.drawable.eight)
        add(R.drawable.nine)
        add(R.drawable.ten)
        add(R.drawable.eleven)
        add(R.drawable.twelve)
        add(R.drawable.thirteen)
        add(R.drawable.fourteen)
        add(R.drawable.fifteen)
        add(R.drawable.sixteen)
        add(R.drawable.seventeen)
        add(R.drawable.eighteen)
        add(R.drawable.nineteen)
        add(R.drawable.twenty)
        add(R.drawable.twenty_one)
        add(R.drawable.twenty_two)
        add(R.drawable.twenty_three)
        add(R.drawable.twenty_four)
        add(R.drawable.twenty_five)
        add(R.drawable.twenty_six)
        add(R.drawable.twenty_seven)
        add(R.drawable.twenty_eight)
        add(R.drawable.twenty_nine)
        add(R.drawable.thirty)
        add(R.drawable.thirty_one)
        add(R.drawable.thirty_two)
        add(R.drawable.thirty_three)
        add(R.drawable.thirty_four)
        add(R.drawable.thirty_five)
        add(R.drawable.thirty_six)
        add(R.drawable.thirty_seven)
        add(R.drawable.thirty_eight)
        add(R.drawable.thirty_nine)
        add(R.drawable.fourty)
    }

    val colors = mutableListOf<Int>().apply {
        add(R.color.purple_800)
//        add(R.color.purple_700)
        add(R.color.purple_600)
//        add(R.color.purple_500)
        add(R.color.purple_400)
//        add(R.color.purple_300)
        add(R.color.purple_200)
        add(R.color.purple_100)
        add(R.color.purple_00)
    }

    val assetColors = hashMapOf<String, Int>().apply {
        this["BTC"] = R.color.bitcoinColor
        this["ETH"] = R.color.etherColor
        this["SOL"] = R.color.solanaColor
        this["FTX"] = R.color.ftxColor
        this["BNB"] = R.color.bnbColor
        this["LUNA"] = R.color.lunaColor
        this["DOGE"] = R.color.dogecoinColor
        this["AVAX"] = R.color.avaxColor
        this["SRM"] = R.color.srmColor
        this["EGLD"] = R.color.egldColor
        this["LINK"] = R.color.linkColor
        this["ADA"] = R.color.adacoinColor
        this["XRP"] = R.color.xrpColor
        this["SHIB"] = R.color.shibColor
        this["DOT"] = R.color.dotColor
        this["USDC"] = R.color.usdcColor
    }

    val LYBER_ASSETS = mutableListOf<String>().apply {
        add("BTC")
        add("ETH")
        add("SOL")
        add("Matic")
        add("BNB")
        add("USDC")
        add("USDT")
        add("EUROC")
    }

    const val SMALL_RANGE = "ghijklmnopqrstuvwxyz"
    const val CAP_RANGE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    const val POPUP_HEIGHT = 400

}