package ru.wasiliysoft.rustoreconsole.network

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import ru.wasiliysoft.rustoreconsole.data.AppListResp
import ru.wasiliysoft.rustoreconsole.data.PurchaseResp
import ru.wasiliysoft.rustoreconsole.data.ReviewsResp

interface APIRuStore {
    @GET("products/applications/{appId}/purchases")
    suspend fun getPurchases(
        @Path(value = "appId") appId: Long,
        @Query("size") size: Int = 10,
        @Query("invoiceStatuses") invoiceStatuses: String = "confirmed,refunded",
    ): PurchaseResp

    @GET("/devs/app/{appId}/comment")
    suspend fun getComments(
        @Path(value = "appId") appId: Long,
        @Query("pageSize") pageSize: Int = 10,
        @Query("pageNumber") pageNumber: Int = 0,
    ): ReviewsResp

    @GET("applicationData/retrieveUserApps")
    suspend fun getRetrieveUserApps(): AppListResp
}