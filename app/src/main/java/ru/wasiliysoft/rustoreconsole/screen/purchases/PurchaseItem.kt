package ru.wasiliysoft.rustoreconsole.screen.purchases

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.wasiliysoft.rustoreconsole.data.ui.PurchaseListItem
import ru.wasiliysoft.rustoreconsole.utils.formatAsAmountSum
import ru.wasiliysoft.rustoreconsole.utils.toMediumTimeString

private fun copyKeyToClipBoard(activity: Activity, str: String) {
    val clipboardManager = activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clipData = ClipData.newPlainText("invoiceId", str)
    clipboardManager.setPrimaryClip(clipData)
    Toast.makeText(activity, "copy: $str", Toast.LENGTH_SHORT).show()
}

private fun openInBrowser(context: Context, app: Long) {
    val url = "https://console.rustore.ru/apps/$app/payments/"
    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
}


@Composable
fun PurchaseItem(
    purchase: PurchaseListItem,
    modifier: Modifier = Modifier
) {
    val date = purchase.invoiceDate
    val context = LocalContext.current
    val activity = LocalActivity.current

    var menuExpanded by remember { mutableStateOf(false) }

    val cardColors =
        if (purchase.amountCurrent == 0) CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        ) else CardDefaults.cardColors()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {
                    // основной тап
                    activity?.let {
                        copyKeyToClipBoard(it, purchase.invoiceId.toString())
                    }
                    openInBrowser(context, purchase.applicationCode)
                },
                onLongClick = { menuExpanded = true }
            ),
        colors = cardColors
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row {
                Text(
                    text = purchase.applicationName,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                )
                Text(
                    text = (purchase.amountCurrent / 100).formatAsAmountSum(),
                    fontWeight = FontWeight.Bold,

                    )

            }
            purchase.productName?.let { name ->
                Row {
                    Text(
                        text = name,
                        fontWeight = FontWeight.Light,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Row {
                Text(
                    text = "Счёт №:" + purchase.invoiceId.toString(),
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

            // Контекстное меню
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Скопировать номер счёта") },
                    onClick = {
                        menuExpanded = false
                        activity?.let {
                            copyKeyToClipBoard(it, purchase.invoiceId.toString())
                        }
                    }
                )
                DropdownMenuItem(
                    text = { Text("Открыть в браузере") },
                    onClick = {
                        menuExpanded = false
                        openInBrowser(context, purchase.applicationCode)
                    }
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