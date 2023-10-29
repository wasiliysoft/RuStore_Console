package ru.wasiliysoft.rustoreconsole

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.wasiliysoft.rustoreconsole.data.Purchase
import ru.wasiliysoft.rustoreconsole.ui.view.PurchaseItem
import ru.wasiliysoft.rustoreconsole.utils.LoadingResult

@Composable
fun PurchasesScreen(
    uiSate: State<LoadingResult<List<Purchase>>>,
    onRefresh: () -> Unit,
    openInBrowser: (appId: Long, invoceId: Long) -> Unit,
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
                    PurchaseListView(
                        purchases = (uiSate.value as LoadingResult.Success<List<Purchase>>).data,
                        openInBrowser = openInBrowser
                    )
                }

                is LoadingResult.Error -> {
                    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        val message = (uiSate.value as LoadingResult.Error).exception.message
                        Text(text = message ?: "Неизвестная ошибка")
                    }
                }
            }
        }
        Surface(tonalElevation = 2.dp, shadowElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = onRefresh, Modifier.padding(16.dp)) {
                Text(text = "Обновить")
            }
        }
    }
}

@Composable
private fun ProgressView(description: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = description)
        }
    }
}

@Composable
private fun PurchaseListView(
    purchases: List<Purchase>,
    openInBrowser: (appId: Long, invoceId: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(16.dp),
        modifier = modifier
    ) {
        items(items = purchases, key = { it.invoiceId }) {
            PurchaseItem(it, openInBrowser = openInBrowser)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun Preview(modifier: Modifier = Modifier) {
    val uiSate = remember {
        mutableStateOf(
            LoadingResult.Success(List(5) {
                Purchase.demo(it.toLong())
            })
        )
    }
    PurchasesScreen(uiSate = uiSate, openInBrowser = { _, _ -> }, onRefresh = {})
}

@Preview(showBackground = true)
@Composable
private fun PreviewLoading(modifier: Modifier = Modifier) {
    val uiSate = remember {
        mutableStateOf(
            LoadingResult.Loading("Загружаем")
        )
    }
    PurchasesScreen(uiSate = uiSate, openInBrowser = { _, _ -> }, onRefresh = {})
}