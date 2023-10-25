package ru.wasiliysoft.rustoreconsole.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.wasiliysoft.rustoreconsole.data.Purchase

@Composable
fun PurchaseItem(
    purchase: Purchase,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(horizontalAlignment = Alignment.Start) {
                Text(text = purchase.applicationName)
                Text(text = purchase.paymentInfo.paymentId.toString())
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(text = purchase.amountCurrent.toString())
                Text(text = purchase.paymentInfo.paymentDate)
            }
        }
    }
}

@Preview()
@Composable
private fun Preview(modifier: Modifier = Modifier) {
    PurchaseItem(
        purchase = Purchase.demo()
    )
}