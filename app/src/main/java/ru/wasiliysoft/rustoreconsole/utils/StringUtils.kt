package ru.wasiliysoft.rustoreconsole.utils

fun Int.formatAsAmmountSumm(): String {
    return String.format("%,d", this) + "₽"
}