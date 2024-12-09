package ru.wasiliysoft.rustoreconsole.data.ui

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class PurchaseListItem(
    val applicationName: String,
    val applicationCode: Long,
    val productName: String?,
    val amountCurrent: Int,
    val invoiceId: Long,
    private val invoiceDateStr: String
) {
    val invoiceDate: LocalDateTime
        get(): LocalDateTime = LocalDateTime.parse(
            "$invoiceDateStr:00",
            DateTimeFormatter.ISO_OFFSET_DATE_TIME
        ).plusHours(3) //TODO вынести смещение в настройки, see List<Purchase>.toMonthSumMap()

    companion object {
        fun demo(paymentId: Long = 13) = PurchaseListItem(
            amountCurrent = 1000,
            invoiceId = paymentId + 11,
            applicationCode = 167L,
            invoiceDateStr = "2023-10-22T12:35:40+00",
            applicationName = "Test app name",
            productName = "Test productName"
//            paymentInfo = PaymentInfo(
//                paymentId = paymentId,
//                paymentDate = "2023-10-22T12:35:40+03"
//            )
        )
    }
}