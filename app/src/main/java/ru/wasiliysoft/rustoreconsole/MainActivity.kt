package ru.wasiliysoft.rustoreconsole

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import ru.wasiliysoft.rustoreconsole.login.LoginActivity
import ru.wasiliysoft.rustoreconsole.network.RetrofitClient
import ru.wasiliysoft.rustoreconsole.purchases.PurchaseViewModel
import ru.wasiliysoft.rustoreconsole.purchases.PurchasesScreen
import ru.wasiliysoft.rustoreconsole.reviews.ReviewViewModel
import ru.wasiliysoft.rustoreconsole.reviews.ReviewsScreen
import ru.wasiliysoft.rustoreconsole.ui.theme.RuStoreConsoleTheme
import ru.wasiliysoft.rustoreconsole.utils.LoadingResult

sealed class Screen(
    val route: String,
    val title: String,
    val selectedVector: ImageVector,
    val uneselectedVector: ImageVector
) {
    object Revews : Screen(
        route = "Revews",
        title = "Отзывы",
        selectedVector = Icons.Filled.ThumbUp,
        uneselectedVector = Icons.Outlined.ThumbUp
    )

    object Purchases : Screen(
        route = "Purchases",
        title = "Платежи",
        selectedVector = Icons.Filled.ShoppingCart,
        uneselectedVector = Icons.Outlined.ShoppingCart
    )
}

val navList = listOf(
    Screen.Purchases, Screen.Revews
)

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

    private val launcherLoginActivity = registerForActivityResult(LoginActivity.Contract()) {
        if (it.isNotEmpty()) {
            Toast.makeText(this, "Auth success", Toast.LENGTH_LONG).show()
            ph.token = it
            RetrofitClient.token = it
            // TODO onRefresh()
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RetrofitClient.token = ph.token
        val purchaseViewModel = PurchaseViewModel(appId)
        val reviewViewModel = ReviewViewModel(appId)
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
                            startDestination = Screen.Purchases.route,
                            modifier = Modifier.padding(innerPadding)
                        ) {
                            composable(route = Screen.Purchases.route) {
                                val state = purchaseViewModel.purchases.observeAsState(
                                    LoadingResult.Loading("Инициализация")
                                )
                                PurchasesScreen(
                                    uiSate = state,
                                    openInBrowser = ::openPurchaseInBrowser,
                                    onRefresh = {
                                        purchaseViewModel.loadPurchases()
                                    }
                                )
                            }
                            composable(route = Screen.Revews.route) {
                                val state = reviewViewModel.reviews.observeAsState(
                                    LoadingResult.Loading("Инициализация")
                                )
                                ReviewsScreen(
                                    uiSate = state,
                                    onRefresh = {
                                        reviewViewModel.loadReviews()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
        purchaseViewModel.purchases.observe(this) {
            if (it is LoadingResult.Error && it.exception.message.toString().trim() == "HTTP 401") {
                onFailureAuth()
            }
        }
        reviewViewModel.loadReviews()
        purchaseViewModel.loadPurchases()
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
                })
        }
    }
}



