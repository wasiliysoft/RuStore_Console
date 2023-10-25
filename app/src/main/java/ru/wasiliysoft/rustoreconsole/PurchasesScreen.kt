package ru.wasiliysoft.rustoreconsole

import android.app.Activity
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import ru.wasiliysoft.rustoreconsole.data.Purchase

@Composable
fun PurchasesScreen(
    purchases: State<List<Purchase>>,
    modifier: Modifier = Modifier
) {
    val activity = (LocalContext.current as Activity)

    Column {
        purchases.value.forEach() {
            Text(text = it.paymentInfo.paymentDate)
        }
    }
}