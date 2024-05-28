package com.Lyber.dev.utils

import android.app.Application
import android.content.Context
import android.util.Base64
import android.util.Log
import com.Lyber.dev.R
import com.google.android.libraries.places.api.Places

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(AppLifeCycleObserver())
        val decodedApiKey = getSplitedDecodedApiKey()
        Places.initialize(
            this,
            decodedApiKey.toString()
        )
//        Places.initialize(this, getString(R.string.API_KEY))
        appContext = this
        prefsManager = PreferenceManager(applicationContext)
        accessToken = prefsManager.accessToken
        isSign = prefsManager.isSign
        isKyc = prefsManager.isKyc
//        getEncodedApiKey()
    }

//        fun getEncodedApiKey() {
//        val plainApiKey = resources.getString(R.string.API_KEY)//
//        val encodedApiKey = String(
//            Base64.encode(
//                Base64.encode(
//                    plainApiKey.toByteArray(),
//                    Base64.DEFAULT
//                ),
//                Base64.DEFAULT
//            )
//        )
//        Log.i("encodedApi", "getEncodedApiKey: $encodedApiKey")
//    }
    fun getSplitedDecodedApiKey(): String? {
        return String(
            Base64.decode(
                Base64.decode(
                    resources.getString(R.string.encodedKey),
                    Base64.DEFAULT
                ),
                Base64.DEFAULT
            )
        )
    }

    companion object {
        lateinit var accessToken: String
         var isSign: Boolean=false
         var isKyc: Boolean=false
         var isLoader: Boolean=false
        lateinit var prefsManager: PreferenceManager
        lateinit var appContext: Context
    }
}