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
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import ru.wasiliysoft.rustoreconsole.ui.theme.RuStoreConsoleTheme


const val TAG = "LoginScreen"

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun LoginScreen(onTokedReceived: (uuid: String, token: String) -> Unit) {
    val mUrl = "https://console.rustore.ru/sign-in"
    val regexUuid = "(?<=\\\"uuid\\\":\\\")[^\\\"]*".toRegex()
    val regexToken = "(?<=\\\"token\\\":\\\")[^\\\"]*".toRegex()
    val mWebViewClient = object : WebViewClient() {
        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            url?.let {
                val uri = Uri.parse(it)
                val payload = uri.getQueryParameter("payload").toString()
                var token = ""
                var uuid = ""
                regexToken.find(payload)?.let { result -> token = result.value }
                regexUuid.find(payload)?.let { result -> uuid = result.value }
                if (token != "" || uuid != "") {
                    Log.d(TAG, "$uuid, $token")
                    onTokedReceived(uuid, token)
                }
            }
        }
    }
    AndroidView(factory = {
        WebView(it).apply {
            clearCache(true)
            layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
            settings.allowContentAccess = true
            settings.domStorageEnabled = true // https://stackoverflow.com/a/33080057/5795315
            settings.cacheMode = WebSettings.LOAD_NO_CACHE;
            settings.javaScriptEnabled = true
            webViewClient = mWebViewClient
            loadUrl(mUrl)
        }
    }, update = {
//        it.loadUrl(mUrl)
    })
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    RuStoreConsoleTheme {
        LoginScreen(onTokedReceived = { _, _ -> })
    }
}