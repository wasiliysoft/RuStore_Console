package ru.wasiliysoft.rustoreconsole

import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import ru.wasiliysoft.rustoreconsole.ui.theme.RuStoreConsoleTheme


const val TAG = "LoginScreen"

@Composable
fun LoginScreen(
    onTokedReceived: (token: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val mUrl = "https://console.rustore.ru/apps"
//    val mUrl = "https://ya.ru/"
    val mWebViewClient = object : WebViewClient() {
        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)
            val cookies: String = CookieManager.getInstance().getCookie(url)
            cookies.split("; ").forEach {
                val cookiePair = it.split("=", limit = 2)
                val key = cookiePair[0]
                val value = cookiePair[1]
                if (key == "vk_dev_console_access_token") {
                    onTokedReceived(value)
                }
            }
        }
    }
    AndroidView(factory = {
        WebView(it).apply {
            layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
            settings.javaScriptEnabled = true
            webViewClient = mWebViewClient
            loadUrl(mUrl)
        }
    }, update = {
        it.loadUrl(mUrl)
    })
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    RuStoreConsoleTheme {
        LoginScreen(onTokedReceived = {})
    }
}