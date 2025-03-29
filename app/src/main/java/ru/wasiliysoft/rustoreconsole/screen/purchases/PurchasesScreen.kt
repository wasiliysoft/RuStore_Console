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
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.wasiliysoft.rustoreconsole.data.ui.PurchaseListItem
import ru.wasiliysoft.rustoreconsole.ui.view.ErrorTextView
import ru.wasiliysoft.rustoreconsole.utils.LoadingResult
import ru.wasiliysoft.rustoreconsole.utils.LoadingResult.Loading
import ru.wasiliysoft.rustoreconsole.utils.toMediumDateString
import java.util.Calendar
import java.util.Locale

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
            val uiSate = viewModel.purchasesByDays.observeAsState(Loading("")).value
            val amountSums = viewModel.amountSumPerMonth.observeAsState(emptyList()).value
            val state = rememberPullToRefreshState()
            PullToRefreshBox(
                state = state,
                isRefreshing = uiSate is Loading,
                onRefresh = viewModel::load
            ) {
                when (uiSate) {
                    is Loading -> Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = uiSate.description)
                    }

                    is LoadingResult.Success -> {
                        PurchaseListView(
                            purchases = uiSate.data,
                            amountSums = amountSums,
                            avgDaylyAmmount = viewModel.avgSumm.value
                        )

                    }

                    is LoadingResult.Error -> ErrorTextView(exception = uiSate.exception)
                }
            }
        }
    }
}


@Composable
private fun PurchaseListView(
    purchases: PurchaseMap,
    amountSums: AmountSumPerMonth,
    avgDaylyAmmount: Int,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(16.dp),
        modifier = modifier
    ) {
        item {
            TitledCard(title = "Прогноз") {
                PredictionItem(avgDaylyAmmount, modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp))
            }
            Spacer(Modifier.size(8.dp))
        }
        item {
            TitledCard(title = "Фактические суммы") {
                amountSums.forEach {
                    AmountPerMonthItem(it, modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp))
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
    Column(modifier = modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(text = "Средн.cут. сумма (28 д.)", modifier = Modifier.weight(1f))
            Text(text = String.format("%,d", avgDaylyAmmount) + "р")
        }
        Spacer(modifier = Modifier.size(8.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            val predictionAmount = avgDaylyAmmount * daysInMonth
            val mName = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG_STANDALONE, Locale.getDefault())
            Text(text = "Прогноз на $mName", modifier = Modifier.weight(1f))
            Text(text = String.format("%,d", predictionAmount) + "р")
        }
    }

}

@Composable
private fun AmountPerMonthItem(
    amountPerMonth: Pair<String, Int>,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.fillMaxWidth()) {
        Text(text = amountPerMonth.first, modifier = Modifier.weight(1f))
        Text(text = String.format("%,d", amountPerMonth.second) + "р")
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
    PurchaseListView(purchases = data, amountSums = emptyList(), avgDaylyAmmount = 100)
}