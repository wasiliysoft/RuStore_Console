package ru.wasiliysoft.rustoreconsole.fragment.apps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.wasiliysoft.rustoreconsole.data.AppInfo
import ru.wasiliysoft.rustoreconsole.ui.view.ProgressView
import ru.wasiliysoft.rustoreconsole.ui.view.RefreshButton
import ru.wasiliysoft.rustoreconsole.utils.LoadingResult

@Composable
fun ApplicationListScreen(
    uiSate: State<LoadingResult<List<AppInfo>>>,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when (uiSate.value) {
                is LoadingResult.Loading -> {
                    ProgressView((uiSate.value as LoadingResult.Loading).description)
                }

                is LoadingResult.Success -> {
                    val result = (uiSate.value as LoadingResult.Success)
                    if (result.comment.isNotEmpty()) {
                        Text(text = result.comment)
                    }
                    ListView(data = result.data)
                }

                is LoadingResult.Error -> {
                    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        val e = (uiSate.value as LoadingResult.Error).exception
                        Text(text = e.message ?: "Неизвестная ошибка: $e")
                    }
                }
            }
        }
        RefreshButton(onRefresh)
    }
}

@Composable
private fun ListView(
    data: List<AppInfo>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(16.dp),
        modifier = modifier
    ) {
        items(items = data, key = { it.packageName }) {
            AppInfoCard(it)
        }
    }
}

@Composable
private fun AppInfoCard(
    appInfo: AppInfo,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(text = appInfo.appName, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(text = appInfo.versionName)
            Text(text = "versionCode:${appInfo.versionCode}")
            Text(text = "Статус: ${appInfo.appStatus}")
        }
    }
}

@Preview
@Composable
private fun PreviewAppInfoCard(modifier: Modifier = Modifier) {
    AppInfoCard(AppInfo.demo())
}