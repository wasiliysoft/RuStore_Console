package ru.wasiliysoft.rustoreconsole.fragment.reviews

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.wasiliysoft.rustoreconsole.data.UserReview
import ru.wasiliysoft.rustoreconsole.network.RetrofitClient
import ru.wasiliysoft.rustoreconsole.repo.AppListRepository
import ru.wasiliysoft.rustoreconsole.utils.LoadingResult

//аналогичная ситуация
//всегда есть вариант собрать луковку (клин) и вынести часть логики в юзкейсы
//тем более они для этого и нужны
class ReviewViewModel : ViewModel() {
    private val api = RetrofitClient.api
    private val appListRepo = AppListRepository

    private val mutex = Mutex()

    private val _reviews = MutableLiveData<LoadingResult<List<UserReview>>>()
    val reviews: LiveData<LoadingResult<List<UserReview>>> = _reviews

    init {
        loadReviews()
    }

    fun loadReviews() {
        viewModelScope.launch(Dispatchers.IO) {
            _reviews.postValue(LoadingResult.Loading("Загружаем..."))
            val list = mutableListOf<UserReview>()
            val exceptionList = mutableListOf<Exception>()
            val appIds = appListRepo.getApps() ?: emptyList()
            if (appIds.isEmpty()) {
                _reviews.postValue(LoadingResult.Error(Exception("Empty app id list")))
                return@launch
            }
            appIds.chunked(3).forEach { idList ->
                idList.map {
                    launch {
                        try {
                            val url = "https://backapi.rustore.ru/devs/app/${it.appId}/comment"
                            val reviews = api.getReviews(url = url).body.reviews
                            mutex.withLock { list.addAll(reviews) }
                        } catch (e: Exception) {
                            exceptionList.add(e)
                            e.printStackTrace()
                        }
                    }
                }.joinAll()
            }
            if (exceptionList.isNotEmpty()) {
                _reviews.postValue(LoadingResult.Error(exceptionList.first()))
            } else {
                list.sortByDescending { it.commentId }
                _reviews.postValue(LoadingResult.Success(list))
            }
        }
    }
}
