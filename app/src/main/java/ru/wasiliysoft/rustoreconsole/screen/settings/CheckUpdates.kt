package ru.wasiliysoft.rustoreconsole.screen.settings

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import ru.wasiliysoft.rustoreconsole.BuildConfig


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckUpdates() {
    val context = LocalContext.current as ComponentActivity
    Surface(
        onClick = {
            val uri = "https://github.com/wasiliysoft/RuStore_Console/releases".toUri()
            val intent = Intent(Intent.ACTION_VIEW, uri)
            context.startActivity(intent)
        },
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(64.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.Center) {
            Text(
                text = "Проверить обновление",
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Текущая версия: ${BuildConfig.VERSION_NAME}",
                maxLines = 2,
                fontWeight = FontWeight.Light,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}