package ru.wasiliysoft.rustoreconsole.screen.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier


@Composable
fun StringRadioGroup(
    radioOptions: List<Pair<String, String>>,
    selectedOption: MutableState<Pair<String, String>>,
    modifier: Modifier = Modifier
) {
    Column(modifier.selectableGroup()) {
        radioOptions.forEach { item ->
            Row(verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { selectedOption.value = item }
            ) {
                RadioButton(
                    selected = (selectedOption.value.first == item.first),
                    onClick = { selectedOption.value = item })
                Text(
                    text = item.second,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}