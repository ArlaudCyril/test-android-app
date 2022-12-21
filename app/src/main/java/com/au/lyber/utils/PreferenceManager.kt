package com.au.lyber.utils

import android.content.Context
import android.content.SharedPreferences
import com.au.lyber.models.User
import com.google.gson.Gson

class PreferenceManager(context: Context) {

    val PREFS_FILENAME = "com.henceforth.lyber.utils.prefs"

    private var mSharedPreferences: SharedPreferences
    private var mEditor: SharedPreferences.Editor

    init {
        mSharedPreferences = context.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)
        mEditor = mSharedPreferences.edit()
    }

    var userPin: String
        get() = mSharedPreferences.getString("user_pin", "") ?: ""
        set(value) {
            mEditor.putString("user_pin", value).apply()
        }

    var refreshToken: String
        get() = mSharedPreferences.getString("refresh_token", "") ?: ""
        set(value) {
            mEditor.putString("refresh_token", value).apply()
            refreshTokenSavedAt = System.currentTimeMillis()
        }

    var accessToken: String
        get() = mSharedPreferences.getString("accessToken", "") ?: ""
        set(value) {
            mEditor.putString("accessToken", value).apply()
            tokenSavedAt = System.currentTimeMillis()
        }


    var user: User?
        get() =
            if ((mSharedPreferences.getString("user", "") ?: "").isNotEmpty()) {
                Gson().fromJson((mSharedPreferences.getString("user", "") ?: ""), User::class.java)
            } else null
        set(value) {
            mEditor.putString("user", Gson().toJson(user).toString())
            mEditor.apply()
        }

    var savedScreen: String
        get() = mSharedPreferences.getString("savedScreen", "") ?: ""
        set(value) {
            mEditor.putString("savedScreen", value).apply()
        }


    var personalDataSteps: Int
        get() = mSharedPreferences.getInt("personalDataSteps", Constants.ACCOUNT_INITIALIZATION)
            ?: 0
        set(value) {
            mEditor.putInt("personalDataSteps", value).apply()
        }

    var tokenSavedAt: Long
        get() = mSharedPreferences.getLong("tokenSavedAt", 0L)
        private set(value) {
            mEditor.putLong("tokenSavedAt", value).apply()
        }

    var refreshTokenSavedAt: Long
        get() = mSharedPreferences.getLong("refreshTokenSavedAt", 0L)
        private set(value) {
            mEditor.putLong("refreshTokenSavedAt", value).apply()
        }


    fun setProfileInfoSteps(step: Int) {
        user?.let {
            it.personal_info_step = step
            user = (it)
        }
    }


    var portfolioCompletionStep: Int
        get() = mSharedPreferences.getInt("portfolioCompletionStep", Constants.ACCOUNT_CREATED)
        set(value) = mEditor.putInt("portfolioCompletionStep", value).apply()


    fun setFaceIdEnabled(isEnabled: Boolean) {
        user?.let {
            it.is_face_id_enabled = if (isEnabled) 1 else 0
            user = (it)
        }
    }


    fun loginPinSet() {
        user?.let {
            it.login_pin_set = true
            user = (it)
        }
    }

    fun enableNotification(enable: Boolean) {
        user?.let {
            it.is_push_enabled = if (enable) 1 else 2
            user = (it)
        }
    }

    fun setBankInfo(iban: String, bic: String) {
        user?.let {
            it.iban = iban
            it.bic = bic
            user = (it)
        }
    }


    fun setWhitelisting(isSet: Boolean) {
        user?.let {
            it.is_address_whitelisting_enabled = isSet
            user = (it)
        }
    }

    fun isWhitelisting(): Boolean {
        user?.let {
            return it.is_address_whitelisting_enabled
        }
        return false
    }

    fun setExtraSecurity(security: String) {
        user?.let {
            it.extra_security = security
            user = (it)
        }
    }

    fun getExtraSecurity(): String {
        return user?.extra_security ?: ""
    }

    fun isStrongAuth(): Boolean {
        user?.let {
            return it.is_strong_auth_enabled
        }
        return false
    }

    fun setStrongAuth(isSet: Boolean) {
        user?.let {
            it.is_strong_auth_enabled = isSet
            user = (it)
        }
    }

    fun setProfileImage(image: String) {
        user?.let {
            it.profile_pic = image
            user = (it)
        }
    }

    fun setBalance(balance: Float) {
        user?.let {
            it.balance = balance
            user = (it)
        }
    }

    fun getBalance(): Float {
        user?.let {
            return it.balance
        }
        return 0F
    }


    val haveDefaultImage get() = user?.default_image != -1

    var defaultImage: Int
        get() = user?.default_image ?: -1
        set(value) {
            user?.let {
                it.default_image = value
                user = (it)
            }
        }


    fun setPhone(phone: String) {
        mEditor.putString("phone", phone)
        mEditor.apply()
    }

    fun getPhone(): String {
        return mSharedPreferences.getString("phone", "") ?: ""

    }

    fun logout() {
        mEditor.clear()
        mEditor.apply()
    }

}