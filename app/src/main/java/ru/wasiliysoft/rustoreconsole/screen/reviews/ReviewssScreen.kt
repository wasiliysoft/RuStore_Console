package ru.wasiliysoft.rustoreconsole.screen.reviews

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.wasiliysoft.rustoreconsole.ui.view.ErrorTextView
import ru.wasiliysoft.rustoreconsole.ui.view.ProgressView
import ru.wasiliysoft.rustoreconsole.ui.view.RefreshButton
import ru.wasiliysoft.rustoreconsole.utils.LoadingResult.Error
import ru.wasiliysoft.rustoreconsole.utils.LoadingResult.Loading
import ru.wasiliysoft.rustoreconsole.utils.LoadingResult.Success

@Composable
fun ReviewsScreen(
    modifier: Modifier = Modifier,
    viewModel: ReviewViewModel = viewModel(viewModelStoreOwner = LocalContext.current as ComponentActivity),
    onClickItem: (commentId: Long) -> Unit = {}
) {
    val uiSate = viewModel.reviews
        .observeAsState(Loading(""))
        .value

    Column(
        modifier = modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when (uiSate) {
                is Loading -> ProgressView(uiSate.description)
                is Success -> ReviewListView(reviews = uiSate.data, onClickItem = onClickItem)
                is Error -> ErrorTextView(exception = uiSate.exception)
            }
        }
        RefreshButton(viewModel::loadReviews)
    }
}

@Composable
private fun ReviewListView(
    reviews: List<Review>,
    modifier: Modifier = Modifier,
    onClickItem: (commentId: Long) -> Unit = {}
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(16.dp),
        modifier = modifier
    ) {
        items(items = reviews, key = { it.userReview.commentId }) { review ->
            ReviewItem(review, onClick = { onClickItem(review.userReview.commentId) })
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun Preview(modifier: Modifier = Modifier) {
    ReviewsScreen()
}