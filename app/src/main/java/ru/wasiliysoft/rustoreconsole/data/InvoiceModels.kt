package ru.wasiliysoft.rustoreconsole.data

import com.google.gson.annotations.SerializedName

class InvoicesResp(
    @SerializedName("message") val message: String,
    @SerializedName("body") val body: Invoices,
)

class Invoices(
    @SerializedName("invoices") val invoices: List<Invoice>
)

class Invoice(
    @SerializedName("invoice_id") val invoiceId: Long,
    @SerializedName("amount_create") val amountCreate: Int,
    @SerializedName("product_name") val productName: String,
    @SerializedName("invoice_date") val invoiceDateStr: String,
    @SerializedName("visual_name") val visualName: String,
) {
    var applicationCode: Long = 0
        private set

    var applicationName: String = ""
        private set

    /**
     * Обогащение модели информацией о приложении
     */
    fun enrich(appInfo: AppInfo): Invoice {
        applicationName = appInfo.appName
        applicationCode = appInfo.appId
        return this
    }
}
