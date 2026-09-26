package ru.wasiliysoft.rustoreconsole.screen.main

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ru.wasiliysoft.rustoreconsole.data.prefs.PrefHelper
import ru.wasiliysoft.rustoreconsole.data.prefs.StringPreferencesImpl
import ru.wasiliysoft.rustoreconsole.screen.apps.ApplicationListScreen
import ru.wasiliysoft.rustoreconsole.screen.bottomsheet.SelectAppBottomSheet
import ru.wasiliysoft.rustoreconsole.screen.paymentstats.PaymentStatScreen
import ru.wasiliysoft.rustoreconsole.screen.purchases.PurchasesScreen
import ru.wasiliysoft.rustoreconsole.screen.reviews.ReviewDetailActivity
import ru.wasiliysoft.rustoreconsole.screen.reviews.ReviewsScreen
import ru.wasiliysoft.rustoreconsole.screen.settings.SettingsScreen


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainScreenViewModel = viewModel()
) {
    val navController: NavHostController = rememberNavController()
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    Scaffold(
        topBar = {
            val selectedApp = viewModel.selectedApp.collectAsStateWithLifecycle().value
            TopAppBar(
                title = {
                    Column {
                        Text(selectedApp?.appName ?: "Все приложения", maxLines = 1, overflow = TextOverflow.Ellipsis)
                        if (selectedApp?.packageName?.isNotEmpty() == true) {
                            Text(
                                text = selectedApp.packageName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                },
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
            .getData(PrefHelper.PREF_HOME_START_TAB_ROUTE, BottomBarItem.Purchases.route)
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
        ) {
            composable(route = BottomBarItem.AppList.route) { ApplicationListScreen() }
            composable(route = BottomBarItem.Purchases.route) { PurchasesScreen() }
            composable(route = BottomBarItem.PaymentStats.route) { PaymentStatScreen() }
            composable(route = BottomBarItem.Settings.route) { SettingsScreen() }
            composable(route = BottomBarItem.Revews.route) {
                ReviewsScreen(
                    onClickItem = { id ->
                        navController.navigate(
                            route = "${BottomBarItem.Revews.route}/$id"
                        )
                    })
            }
            composable(
                route = "${BottomBarItem.Revews.route}/{commnetId}",
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up) },
                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down) },
            ) {
                ReviewDetailActivity(
                    commentId = it.arguments?.getString("commnetId")?.toLong() ?: 0
                )
            }
        }

        if (showBottomSheet) {
            SelectAppBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState
            )
        }
    }
}



