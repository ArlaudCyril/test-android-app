package com.au.lyber.utils

import android.content.Context
import android.content.SharedPreferences
import com.au.lyber.models.AssetBaseDataResponse
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
            mEditor.putString("user", Gson().toJson(value).toString())
            mEditor.apply()
        }
    var assetBaseDataResponse: AssetBaseDataResponse?
        get() =
            if ((mSharedPreferences.getString("AssetBaseDataResponse", "") ?: "").isNotEmpty()) {
                Gson().fromJson((mSharedPreferences.getString("AssetBaseDataResponse", "") ?: ""), AssetBaseDataResponse::class.java)
            } else null
        set(value) {
            mEditor.putString("AssetBaseDataResponse", Gson().toJson(value).toString())
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





    var portfolioCompletionStep: Int
        get() = mSharedPreferences.getInt("portfolioCompletionStep", Constants.ACCOUNT_CREATED)
        set(value) = mEditor.putInt("portfolioCompletionStep", value).apply()


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