package ru.wasiliysoft.rustoreconsole.network

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url
import ru.wasiliysoft.rustoreconsole.data.AuthTokenResp
import ru.wasiliysoft.rustoreconsole.data.InvoicesResp
import ru.wasiliysoft.rustoreconsole.data.PaymentResp
import ru.wasiliysoft.rustoreconsole.data.ReviewsResp

interface APIRuStore {
    /**
     * @param dateFrom формат YYYY-MM-DD
     * @param dateTo формат YYYY-MM-DD
     */

    @GET("invoices-history/public/v1/apps/{appId}/invoice-payments")
    suspend fun getInvoices(
        @Path("appId") appId: String,
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("dateFrom") dateFrom: String,
        @Query("dateTo") dateTo: String,
        @Query("invoiceStatuses") invoiceStatuses: String = "confirmed,refunded",
    ): InvoicesResp

    @GET("/v2/dev-console/devs/apps/{appId}/feedbacks")
    suspend fun getReviews(
        @Path("appId") appId: String,
        @Query("limit") limit: Int = 20,
    ): ReviewsResp

    @GET("invoices-history/public/v1/apps/{appId}/invoice-payments/statistics")
    suspend fun getPaymentStats(@Path("appId") appId: String): PaymentResp

    @GET
    suspend fun getRetrieveUserApps(@Url url: String): ResponseBody

    @POST("feedbacks/devs/app/{appId}/comment/{commentId}/devresponse")
    suspend fun sendDevResponse(
        @Path("appId") appId: String,
        @Path("commentId") commentId: String,
        @Body body: RequestBody
    ): Response<ResponseBody>


    @POST
    suspend fun getToken(
        @Url url: String,
        @Body body: RequestBody,
    ): AuthTokenResp

    @POST
    suspend fun logout(
        @Url url: String
    ): Response<Any>
}

// Пожаловаться
//fetch("https://api.rustore.ru/feedbacks/devs/app/2063589727/comment/2375687014/complaint", {
//    "headers": {
//        "accept": "*/*",
//        "accept-language": "ru,en;q=0.9",
//        "authorization": "vk1.a.",
//        "console-accept-language": "ru",
//        "sec-ch-ua": "\"Chromium\";v=\"148\", \"YaBrowser\";v=\"26.6\", \"Not/A)Brand\";v=\"99\", \"Yowser\";v=\"2.5\"",
//        "sec-ch-ua-mobile": "?0",
//        "sec-ch-ua-platform": "\"Windows\"",
//        "sec-fetch-dest": "empty",
//        "sec-fetch-mode": "cors",
//        "sec-fetch-site": "same-site"
//    },
//    "referrer": "https://console.rustore.ru/",
//    "body": null,
//    "method": "POST",
//    "mode": "cors",
//    "credentials": "include"
//});
// Ответ
// {"code":"OK","message":"OK","body":null,"timestamp":"2026-08-20T14:54:51.180Z"}