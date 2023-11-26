package ru.wasiliysoft.rustoreconsole

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ru.wasiliysoft.rustoreconsole.login.LoginActivity
import ru.wasiliysoft.rustoreconsole.network.RetrofitClient
import ru.wasiliysoft.rustoreconsole.screen.apps.ApplicationListViewModel
import ru.wasiliysoft.rustoreconsole.screen.main.HomeScreen
import ru.wasiliysoft.rustoreconsole.ui.theme.RuStoreConsoleTheme
import ru.wasiliysoft.rustoreconsole.utils.LoadingResult
import ru.wasiliysoft.rustoreconsole.utils.PrefHelper

class MainActivity : ComponentActivity() {
    private val LOG_TAG = "MainActivity"
    private val ph by lazy { PrefHelper.getInstance() }

    private val launcherLoginActivity = registerForActivityResult(LoginActivity.Contract()) {
        if (it.isNotEmpty()) {
            Toast.makeText(this, "Auth success, restart app", Toast.LENGTH_LONG).show()
            ph.token = it
            RetrofitClient.token = it
            appListVM.load()
        }
    }

    private val appListVM by viewModels<ApplicationListViewModel>()

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
        appListVM.list.observe(this) { result ->
            Log.d(LOG_TAG, result.toString())
            //FIXME работает не стабильно (issue #6)
            if (result is LoadingResult.Error
                && result.exception.message.toString().trim().contains("HTTP 401")
            ) {
                onFailureAuth()
            }
        }
    }

    private fun onFailureAuth() {
        launcherLoginActivity.launch(null)
    }
}



