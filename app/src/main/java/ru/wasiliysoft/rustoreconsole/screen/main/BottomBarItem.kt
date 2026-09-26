package ru.wasiliysoft.rustoreconsole.screen.main

import ru.wasiliysoft.rustoreconsole.R

sealed class BottomBarItem(
    val route: String,
    val title: String,
    val selectedDrawableResId: Int,
    val uneselectedDrawableResId: Int,
) {
    object AppList : BottomBarItem(
        route = "AppList",
        title = "Приложения",
        selectedDrawableResId = R.drawable.baseline_apps_24,
        uneselectedDrawableResId = R.drawable.baseline_apps_24,
    )

    object Revews : BottomBarItem(
        route = "Revews",
        title = "Отзывы",
        selectedDrawableResId = R.drawable.baseline_chat_24,
        uneselectedDrawableResId = R.drawable.outline_chat_24,
    )

    object Purchases : BottomBarItem(
        route = "Purchases",
        title = "Платежи",
        selectedDrawableResId = R.drawable.baseline_credit_card_24,
        uneselectedDrawableResId = R.drawable.baseline_credit_card_24,
    )

    object PaymentStats : BottomBarItem(
        route = "PaymentStats",
        title = "Статистика",
        selectedDrawableResId = R.drawable.baseline_query_stats_24,
        uneselectedDrawableResId = R.drawable.baseline_query_stats_24,
    )

    object Settings : BottomBarItem(
        route = "Settings",
        title = "Настройки",
        selectedDrawableResId = R.drawable.baseline_settings_24,
        uneselectedDrawableResId = R.drawable.outline_settings_24,
    )
}
