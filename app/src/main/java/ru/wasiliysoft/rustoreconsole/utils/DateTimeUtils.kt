package ru.wasiliysoft.rustoreconsole.utils

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle


fun LocalDateTime.toMediumDateString(): String =
    format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))

fun LocalDateTime.toMediumTimeString(): String =
    format(DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM))

/**
 * Format to yyyy.MM
 */
fun LocalDateTime.toYearAndMonthString(): String =
    format(DateTimeFormatter.ofPattern("yyyy.MM"))