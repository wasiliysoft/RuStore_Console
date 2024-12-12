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

    @GET("/feedbacks/devs/app/{appId}/comment")
    suspend fun getReviews(
        @Path("appId") appId: String,
        @Query("pageSize") pageSize: Int = 20,
        @Query("pageNumber") pageNumber: Int = 0,
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