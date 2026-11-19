package ru.wasiliysoft.rustoreconsole.utils

fun Int.formatAsAmountSum(): String {
    return String.format("%,d", this) + " ₽"
}