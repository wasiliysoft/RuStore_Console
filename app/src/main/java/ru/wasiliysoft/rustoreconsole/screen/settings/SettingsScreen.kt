package ru.wasiliysoft.rustoreconsole.screen.settings

import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.wasiliysoft.rustoreconsole.BuildConfig
import ru.wasiliysoft.rustoreconsole.data.prefs.PrefHelper
import ru.wasiliysoft.rustoreconsole.network.RetrofitClient
import ru.wasiliysoft.rustoreconsole.screen.BottomBarScreen.AppList
import ru.wasiliysoft.rustoreconsole.screen.BottomBarScreen.PaymentStats
import ru.wasiliysoft.rustoreconsole.screen.BottomBarScreen.Purchases
import ru.wasiliysoft.rustoreconsole.screen.BottomBarScreen.Revews
import ru.wasiliysoft.rustoreconsole.ui.view.preference.PreferenceCategoryView
import ru.wasiliysoft.rustoreconsole.ui.view.preference.PreferenceView

internal const val LOG_TAG = "SettingsScreen"

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
            Logout()
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

@Composable
fun Logout() {
    val context = LocalContext.current as ComponentActivity
    var isShow by remember { mutableStateOf(false) }
    PreferenceView(
        title = "Выйти из аккаунта",
        onClick = { isShow = true }
    )
    if (isShow) {
        AlertDialog(
            title = { Text("Подтвердите выход") },
            onDismissRequest = { isShow = false },
            confirmButton = {
                TextButton(onClick = {
                    context.lifecycleScope.launch {
                        Log.d(LOG_TAG, "Logout")
                        var msg = "Вы вышли из аккаунта"
                        try {
                            if (!RetrofitClient.api.logout(url = "https://backapi.rustore.ru/auth/logout").isSuccessful) {
                                msg = "Вы уже вышли из аккаунта, или что-то пошло не так"
                            }
                        } catch (e: Exception) {
                            msg = e.message.toString()
                            e.printStackTrace()
                        }
                        withContext(Dispatchers.Main) {
                            isShow = false
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                        }
                    }
                }) { Text("ДА") }
            },
            dismissButton = { TextButton({ isShow = false }) { Text("ОТМЕНА") } },
            text = { Text("Выйти из аккаунта?") })
    }
}


@Preview(showBackground = true)
@Composable
fun Preview() {
    SettingsScreen()
}