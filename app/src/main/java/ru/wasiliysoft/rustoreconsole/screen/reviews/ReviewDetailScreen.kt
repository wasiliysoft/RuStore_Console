package ru.wasiliysoft.rustoreconsole.screen.reviews

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.wasiliysoft.rustoreconsole.ui.view.ErrorTextView
import ru.wasiliysoft.rustoreconsole.ui.view.ProgressView
import ru.wasiliysoft.rustoreconsole.utils.LoadingResult

@Composable
fun ReviewDetailScreen(
    commentId: Long,
    viewModel: ReviewViewModel = viewModel(viewModelStoreOwner = LocalContext.current as ComponentActivity),
) {
    val uiSate = viewModel.reviews
        .observeAsState(LoadingResult.Loading(""))
        .value
    when (uiSate) {
        is LoadingResult.Loading -> ProgressView(uiSate.description)
        is LoadingResult.Success -> ReviewDetailItem(review = uiSate.data.find { it.userReview.commentId == commentId }!!)
        is LoadingResult.Error -> ErrorTextView(exception = uiSate.exception)
    }
}

