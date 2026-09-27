package ru.wasiliysoft.rustoreconsole.screen.reviews.detail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import ru.wasiliysoft.rustoreconsole.network.RetrofitClient
import ru.wasiliysoft.rustoreconsole.repo.ReviewRepository
import ru.wasiliysoft.rustoreconsole.repo.ReviewRepository.Review

import ru.wasiliysoft.rustoreconsole.utils.LoadingResult
import java.util.concurrent.ConcurrentLinkedDeque

class ReviewDetailViewModel : ViewModel() {
    private val LOG_TAG = "ReviewDetailViewModelTag"
    private val api = RetrofitClient.api
    private val reviewRepository = ReviewRepository

    private val refreshTrigger = MutableSharedFlow<Unit>(replay = 1).apply { tryEmit(Unit) }

    fun load() {
        refreshTrigger.tryEmit(Unit)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val reviews: StateFlow<LoadingResult<List<Review>>> = refreshTrigger
        .transformLatest {
            try {
                emit(LoadingResult.Loading("Загружаем..."))
                val review = reviewRepository.selectedReview.value ?: throw Exception("Selected review not set in repository")
                val list = ConcurrentLinkedDeque<Review>()

                val resp = api.getReviews("${review.appInfo.appId}").reviews
                val reviews = resp.map { Review(appInfo = review.appInfo, userReview = it) }
                list.addAll(reviews)

                val result: List<Review> = list.toList().sortedByDescending { it.userReview.editedAt }
                emit(LoadingResult.Success(result))
            } catch (e: Exception) {
                emit(LoadingResult.Error(Exception(e.message, e)))
                Log.e(LOG_TAG, e.message.toString())
                e.printStackTrace()
            }
        }
        .flowOn(Dispatchers.IO)
        .stateIn(
            scope = viewModelScope, started = SharingStarted.Eagerly,
            initialValue = LoadingResult.Loading("Загружаем...")
        )

    fun sendDevResponse(review: Review, devComment: String) {
        if (devComment.isEmpty()) return
        viewModelScope.launch {
            val paramMap = JSONObject()
            paramMap.accumulate("responseText", devComment)
            val type = "application/json; charset=utf-8".toMediaTypeOrNull()
            val requestBody = paramMap.toString().toRequestBody(type)
            val appId = review.appInfo.appId
            val commentId = review.userReview.commentId
            val resp = RetrofitClient.api.sendDevResponse(
                appId = "$appId",
                commentId = "$commentId",
                body = requestBody
            )
            if (resp.isSuccessful && resp.code() == 200) load()

        }
    }
}