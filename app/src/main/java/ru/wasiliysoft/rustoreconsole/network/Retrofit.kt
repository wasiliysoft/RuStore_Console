package ru.wasiliysoft.rustoreconsole.network

import android.util.Log
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object RetrofitClient {
    private var retrofit: Retrofit? = null

    private fun getClient(token: String): Retrofit {
        if (retrofit == null) {
//            val interceptorLogging = HttpLoggingInterceptor() {}
//            interceptorLogging.setLevel(HttpLoggingInterceptor.Level.BODY)

            val interceptor = Interceptor() {
                val newRequest = it.request()
                    .newBuilder().header("authorization", token).build()
                Log.d(
                    "RetrofitClient",
                    "${it.request().url} headers: ${newRequest.header("authorization")}"
                )
                it.proceed(newRequest)
            }

            val client = OkHttpClient.Builder().addInterceptor(interceptor).build()

            retrofit = Retrofit.Builder()
                .baseUrl("https://pay-backapi.rustore.ru/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit!!
    }


    fun getPurchasesAPI(token: String) = getClient(token).create(PurchasesAPI::class.java)

}