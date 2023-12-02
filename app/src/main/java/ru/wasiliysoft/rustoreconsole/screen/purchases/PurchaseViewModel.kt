package ru.wasiliysoft.rustoreconsole.screen.purchases

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.wasiliysoft.rustoreconsole.data.Purchase
import ru.wasiliysoft.rustoreconsole.network.RetrofitClient
import ru.wasiliysoft.rustoreconsole.repo.AppListRepository
import ru.wasiliysoft.rustoreconsole.utils.LoadingResult
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

// key = day as String
typealias PurchaseMap = Map<String, List<Purchase>>

class PurchaseViewModel : ViewModel() {
    private val appListRepo = AppListRepository
    private val api = RetrofitClient.api
    private val mutex = Mutex()

    private val _purchasesByDays = MutableLiveData<LoadingResult<PurchaseMap>>()
    val purchasesByDays: LiveData<LoadingResult<PurchaseMap>> = _purchasesByDays

    init {
        load()
    }

    fun load() {
        viewModelScope.launch(Dispatchers.IO) {
            _purchasesByDays.postValue(LoadingResult.Loading("Загружаем..."))
            val list = mutableListOf<Purchase>()
            val exceptionList = mutableListOf<Exception>()
            val appIds = appListRepo.getApps() ?: emptyList()
            if (appIds.isEmpty()) {
                _purchasesByDays.postValue(LoadingResult.Error(Exception("Empty app id list")))
                return@launch
            }
            appIds.chunked(3).forEach { idList ->
                idList.map {
                    launch {
                        try {
                            val purchases = api.getPurchases(it.appId).body.list
                            mutex.withLock { list.addAll(purchases) }
                        } catch (e: Exception) {
                            exceptionList.add(e)
                            e.printStackTrace()
                        }
                    }
                }.joinAll()
            }
            if (exceptionList.isNotEmpty()) {
                _purchasesByDays.postValue(LoadingResult.Error(exceptionList.first()))
            } else {
                list.sortByDescending { it.invoiceId }
                val purchaseMap: PurchaseMap = list.groupBy {
                    it.invoiceDate.format(
                        DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
                    )
                }
                _purchasesByDays.postValue(LoadingResult.Success(purchaseMap))
            }
        }
    }
}
