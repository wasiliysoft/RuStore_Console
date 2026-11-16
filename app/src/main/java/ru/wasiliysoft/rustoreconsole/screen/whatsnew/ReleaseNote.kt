package ru.wasiliysoft.rustoreconsole.screen.whatsnew

data class ReleaseNote(
    val versionCode: Int,
    val title: String,
    val items: List<String>
)

object WhatsNewRegistry {
    // Храните в порядке возрастания версий
    val ALL_NOTES = listOf(
        ReleaseNote(34, "Версия 2.10.0", listOf("Удержание на плитке платежа открывает контекстное меню.")),
    )
}