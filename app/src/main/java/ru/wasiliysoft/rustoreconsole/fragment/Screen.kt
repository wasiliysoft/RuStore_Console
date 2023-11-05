package ru.wasiliysoft.rustoreconsole.fragment

import ru.wasiliysoft.rustoreconsole.R

sealed class Screen(
    val route: String,
    val title: String,
    val selectedVector: Int,
    val uneselectedVector: Int,
) {
    object AppList : Screen(
        route = "AppList",
        title = "Приложения",
        selectedVector = R.drawable.baseline_apps_24,
        uneselectedVector = R.drawable.baseline_apps_24,
    )

    object Revews : Screen(
        route = "Revews",
        title = "Отзывы",
        selectedVector = R.drawable.baseline_chat_24,
        uneselectedVector = R.drawable.outline_chat_24,
    )

    object Purchases : Screen(
        route = "Purchases",
        title = "Платежи",
        selectedVector = R.drawable.baseline_credit_card_24,
        uneselectedVector = R.drawable.baseline_credit_card_24,
    )

    object PaymentStats : Screen(
        route = "PaymentStats",
        title = "Статистика",
        selectedVector = R.drawable.baseline_query_stats_24,
        uneselectedVector = R.drawable.baseline_query_stats_24,
    )
}