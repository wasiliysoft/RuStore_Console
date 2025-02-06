package ru.wasiliysoft.rustoreconsole.screen.reviews

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.wasiliysoft.rustoreconsole.data.AppInfo
import ru.wasiliysoft.rustoreconsole.data.UserReview
import ru.wasiliysoft.rustoreconsole.ui.view.ErrorTextView
import ru.wasiliysoft.rustoreconsole.ui.view.RateStarView
import ru.wasiliysoft.rustoreconsole.utils.LoadingResult.Error
import ru.wasiliysoft.rustoreconsole.utils.LoadingResult.Loading
import ru.wasiliysoft.rustoreconsole.utils.LoadingResult.Success
import ru.wasiliysoft.rustoreconsole.utils.toMediumDateString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewsScreen(
    modifier: Modifier = Modifier,
    viewModel: ReviewViewModel = viewModel(viewModelStoreOwner = LocalActivity.current as ComponentActivity),
    onClickItem: (commentId: Long) -> Unit = {}
) {
    Surface(Modifier.background(MaterialTheme.colorScheme.background)) {
        Column(
            modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val uiSate = viewModel.reviews.observeAsState(Loading("")).value
            val state = rememberPullToRefreshState()
            PullToRefreshBox(
                state = state,
                isRefreshing = uiSate is Loading,
                onRefresh = viewModel::loadReviews
            ) {
                when (uiSate) {
                    is Loading -> Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = uiSate.description)
                    }

                    is Success -> ReviewListView(reviews = uiSate.data, onClickItem = onClickItem)
                    is Error -> ErrorTextView(exception = uiSate.exception)
                }
            }
        }
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReviewItem(
    review: Review,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {

    val userReview = review.userReview
    val appInfo = review.appInfo

    val date = userReview.commentDate
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = appInfo.appName, fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RateStarView(rate = userReview.appRating, modifier = Modifier.weight(1f))
                Text(text = date.toMediumDateString())
            }
            Text(text = userReview.commentText, maxLines = 2, overflow = TextOverflow.Ellipsis)
            if (userReview.likeCounter != 0 || userReview.dislikeCounter != 0) {
                Text(
                    text = "like ${userReview.likeCounter} / dislike ${userReview.dislikeCounter}",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Right
                )
            }

            userReview.devResponse?.find { it.status == "PUBLISHED" }?.date?.let { date ->
                val dateStr = date.toMediumDateString()
                Text(text = "Дата ответа $dateStr", fontWeight = FontWeight.Light)
            }

        }
    }
}

@Preview()
@Composable
private fun PreviewItem() {
    ReviewItem(
        review = Review(
            appInfo = AppInfo.demo(),
            userReview = UserReview.demo(),
        )
    )
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    ReviewsScreen()
}