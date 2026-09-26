package ru.wasiliysoft.rustoreconsole.screen.main

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState


@Composable
fun BottomBar(navController: NavController) {
    val screens = listOf(
        BottomBarItem.Purchases,
        BottomBarItem.Revews,
        BottomBarItem.PaymentStats,
        BottomBarItem.AppList,
        BottomBarItem.Settings,
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
    screen: BottomBarItem,
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