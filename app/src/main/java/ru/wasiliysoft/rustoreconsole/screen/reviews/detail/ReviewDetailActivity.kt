package ru.wasiliysoft.rustoreconsole.screen.reviews.detail

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import ru.wasiliysoft.rustoreconsole.ui.theme.RuStoreConsoleTheme

class ReviewDetailActivity : ComponentActivity() {
    val commentId: Long by lazy { intent.extras?.getLong("EXTRA_COMMENT_ID") ?: 0L }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RuStoreConsoleTheme {
                Scaffold { paddingValues ->
                    ReviewDetailScreen(
                        modifier = Modifier.padding(paddingValues = paddingValues),
                        commentId = commentId,
                        onActivityResult = ::setResult
                    )
                }
            }
        }
    }
}