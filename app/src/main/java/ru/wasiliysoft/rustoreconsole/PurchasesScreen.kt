package ru.wasiliysoft.rustoreconsole

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.wasiliysoft.rustoreconsole.data.Purchase
import ru.wasiliysoft.rustoreconsole.ui.view.PurchaseItem

@Composable
fun PurchasesScreen(
    purchases: State<List<Purchase>>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(16.dp),
        modifier = modifier
    ) {
        items(
            items = purchases.value,
            key = { it.paymentInfo.paymentId }) {
            PurchaseItem(it)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun Preview(modifier: Modifier = Modifier) {
    val purchases = remember {
        mutableStateOf(List(5) {
            Purchase.demo(it.toLong())
        })
    }
    PurchasesScreen(purchases = purchases)
}