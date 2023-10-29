package ru.wasiliysoft.rustoreconsole.utils

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun formatFromInvoice(dateStr: String): LocalDateTime = LocalDateTime.parse(
    "$dateStr:00",
    DateTimeFormatter.ISO_OFFSET_DATE_TIME
)

fun formatFromReview(dateStr: String): LocalDateTime = LocalDateTime.parse(
    dateStr.take(19).replace(' ', 'T'),
    DateTimeFormatter.ISO_DATE_TIME
)