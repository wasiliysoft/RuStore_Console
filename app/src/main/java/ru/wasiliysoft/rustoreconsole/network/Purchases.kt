package ru.wasiliysoft.rustoreconsole.network

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import ru.wasiliysoft.rustoreconsole.data.PurchaseResp
import ru.wasiliysoft.rustoreconsole.data.RetrieveUserApps

interface APIRuStore {
    @GET("products/applications/{appId}/purchases")
    suspend fun getPurchases(
        @Path(value = "appId") appId: Long,
        @Query("size") size: Int = 100,
        @Query("invoiceStatuses") invoiceStatuses: String = "confirmed,refunded",
    ): PurchaseResp

    @GET("applicationData/retrieveUserApps")
    suspend fun getRetrieveUserApps(): RetrieveUserApps
}