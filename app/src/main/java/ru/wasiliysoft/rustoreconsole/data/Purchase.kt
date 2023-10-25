package ru.wasiliysoft.rustoreconsole.data

import com.google.gson.annotations.SerializedName

data class PurchaseResp(
    @SerializedName("code")
    val code: String,
    @SerializedName("message")
    val message: String,
    @SerializedName("body")
    val body: Invoices,
)

data class Invoices(
    @SerializedName("invoices")
    val list: List<Purchase>,
)

data class Purchase(
    @SerializedName("amount_current")
    val amountCurrent: Int,
    @SerializedName("application_name")
    val applicationName: String,
    @SerializedName("payment_info")
    val paymentInfo: PaymentInfo
)

data class PaymentInfo(
    @SerializedName("payment_id")
    val paymentId: Long,
    @SerializedName("payment_date")
    val paymentDate: String,
)
