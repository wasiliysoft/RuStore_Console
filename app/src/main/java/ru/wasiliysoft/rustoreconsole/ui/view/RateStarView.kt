package ru.wasiliysoft.rustoreconsole.ui.view

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun RateStarView(
    rate: Int,
    modifier: Modifier = Modifier,
    height: Dp = 16.dp
) {
    val iconModifier = Modifier.height(height)
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        repeat(rate) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = "", modifier = iconModifier
            )
        }
        Spacer(Modifier.size(4.dp))
        Text("($rate)")
    }
}