package ru.wasiliysoft.rustoreconsole

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import ru.wasiliysoft.rustoreconsole.ui.theme.RuStoreConsoleTheme

class MainActivity : ComponentActivity() {
    private val ph by lazy { PrefHelper.get(applicationContext) }
    private val vm by lazy { PurchaseViewModel(ph.token) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            RuStoreConsoleTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val token = remember { ph.token }
                    if (token.isEmpty()) {
                        LoginScreen(onTokedReceived = ::onTokenReceived)
                    } else {
                        Log.d("MainActivity", "token not empty")
                        val list = vm.getPurchases(2063488048).observeAsState(emptyList())
                        PurchasesScreen(purchases = list)
                    }
                }
            }
        }
    }

    private fun onTokenReceived(token: String) {
        Toast.makeText(this, "Auth success", Toast.LENGTH_SHORT).show()
        ph.token = token
    }
}



