package ru.wasiliysoft.rustoreconsole.ui.view

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun RefreshButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        tonalElevation = BottomAppBarDefaults.ContainerElevation,
        color = BottomAppBarDefaults.containerColor,
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedButton(onClick = onClick, Modifier.padding(16.dp)) {
            Text(text = "Обновить")
        }
    }
}