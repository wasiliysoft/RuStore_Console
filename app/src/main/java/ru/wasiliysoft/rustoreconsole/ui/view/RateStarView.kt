package ru.wasiliysoft.rustoreconsole.ui.view

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.twotone.Star
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun RateStarView(
    rate: Int,
    modifier: Modifier = Modifier,
    maxRate: Int = 5,
    height: Dp = 16.dp
) {
    val iconModifier = Modifier.height(height)
    Row(modifier = modifier) {
        repeat(rate) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = "", modifier = iconModifier
            )
        }
        repeat(maxRate - rate) {
            Icon(
                imageVector = Icons.TwoTone.Star,
                contentDescription = "",
                modifier = iconModifier
            )
        }
    }
}