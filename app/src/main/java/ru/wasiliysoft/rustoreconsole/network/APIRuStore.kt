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
import ru.wasiliysoft.rustoreconsole.data.PaymentResp
import ru.wasiliysoft.rustoreconsole.data.PurchaseResp
import ru.wasiliysoft.rustoreconsole.data.ReviewsResp

interface APIRuStore {
    @GET("products/applications/{appId}/purchases")
    suspend fun getPurchases(
        @Path(value = "appId") appId: Long,
        @Query("size") size: Int = 500,
        @Query("invoiceStatuses") invoiceStatuses: String = "confirmed,refunded",
    ): PurchaseResp

    @GET
    suspend fun getReviews(
        @Url url: String,
        @Query("pageSize") pageSize: Int = 20,
        @Query("pageNumber") pageNumber: Int = 0,
    ): ReviewsResp

    @GET
    suspend fun getPaymentStats(@Url url: String): PaymentResp

    @GET
    suspend fun getRetrieveUserApps(@Url url: String): ResponseBody

    @POST
    suspend fun sendDevResponse(
        @Url url: String,
        @Body body: RequestBody
    ): Response<ResponseBody>


    @POST
    suspend fun getToken(
        @Url url: String,
        @Body body: RequestBody,
    ): AuthTokenResp
}