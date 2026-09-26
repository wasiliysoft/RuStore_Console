package ru.wasiliysoft.rustoreconsole.screen.bottomsheet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import ru.wasiliysoft.rustoreconsole.data.AppInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectAppBottomSheet(
    onDismissRequest: () -> Unit = {},
    viewModel: SelectAppBottomSheetViewModel = viewModel(),
    sheetState: SheetState
) {
    val coroutineScope = rememberCoroutineScope()
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
//                .padding(16.dp)
        ) {
            val appList = viewModel.appsList.collectAsStateWithLifecycle().value
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
            ) {
                // 1. Добавляем элемент "Все приложения" на самый верх списка
                item(key = "all_apps") {
                    AppRowItem(
                        app = AppInfo.demo().copy(appName = "Все приложения", packageName = ""),
                        onClickItem = {
                            viewModel.selectApp(null) // Сбрасываем фильтр (null означает "Все")
                            coroutineScope.launch {
                                sheetState.hide() // Анимированно прячем шторку
                                onDismissRequest() // Полностью закрываем (убираем из UI) после анимации
                            }
                        }
                    )
                }
                items(appList, key = { item -> item.packageName }) { app ->
                    AppRowItem(
                        app = app,
                        onClickItem = {
                            viewModel.selectApp(app)
                            coroutineScope.launch {
                                sheetState.hide() // Анимированно прячем шторку
                                onDismissRequest() // Полностью закрываем (убираем из UI) после анимации
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun AppRowItem(
    app: AppInfo,
    onClickItem: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClickItem)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = app.appName, style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                text = app.packageName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}