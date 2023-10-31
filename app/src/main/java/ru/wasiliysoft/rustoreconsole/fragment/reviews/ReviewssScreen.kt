package ru.wasiliysoft.rustoreconsole.fragment.reviews

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.wasiliysoft.rustoreconsole.data.UserReview
import ru.wasiliysoft.rustoreconsole.ui.view.ProgressView
import ru.wasiliysoft.rustoreconsole.ui.view.RefreshButton
import ru.wasiliysoft.rustoreconsole.utils.LoadingResult

@Composable
fun ReviewsScreen(
    uiSate: State<LoadingResult<List<UserReview>>>,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when (uiSate.value) {
                is LoadingResult.Loading -> {
                    ProgressView((uiSate.value as LoadingResult.Loading).description)
                }

                is LoadingResult.Success -> {
                    ReviewListView(
                        reviews = (uiSate.value as LoadingResult.Success<List<UserReview>>).data
                    )
                }

                is LoadingResult.Error -> {
                    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        val message = (uiSate.value as LoadingResult.Error).exception.message
                        Text(text = message ?: "Неизвестная ошибка")
                    }
                }
            }
        }
        RefreshButton(onRefresh)
    }
}


@Composable
private fun ReviewListView(
    reviews: List<UserReview>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(16.dp),
        modifier = modifier
    ) {
        items(items = reviews, key = { it.commentId }) {
            ReviewItem(it)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun Preview(modifier: Modifier = Modifier) {
    val uiSate = remember {
        mutableStateOf(
            LoadingResult.Success(List(5) {
                UserReview.demo(it.toLong())
            })
        )
    }
    ReviewsScreen(uiSate = uiSate, onRefresh = {})
}

@Preview(showBackground = true)
@Composable
private fun PreviewLoading(modifier: Modifier = Modifier) {
    val uiSate = remember {
        mutableStateOf(
            LoadingResult.Loading("Загружаем")
        )
    }
    ReviewsScreen(uiSate = uiSate, onRefresh = {})
}