package ru.wasiliysoft.rustoreconsole.screen.reviews

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.wasiliysoft.rustoreconsole.data.AppInfo
import ru.wasiliysoft.rustoreconsole.data.DeveloperComment
import ru.wasiliysoft.rustoreconsole.data.UserReview
import ru.wasiliysoft.rustoreconsole.ui.view.RateStarView
import ru.wasiliysoft.rustoreconsole.utils.toMediumDateString


@Composable
fun ReviewDetailItem(
    review: Review,
    modifier: Modifier = Modifier,
) {
    val userReview = review.userReview
    val appInfo = review.appInfo
    val date = userReview.commentDate
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
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
        Text(text = userReview.firstName)
        Text(text = userReview.commentText)
        Text(
            text = "like ${userReview.likeCounter} / dislike ${userReview.dislikeCounter}",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Right
        )
        userReview.devResponse?.let { devResponse ->
            DeveloperResponseListView(
                devResponse = devResponse,
                modifier = Modifier.padding(start = 16.dp, top = 8.dp)
            )
        }
    }
}


@Composable
private fun DeveloperResponseListView(
    devResponse: List<DeveloperComment>,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            devResponse.forEach {
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = it.status, modifier = Modifier.weight(1f))
                    Text(text = it.date.toMediumDateString())
                }
                Text(text = it.text)
            }

        }
    }
}

@Preview()
@Composable
private fun Preview() {
    ReviewDetailItem(
        review = Review(
            appInfo = AppInfo.demo(),
            userReview = UserReview.demo(),
        )
    )
}