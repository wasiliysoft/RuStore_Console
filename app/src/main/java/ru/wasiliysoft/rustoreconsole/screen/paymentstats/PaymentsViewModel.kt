package ru.wasiliysoft.rustoreconsole.screen.paymentstats

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
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.Duration.Companion.milliseconds


class PaymentsViewModel : ViewModel() {
    private val LOG_TAG = "PaymentsViewModel"
    private val repo = AppListRepository
    private val api = RetrofitClient.api
    private val mutex = Mutex()

    private val _overallSum = MutableLiveData<LoadingResult<List<AppStats>>>()
    val overallSum: LiveData<LoadingResult<List<AppStats>>> = _overallSum

    init {
        load()
    }

    fun load() {
        val appIds = repo.fromStorage() ?: emptyList()
        if (appIds.isEmpty()) {
            _overallSum.postValue(LoadingResult.Error(Exception("Список приложений пуст")))
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            _overallSum.postValue(LoadingResult.Loading("Наберитесь терпения,\nсервер капризный..."))
            val list = mutableListOf<AppStats>()
            val progress = AtomicInteger(0)

            appIds.forEachIndexed { index, appInfo ->
                try {
                    // Сервер чувствителен к частоте запросов
                    if (index != 0) delay(1000.milliseconds)

                    val resp = api.getPaymentStats("${appInfo.appId}")
                    resp["income"]
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
                    val msg = "Наберитесь терпения,\nсервер капризный... \nЗагружено ${progress.incrementAndGet()} из ${appIds.size}..."
                    _overallSum.postValue(LoadingResult.Loading(msg))
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
