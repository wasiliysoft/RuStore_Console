package ru.wasiliysoft.rustoreconsole.login

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import ru.wasiliysoft.rustoreconsole.ui.theme.RuStoreConsoleTheme


const val TAG = "LoginScreen"

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun LoginScreen(
    onTokedReceived: (uuid: String, token: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val mUrl = "https://console.rustore.ru/sign-in"
    val regexUuid = "(?<=\\\"uuid\\\":\\\")[^\\\"]*".toRegex()
    val regexToken = "(?<=\\\"token\\\":\\\")[^\\\"]*".toRegex()

    // Состояния загрузки
    var isLoading by remember { mutableStateOf(true) }
    // Ссылка на WebView, чтобы вызывать reload()
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    val mWebViewClient = remember {
        object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                isLoading = true
                url?.let {
                    val uri = Uri.parse(it)
                    val payload = uri.getQueryParameter("payload").toString()
                    var token = ""
                    var uuid = ""
                    regexToken.find(payload)?.let { result -> token = result.value }
                    regexUuid.find(payload)?.let { result -> uuid = result.value }
                    if (token != "" || uuid != "") {
                        Log.d(TAG, "uuid and token extract success")
                        onTokedReceived(uuid, token)
                    }
                }
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                isLoading = false
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column {
            AndroidView(
                factory = {
                    WebView(it).apply {
                        clearCache(true)
                        layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                        settings.allowContentAccess = true
                        settings.domStorageEnabled = true
                        settings.cacheMode = WebSettings.LOAD_NO_CACHE
                        settings.javaScriptEnabled = true
                        webViewClient = mWebViewClient
                        loadUrl(mUrl)
                        webViewRef = this
                    }
                },
                update = {
                    webViewRef = it
                },
                modifier = Modifier.fillMaxSize().weight(1f)
            )

            // Прогрессбар пока идёт загрузка
            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }

        // Кнопка обновления в правом нижнем углу
        FloatingActionButton(
            onClick = {
                webViewRef?.reload()
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = "Обновить")
        }
    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    RuStoreConsoleTheme {
        LoginScreen(onTokedReceived = { _, _ -> })
    }
}