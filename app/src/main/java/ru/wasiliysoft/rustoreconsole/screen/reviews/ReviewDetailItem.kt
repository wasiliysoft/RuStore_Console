package ru.wasiliysoft.rustoreconsole.screen.reviews

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
    onEnterEditComment: (text: String) -> Unit,
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = appInfo.appName, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            RateStarView(rate = userReview.appRating)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = userReview.firstName, modifier = Modifier.weight(1f))
            Text(text = date.toMediumDateString())
        }
        SelectionContainer {
            Text(text = userReview.commentText)
        }
        Spacer(modifier = Modifier.size(4.dp))
        userReview.deviceInfo?.let { devInfo ->
            devInfo.manufacturer?.let { Text(text = "manufacturer: $it") }
            devInfo.model?.let { Text(text = "model: $it") }
            devInfo.firmwareVersion?.let { Text(text = "firmwareVersion: $it") }
        }

        Text(
            text = "like ${userReview.likeCounter} / dislike ${userReview.dislikeCounter}",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Right
        )
        userReview.devResponse?.let { devResponse ->
            DeveloperResponseListView(
                devResponse = devResponse,
                modifier = Modifier
                    .padding(start = 16.dp, top = 8.dp),
                onClick = onEnterEditComment
            )
        }
    }
}


@Composable
private fun DeveloperResponseListView(
    devResponse: List<DeveloperComment>,
    onClick: (text: String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        devResponse.forEach {
            Column(
                Modifier
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(8.dp)
                    ) {
                        Text(text = it.status, Modifier.padding(bottom = 8.dp))
                        Text(text = it.date.toMediumDateString())
                    }
                    IconButton(onClick = { onClick(it.text) }) {
                        Icon(imageVector = Icons.Filled.Edit, contentDescription = "Edit")
                    }
                }
                SelectionContainer {
                    Text(text = it.text, Modifier.padding(8.dp))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    ReviewDetailItem(
        review = Review(
            appInfo = AppInfo.demo(),
            userReview = UserReview.demo()
        ), onEnterEditComment = {}
    )
}