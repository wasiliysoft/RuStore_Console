package ru.wasiliysoft.rustoreconsole.network

import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Query
import ru.wasiliysoft.rustoreconsole.data.PurchaseResp

interface PurchasesAPI {
    @GET("products/applications/{appId}/purchases")
    suspend fun getPurchases(
        @Path(value = "appId" ) appId: Int,
        @Query("size") size: Int = 100,
        @Query("invoiceStatuses") invoiceStatuses: String = "confirmed,refunded",
    ): PurchaseResp
}