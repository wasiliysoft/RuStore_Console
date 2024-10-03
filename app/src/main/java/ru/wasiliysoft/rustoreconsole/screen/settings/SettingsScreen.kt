package ru.wasiliysoft.rustoreconsole.screen.settings

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.net.toUri
import ru.wasiliysoft.rustoreconsole.BuildConfig
import ru.wasiliysoft.rustoreconsole.data.prefs.PrefHelper
import ru.wasiliysoft.rustoreconsole.screen.BottomBarScreen.AppList
import ru.wasiliysoft.rustoreconsole.screen.BottomBarScreen.PaymentStats
import ru.wasiliysoft.rustoreconsole.screen.BottomBarScreen.Purchases
import ru.wasiliysoft.rustoreconsole.screen.BottomBarScreen.Revews
import ru.wasiliysoft.rustoreconsole.ui.view.preference.PreferenceCategoryView
import ru.wasiliysoft.rustoreconsole.ui.view.preference.PreferenceView

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier
) {
    Surface(Modifier.background(MaterialTheme.colorScheme.background)) {
        Column(
            modifier = modifier.fillMaxSize()
        ) {
            PreferenceCategoryView("Общие")
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

@Composable
fun CheckUpdates() {
    val context = LocalContext.current as ComponentActivity
    PreferenceView(
        title = "Проверить обновление",
        summary = "Текущая версия: ${BuildConfig.VERSION_NAME}"
    ) {
        val uri = "https://github.com/wasiliysoft/RuStore_Console/releases".toUri()
        val intent = Intent(Intent.ACTION_VIEW, uri)
        context.startActivity(intent)
    }
}

@Preview(showBackground = true)
@Composable
fun Preview() {
    SettingsScreen()
}