package ru.wasiliysoft.rustoreconsole.fragment.paymentstats

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.wasiliysoft.rustoreconsole.data.Stats
import ru.wasiliysoft.rustoreconsole.network.RetrofitClient
import ru.wasiliysoft.rustoreconsole.repo.AppListRepository
import ru.wasiliysoft.rustoreconsole.utils.LoadingResult


class PaymentsViewModel : ViewModel() {
    private val LOG_TAG = "PaymentsViewModel"
    private val appListRepo = AppListRepository
    private val api = RetrofitClient.api
    private val mutex = Mutex()

    private val _overallSum = MutableLiveData<LoadingResult<Map<Long, Stats>>>()
    val overallSum: LiveData<LoadingResult<Map<Long, Stats>>> = _overallSum

    init {
        load()
    }

    fun load() {
        viewModelScope.launch(Dispatchers.IO) {
            _overallSum.postValue(LoadingResult.Loading("Загружаем..."))
            val list = mutableMapOf<Long, Stats>()
            val exceptionList = mutableListOf<Exception>()
            val appIds = appListRepo.getApps() ?: emptyList()
            if (appIds.isEmpty()) {
                _overallSum.postValue(LoadingResult.Error(Exception("Empty app id list")))
                return@launch
            }
            appIds.chunked(3).forEach { idList ->
                idList.map { appInfo ->
                    launch {
                        try {
                            val url =
                                "https://backapi.rustore.ru/statistics/${appInfo.appId}/payment"
                            val resp = api.getPaymentStats(url)
                            resp.body["income"]
                                ?.get("sum")
                                ?.get("overallSum")
                                ?.let {
                                    mutex.withLock { list[appInfo.appId] = it }
                                    Log.d(LOG_TAG, appInfo.appName + " " + it.toString())
                                }
                        } catch (e: Exception) {
                            exceptionList.add(e)
                            e.printStackTrace()
                        }
                    }
                }.joinAll()
            }
            if (exceptionList.isNotEmpty()) {
                _overallSum.postValue(LoadingResult.Error(exceptionList.first()))
            } else {
                _overallSum.postValue(LoadingResult.Success(list))
            }
        }
    }
}
