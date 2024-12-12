package ru.wasiliysoft.rustoreconsole.screen.reviews

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import ru.wasiliysoft.rustoreconsole.data.AppInfo
import ru.wasiliysoft.rustoreconsole.data.UserReview
import ru.wasiliysoft.rustoreconsole.network.RetrofitClient
import ru.wasiliysoft.rustoreconsole.repo.AppListRepository
import ru.wasiliysoft.rustoreconsole.utils.LoadingResult

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
    private val appListRepo = AppListRepository

    private val mutex = Mutex()

    private val _reviews =
        MutableLiveData<LoadingResult<List<Review>>>(LoadingResult.Loading("Инициализация"))
    val reviews: LiveData<LoadingResult<List<Review>>> = _reviews

    private val errorHandler = CoroutineExceptionHandler { _, exception ->
        _reviews.postValue(LoadingResult.Error(Exception(exception.message, exception)))
        Log.e(LOG_TAG, exception.message.toString())
        exception.printStackTrace()
    }

    init {
        Log.d(LOG_TAG, "init")
        loadReviews()
    }

    fun loadReviews() {
        viewModelScope.launch(errorHandler) {
            _reviews.postValue(LoadingResult.Loading("Загружаем..."))
            val appIds = appListRepo.getApps() ?: emptyList()
            if (appIds.isEmpty()) {
                _reviews.postValue(LoadingResult.Error(Exception("Empty app id list")))
                return@launch
            }
            val list = mutableListOf<Review>()
            appIds.chunked(3).forEach { idList ->
                idList.map { appInfo ->
                    launch {
                        val reviews = loadReviews(appInfo)
                        mutex.withLock { list.addAll(reviews) }
                    }
                }.joinAll()
            }
            list.sortByDescending { it.userReview.commentId }
            _reviews.postValue(LoadingResult.Success(list))
        }
    }

    private suspend fun loadReviews(appInfo: AppInfo): List<Review> = withContext(Dispatchers.IO) {
        return@withContext api.getReviews("${appInfo.appId}").body.reviews.map {
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
