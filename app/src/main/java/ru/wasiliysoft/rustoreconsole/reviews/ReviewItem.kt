package ru.wasiliysoft.rustoreconsole.reviews

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.wasiliysoft.rustoreconsole.data.UserReview
import ru.wasiliysoft.rustoreconsole.utils.formatFromReview
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun ReviewItem(
    review: UserReview,
    modifier: Modifier = Modifier
) {
    val date = formatFromReview(review.commentDate)
    val cardColor = if (date.toLocalDate() == LocalDate.now()) CardDefaults.cardColors()
    else CardDefaults.outlinedCardColors()

    Card(
        colors = cardColor,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "${review.firstName} ${review.appRating}",
                    modifier = Modifier.weight(1f)
                )
                Text(text = date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)))
            }
            Text(text = review.commentText, modifier = Modifier.padding(vertical = 4.dp))
            Text(
                text = "like ${review.likeCounter} / dislike ${review.dislikeCounter}",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Right
            )
        }
    }
}

@Preview()
@Composable
private fun Preview(modifier: Modifier = Modifier) {
    ReviewItem(
        review = UserReview.demo(),
    )
}