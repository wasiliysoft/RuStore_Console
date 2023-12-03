package ru.wasiliysoft.rustoreconsole.screen.purchases

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
import ru.wasiliysoft.rustoreconsole.data.Purchase
import ru.wasiliysoft.rustoreconsole.network.RetrofitClient
import ru.wasiliysoft.rustoreconsole.repo.AppListRepository
import ru.wasiliysoft.rustoreconsole.utils.LoadingResult
import ru.wasiliysoft.rustoreconsole.utils.toMediumDateString
import java.time.LocalDateTime

// key = day as String
typealias PurchaseMap = Map<String, List<Purchase>>

class PurchaseViewModel : ViewModel() {
    private val LOG_TAG = "PurchaseViewModel"
    private val appListRepo = AppListRepository
    private val api = RetrofitClient.api
    private val mutex = Mutex()
    private val minimalPurchaseDateByApp = mutableListOf<LocalDateTime>()

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
                idList.map { appInfo ->
                    launch {
                        try {
                            val querySize = 500
                            val purchases = api.getPurchases(
                                appId = appInfo.appId,
                                size = querySize
                            ).body.list
                            if (purchases.size < querySize) {
                                // сервер вернул меньше чем мы просили,
                                // либо он отдал всё что есть либо изменилось API и лимиты
                                Log.i(LOG_TAG, "${appInfo.appName} loaded all available purchases")
                            } else {
                                // сервер вернул больше или ровно столько сколько мы просили
                                // большая вероятность что загружены не все покупки
                                // сохраним самую старую покупку из загруженного списка
                                // чтобы в дальнейшем можно было вычислить
                                // максимульную дату среди минимальных и отсечь пкупки которые младше :D
                                val minIvoiceDate = purchases.minBy { it.invoiceDate }.invoiceDate
                                mutex.withLock { minimalPurchaseDateByApp.add(minIvoiceDate) }
                                Log.d(LOG_TAG, "${appInfo.appName} $minIvoiceDate")
                            }
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
                if (minimalPurchaseDateByApp.isNotEmpty()) {
                    // Есть приложения для которых загружены не все платежи
                    // нужно обрезать list до самой максимальной даты среди минимальных.
                    //
                    // Это решает проблему растягивания "хвоста" до самого редко покупаемого приложения
                    // даже если список покупок самых популярных приложений уже исчерапан,
                    // что в свою очередь приводит к неправильному расчету суммы покупок за день, месяц.

                    val maxByMinPurchasesDate = minimalPurchaseDateByApp.max()
                    val lastPurchaseDateForShow = maxByMinPurchasesDate.plusDays(1).toLocalDate()
                    Log.d(LOG_TAG, "maxByMinPurchasesDate $maxByMinPurchasesDate")
                    Log.d(LOG_TAG, "lastPurchaseDateForShow $lastPurchaseDateForShow")
                    val beforeSize = list.size
                    list.removeIf { it.invoiceDate.toLocalDate() < lastPurchaseDateForShow }
                    Log.d(LOG_TAG, "removed ${beforeSize - list.size} purchases")
                } else {
                    Log.i(LOG_TAG, "loaded all available purchases")
                }
                val purchaseMap = list.toPurchaseMap()
                val result = LoadingResult.Success(purchaseMap)
                _purchasesByDays.postValue(result)
            }
        }
    }

    private fun List<Purchase>.toPurchaseMap(): PurchaseMap {
        return sortedByDescending { it.invoiceId }.groupBy {
            it.invoiceDate.toMediumDateString()
        }
    }
}
