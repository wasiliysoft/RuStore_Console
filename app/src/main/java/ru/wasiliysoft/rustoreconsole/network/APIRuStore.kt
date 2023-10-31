package ru.wasiliysoft.rustoreconsole.network

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url
import ru.wasiliysoft.rustoreconsole.data.PurchaseResp
import ru.wasiliysoft.rustoreconsole.data.ReviewsResp

interface APIRuStore {
    @GET("products/applications/{appId}/purchases")
    suspend fun getPurchases(
        @Path(value = "appId") appId: Long,
        @Query("size") size: Int = 10,
        @Query("invoiceStatuses") invoiceStatuses: String = "confirmed,refunded",
    ): PurchaseResp

    //    @GET("/devs/app/{appId}/comment")
    @GET
    suspend fun getReviews(
        @Url url: String,
        @Query("pageSize") pageSize: Int = 10,
        @Query("pageNumber") pageNumber: Int = 0,
    ): ReviewsResp

    @GET
    suspend fun getRetrieveUserApps(@Url url: String): ResponseBody
}