package ru.wasiliysoft.rustoreconsole.screen.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import ru.wasiliysoft.rustoreconsole.data.prefs.StringPreferencesImpl

data class ListPreferenceItem(
    val value: String,
    val label: String,
)

@Composable
fun ListPreferenceView(
    title: String,
    sharedPrefKey: String,
    items: List<ListPreferenceItem>,
    summary: String? = null,
    preferences: StringPreferencesImpl = remember { StringPreferencesImpl() },
) {
    if (items.isEmpty()) return

    var openDialog by remember { mutableStateOf(false) }

    // Текущее сохранённое значение — перечитываем при каждом открытии диалога
    val savedValue = remember(openDialog, sharedPrefKey) {
        preferences.getData(sharedPrefKey, null)
    }

    val selectedItem = remember(items, savedValue) {
        items.find { it.value == savedValue }
    }

    // Локальный выбор внутри диалога (до нажатия OK)
    var pendingSelection by remember(openDialog) {
        mutableStateOf(selectedItem)
    }

    PreferenceView(
        onClick = { openDialog = true },
        title = title,
        summary = summary ?: selectedItem?.label
    )

    if (openDialog) {
        AlertDialog(
            onDismissRequest = { openDialog = false },
            title = { Text(text = title) },
            text = {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .fillMaxWidth(),
                ) {
                    items.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = item == pendingSelection,
                                    onClick = { pendingSelection = item },
                                    role = Role.RadioButton,
                                )
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                selected = item == pendingSelection,
                                onClick = null, // клик обрабатывает Row
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = item.label,
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingSelection?.let {
                            preferences.setData(sharedPrefKey, it.value)
                        }
                        openDialog = false
                    },
                ) {
                    Text(stringResource(android.R.string.ok).uppercase())
                }
            },
            dismissButton = {
                TextButton(onClick = { openDialog = false }) {
                    Text(stringResource(android.R.string.cancel).uppercase())
                }
            },
        )
    }
}
