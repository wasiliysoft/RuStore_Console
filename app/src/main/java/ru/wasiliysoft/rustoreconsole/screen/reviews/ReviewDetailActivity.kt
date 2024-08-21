package ru.wasiliysoft.rustoreconsole.screen.reviews

import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.wasiliysoft.rustoreconsole.ui.view.ErrorTextView
import ru.wasiliysoft.rustoreconsole.ui.view.ProgressView
import ru.wasiliysoft.rustoreconsole.utils.LoadingResult


@Composable
fun ReviewDetailActivity(
    commentId: Long,
    viewModel: ReviewViewModel = viewModel(viewModelStoreOwner = LocalContext.current as ComponentActivity),
) {
    Surface(Modifier.background(MaterialTheme.colorScheme.background)) {
        val uiSate = viewModel.reviews
            .observeAsState(LoadingResult.Loading(""))
            .value
        when (uiSate) {
            is LoadingResult.Loading -> ProgressView(uiSate.description)
            is LoadingResult.Success -> {
                uiSate.data.find { it.userReview.commentId == commentId }?.let { review: Review ->
                    ReviewDetailScreen(review = review, onSend = {
                        viewModel.sendDevResponse(review = review, devComment = it)
                    })
                }
            }

            is LoadingResult.Error -> ErrorTextView(exception = uiSate.exception)
        }
    }
}


@Composable
fun ReviewDetailScreen(review: Review, onSend: (comment: String) -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        val (devCommnet, onChange) = remember { mutableStateOf("") }
        ReviewDetailItem(review = review, modifier = Modifier.weight(1f),
            onEnterEditComment = { onChange(it) })
        Surface(
            tonalElevation = 4.dp
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
