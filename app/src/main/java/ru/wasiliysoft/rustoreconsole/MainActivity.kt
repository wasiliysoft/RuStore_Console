package ru.wasiliysoft.rustoreconsole

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import ru.wasiliysoft.rustoreconsole.data.prefs.PrefHelper
import ru.wasiliysoft.rustoreconsole.login.LoginActivity
import ru.wasiliysoft.rustoreconsole.network.RetrofitClient
import ru.wasiliysoft.rustoreconsole.repo.AppListRepository
import ru.wasiliysoft.rustoreconsole.screen.main.HomeScreen
import ru.wasiliysoft.rustoreconsole.ui.theme.RuStoreConsoleTheme
import ru.wasiliysoft.rustoreconsole.utils.LoadingResult

class MainActivity : ComponentActivity() {
    private val LOG_TAG = "MainActivity"
    private val ph by lazy { PrefHelper.getInstance() }

    private val launcherLoginActivity = registerForActivityResult(LoginActivity.Contract()) {
        if (it.isNotEmpty()) {
            ph.token = it
            Toast.makeText(this, "Success", Toast.LENGTH_LONG).show()
            // Полный перезапуск текущей активити с очисткой стека
            val intent = intent // Получаем интент, которым была открыта ТЕКУЩАЯ активити
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            startActivity(intent)
            finish()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        ph.token = "" // used for auth testing
        RetrofitClient.token = ph.token

        setContent {
            RuStoreConsoleTheme {
                val navController = rememberNavController()
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavHost(navController = navController, startDestination = NavGraph.Home.name) {
                        composable(NavGraph.Home.name) { HomeScreen() }
                    }
                }
            }
        }
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                AppListRepository.appListResultFlow.collect { result ->
                    Log.d(LOG_TAG, result.toString())
                    //FIXME работает не стабильно (issue #6)
                    if (result is LoadingResult.Error
                        && result.exception.message.toString().trim().contains("HTTP 401")
                    ) {
                        onFailureAuth()
                    }
                }
            }
        }
    }

    private fun onFailureAuth() {
        launcherLoginActivity.launch(null)
    }
}



