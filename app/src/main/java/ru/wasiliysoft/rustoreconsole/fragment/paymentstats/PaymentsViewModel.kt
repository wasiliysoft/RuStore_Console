package ru.wasiliysoft.rustoreconsole.fragment.paymentstats

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.wasiliysoft.rustoreconsole.network.RetrofitClient
import ru.wasiliysoft.rustoreconsole.repo.AppListRepository
import ru.wasiliysoft.rustoreconsole.utils.LoadingResult


class PaymentsViewModel : ViewModel() {
    private val LOG_TAG = "PaymentsViewModel"
    private val appListRepo = AppListRepository
    private val api = RetrofitClient.api
    private val mutex = Mutex()

    private val _overallSum = MutableLiveData<LoadingResult<List<AppStats>>>()
    val overallSum: LiveData<LoadingResult<List<AppStats>>> = _overallSum

    init {
        load()
    }

    fun load() {
        viewModelScope.launch(Dispatchers.IO) {
            _overallSum.postValue(LoadingResult.Loading("Загружаем..."))
            val list = mutableListOf<AppStats>()

            val appIds = appListRepo.getApps() ?: emptyList()
            if (appIds.isEmpty()) {
                _overallSum.postValue(LoadingResult.Error(Exception("Empty app id list")))
                return@launch
            }
            appIds.forEach { appInfo ->
                try {
                    val url = "https://backapi.rustore.ru/statistics/${appInfo.appId}/payment"
                    val resp = api.getPaymentStats(url)
                    resp.body["income"]
                        ?.get("sum")
                        ?.get("overallSum")
                        ?.let {
                            val appStats = AppStats(
                                appId = appInfo.appId,
                                appName = appInfo.appName,
                                overallSum = it
                            )
                            mutex.withLock { list.add(appStats) }
                            Log.d(LOG_TAG, appStats.toString())
                        }
                    //TODO Сервер чувствителен к частоте запросов, было бы хорошо блокировать кнопку
                    // "обновить" на 10-15 секунд в случае кода HTTP 429
                    delay(1000)
                } catch (e: Exception) {
                    _overallSum.postValue(LoadingResult.Error(e))
                    e.printStackTrace()
                    return@launch
                }
            }
            list.sortByDescending { it.overallSum.monthlyStats }
            _overallSum.postValue(LoadingResult.Success(list))
        }
    }
}
