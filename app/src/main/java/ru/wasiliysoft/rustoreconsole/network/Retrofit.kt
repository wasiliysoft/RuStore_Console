package ru.wasiliysoft.rustoreconsole.network

import android.util.Log
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object RetrofitClient {
    private const val LOG_TAG = "RetrofitClient"
    var token: String = ""

    private val authInterceptor = Interceptor {
        if (token.isEmpty()) Log.e(LOG_TAG, "token not set or empty")
        val newRequest = it.request()
            .newBuilder()
            .header("authorization", token)
            .build()
        Log.d(LOG_TAG, "${it.request().url}")
        it.proceed(newRequest)
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://pay-backapi.rustore.ru/")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api: APIRuStore = retrofit.create(APIRuStore::class.java)
}