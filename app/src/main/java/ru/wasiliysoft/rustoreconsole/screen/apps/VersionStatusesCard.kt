package ru.wasiliysoft.rustoreconsole.screen.apps

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.wasiliysoft.rustoreconsole.data.AppInfo


@Composable
fun VersionStatusesCard(
    data: List<AppInfo>,
    modifier: Modifier = Modifier
) {
    // Кешируем и вычисляем карту статусов только при изменении списка data
    val statusesState by remember(data) {
        derivedStateOf {
            data.groupingBy { it.lastVersion?.appVersionStatus ?: "Загружаем..." }
                .eachCount()
                .toList()
        }
    }
    Card(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 8.dp),
        ) {
            StatusChip(label = "Статусы версий", status = "", modifier = Modifier.padding(start = 8.dp))
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .animateContentSize(), // Плавно изменит высоту, если добавится новая строка
            ) {
                statusesState.forEach { (status, count) ->
                    key(status) { // Позволяет обновлять конкретную чипсу без перерисовки всего блока
                        StatusChip(label = "", status = "$status: $count")
                    }
                }
            }
        }
    }

}