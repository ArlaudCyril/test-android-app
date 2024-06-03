package com.Lyber.utils

import android.accounts.Account
import com.Lyber.R
import com.Lyber.models.AvatarData

object Constants {


    const val USING_BUY: String = "buy"
    const val IS_CHANGE_PIN: String = "changePin"
    const val FREQUENCY: String = "frequency"
    const val AMOUNT: String = "amount"
    const val URL: String = "KYCURL"
    const val IS_REVIEW: String = "isReview"
    const val ORDER_ID: String = "order_id"
    const val ID: String = "ID"
    const val LOADING: Int = 0
    const val LOADING_SUCCESS: Int = 1
    const val LOADING_FAILURE: Int = 2
    const val FROM_SWAP: String = "from_swap"
    const val TO_SWAP: String = "to_swap"
    const val FROM: String = "from"

    /* steps fill personal data */
    const val ACCOUNT_INITIALIZATION = 0

    //    const val PERSONAL_DATA = 1
    const val EMAIL_ADDRESS = 2
    const val EMAIL_VERIFIED = 3
    const val ADDRESS = 4
    const val INVESTMENT_EXP = 5

    const val ACCOUNT_CREATED = 1
    const val ACCOUNT_CREATING = 6
    const val Account_CREATION_STEP_PHONE = 1
    const val Account_CREATION_STEP_EMAIL = 2
    const val Account_CREATION_STEP_CREATE_PIN = 3
    const val PERSONAL_DATA_FILLED = 2
    const val KYC_COMPLETED = 3
    const val PROFILE_COMPLETED = 4
    const val DONE_EDUCATION = 5


    const val IS_LOGOUT: String = "is_logout"


    //staging

//    const val BASE_URL = "https://staging.lyber.com/"
//    const val NEW_BASE_URL = "https://staging.lyber.com/"
//    const val SOCKET_BASE_URL = "wss://ws.lyber.com/websocket/"
//    const val STRIPE_KEY =
//        "pk_test_51NVVY7F2A3romcuHdC3JDD9evsFhQvyZ5cYS6wpy9OznXgmYzLvWTG81Zfj2nWGQFZ2zs8RboA3uMLCNPpPV08Zk00McUdiPAt"


    //Live also change environment on PreviewMyPurchase
    const val BASE_URL = "https://prod.lyber.com/"
    const val NEW_BASE_URL = "https://prod.lyber.com/"
    const val SOCKET_BASE_URL = "wss://ws.lyber.com/websocket/"
    const val STRIPE_KEY =
        "pk_live_51NVVY7F2A3romcuHShL3mg16ls7OVvdPNIQkPuU14mCEZNKfci5BLj0eEjcxQyi5MkD5wf9AF0lajeYhjo8OFpkb00akLPszlE"

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
    const val ACTION_WITHDRAW = "withdraw"
    const val SINGULAR = "SINGULAR"
    const val TO_EDIT = "toEdit"
    const val USING_EXCHANGE = "using_exchange"
    const val USING_SELL = "using_sell"
    const val NAME = "name"
    const val USING_ALL_PORTFOLIO = "using_all_portfolio"
    const val USING_WITHDRAW_FIAT = "using_withdraw_fiat"
    const val EMAIL_SENT = "emailSent"
    const val EXPORT_DONE = "exportDone"
    const val STRATEGY_TYPE = "SingleAsset"

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
    const val HOURS_72 = "3d"
    const val HOURS_24 = "1d"
    const val NO_EXTRA_SECURITY = "none"

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
    const val ONE_TIME = "oneTime"


    val defaults = mutableListOf<AvatarData>().apply {
        add(AvatarData(CHIMPANZEE, R.drawable.one))
        add(AvatarData(MONKEY, R.drawable.two))
        add(AvatarData(GORILLA, R.drawable.three))
        add(AvatarData(ORANGUTAN, R.drawable.four))
        add(AvatarData(DOG, R.drawable.five))
        add(AvatarData(SHIBA, R.drawable.six))
        add(AvatarData(POODLE, R.drawable.seven))
        add(AvatarData(WOLF, R.drawable.eight))
        add(AvatarData(FOX, R.drawable.nine))
        add(AvatarData(RACCOON, R.drawable.ten))
        add(AvatarData(CAT, R.drawable.eleven))
        add(AvatarData(TIGER_CAT, R.drawable.twelve))
        add(AvatarData(LEO, R.drawable.thirteen))
        add(AvatarData(CHEETAH, R.drawable.fourteen))
        add(AvatarData(UNICORN, R.drawable.fifteen))
        add(AvatarData(COW_HEAD, R.drawable.sixteen))
        add(AvatarData(COW, R.drawable.seventeen))
        add(AvatarData(PIG, R.drawable.eighteen))
        add(AvatarData(BOAR, R.drawable.nineteen))
        add(AvatarData(GIRAFFE, R.drawable.twenty))
        add(AvatarData(ELEPHANT, R.drawable.twenty_one))
        add(AvatarData(MOUSE, R.drawable.twenty_two))
        add(AvatarData(HAMSTER, R.drawable.twenty_three))
        add(AvatarData(RABBIT_HEAD, R.drawable.twenty_four))
        add(AvatarData(RABBIT, R.drawable.twenty_five))
        add(AvatarData(SQUIRREL, R.drawable.twenty_six))
        add(AvatarData(HEDGEHOG, R.drawable.twenty_seven))
        add(AvatarData(BAT, R.drawable.twenty_eight))
        add(AvatarData(BEAR_HEAD, R.drawable.twenty_nine))
        add(AvatarData(SLOTH, R.drawable.thirty))
        add(AvatarData(PANDA, R.drawable.thirty_one))
        add(AvatarData(KANGAROO, R.drawable.thirty_two))
        add(AvatarData(BADGER, R.drawable.thirty_three))
        add(AvatarData(CHICKEN, R.drawable.thirty_four))
        add(AvatarData(CHICK_EGG, R.drawable.thirty_five))
        add(AvatarData(CHICK_HEAD, R.drawable.thirty_six))
        add(AvatarData(CHICK, R.drawable.thirty_seven))
        add(AvatarData(PINGUIN, R.drawable.thirty_eight))
        add(AvatarData(OWL, R.drawable.thirty_nine))
        add(AvatarData(FROG, R.drawable.fourty))
    }

    val colors = mutableListOf<Int>().apply {
        add(R.color.purple_800)
        add(R.color.purple_700)
        add(R.color.purple_600)
        add(R.color.purple_500)
        add(R.color.purple_450)
        add(R.color.purple_400)
        add(R.color.purple_350)
        add(R.color.purple_300)
        add(R.color.purple_250)
        add(R.color.purple_200)
        add(R.color.purple_150)
        add(R.color.purple_100)
        add(R.color.purple_100)
        add(R.color.purple_50)
        add(R.color.purple_50)
        add(R.color.purple_40)
        add(R.color.purple_40)
        add(R.color.purple_30)
        add(R.color.purple_30)
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

    const val LOGIN = "login"
    const val WITHDRAWAL = "withdrawal"
    const val WHITELISTING = "whitelisting"
    const val GOOGLE = "google"
    const val PHONE = "phone"
    const val EMAIL = "email"
    const val SCOPE = "scope"
    const val WITHDRAW = "withdraw"

    const val FAILURE = "FAILURE"
    const val ORDER = "order"
    const val STRATEGY = "strategy"
    const val DEPOSIT = "deposit"
    const val SELECTED_LANGUAGE = "selectedLanguage"

    const val CHANGE_PASSWORD = "changePassword"

    const val FRENCH = "FR"
    const val ENGLISH = "EN"
    const val ACTION_CLOSE_ACCOUNT = "close-account"


    const val BADGER = "badger"
    const val BAT = "bat"
    const val BEAR_HEAD = "bear_head"
    const val BOAR = "boar"
    const val CAT = "cat"
    const val CHEETAH = "cheetah"
    const val CHICK_EGG = "chick_egg"
    const val CHICK_HEAD = "chick_head"
    const val CHICK = "chick"
    const val CHICKEN = "chicken"
    const val CHIMPANZEE = "chimpanzee"
    const val COW_HEAD = "cow_head"
    const val COW = "cow"
    const val DOG = "dog"
    const val ELEPHANT = "elephant"
    const val FOX = "fox"
    const val FROG = "frog"
    const val GIRAFFE = "giraffe"
    const val GORILLA = "gorilla"
    const val HAMSTER = "hamster"
    const val HEDGEHOG = "hedgehog"
    const val KANGAROO = "kangaroo"
    const val LEO = "leo"
    const val MONKEY = "monkey"
    const val MOUSE = "mouse"
    const val ORANGUTAN = "orangutan"
    const val OWL = "owl"
    const val PANDA = "panda"
    const val PIG = "pig"
    const val PINGUIN = "pinguin"
    const val POODLE = "poodle"
    const val RABBIT_HEAD = "rabbit_head"
    const val RABBIT = "rabbit"
    const val RACCOON = "raccoon"
    const val SHIBA = "shiba"
    const val SLOTH = "sloth"
    const val SQUIRREL = "squirrel"
    const val TIGER_CAT = "tiger_cat"
    const val UNICORN = "unicorn"
    const val WOLF = "wolf"
    const val GENERAL_TERMS_CONDITIONS = "https://www.lyber.com/terms-conditions"
    const val PRIVACY_URL = "https://www.lyber.com/privacy"
    const val EDIT_ACTIVE_STRATEGY = "edit_active_strategy"

    const val API_VERSION = "0.2"
    const val APP_NAME = "Lyber"
    const val VERSION = "1.0"
}