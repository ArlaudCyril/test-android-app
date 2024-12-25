package com.Lyber.network

import com.Lyber.utils.App.Companion.prefsManager
import com.Lyber.utils.Constants
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.Collections
import java.util.concurrent.TimeUnit
import javax.net.SocketFactory

object RestClient {

    interface OnRetrofitError {
        fun onRetrofitError(errorCode: Int, msg: String)
//        fun onRetrofitError(errorCode: Int, responseBody: ResponseBody?)
        fun onError()
    }

    private lateinit var retrofit: Retrofit
    private lateinit var REST_CLIENT: Api

    fun get(baseUrl: String = Constants.BASE_URL): Api {

        retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(getOkHttpClient())
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(
                GsonConverterFactory.create(
                    GsonBuilder().setLenient()
                        .disableHtmlEscaping()
                        .create()
                )
            )
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()

        REST_CLIENT = retrofit.create(Api::class.java)
        return REST_CLIENT
    }

    fun getRetrofit(): Retrofit {
        retrofit = Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(getOkHttpClient())
            .addConverterFactory(
                GsonConverterFactory.create(
                    GsonBuilder().setLenient()
                        .disableHtmlEscaping()
                        .create()
                )
            )
            .build()
        return retrofit
    }

    private fun getOkHttpClient(): OkHttpClient {
        val httpLoggingInterceptor = HttpLoggingInterceptor()
        httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

        val builder = OkHttpClient.Builder()
        builder.protocols(Collections.singletonList(Protocol.HTTP_1_1))
        builder.connectTimeout(5, TimeUnit.MINUTES)
        builder.readTimeout(5, TimeUnit.MINUTES)
        builder.socketFactory(SocketFactory.getDefault())
        builder.retryOnConnectionFailure(true)
        builder.addNetworkInterceptor(httpLoggingInterceptor)
        builder.addInterceptor { chain ->
//            val request = chain.request()
//            val header = request.newBuilder().header(
//                "Authorization",
//                "Bearer " + prefsManager.accessToken
//            )
//                .header("x-api-version", Constants.API_VERSION)
//            val build = header.build()
            val originalRequest = chain.request()
            val modifiedRequest = originalRequest.newBuilder()
                .header("Authorization", "Bearer ${prefsManager.accessToken ?: ""}")
//                .header("User-Agent", Constants.USER_AGENT)
                .header("User-Agent", "${Constants.APP_NAME}/${Constants.VERSION}")
                .header("x-api-version", Constants.API_VERSION)
                .build()
            chain.proceed(modifiedRequest)
        }
        return builder.build()
    }

    fun getRetrofitInstance(): Retrofit {
        return retrofit
    }

    private fun getOkHttpClient2(
        token: String,
        signature: String,
        timestamp: String
    ): OkHttpClient {
        val httpLoggingInterceptor = HttpLoggingInterceptor()
        httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

        return OkHttpClient.Builder()
            .protocols(Collections.singletonList(Protocol.HTTP_1_1))
            .connectTimeout(5, TimeUnit.MINUTES)
            .readTimeout(5, TimeUnit.MINUTES)
            .socketFactory(SocketFactory.getDefault())
            .retryOnConnectionFailure(true)
            .addNetworkInterceptor(httpLoggingInterceptor)
            .addInterceptor { chain ->
                val request = chain.request()
                val header = request.newBuilder().header(
                    "Authorization",
                    "Bearer " + ""
                )
                    .header("X-Signature", signature)
                    .header("X-Timestamp", timestamp.toString())
                    .header("x-integrity-token", token)
                    .header("x-api-version", Constants.API_VERSION)
                    .header("User-Agent", "${Constants.APP_NAME}/${Constants.VERSION}")
//                    .header("User-Agent", Constants.USER_AGENT)
                val build = header.build()
                chain.proceed(build)
            }
//            .addInterceptor(RequestInterceptor(accessToken, apiVersion, signature, timestamp))
            .build()
    }

    fun setPhoneInstance(
        token: String,
        signature: String,
        timestamp: String
    ): Api {

        retrofit = Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(getOkHttpClient2(token,signature, timestamp))
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(
                GsonConverterFactory.create(
                    GsonBuilder().setLenient()
                        .disableHtmlEscaping()
                        .create()
                )
            )
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()

        REST_CLIENT = retrofit.create(Api::class.java)
        return REST_CLIENT
    }

    private fun getOkHttpClientSecure(
        token: String
    ): OkHttpClient {
        val httpLoggingInterceptor = HttpLoggingInterceptor()
        httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

        return OkHttpClient.Builder()
            .protocols(Collections.singletonList(Protocol.HTTP_1_1))
            .connectTimeout(5, TimeUnit.MINUTES)
            .readTimeout(5, TimeUnit.MINUTES)
            .socketFactory(SocketFactory.getDefault())
            .retryOnConnectionFailure(true)
            .addNetworkInterceptor(httpLoggingInterceptor)
            .addInterceptor { chain ->
                val request = chain.request()
                val header = request.newBuilder().header(
                    "Authorization",
                    "Bearer " +  prefsManager.accessToken
                )
                    .header("x-integrity-token", token)
                    .header("x-api-version", Constants.API_VERSION)
                val build = header.build()
                chain.proceed(build)
            }
//            .addInterceptor(RequestInterceptor(accessToken, apiVersion, signature, timestamp))
            .build()
    }
    fun getRetrofitInstanceSecure(
       token: String
    ): Api {

        retrofit = Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(getOkHttpClientSecure(token))
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(
                GsonConverterFactory.create(
                    GsonBuilder().setLenient()
                        .disableHtmlEscaping()
                        .create()
                )
            )
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()

        REST_CLIENT = retrofit.create(Api::class.java)
        return REST_CLIENT
    }
}