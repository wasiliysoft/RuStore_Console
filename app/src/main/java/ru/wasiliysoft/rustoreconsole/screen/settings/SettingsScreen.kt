package ru.wasiliysoft.rustoreconsole.screen.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import ru.wasiliysoft.rustoreconsole.data.prefs.PrefHelper
import ru.wasiliysoft.rustoreconsole.screen.BottomBarScreen.AppList
import ru.wasiliysoft.rustoreconsole.screen.BottomBarScreen.PaymentStats
import ru.wasiliysoft.rustoreconsole.screen.BottomBarScreen.Purchases
import ru.wasiliysoft.rustoreconsole.screen.BottomBarScreen.Revews

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier
) {
    Surface(Modifier.background(MaterialTheme.colorScheme.background)) {
        Column(
            modifier = modifier.fillMaxSize()
        ) {
            GroupTitleView("Общие")
            SelectStartScreenPrefView()
            CheckUpdates()
        }
    }
}

@Composable
private fun SelectStartScreenPrefView() {
    val screenOptions = remember {
        listOf(
            Pair(Purchases.route, Purchases.title),
            Pair(Revews.route, Revews.title),
            Pair(AppList.route, AppList.title),
            Pair(PaymentStats.route, PaymentStats.title),
        )
    }
    ListPreferenceView(
        title = "Начальный экран",
        items = screenOptions,
        sharedPrefKey = PrefHelper.PREF_HOME_START_TAB_ROUTE,
    )
}


@Preview(showBackground = true)
@Composable
fun Preview() {
    SettingsScreen()
}