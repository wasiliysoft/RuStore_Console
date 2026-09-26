package ru.wasiliysoft.rustoreconsole.screen.reviews

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import ru.wasiliysoft.rustoreconsole.data.AppInfo
import ru.wasiliysoft.rustoreconsole.data.UserReview
import ru.wasiliysoft.rustoreconsole.network.RetrofitClient
import ru.wasiliysoft.rustoreconsole.repo.AppListRepository
import ru.wasiliysoft.rustoreconsole.utils.LoadingResult
import java.util.concurrent.ConcurrentLinkedDeque

data class Review(
    val appInfo: AppInfo,
    val userReview: UserReview
)

//аналогичная ситуация
//всегда есть вариант собрать луковку (клин) и вынести часть логики в юзкейсы
//тем более они для этого и нужны
class ReviewViewModel : ViewModel() {
    private val LOG_TAG = "ReviewViewModel"
    private val api = RetrofitClient.api
    private val repo = AppListRepository

    private val refreshTrigger = MutableSharedFlow<Unit>(replay = 1).apply { tryEmit(Unit) }
    fun load() {
        refreshTrigger.tryEmit(Unit)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val _reviews: StateFlow<LoadingResult<List<Review>>> = refreshTrigger
        .transformLatest {
            val appIds = repo.fromStorage() ?: emptyList()
            if (appIds.isEmpty()) {
                emit(LoadingResult.Error(Exception("Empty app id list")))
                return@transformLatest
            }

            try {
                emit(LoadingResult.Loading("Загружаем..."))
                val list = ConcurrentLinkedDeque<Review>()
                appIds.chunked(3).forEach { idList ->
                    coroutineScope {
                        idList.forEach { appInfo ->
                            launch {
                                val reviews = loadReviews(appInfo)
                                list.addAll(reviews)
                            }
                        }
                    }
                }
                val result: List<Review> = list.toList().sortedByDescending { it.userReview.commentId }
                emit(LoadingResult.Success(result))
            } catch (e: Exception) {
                emit(LoadingResult.Error(Exception(e.message, e)))
                Log.e(LOG_TAG, e.message.toString())
                e.printStackTrace()
            }
        }
        .flowOn(Dispatchers.IO)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = LoadingResult.Loading("Загружаем...")
        )

    val reviews: StateFlow<LoadingResult<List<Review>>> = combine(
        _reviews,
        repo.selectedApp // Слушаем триггер выбранного приложения из репозитория
    ) { loadingResult, selectedApp ->

        // Фильтруем только если сеть успешно вернула данные (Success)
        if (loadingResult is LoadingResult.Success) {
            val fullMap = loadingResult.data

            if (selectedApp == null) {
                // Если приложение не выбрано, отдаем всё как есть
                LoadingResult.Success(fullMap)
            } else {
                // Фильтруем карту: внутри списков Invoice оставляем только те,
                // которые принадлежат выбранному appId
                val filteredMap = fullMap.filter { it.appInfo.appId == selectedApp.appId }
                LoadingResult.Success(filteredMap)
            }
        } else {
            // Если там Loading или Error — просто пробрасываем их наружу в UI без изменений
            loadingResult
        }
    }
        .flowOn(Dispatchers.Default) // Тяжелую фильтрацию мапы делаем на Default потоке
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = LoadingResult.Loading("Загружаем...")
        )


    fun loadReviews() {
        refreshTrigger.tryEmit(Unit)
    }

    private suspend fun loadReviews(appInfo: AppInfo): List<Review> = withContext(Dispatchers.IO) {
        return@withContext api.getReviews("${appInfo.appId}").reviews.map {
            Review(
                appInfo = appInfo,
                userReview = it
            )
        }
    }

    fun sendDevResponse(review: Review, devComment: String) {
        if (devComment.isEmpty()) return
        viewModelScope.launch {
            val paramMap = JSONObject()
            paramMap.accumulate("responseText", devComment)
            val type = "application/json; charset=utf-8".toMediaTypeOrNull()
            val requestBody = paramMap.toString().toRequestBody(type)
            val appId = review.appInfo.appId
            val commentId = review.userReview.commentId
            val result = RetrofitClient.api.sendDevResponse(
                appId = "$appId",
                commentId = "$commentId",
                body = requestBody
            )
            if (result.code() == 200) loadReviews()
        }
    }
}
