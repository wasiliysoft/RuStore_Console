package ru.wasiliysoft.rustoreconsole.screen.purchases

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.wasiliysoft.rustoreconsole.data.Purchase
import ru.wasiliysoft.rustoreconsole.utils.toMediumTimeString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchaseItem(
    purchase: Purchase,
    modifier: Modifier = Modifier
) {
    val date = purchase.invoiceDate
    val context = LocalContext.current
    val cardColors =
        if (purchase.amountCurrent == 0) CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        ) else CardDefaults.cardColors()
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = cardColors,
        onClick = {
            val app = purchase.applicationCode
            val invoceId = purchase.invoiceId
            val url = "https://console.rustore.ru/apps/$app/payments/$invoceId"
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }
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
                Text(
                    text = purchase.applicationName,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "invoiceId: " + purchase.invoiceId.toString(),
                    fontWeight = FontWeight.Light
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(text = (purchase.amountCurrent / 100).toString() + "p")
                Text(
                    text = date.toMediumTimeString(),
                    fontWeight = FontWeight.Light
                )
            }
        }
    }
}


@Preview()
@Composable
private fun Preview() {
    Column {
        PurchaseItem(purchase = Purchase.demo())
        PurchaseItem(purchase = Purchase.demo().copy(amountCurrent = 0))
    }
}