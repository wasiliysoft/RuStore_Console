package ru.wasiliysoft.rustoreconsole

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
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

    private fun onTokenReceived(token: String) {
        intent?.let {
            it.putExtra(EXTRA_TOKEN, token)
            setResult(Activity.RESULT_OK, it)
            finish()
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