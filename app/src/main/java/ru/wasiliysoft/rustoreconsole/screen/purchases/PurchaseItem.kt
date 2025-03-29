package ru.wasiliysoft.rustoreconsole.screen.purchases

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.wasiliysoft.rustoreconsole.data.ui.PurchaseListItem
import ru.wasiliysoft.rustoreconsole.utils.toMediumTimeString


private fun copyKeyToClipBoard(activity: Activity, str: String) {
    val clipboardManager = activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clipData = ClipData.newPlainText("invoiceId", str)
    clipboardManager.setPrimaryClip(clipData)
    Toast.makeText(activity, "copy: $str", Toast.LENGTH_SHORT).show()
}

@Composable
fun PurchaseItem(
    purchase: PurchaseListItem,
    modifier: Modifier = Modifier
) {
    val date = purchase.invoiceDate
    val context = LocalContext.current
    val activity = LocalActivity.current
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
            activity?.let {
                copyKeyToClipBoard(activity = it, invoceId.toString())
            }
            val url = "https://console.rustore.ru/apps/$app/payments/"
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row {
                Text(
                    text = purchase.applicationName,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                )
                Text(text = (purchase.amountCurrent / 100).toString() + "p")

            }
            purchase.productName?.let { name ->
                Text(text = name, fontWeight = FontWeight.Light)
            }
            Row {
                Text(
                    text = "invoiceId: " + purchase.invoiceId.toString(),
                    fontWeight = FontWeight.Light,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                )
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
        PurchaseItem(purchase = PurchaseListItem.demo())
        PurchaseItem(purchase = PurchaseListItem.demo().copy(amountCurrent = 0))
    }
}