package ru.wasiliysoft.rustoreconsole.repo

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch
import ru.wasiliysoft.rustoreconsole.data.AppInfo
import ru.wasiliysoft.rustoreconsole.data.UserReview
import ru.wasiliysoft.rustoreconsole.network.RetrofitClient
import ru.wasiliysoft.rustoreconsole.utils.LoadingResult
import java.util.concurrent.ConcurrentLinkedDeque

object ReviewRepository {
    data class Review(
        val appInfo: AppInfo,
        val userReview: UserReview
    )

    private val api by lazy { RetrofitClient.api }


    private val _selectedReview = MutableStateFlow<Review?>(null)
    val selectedReview: StateFlow<Review?> = _selectedReview.asStateFlow()
    fun selectReview(review: Review?) {
        _selectedReview.value = review
    }

    private val refreshTrigger = MutableSharedFlow<Unit>(replay = 1).apply {
        tryEmit(Unit)
    }

    fun refreshData() {
        refreshTrigger.tryEmit(Unit)
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    val reviewList: Flow<LoadingResult<List<Review>>> = refreshTrigger.transformLatest<Unit, LoadingResult<List<Review>>> {
        emit(LoadingResult.Loading("Загружаем..."))
        try {
            emit(LoadingResult.Loading("Загружаем..."))
            val apps = AppListRepository.fromStorage() ?: emptyList()
            val reviews = loadReviews(apps)
            emit(LoadingResult.Success(reviews))
        } catch (e: Exception) {
            e.printStackTrace()
            emit(LoadingResult.Error(e))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Возвращает отсоритированный по дате список
     */
    private suspend fun loadReviews(apps: List<AppInfo>): List<Review> {
        val result = ConcurrentLinkedDeque<Review>()
        apps.chunked(3).forEach { idList ->
            coroutineScope {
                idList.forEach { appInfo ->
                    launch {
                        val resp = api.getReviews("${appInfo.appId}").reviews
                        val reviews = resp.map { Review(appInfo = appInfo, userReview = it) }
                        result.addAll(reviews)
                    }
                }
            }
        }

        return result.sortedByDescending { it.userReview.editedAt }.toList()
    }
}
