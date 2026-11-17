package ru.wasiliysoft.rustoreconsole.screen.whatsnew

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.wasiliysoft.rustoreconsole.BuildConfig
import ru.wasiliysoft.rustoreconsole.data.prefs.PrefHelper
import ru.wasiliysoft.rustoreconsole.ui.theme.RuStoreConsoleTheme
import ru.wasiliysoft.rustoreconsole.ui.view.TitledCard

class WhatsNewActivity : ComponentActivity() {
    private val ph by lazy { PrefHelper.getInstance() }

    private val isAll by lazy { intent.getBooleanExtra("EXTRA_ALL", false) }

    companion object {
        fun getList(currentVersion: Int, lastSeenVersion: Int) =
            WhatsNewRegistry.ALL_NOTES
                .filter { it.versionCode in (lastSeenVersion + 1)..currentVersion }
                .sortedByDescending { it.versionCode }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RuStoreConsoleTheme {
                WhatsNewScreen(
                    notes = getList(
                        currentVersion = BuildConfig.VERSION_CODE,
                        lastSeenVersion = if (isAll) 0 else ph.lastSeenVersion
                    ),
                    onConfirm = {
                        ph.lastSeenVersion = BuildConfig.VERSION_CODE
                        finish()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WhatsNewScreen(
    notes: List<ReleaseNote>,
    onConfirm: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Что нового") })
        }
    ) { paddingValues ->
        WhatsNewFragment(
            notes = notes,
            onConfirm = onConfirm,
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@Composable
private fun WhatsNewFragment(
    notes: List<ReleaseNote>,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(16.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(items = notes, key = { it.versionCode }) { note ->
                TitledCard(title = note.title) {
                    note.items.forEach { item ->
                        Text("• $item", modifier = Modifier.padding(horizontal = 8.dp))
                    }
                }
            }
        }
        Button(
            onClick = onConfirm,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Понятно")
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun Preview() {
    val previewNotes = listOf(
        ReleaseNote(versionCode = 1, title = "Версия 1.0", items = listOf("Первый релиз")),
        ReleaseNote(
            versionCode = 2,
            title = "Версия 1.1",
            items = listOf("Исправлены ошибки", "Новая тема")
        ),
        ReleaseNote(
            versionCode = 3,
            title = "Версия 1.2",
            items = listOf("Исправлены ошибки", "Новая тема")
        ),
        ReleaseNote(
            versionCode = 4,
            title = "Версия 1.3",
            items = listOf("Исправлены ошибки", "Новая тема")
        ),
        ReleaseNote(
            versionCode = 5,
            title = "Версия 1.4",
            items = listOf("Исправлены ошибки", "Новая тема")
        ),
        ReleaseNote(
            versionCode = 6,
            title = "Версия 1.5",
            items = listOf("Исправлены ошибки", "Новая тема")
        ),
        ReleaseNote(
            versionCode = 7,
            title = "Версия 1.6",
            items = listOf("Исправлены ошибки", "Новая тема")
        ),
        ReleaseNote(
            versionCode = 8,
            title = "Версия 1.7",
            items = listOf("Исправлены ошибки", "Новая тема")
        ),
    ).sortedByDescending { it.versionCode }

    RuStoreConsoleTheme {
        WhatsNewScreen(
            notes = previewNotes,
            onConfirm = {}
        )
    }
}