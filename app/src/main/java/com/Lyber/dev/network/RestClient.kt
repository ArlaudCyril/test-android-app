package com.Lyber.dev.network

import com.Lyber.dev.utils.App.Companion.prefsManager
import com.Lyber.dev.utils.Constants
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
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
        fun onRetrofitError(responseBody: ResponseBody?)
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
            val request = chain.request()
            val header = request.newBuilder().header(
                "Authorization",
//                "token",
//                "Bearer",
                "Bearer " + prefsManager.accessToken
//                prefs.getUserProfile().data.access_token
            )
                .header("x-api-version", Constants.API_VERSION)
            val build = header.build()
            chain.proceed(build)
        }
        return builder.build()
    }

    fun getRetrofitInstance(): Retrofit {
        return retrofit
    }


    class RequestInterceptor(
        private val accessToken: String,
        private val apiVersion: String,
        private val signature: String,
        private val timestamp: String
    ) :
        Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request().newBuilder()
                .header("Authorization", "Bearer $accessToken")
                .header("x-api-version", apiVersion)
                .header("X-Signature", signature)
                .header("X-Timestamp", timestamp.toString())
                .build()

            return chain.proceed(request)
        }
    }

     fun getOkHttpClient2(
         accessToken: String,
         apiVersion: String,
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
            .addInterceptor(RequestInterceptor(accessToken, apiVersion, signature, timestamp))
            .build()
    }


}