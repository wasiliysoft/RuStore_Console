package ru.wasiliysoft.rustoreconsole.screen

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import ru.wasiliysoft.rustoreconsole.R
import ru.wasiliysoft.rustoreconsole.data.AppInfo
import ru.wasiliysoft.rustoreconsole.data.prefs.PrefHelper
import ru.wasiliysoft.rustoreconsole.data.prefs.StringPreferencesImpl
import ru.wasiliysoft.rustoreconsole.screen.BottomBarScreen.AppList
import ru.wasiliysoft.rustoreconsole.screen.BottomBarScreen.PaymentStats
import ru.wasiliysoft.rustoreconsole.screen.BottomBarScreen.Purchases
import ru.wasiliysoft.rustoreconsole.screen.BottomBarScreen.Revews
import ru.wasiliysoft.rustoreconsole.screen.BottomBarScreen.Settings
import ru.wasiliysoft.rustoreconsole.screen.apps.ApplicationListScreen
import ru.wasiliysoft.rustoreconsole.screen.apps.ApplicationListViewModel
import ru.wasiliysoft.rustoreconsole.screen.paymentstats.PaymentStatScreen
import ru.wasiliysoft.rustoreconsole.screen.purchases.PurchasesScreen
import ru.wasiliysoft.rustoreconsole.screen.reviews.ReviewDetailActivity
import ru.wasiliysoft.rustoreconsole.screen.reviews.ReviewsScreen
import ru.wasiliysoft.rustoreconsole.screen.settings.SettingsScreen


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(appListViewMOdel: ApplicationListViewModel) {
    val navController: NavHostController = rememberNavController()
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    Scaffold(
        topBar = {
            val selectedApp = appListViewMOdel.selectedAppState.collectAsState().value
            val appList = appListViewMOdel.appsList.collectAsState().value
            TopAppBar(
                title = { Text(selectedApp?.appName ?: "Все приложения") },
                actions = {
                    IconButton(onClick = {
                        showBottomSheet = true
                    }) {
                        Icon(Icons.AutoMirrored.Filled.List, null)
                    }
                }
            )
        },
        bottomBar = { BottomBar(navController) }) { innerPadding ->
        val startDestination = StringPreferencesImpl()
            .getData(PrefHelper.PREF_HOME_START_TAB_ROUTE, Purchases.route)
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
        ) {
            composable(route = AppList.route) { ApplicationListScreen() }
            composable(route = Purchases.route) { PurchasesScreen() }
            composable(route = PaymentStats.route) { PaymentStatScreen() }
            composable(route = Settings.route) { SettingsScreen() }
            composable(route = Revews.route) {
                ReviewsScreen(
                    onClickItem = { id ->
                        navController.navigate(
                            route = "${Revews.route}/$id"
                        )
                    })
            }
            composable(
                route = "${Revews.route}/{commnetId}",
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up) },
                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down) },
            ) {
                ReviewDetailActivity(
                    commentId = it.arguments?.getString("commnetId")?.toLong() ?: 0
                )
            }
//            navigation(route = BottomBarScreen.Revews.route, startDestination = "Review") {
//                composable("Review") { ReviewDetailScreen() }
//            }
        }
        if (showBottomSheet) {

            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
//                    Text(
//                        text = "Скрытые приложения",
//                        style = MaterialTheme.typography.titleLarge,
//                        modifier = Modifier.padding(bottom = 16.dp)
//                    )
                    val appList = appListViewMOdel.appsList.collectAsState().value
                    if (appList.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Список приложений пуст")
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(
                                    1f,
                                    fill = false
                                )
                        ) {
                            items(appList, key = { item -> item.packageName }) { app ->
                                AppRowItem(
                                    app = app,
                                    onClickItem = { appListViewMOdel.selectApp(app) },
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun AppRowItem(
    app: AppInfo,
    onClickItem: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClickItem)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = app.appName, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = app.packageName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}


@Composable
private fun BottomBar(navController: NavController) {
    val screens = listOf(
        Purchases,
        Revews,
        PaymentStats,
        AppList,
        Settings,
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
            val resId =
                if (isSelected) screen.selectedDrawableResId else screen.uneselectedDrawableResId
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
        },
//        alwaysShowLabel = false
    )
}

sealed class BottomBarScreen(
    val route: String,
    val title: String,
    val selectedDrawableResId: Int,
    val uneselectedDrawableResId: Int,
) {
    object AppList : BottomBarScreen(
        route = "AppList",
        title = "Приложения",
        selectedDrawableResId = R.drawable.baseline_apps_24,
        uneselectedDrawableResId = R.drawable.baseline_apps_24,
    )

    object Revews : BottomBarScreen(
        route = "Revews",
        title = "Отзывы",
        selectedDrawableResId = R.drawable.baseline_chat_24,
        uneselectedDrawableResId = R.drawable.outline_chat_24,
    )

    object Purchases : BottomBarScreen(
        route = "Purchases",
        title = "Платежи",
        selectedDrawableResId = R.drawable.baseline_credit_card_24,
        uneselectedDrawableResId = R.drawable.baseline_credit_card_24,
    )

    object PaymentStats : BottomBarScreen(
        route = "PaymentStats",
        title = "Статистика",
        selectedDrawableResId = R.drawable.baseline_query_stats_24,
        uneselectedDrawableResId = R.drawable.baseline_query_stats_24,
    )

    object Settings : BottomBarScreen(
        route = "Settings",
        title = "Настройки",
        selectedDrawableResId = R.drawable.baseline_settings_24,
        uneselectedDrawableResId = R.drawable.outline_settings_24,
    )
}
