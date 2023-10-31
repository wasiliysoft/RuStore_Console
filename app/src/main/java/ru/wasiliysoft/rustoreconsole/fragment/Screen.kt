package ru.wasiliysoft.rustoreconsole.fragment

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val selectedVector: ImageVector,
    val uneselectedVector: ImageVector
) {
    object AppList : Screen(
        route = "AppList",
        title = "Приложения",
        selectedVector = Icons.Filled.List,
        uneselectedVector = Icons.Outlined.List
    )

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