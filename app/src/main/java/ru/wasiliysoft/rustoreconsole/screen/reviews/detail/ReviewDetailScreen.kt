package ru.wasiliysoft.rustoreconsole.screen.reviews.detail

import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.wasiliysoft.rustoreconsole.repo.ReviewRepository.Review
import ru.wasiliysoft.rustoreconsole.ui.view.ErrorTextView
import ru.wasiliysoft.rustoreconsole.utils.LoadingResult
import ru.wasiliysoft.rustoreconsole.utils.LoadingResult.Loading


@Composable
fun ReviewDetailScreen(
    commentId: Long,
    modifier: Modifier = Modifier,
    onActivityResult: (result: Int) -> Unit,
    viewModel: ReviewDetailViewModel = viewModel(viewModelStoreOwner = LocalActivity.current as ComponentActivity)
) {
    Surface(modifier = modifier) {
        val uiSate = viewModel.reviews.collectAsStateWithLifecycle().value
        val state = rememberPullToRefreshState()
        PullToRefreshBox(
            state = state,
            isRefreshing = uiSate is Loading,
            onRefresh = viewModel::load
        ) {
            when (uiSate) {
                is Loading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = uiSate.description)
                }

                is LoadingResult.Success -> {
                    uiSate.data.find { it.userReview.commentId == commentId }?.let { review ->
                        ReviewDetailView(review = review, onSend = {
                            viewModel.sendDevResponse(review = review, devComment = it)
                            onActivityResult(Activity.RESULT_OK)
                        })
                    }
                }

                is LoadingResult.Error -> ErrorTextView(exception = uiSate.exception)
            }
        }
    }
}

@Composable
fun ReviewDetailView(review: Review, onSend: (comment: String) -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        val (devCommnet, onChange) = remember { mutableStateOf("") }
        ReviewDetailItem(
            review = review, modifier = Modifier.weight(1f),
            onEnterEditComment = { onChange(it) })
        Surface(
            tonalElevation = 4.dp,
            modifier = Modifier
                .navigationBarsPadding() // Отступ снизу для системной полосы навигации
                .imePadding()           // МАГИЯ: Автоматически поднимает поле над клавиатурой!
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                OutlinedTextField(
                    value = devCommnet,
                    onValueChange = onChange,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { onSend(devCommnet) }) {
                    Icon(imageVector = Icons.Filled.Send, contentDescription = null)
                }
            }
        }
    }
}