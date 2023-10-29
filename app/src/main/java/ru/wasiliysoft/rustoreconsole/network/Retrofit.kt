package ru.wasiliysoft.rustoreconsole.network

import android.util.Log
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object RetrofitClient {
    private const val LOG_TAG = "RetrofitClient"
    private var retrofit: Retrofit? = null
    var token: String = ""
        get(): String {
            Log.d(LOG_TAG, "call getToken()")
            return field
        }
        set(value) {
            Log.d(LOG_TAG, "call setToken()")
            field = value
        }

    private fun getClient(): Retrofit {
        if (token.isEmpty()) Log.e(LOG_TAG, "token not set or empty")

        if (retrofit == null) {
            val interceptor = Interceptor {
                val newRequest = it.request()
                    .newBuilder()
                    .header("authorization", token)
                    .build()
                Log.d(LOG_TAG, "${it.request().url}")
                it.proceed(newRequest)
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .build()

            retrofit = Retrofit.Builder().baseUrl("https://pay-backapi.rustore.ru/").client(client)
                .addConverterFactory(GsonConverterFactory.create()).build()
        }
        return retrofit!!
    }

    fun apiRuStore(): APIRuStore = getClient().create(APIRuStore::class.java)
}