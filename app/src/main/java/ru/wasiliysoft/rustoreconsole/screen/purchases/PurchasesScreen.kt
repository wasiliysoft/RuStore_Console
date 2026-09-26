package ru.wasiliysoft.rustoreconsole.screen.purchases

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.wasiliysoft.rustoreconsole.data.ui.PurchaseListItem
import ru.wasiliysoft.rustoreconsole.ui.view.ErrorTextView
import ru.wasiliysoft.rustoreconsole.utils.LoadingResult
import ru.wasiliysoft.rustoreconsole.utils.LoadingResult.Loading
import ru.wasiliysoft.rustoreconsole.utils.toMediumDateString
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchasesScreen(
    modifier: Modifier = Modifier,
    viewModel: PurchaseViewModel = viewModel(),
) {
    Surface(Modifier.background(MaterialTheme.colorScheme.background)) {
        Column(
            modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val loadingResult = viewModel.purchasesByDays.collectAsStateWithLifecycle().value

            val state = rememberPullToRefreshState()
            PullToRefreshBox(
                state = state,
                isRefreshing = loadingResult is Loading,
                onRefresh = viewModel::load
            ) {
                when (loadingResult) {
                    is Loading -> Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = loadingResult.description)
                    }

                    is LoadingResult.Success -> {
                        val purchases = loadingResult.data
                        val amountDaylyAvg = viewModel.avgSumm.collectAsStateWithLifecycle().value
                        val amountPerMonth = viewModel.amountPerMonth.collectAsStateWithLifecycle().value
                        val amountDaylyByAppAvg = viewModel.avgSummByApp.collectAsStateWithLifecycle().value
                        PurchaseListView(
                            purchases = purchases,
                            amountDaylyAvg = amountDaylyAvg,
                            amountPerMonth = amountPerMonth,
                            amountDaylyByAppAvg = amountDaylyByAppAvg
                        )
                    }

                    is LoadingResult.Error -> ErrorTextView(exception = loadingResult.exception)
                }
            }
        }
    }
}


@Composable
private fun PurchaseListView(
    purchases: PurchaseMap,
    amountPerMonth: AmountPerMonth,
    amountDaylyAvg: Int,
    modifier: Modifier = Modifier,
    amountDaylyByAppAvg: Map<String, Int>,
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(16.dp),
        modifier = modifier
    ) {
        item {
            TitledCard(title = "Фактические суммы") {
                amountPerMonth.forEach {
                    AmountPerMonthItem(it)
                }
            }
            Spacer(Modifier.size(8.dp))
        }
        item {
            TitledCard(title = "Прогноз на основе средн. за 28 д.") {
                PredictionItem(amountDaylyAvg)
            }
            Spacer(Modifier.size(8.dp))
        }
        item {
            TitledCard(title = "Средн.cут. сумма за 28 д.") {
                amountDaylyByAppAvg.forEach {
                    AmountPerMonthItem(it.toPair())
                }
            }
        }

        purchases.forEach { purchasesPerDay ->
            itemsIndexed(
                items = purchasesPerDay.value,
                key = { _, p -> p.invoiceId }) { index, purchase ->
                if (index == 0) {
                    PurchaseDayHeader(
                        dateStr = purchasesPerDay.key,
                        list = purchasesPerDay.value
                    )
                }
                PurchaseItem(purchase)
            }
        }
    }
}

@Composable
private fun TitledCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
        Row(
            Modifier
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .fillMaxWidth()
        ) {
            Text(text = title, fontWeight = FontWeight.Bold, modifier = Modifier.padding(8.dp))
        }
        Spacer(Modifier.size(8.dp))
        Column {
            content()
        }
        Spacer(Modifier.size(8.dp))
    }
}

@Composable
private fun PredictionItem(
    avgDaylyAmmount: Int,
    modifier: Modifier = Modifier
) {
    val calendar = Calendar.getInstance()
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val predictionAmount = avgDaylyAmmount * daysInMonth
    val mName = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG_STANDALONE, LocalLocale.current.platformLocale)

    Column(modifier = modifier.fillMaxWidth()) {
        AmountPerMonthItem(Pair("Среднесуточная сумма", avgDaylyAmmount))
        AmountPerMonthItem(Pair("Прогноз на $mName", predictionAmount))
    }
}

@Composable
private fun AmountPerMonthItem(
    amountPerMonth: Pair<String, Int>,
    modifier: Modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp).fillMaxWidth()
) {
    Row(modifier = modifier) {
        Text(text = amountPerMonth.first, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(text = String.format("%,d", amountPerMonth.second) + "р", modifier = Modifier.padding(start = 8.dp))
    }
}

@Composable
fun PurchaseDayHeader(
    dateStr: String,
    list: List<PurchaseListItem>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .padding(top = 16.dp, bottom = 8.dp)
    ) {
        Text(
            text = dateStr,
            modifier = Modifier.weight(1f),
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Сумма: " + list.sumOf { it.amountCurrent / 100 }.toString() + "p",
            fontWeight = FontWeight.Bold
        )
    }

}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    val data = List(5) {
        PurchaseListItem.demo(it.toLong())
    }.groupBy {
        it.invoiceDate.toMediumDateString()
    }
    PurchaseListView(
        purchases = data,
        amountPerMonth = emptyList(),
        amountDaylyAvg = 100,
        amountDaylyByAppAvg = emptyMap(),
    )
}