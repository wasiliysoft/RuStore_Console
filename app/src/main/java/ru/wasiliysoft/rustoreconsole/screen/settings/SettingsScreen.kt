package ru.wasiliysoft.rustoreconsole.screen.settings

import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.wasiliysoft.rustoreconsole.BuildConfig
import ru.wasiliysoft.rustoreconsole.MainActivity
import ru.wasiliysoft.rustoreconsole.data.prefs.PrefHelper
import ru.wasiliysoft.rustoreconsole.network.RetrofitClient
import ru.wasiliysoft.rustoreconsole.screen.main.BottomBarItem

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
            PreferenceCategoryView("Аккаунт")
            LogIn()
            Logout()
            PreferenceCategoryView("Приложение")
            CheckUpdates()
        }
    }
}

@Composable
private fun SelectStartScreenPrefView() {
    val screenOptions = remember {
        listOf(
            ListPreferenceItem(BottomBarItem.Purchases.route, BottomBarItem.Purchases.title),
            ListPreferenceItem(BottomBarItem.Revews.route, BottomBarItem.Revews.title),
            ListPreferenceItem(BottomBarItem.AppList.route, BottomBarItem.AppList.title),
            ListPreferenceItem(BottomBarItem.PaymentStats.route, BottomBarItem.PaymentStats.title),
        )
    }
    ListPreferenceView(
        title = "Начальный экран",
        items = screenOptions,
        sharedPrefKey = PrefHelper.PREF_HOME_START_TAB_ROUTE,
    )
}

@Composable
private fun CheckUpdates() {
    val context = LocalActivity.current as ComponentActivity
    PreferenceView(
        title = "Исходный код на GitHub",
        summary = "Версия: ${BuildConfig.VERSION_NAME}"
    ) {
        val uri = "https://github.com/wasiliysoft/RuStore_Console/releases".toUri()
        val intent = Intent(Intent.ACTION_VIEW, uri)
        context.startActivity(intent)
    }
}

@Composable
private fun LogIn() {
    val activity = LocalActivity.current as ComponentActivity
    PreferenceView(
        title = "Войти в аккаунт",
        onClick = {
            if (activity is MainActivity) {
                activity.launchLoginFlow()
            } else {
                Toast.makeText(activity, "Что-то пошло не так", Toast.LENGTH_LONG).show()
            }
        }
    )
}

@Composable
private fun Logout() {
    val context = LocalActivity.current as ComponentActivity
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
                        } finally {
                            PrefHelper.getInstance().token = ""
                        }
                        withContext(Dispatchers.Main) {
                            isShow = false
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                        }
                    }
                }) { Text(stringResource(android.R.string.ok).uppercase()) }
            },
            dismissButton = { TextButton({ isShow = false }) { Text(stringResource(android.R.string.cancel).uppercase()) } },
            text = { Text("Выйти из аккаунта?") })
    }
}


@Preview(showBackground = true)
@Composable
fun Preview() {
    SettingsScreen()
}