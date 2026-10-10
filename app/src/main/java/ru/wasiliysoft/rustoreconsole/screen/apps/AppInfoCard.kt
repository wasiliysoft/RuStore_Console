package ru.wasiliysoft.rustoreconsole.screen.apps

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import ru.wasiliysoft.rustoreconsole.data.AppInfo

@Preview
@Composable
private fun PreviewAppInfoCard() {
    AppInfoCard(AppInfo.demo())
}

@Composable
fun AppInfoCard(
    appInfo: AppInfo,
    modifier: Modifier = Modifier
) {
    val context = LocalActivity.current as ComponentActivity

    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = {
            val uri = "https://console.rustore.ru/apps/${appInfo.appId}".toUri()
            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Заголовок + кнопка "Поделиться"
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = appInfo.appName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "v${appInfo.versionName}  •  code ${appInfo.versionCode}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                ShareAppLinkButton(appInfo = appInfo)
            }


            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            // Статусы в виде чипов
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                StatusChip(
                    label = "Страница приложения",
                    status = appInfo.appStatus
                )
                StatusChip(
                    label = "Последн. версия",
                    status = appInfo.lastVersion?.appVersionStatus ?: "загружаем..."
                )
            }
        }
    }
}

@Composable
fun StatusChip(
    label: String,
    status: String,
    modifier: Modifier = Modifier
) {
    val (bg, fg) = statusColors(status)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.padding(vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.width(6.dp))
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = bg
        ) {
            Text(
                text = status,
                style = MaterialTheme.typography.labelMedium,
                color = fg,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(vertical = 2.dp, horizontal = 8.dp)
            )
        }
    }

}

@Composable
private fun statusColors(status: String): Pair<Color, Color> {
    val normalized = status.lowercase()
    return when {
        normalized.contains("publish") || normalized.contains("active") ->
            MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer

        normalized.contains("moderation") ->
            MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer

        normalized.contains("reject") ->
            MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer

        else ->
            MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }
}

@Composable
private fun ShareAppLinkButton(appInfo: AppInfo, modifier: Modifier = Modifier) {
    val contex = LocalActivity.current as ComponentActivity
    IconButton(
        onClick = {
            val url = "https://apps.rustore.ru/app/${appInfo.packageName}"
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, "${appInfo.appName} $url")
                type = "text/plain"
            }
            val shareIntent = Intent.createChooser(sendIntent, null)
            contex.startActivity(shareIntent)
        }, modifier = modifier
    ) {
        Icon(imageVector = Icons.Filled.Share, contentDescription = null)
    }
}
