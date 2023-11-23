package ru.wasiliysoft.rustoreconsole.login

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import ru.wasiliysoft.rustoreconsole.network.RetrofitClient
import ru.wasiliysoft.rustoreconsole.ui.theme.RuStoreConsoleTheme


class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RuStoreConsoleTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LoginScreen(onTokedReceived = ::onTokenReceived)
                }
            }
        }
    }

    private fun onAuthTokenReceived(token: String) {
        intent?.let {
            it.putExtra(EXTRA_TOKEN, token)
            setResult(Activity.RESULT_OK, it)
            finish()
        }
    }

    private fun onTokenReceived(uuid: String, token: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val paramMap = JSONObject()
                paramMap.accumulate("accessToken", token)
                paramMap.accumulate("uuid", uuid)
                val type = "application/json; charset=utf-8".toMediaTypeOrNull()
                val requestBody = paramMap.toString().toRequestBody(type)
                val result = RetrofitClient.api.getToken(
                    url = "https://backapi.rustore.ru/auth/token",
                    body = requestBody
                )
                result.body["accessToken"]?.let { onAuthTokenReceived(it) }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@LoginActivity, e.message.toString(), Toast.LENGTH_SHORT)
                        .show()
                }
            }

        }
    }

    companion object {
        private const val EXTRA_TOKEN = "EXTRA_TOKEN"
    }

    class Contract : ActivityResultContract<Unit?, String>() {
        override fun createIntent(context: Context, input: Unit?): Intent {
            return Intent(context, LoginActivity::class.java)
        }

        override fun parseResult(resultCode: Int, intent: Intent?): String {
            return intent?.extras?.getString(EXTRA_TOKEN, "") ?: ""
        }
    }
}