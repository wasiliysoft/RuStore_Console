package ru.wasiliysoft.rustoreconsole.screen.apps

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.wasiliysoft.rustoreconsole.data.AppInfo
import ru.wasiliysoft.rustoreconsole.ui.view.ProgressView
import ru.wasiliysoft.rustoreconsole.ui.view.RefreshButton
import ru.wasiliysoft.rustoreconsole.utils.LoadingResult
import ru.wasiliysoft.rustoreconsole.utils.LoadingResult.Loading

@Composable
fun ApplicationListScreen(
    modifier: Modifier = Modifier,
    viewModel: ApplicationListViewModel = viewModel(viewModelStoreOwner = LocalContext.current as ComponentActivity),
) {
    Surface(Modifier.background(MaterialTheme.colorScheme.background)) {
        val uiSate = viewModel.list.observeAsState(Loading(""))
        Column(
            modifier = modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when (uiSate.value) {
                    is Loading -> {
                        ProgressView((uiSate.value as Loading).description)
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
            RefreshButton(viewModel::load)
        }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppInfoCard(
    appInfo: AppInfo,
    modifier: Modifier = Modifier
) {
    val contex = LocalContext.current as ComponentActivity
    Card(modifier = modifier.fillMaxWidth(), onClick = {
        val uri = "https://console.rustore.ru/apps/${appInfo.appId}".toUri()
        val intent = Intent(Intent.ACTION_VIEW, uri)
        contex.startActivity(intent)
    }) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = appInfo.appName, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text(text = appInfo.versionName)
                    Text(text = "versionCode:${appInfo.versionCode}")
                    Text(text = "Статус: ${appInfo.appStatus}")
                }
                ShareAppLinkButton(appInfo = appInfo)
            }
        }
    }
}


@Composable
private fun ShareAppLinkButton(appInfo: AppInfo, modifier: Modifier = Modifier) {
    val contex = LocalContext.current as ComponentActivity
    IconButton(
        onClick = {
            val url = "https://apps.rustore.ru/app/${appInfo.packageName}"
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, "${appInfo.appName} $url")
                type = "text/plain"
            }
            val shareIntent = Intent.createChooser(sendIntent, null)
            contex.startActivity(shareIntent)
        }, modifier = modifier
    ) {
        Icon(imageVector = Icons.Filled.Share, contentDescription = null)
    }
}

@Preview
@Composable
private fun PreviewAppInfoCard() {
    AppInfoCard(AppInfo.demo())
}