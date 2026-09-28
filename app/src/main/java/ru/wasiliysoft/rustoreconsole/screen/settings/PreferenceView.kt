package ru.wasiliysoft.rustoreconsole.screen.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun PreferenceView(
    title: String,
    modifier: Modifier = Modifier,
    summary: String? = null,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 64.dp),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            summary?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
//
//
//    Surface(
//        onClick = onClick,
//        modifier = modifier
//            .fillMaxWidth()
//            .defaultMinSize(64.dp)
//    ) {
//        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.Center) {
//            Text(text = title, style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
//            summary?.let {
//                Text(
//                    text = it,
//                    maxLines = 1,
//                    overflow = TextOverflow.Ellipsis,
//                    style = MaterialTheme.typography.bodySmall,
//                    color = MaterialTheme.colorScheme.outline,
//                    modifier = Modifier.padding(vertical = 4.dp)
//                )
//            }
//        }
//    }
}