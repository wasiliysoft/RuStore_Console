package ru.wasiliysoft.rustoreconsole

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import ru.wasiliysoft.rustoreconsole.ui.theme.RuStoreConsoleTheme
import ru.wasiliysoft.rustoreconsole.utils.LoadingResult

class MainActivity : ComponentActivity() {
    private val appId = listOf<Long>(
        2063486239, // microlab
        2063486363, // sven
        2063486368, // edifier
        2063486369, // dialog
        2063487352, // bbk
        2063487890, // dexp
        2063488048, // irf
    )
    private val ph by lazy { PrefHelper.get(applicationContext) }
    private val vm by lazy { PurchaseViewModel(ph.token, appId) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RuStoreConsoleTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val state = vm.purchases
                        .observeAsState(LoadingResult.Loading("Инициализация"))
                    PurchasesScreen(uiSate = state, onRefresh = ::onRefresh)
                    onRefresh()
                }
            }
        }
        vm.purchases.observe(this) {
            if (it is LoadingResult.Error && it.exception.message.toString().trim() == "HTTP 401") {
                onFailureAuth()
            }
        }
    }

    private fun onFailureAuth() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun onRefresh() {
        vm.loadPurchases()
    }
}



