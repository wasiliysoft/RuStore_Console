package ru.wasiliysoft.rustoreconsole.screen.settings

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ru.wasiliysoft.rustoreconsole.data.prefs.StringPreferencesImpl

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListPreferenceView(
    title: String,
    summary: String? = null,
    sharedPrefKey: String,
    items: List<Pair<String, String>>,
) {
    var openDialog by remember { mutableStateOf(false) }
    Surface(
        onClick = { openDialog = true },
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(64.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.Center) {
            Text(
                text = title,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            summary?.let {
                Text(
                    text = summary,
                    fontWeight = FontWeight.Light,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
    if (openDialog) {
        val selectedItem = remember {
            mutableStateOf(items.find {
                it.first == StringPreferencesImpl().getData(sharedPrefKey, null)
            } ?: items[0])
        }

        AlertDialog(
            title = { Text(text = title) },
            onDismissRequest = { openDialog = false },
            text = {
                StringRadioGroup(
                    radioOptions = items,
                    selectedOption = selectedItem,
                    modifier = Modifier.verticalScroll(rememberScrollState())
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        Log.d("ListPreferenceView", "Selected confirm")
                        openDialog = false
                        StringPreferencesImpl().setData(sharedPrefKey, selectedItem.value.first)
                    },
                    modifier = Modifier.padding(8.dp),
                ) {
                    Text(stringResource(id = android.R.string.ok))
                }
            },
            modifier = Modifier.padding(vertical = 48.dp)
        )
    }
}