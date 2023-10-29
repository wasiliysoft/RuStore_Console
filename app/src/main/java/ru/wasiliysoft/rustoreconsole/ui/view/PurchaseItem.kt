package ru.wasiliysoft.rustoreconsole.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.wasiliysoft.rustoreconsole.data.Purchase
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchaseItem(
    purchase: Purchase,
    openInBrowser: (appId: Long, invoceId: Long) -> Unit,
    modifier: Modifier = Modifier
) {

    val date = LocalDateTime.parse(
        purchase.invoiceDate + ":00",
        DateTimeFormatter.ISO_OFFSET_DATE_TIME
    )
    val cardColor = if (date.toLocalDate() == LocalDate.now()) CardDefaults.cardColors()
    else CardDefaults.outlinedCardColors()
    Card(
        colors = cardColor,
        modifier = modifier.fillMaxWidth(),
        onClick = { openInBrowser(purchase.applicationCode, purchase.invoiceId) }
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.Start, modifier = Modifier.weight(1f)
            ) {
                Text(text = purchase.applicationName)
                Text(text = purchase.invoiceId.toString())
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(text = (purchase.amountCurrent / 100).toString())
                Text(text = date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)))
                Text(text = date.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM)))
            }
        }
    }
}

@Preview()
@Composable
private fun Preview(modifier: Modifier = Modifier) {
    PurchaseItem(
        purchase = Purchase.demo(),
        openInBrowser = { _, _ -> }
    )
}