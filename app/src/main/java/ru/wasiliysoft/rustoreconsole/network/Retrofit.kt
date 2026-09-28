package ru.wasiliysoft.rustoreconsole.network

import android.os.SystemClock
import android.util.Log
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.wasiliysoft.rustoreconsole.data.prefs.PrefHelper
import ru.wasiliysoft.rustoreconsole.network.interceptor.DemoMockInterceptor


object RetrofitClient {
    private const val LOG_TAG = "RetrofitClientTag"
    private val ph by lazy { PrefHelper.getInstance() }

    object AuthEvents {
        private val _unauthorized = MutableSharedFlow<Unit>(
            replay = 0,
            extraBufferCapacity = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        )
        val unauthorized: SharedFlow<Unit> = _unauthorized.asSharedFlow()

        private var lastNotifyTime = 0L
        private const val DEBOUNCE_MS = 3000L

        fun notifyUnauthorized() {
            val now = SystemClock.elapsedRealtime()
            if (now - lastNotifyTime < DEBOUNCE_MS) return
            lastNotifyTime = now
            _unauthorized.tryEmit(Unit)
        }
    }

    private val authInterceptor = Interceptor { chain ->
        val token = ph.token
        if (token.isEmpty()) {
            Log.e(LOG_TAG, "token not set or empty")
            AuthEvents.notifyUnauthorized()
            throw okio.IOException("Необходима авторизация") // прерываем запрос
        }

        val request = chain.request()
            .newBuilder()
            .header("authorization", token)
            .build()
        Log.d(LOG_TAG, "${chain.request().url}")
        val response = chain.proceed(request)

        if (response.code == 401) {
            Log.e(LOG_TAG, "401 Unauthorized: ${request.url}")
            AuthEvents.notifyUnauthorized()
        }
        response
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(DemoMockInterceptor())
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.rustore.ru/")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api: APIRuStore = retrofit.create(APIRuStore::class.java)
}