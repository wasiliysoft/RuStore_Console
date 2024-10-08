package ru.wasiliysoft.rustoreconsole.screen.purchases

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import ru.wasiliysoft.rustoreconsole.data.AppInfo
import ru.wasiliysoft.rustoreconsole.data.Purchase
import ru.wasiliysoft.rustoreconsole.network.RetrofitClient
import ru.wasiliysoft.rustoreconsole.repo.AppListRepository
import ru.wasiliysoft.rustoreconsole.utils.LoadingResult
import ru.wasiliysoft.rustoreconsole.utils.toMediumDateString
import ru.wasiliysoft.rustoreconsole.utils.toYearAndMonthString
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.concurrent.atomic.AtomicInteger

// key = day as String
typealias PurchaseMap = Map<String, List<Purchase>>

typealias AmountSumPerMonth = List<Pair<String, Int>>

class PurchaseViewModel : ViewModel() {
    private val LOG_TAG = "PurchaseViewModel"
    private val appListRepo = AppListRepository
    private val api = RetrofitClient.api
    private val mutex = Mutex()

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
        val progress = AtomicInteger(0)

        viewModelScope.launch(errorHandler) {
            _purchasesByDays.postValue(LoadingResult.Loading("Загружаем..."))
            appIds.chunked(3).forEach { idList ->
                idList.map { appInfo ->
                    launch(Dispatchers.IO) {
                        val purchases = query(appInfo)
                        mutex.withLock {
                            val msg = "Загружено ${progress.incrementAndGet()} из ${appIds.size}..."
                            val steta = LoadingResult.Loading(msg)
                            _purchasesByDays.postValue(steta)
                            list.addAll(purchases)
                        }
                    }
                }.joinAll()
            }
            _amountSumPerMonth.postValue(list.toMonthSumMap())
            val purchaseMap = list.toPurchaseMap()
            val result = LoadingResult.Success(purchaseMap)
            _purchasesByDays.postValue(result)
        }
    }

    /**
     * Рекурсивная постраничкая загрузка платежей
     */
    private suspend fun query(appInfo: AppInfo, page: Int = 0): List<Purchase> = withContext(Dispatchers.IO) {
        val dateFrom = LocalDate.now()
            .minusMonths(3) // TODO настройка количества загружаемых месяцев
            .withDayOfMonth(1)
            .format(DateTimeFormatter.ISO_DATE)

        val dateTo = LocalDate.now()
            .plusDays(1)
            .format(DateTimeFormatter.ISO_DATE)

        val querySize = 250
        val result = api.getPurchases(
            page = page,
            appId = appInfo.appId,
            dateFrom = dateFrom,
            dateTo = dateTo,
            size = querySize
        ).body.list

        if (result.size < querySize) {
            Log.i(LOG_TAG, "${appInfo.appName} loaded all available purchases")
        } else {
            Log.i(LOG_TAG, "${appInfo.appName} need recursive call next page ${page + 1}")
            delay(1000)
            return@withContext result.plus(query(appInfo = appInfo, page = (page + 1)))
        }
        return@withContext result
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
    }
}
