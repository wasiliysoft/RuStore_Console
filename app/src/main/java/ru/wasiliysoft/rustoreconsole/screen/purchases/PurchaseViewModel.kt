package ru.wasiliysoft.rustoreconsole.screen.purchases

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
import ru.wasiliysoft.rustoreconsole.data.Purchase
import ru.wasiliysoft.rustoreconsole.network.RetrofitClient
import ru.wasiliysoft.rustoreconsole.repo.AppListRepository
import ru.wasiliysoft.rustoreconsole.utils.LoadingResult
import ru.wasiliysoft.rustoreconsole.utils.toMediumDateString
import ru.wasiliysoft.rustoreconsole.utils.toYearAndMonthString
import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicInteger

// key = day as String
typealias PurchaseMap = Map<String, List<Purchase>>

typealias AmountSumPerMonth = List<Pair<String, Int>>

class PurchaseViewModel : ViewModel() {
    private val LOG_TAG = "PurchaseViewModel"
    private val appListRepo = AppListRepository
    private val api = RetrofitClient.api
    private val mutex = Mutex()
    private val minimalPurchaseDateByApp = mutableListOf<LocalDateTime>()

    private val _purchasesByDays = MutableLiveData<LoadingResult<PurchaseMap>>()
    val purchasesByDays: LiveData<LoadingResult<PurchaseMap>> = _purchasesByDays

    private val _amountSumPerMonth = MutableLiveData<AmountSumPerMonth>(emptyList())
    val amountSumPerMonth: LiveData<AmountSumPerMonth> = _amountSumPerMonth

    private val errorHandler = CoroutineExceptionHandler { _, exception ->
        _purchasesByDays.postValue(LoadingResult.Error(Exception(exception.message, exception)))
        Log.e(LOG_TAG, exception.message.toString())
        exception.printStackTrace()
    }

    init {
        load()
    }

    fun load() {
        val appIds = appListRepo.getApps() ?: emptyList()
        if (appIds.isEmpty()) {
            _purchasesByDays.value = LoadingResult.Error(Exception("Empty app id list"))
            return
        }
        val list = mutableListOf<Purchase>()
        val querySize = 500 // TODO создать настройку
        val progress = AtomicInteger(0)

        viewModelScope.launch(errorHandler) {
            _purchasesByDays.postValue(LoadingResult.Loading("Загружаем..."))
            appIds.chunked(3).forEach { idList ->
                idList.map { appInfo ->
                    launch(Dispatchers.IO) {
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
                        mutex.withLock {
                            val msg = "Загружено ${progress.incrementAndGet()} из ${appIds.size}..."
                            val steta = LoadingResult.Loading(msg)
                            _purchasesByDays.postValue(steta)
                            list.addAll(purchases)
                        }
                    }
                }.joinAll()
            }

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
            _amountSumPerMonth.postValue(list.toMonthSumMap())
            val purchaseMap = list.toPurchaseMap()
            val result = LoadingResult.Success(purchaseMap)
            _purchasesByDays.postValue(result)
        }
    }

    private fun List<Purchase>.toPurchaseMap(): PurchaseMap {
        return sortedByDescending { it.invoiceId }.groupBy {
            it.invoiceDate.toMediumDateString()
        }
    }

    // TODO создать настройку количества строк,
    //  опасаться неполных данных за первый и последний месяц
    private fun List<Purchase>.toMonthSumMap(): AmountSumPerMonth {
        return groupBy { it.invoiceDate.toYearAndMonthString() }
            .map { entry -> Pair(entry.key, entry.value.sumOf { it.amountCurrent / 100 }) }
            .sortedByDescending { it.first }
            .toMutableList().apply {
                removeLast() // защита от неполного первого месяца
            }.take(4)
    }
}
