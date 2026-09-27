package ru.wasiliysoft.rustoreconsole.screen.reviews

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import ru.wasiliysoft.rustoreconsole.repo.AppListRepository
import ru.wasiliysoft.rustoreconsole.repo.ReviewRepository
import ru.wasiliysoft.rustoreconsole.repo.ReviewRepository.Review
import ru.wasiliysoft.rustoreconsole.utils.LoadingResult


class ReviewViewModel : ViewModel() {
    private val LOG_TAG = "ReviewViewModel"
    private val repo = AppListRepository
    private val reviewRepository = ReviewRepository


    fun load() {
        reviewRepository.refreshData()
    }

    fun selectReview(review: Review) {
        reviewRepository.selectReview(review)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val reviewsAll: StateFlow<LoadingResult<List<Review>>> = reviewRepository.reviewList
        .stateIn(
            scope = viewModelScope, started = SharingStarted.Eagerly,
            initialValue = LoadingResult.Loading("Загружаем...")
        )

    /**
     * Отзывы отфильтрованные по приложению
     * @see ru.wasiliysoft.rustoreconsole.repo.AppListRepository.selectedApp
     */
    val reviewsFiltered: StateFlow<LoadingResult<List<Review>>> = combine(
        reviewsAll,
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
            scope = viewModelScope, started = SharingStarted.Eagerly,
            initialValue = LoadingResult.Loading("Загружаем...")
        )
}
