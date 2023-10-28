package ru.wasiliysoft.rustoreconsole

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import ru.wasiliysoft.rustoreconsole.ui.theme.RuStoreConsoleTheme

class LoginActivity : ComponentActivity() {
    private val ph by lazy { PrefHelper.get(applicationContext) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ph.token = ""
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
        Toast.makeText(this, "Auth success, restart app", Toast.LENGTH_LONG).show()
        ph.token = token
    }
}