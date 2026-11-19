package ru.wasiliysoft.rustoreconsole.screen.settings

import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
import ru.wasiliysoft.rustoreconsole.screen.whatsnew.WhatsNewActivity
import kotlin.math.roundToInt

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
            SettingPurchaseChunkSize()
            PreferenceCategoryView("Аккаунт")
            LogIn()
            Logout()
            PreferenceCategoryView("Приложение")
            WhatsNew()
            SourceCodePage()
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
private fun SourceCodePage() {
    val context = LocalActivity.current as ComponentActivity
    PreferenceView(
        title = "Исходный код на GitHub",
        summary = "Версия: ${BuildConfig.VERSION_NAME}"
    ) {
        val uri = "https://github.com/wasiliysoft/RuStore_Console".toUri()
        val intent = Intent(Intent.ACTION_VIEW, uri)
        context.startActivity(intent)
    }
}

@Composable
private fun WhatsNew() {
    val context = LocalActivity.current as ComponentActivity
    PreferenceView(
        title = "Что нового",
    ) {
        val intent = Intent(context, WhatsNewActivity::class.java)
        intent.putExtra("EXTRA_ALL", true)
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


@Composable
private fun SettingPurchaseChunkSize() {
    var isShow by remember { mutableStateOf(false) }

    var newValue by remember {
        mutableFloatStateOf(
            PrefHelper.getInstance().purchasesRequestChunkSize.toFloat()
        )
    }
    PreferenceView(
        title = "Потоков загрузки платежей",
        onClick = { isShow = true }
    )
    if (isShow) {
        AlertDialog(
            onDismissRequest = { isShow = false },
            confirmButton = {
                TextButton(onClick = {
                    PrefHelper.getInstance().purchasesRequestChunkSize = newValue.roundToInt()
                    isShow = false
                }) { Text(stringResource(android.R.string.ok).uppercase()) }
            },
            dismissButton = { TextButton({ isShow = false }) { Text(stringResource(android.R.string.cancel).uppercase()) } },
            text = {
                Column {
                    Text(text = "Количество одновременных потоков загрузки платежей", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.size(16.dp))
                    Text(text = "Текущее значение: ${newValue.roundToInt()}", style = MaterialTheme.typography.bodySmall)
                    Slider(
                        value = newValue,
                        valueRange = 1f..10f,
                        steps = 8,
                        onValueChange = { newValue = it }
                    )
                    Spacer(Modifier.size(16.dp))
                    Text(text = "Уменьшайте значение, если часто получаете HTTP ошибку 429", style = MaterialTheme.typography.bodySmall)
                }
            })
    }
}

@Preview(showBackground = true)
@Composable
fun Preview() {
    SettingsScreen()
}