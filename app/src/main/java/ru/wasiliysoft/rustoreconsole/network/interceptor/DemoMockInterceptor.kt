package ru.wasiliysoft.rustoreconsole.network.interceptor

import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

class DemoMockInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (request.header("authorization") != "demo") {
            return chain.proceed(request)
        }

        val path = request.url.encodedPath
        val json: String = when {
            path.contains("retrieveUserApps") || path.contains("/apps") && path.endsWith("/apps") ->
                DemoDataGenerator.retrieveUserApps()

            path.matches(Regex(".*/apps/(\\d+)/invoice-payments$")) -> {
                val appId = Regex(".*/apps/(\\d+)/invoice-payments$").find(path)!!.groupValues[1]
                DemoDataGenerator.invoices(appId)
            }

            path.matches(Regex(".*/apps/(\\d+)/invoice-payments/statistics$")) -> {
                val appId = Regex(".*/apps/(\\d+)/invoice-payments/statistics$").find(path)!!.groupValues[1]
                DemoDataGenerator.stats(appId)
            }

            path.matches(Regex(".*/apps/(\\d+)/feedbacks$")) -> {
                val appId = Regex(".*/apps/(\\d+)/feedbacks$").find(path)!!.groupValues[1]
                DemoDataGenerator.feedbacks(appId)
            }

            else -> return chain.proceed(request)
        }

        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK (Demo Mode)")
            .body(json.toResponseBody("application/json".toMediaTypeOrNull()))
            .build()
    }
}
