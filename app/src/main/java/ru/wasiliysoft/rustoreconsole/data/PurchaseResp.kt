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
    @SerializedName("invoice_id")
    val invoiceId: Long,
    @SerializedName("amount_current")
    val amountCurrent: Int,
    @SerializedName("application_name")
    val applicationName: String,
    @SerializedName("application_code")
    val applicationCode: Long,
    @SerializedName("invoice_date")
    val invoiceDate: String,
//    @SerializedName("payment_info")
//    val paymentInfo: PaymentInfo
) {
    companion object {
        fun demo(paymentId: Long = 13) = Purchase(
            amountCurrent = 1000,
            invoiceId = paymentId + 11,
            applicationCode = 167L,
            invoiceDate = "2023-10-22T12:35:40+03",
            applicationName = "Test app name",
//            paymentInfo = PaymentInfo(
//                paymentId = paymentId,
//                paymentDate = "2023-10-22T12:35:40+03"
//            )
        )
    }
}


data class PaymentInfo(
    @SerializedName("payment_id")
    val paymentId: Long,
    @SerializedName("payment_date")
    val paymentDate: String,
)
