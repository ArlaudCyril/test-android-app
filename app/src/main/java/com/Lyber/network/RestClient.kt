package com.Lyber.network

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.ImageView
import com.Lyber.utils.App.Companion.prefsManager
import com.Lyber.utils.Constants
import com.google.gson.GsonBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.net.URL
import java.util.*
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

//    fun ImageView.fetchSvg(url: String) {
////                if (okHttpClient == null)
////                    okHttpClient = OkHttpClient().newBuilder()
////                        .cache(Cache(context.cacheDir, 5 * 1024 * 1014))
////                        .build()
////                val request = Request.Builder().url(url).build()
//        CoroutineScope(Dispatchers.IO).launch {
//            val connection = withContext(Dispatchers.IO) {
//                URL(url).openConnection()
//            }
////            connection.doInput = true
//            val bitmap: Bitmap = BitmapFactory.decodeStream(connection.getInputStream())
//            setImageBitmap(bitmap)
//        }
//    }

//    class Utils {
//        companion object {
//            //            private var okHttpClient: OkHttpClient? = null
//            fun fetchSvg(url: String, imageView: ImageView) {
////                if (okHttpClient == null)
////                    okHttpClient = OkHttpClient().newBuilder()
////                        .cache(Cache(context.cacheDir, 5 * 1024 * 1014))
////                        .build()
////                val request = Request.Builder().url(url).build()
//                CoroutineScope(Dispatchers.Default).launch {
//                    val connection = URL(url).openConnection()
//                    connection.doInput = true
//                    val bitmap: Bitmap = BitmapFactory.decodeStream(connection.getInputStream())
//                    imageView.setImageBitmap(bitmap)
//                }
//            }
//        }
//    }


}