package ru.wasiliysoft.rustoreconsole.screen.main

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import ru.wasiliysoft.rustoreconsole.R
import ru.wasiliysoft.rustoreconsole.screen.apps.ApplicationListScreen
import ru.wasiliysoft.rustoreconsole.screen.paymentstats.PaymentStatScreen
import ru.wasiliysoft.rustoreconsole.screen.purchases.PurchasesScreen
import ru.wasiliysoft.rustoreconsole.screen.reviews.ReviewDetailScreen
import ru.wasiliysoft.rustoreconsole.screen.reviews.ReviewsScreen


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    val navController: NavHostController = rememberNavController()
    Scaffold(bottomBar = {
        BottomBar(navController)
    }) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomBarScreen.Purchases.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(route = BottomBarScreen.AppList.route) { ApplicationListScreen() }
            composable(route = BottomBarScreen.Purchases.route) { PurchasesScreen() }
            composable(route = BottomBarScreen.PaymentStats.route) { PaymentStatScreen() }
            composable(route = BottomBarScreen.Revews.route) {
                ReviewsScreen(
                    onClickItem = { id ->
                        navController.navigate(
                            route = "${BottomBarScreen.Revews.route}/$id"
                        )
                    })
            }
            composable(route = "${BottomBarScreen.Revews.route}/{commnetId}") {
                ReviewDetailScreen(
                    commentId = it.arguments?.getString("commnetId")?.toLong() ?: 0
                )
            }
//            navigation(route = BottomBarScreen.Revews.route, startDestination = "Review") {
//                composable("Review") { ReviewDetailScreen() }
//            }
        }
    }
}

@Composable
private fun BottomBar(navController: NavController) {
    val screens = listOf(
        BottomBarScreen.Purchases,
        BottomBarScreen.Revews,
        BottomBarScreen.PaymentStats,
        BottomBarScreen.AppList,
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val bottomBarDestination = screens.any { it.route == currentDestination?.route }
    if (!bottomBarDestination) return

    NavigationBar {
        screens.forEach { screen ->
            val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
            AddItem(screen = screen, isSelected = isSelected, navController = navController)
        }
    }
}

@Composable
private fun RowScope.AddItem(
    screen: BottomBarScreen,
    isSelected: Boolean,
    navController: NavController
) {
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
            val resId = if (isSelected) screen.selectedVector else screen.uneselectedVector
            Icon(
                painter = painterResource(id = resId),
                contentDescription = screen.title
            )
        },
        label = {
            Text(
                text = screen.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        })
}

private sealed class BottomBarScreen(
    val route: String,
    val title: String,
    val selectedVector: Int,
    val uneselectedVector: Int,
) {
    object AppList : BottomBarScreen(
        route = "AppList",
        title = "Приложения",
        selectedVector = R.drawable.baseline_apps_24,
        uneselectedVector = R.drawable.baseline_apps_24,
    )

    object Revews : BottomBarScreen(
        route = "Revews",
        title = "Отзывы",
        selectedVector = R.drawable.baseline_chat_24,
        uneselectedVector = R.drawable.outline_chat_24,
    )

    object Purchases : BottomBarScreen(
        route = "Purchases",
        title = "Платежи",
        selectedVector = R.drawable.baseline_credit_card_24,
        uneselectedVector = R.drawable.baseline_credit_card_24,
    )

    object PaymentStats : BottomBarScreen(
        route = "PaymentStats",
        title = "Статистика",
        selectedVector = R.drawable.baseline_query_stats_24,
        uneselectedVector = R.drawable.baseline_query_stats_24,
    )
}
