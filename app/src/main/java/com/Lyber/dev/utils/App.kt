package com.Lyber.dev.utils

import android.app.Application
import android.content.Context
import android.util.Base64
import android.util.Log
import androidx.core.content.PackageManagerCompat.LOG_TAG
import com.Lyber.dev.R
import com.google.android.libraries.places.api.Places
import javax.crypto.SecretKey
import com.appsflyer.AppsFlyerLib
import com.appsflyer.attribution.AppsFlyerRequestListener

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(AppLifeCycleObserver())
        val decodedApiKey = getSplitedDecodedApiKey()
        Places.initialize(
            this,
            decodedApiKey.toString()
        )
        Places.initialize(this, getString(R.string.API_KEY))
        appContext = this
        prefsManager = PreferenceManager(applicationContext)
        accessToken = prefsManager.accessToken
        isSign = prefsManager.isSign
        isKyc = prefsManager.isKyc
        KeystoreHelper.generateAndStoreKey(this)
        // Get the stored key
        secretKey = KeystoreHelper.getSecretKey()
        // Encrypt data
        encryptedKey = EncryptionHelper.encrypt(secretKey, Constants.key)

        // Encrypt dat
        AppsFlyerLib.getInstance().init(Constants.APP_FLYER_KEY, null, this)
//        AppsFlyerLib.getInstance().start(this)
        AppsFlyerLib.getInstance().setDebugLog(true)
        AppsFlyerLib.getInstance().start(this, Constants.APP_FLYER_KEY, object :
            AppsFlyerRequestListener {
            override fun onSuccess() {
                Log.d("LOG_TAG", "Launch sent successfully")
            }

            override fun onError(errorCode: Int, errorDesc: String) {
                Log.d("LOG_TAG", "Launch failed to be sent:\n" +
                        "Error code: " + errorCode + "\n"
                        + "Error description: " + errorDesc)
            }
        })

//        getEncodedApiKey()
    }

    fun getEncodedApiKey() {
        val plainApiKey = resources.getString(R.string.API_KEY)//
        val encodedApiKey = String(
            Base64.encode(
                plainApiKey.toByteArray(),
                Base64.DEFAULT
            )
        )
        Log.i("encodedApi", "getEncodedApiKey: $encodedApiKey")
    }

    fun getSplitedDecodedApiKey(): String? {
        return String(
            Base64.decode(
                resources.getString(R.string.encodedKey),
                Base64.DEFAULT
            )
        )
    }

    companion object {
        lateinit var accessToken: String
        lateinit var encryptedKey: String
        lateinit var secretKey: SecretKey
        var isSign: Boolean = false
        var isKyc: Boolean = false
        var isLoader: Boolean = false
        lateinit var prefsManager: PreferenceManager
        lateinit var appContext: Context
    }


}