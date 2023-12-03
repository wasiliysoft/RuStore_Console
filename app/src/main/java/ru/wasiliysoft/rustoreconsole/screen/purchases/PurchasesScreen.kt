package ru.wasiliysoft.rustoreconsole.screen.purchases

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.wasiliysoft.rustoreconsole.data.Purchase
import ru.wasiliysoft.rustoreconsole.ui.view.ErrorTextView
import ru.wasiliysoft.rustoreconsole.ui.view.RefreshButton
import ru.wasiliysoft.rustoreconsole.utils.LoadingResult
import ru.wasiliysoft.rustoreconsole.utils.LoadingResult.Loading
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun PurchasesScreen(
    modifier: Modifier = Modifier,
    viewModel: PurchaseViewModel = viewModel(),
) {
    val uiSate = viewModel.purchasesByDays.observeAsState(Loading("")).value
    val amountSums = viewModel.amountSumPerMonth.observeAsState(emptyList()).value
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when (uiSate) {
                is Loading -> ProgressView(uiSate.description)
                is LoadingResult.Success -> PurchaseListView(
                    purchases = uiSate.data,
                    amountSums = amountSums
                )

                is LoadingResult.Error -> ErrorTextView(exception = uiSate.exception)
            }
        }
        RefreshButton(viewModel::load)
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
    purchases: PurchaseMap,
    amountSums: AmountSumPerMonth,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(16.dp),
        modifier = modifier
    ) {
        itemsIndexed(items = amountSums, key = { _, item -> item.first }) { index, item ->
            val paddingValues = PaddingValues(horizontal = 8.dp)
            if (index == 0) {
                Text(
                    text = "Сводные суммы",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(paddingValues)
                        .padding(bottom = 16.dp)
                )
            }
            AmountPerMonthItem(item, modifier = Modifier.padding(paddingValues))
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
    list: List<Purchase>,
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
        Purchase.demo(it.toLong())
    }.groupBy {
        it.invoiceDate.format(
            DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
        )
    }
    PurchaseListView(purchases = data, amountSums = emptyList())
}