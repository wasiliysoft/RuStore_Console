package ru.wasiliysoft.rustoreconsole.screen.apps

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import ru.wasiliysoft.rustoreconsole.data.AppInfo
import ru.wasiliysoft.rustoreconsole.ui.view.TitledCard


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

    TitledCard(title = "Статусы версий") {
        FlowRow(
            modifier = modifier
                .fillMaxWidth()
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
