package ru.wasiliysoft.rustoreconsole

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import ru.wasiliysoft.rustoreconsole.fragment.Screen
import ru.wasiliysoft.rustoreconsole.fragment.apps.ApplicationListScreen
import ru.wasiliysoft.rustoreconsole.fragment.apps.ApplicationListViewModel
import ru.wasiliysoft.rustoreconsole.fragment.purchases.PurchaseViewModel
import ru.wasiliysoft.rustoreconsole.fragment.purchases.PurchasesScreen
import ru.wasiliysoft.rustoreconsole.fragment.reviews.ReviewViewModel
import ru.wasiliysoft.rustoreconsole.fragment.reviews.ReviewsScreen
import ru.wasiliysoft.rustoreconsole.login.LoginActivity
import ru.wasiliysoft.rustoreconsole.network.RetrofitClient
import ru.wasiliysoft.rustoreconsole.ui.theme.RuStoreConsoleTheme
import ru.wasiliysoft.rustoreconsole.utils.LoadingResult
import ru.wasiliysoft.rustoreconsole.utils.PrefHelper

val navList = listOf(
    Screen.AppList,
    Screen.Purchases,
    Screen.Revews
)

class MainActivity : ComponentActivity() {
    private val initLoadingState = LoadingResult.Loading("Инициализация")
    private val ph by lazy { PrefHelper.getInstance() }

    private val launcherLoginActivity = registerForActivityResult(LoginActivity.Contract()) {
        if (it.isNotEmpty()) {
            Toast.makeText(this, "Auth success, restart app", Toast.LENGTH_LONG).show()
            ph.token = it
            RetrofitClient.token = it
            appListVM.loadData()
        }
    }
    private val appListVM by viewModels<ApplicationListViewModel>()

    private val purchaseVM by lazy { PurchaseViewModel() }
    private val reviewVM by lazy { ReviewViewModel() }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RetrofitClient.token = ph.token

        setContent {
            RuStoreConsoleTheme {
                val navController = rememberNavController()
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Scaffold(bottomBar = {
                        BottomBar(navController)
                    }) { innerPadding ->
                        NavHost(
                            navController = navController,
                            startDestination = Screen.AppList.route,
                            modifier = Modifier.padding(innerPadding)
                        ) {
                            composable(route = Screen.AppList.route) {
                                ApplicationListScreen(
                                    uiSate = appListVM.list.observeAsState(initLoadingState),
                                    onRefresh = { appListVM.loadData() }
                                )
                            }
                            composable(route = Screen.Purchases.route) {
                                PurchasesScreen(
                                    uiSate = purchaseVM.purchases.observeAsState(initLoadingState),
                                    openInBrowser = ::openPurchaseInBrowser,
                                    onRefresh = { purchaseVM.loadPurchases() }
                                )
                            }
                            composable(route = Screen.Revews.route) {
                                ReviewsScreen(
                                    uiSate = reviewVM.reviews.observeAsState(initLoadingState),
                                    onRefresh = { reviewVM.loadReviews() }
                                )
                            }
                        }
                    }
                }
            }
        }
        appListVM.list.observe(this) { result ->
            if (result is LoadingResult.Error
                && result.exception.message.toString().trim() == "HTTP 401"
            ) {
                onFailureAuth()
            }
        }
    }

    private fun openPurchaseInBrowser(appId: Long, invoiceId: Long) {
        val url = "https://console.rustore.ru/apps/$appId/payments/$invoiceId"
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    private fun onFailureAuth() {
        launcherLoginActivity.launch(null)
    }
}

@Composable
private fun BottomBar(navController: NavController) {
    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination
        navList.forEach { screen ->
            val isSelected =
                currentDestination?.hierarchy?.any { it.route == screen.route } == true
            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    navController.navigate(screen.route) {
                        // Pop up to the start destination of the graph to
                        // avoid building up a large stack of destinations
                        // on the back stack as users select items
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        // Avoid multiple copies of the same destination when
                        // reselecting the same item
                        launchSingleTop = true
                        // Restore state when reselecting a previously selected item
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = if (isSelected) screen.selectedVector else screen.uneselectedVector,
                        contentDescription = screen.title
                    )
                },
                label = { Text(text = screen.title) })
        }
    }
}



