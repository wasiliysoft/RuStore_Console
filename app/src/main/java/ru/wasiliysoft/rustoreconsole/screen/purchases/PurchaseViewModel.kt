package ru.wasiliysoft.rustoreconsole.screen.purchases

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.wasiliysoft.rustoreconsole.data.AppInfo
import ru.wasiliysoft.rustoreconsole.data.Invoice
import ru.wasiliysoft.rustoreconsole.data.ui.PurchaseListItem
import ru.wasiliysoft.rustoreconsole.network.RetrofitClient
import ru.wasiliysoft.rustoreconsole.repo.AppListRepository
import ru.wasiliysoft.rustoreconsole.utils.LoadingResult
import ru.wasiliysoft.rustoreconsole.utils.calculateAverageDailyAmmount
import ru.wasiliysoft.rustoreconsole.utils.toAmountSumPerMonth
import ru.wasiliysoft.rustoreconsole.utils.toMediumDateString
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.concurrent.ConcurrentLinkedDeque
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.Duration.Companion.milliseconds

// key = day as String
typealias PurchaseMap = Map<String, List<PurchaseListItem>>

typealias AmountSumPerMonth = List<Pair<String, Int>>

class PurchaseViewModel : ViewModel() {
    private val LOG_TAG = "PurchaseViewModel"
    private val repo = AppListRepository
    private val api = RetrofitClient.api

    private val refreshTrigger = MutableSharedFlow<Unit>(replay = 1).apply { tryEmit(Unit) }
    fun load() {
        refreshTrigger.tryEmit(Unit)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val _purchasesByDays: StateFlow<LoadingResult<PurchaseMap>> = refreshTrigger.transformLatest {
        val appIds = repo.fromStorage() ?: emptyList()
        if (appIds.isEmpty()) {
            emit(LoadingResult.Error(Exception("Список приложений пуст")))
            return@transformLatest
        }
        val list = ConcurrentLinkedDeque<Invoice>()
        val progress = AtomicInteger(0)

        emit(LoadingResult.Loading("Загружаем..."))

        try {
            appIds.chunked(3).forEach { idList ->
                coroutineScope {
                    idList.forEach { appInfo ->
                        launch {
                            val purchases = query(appInfo)
                            list.addAll(purchases)
                            val msg = "Загружено ${progress.incrementAndGet()} из ${appIds.size}..."
                            val state = LoadingResult.Loading(msg)
                            emit(state)
                        }
                    }
                    delay(1000.milliseconds)
                }
            }
            val purchaseMap = list.toList().toPurchaseMap()
            emit(LoadingResult.Success(purchaseMap))
        } catch (e: Exception) {
            emit(LoadingResult.Error(Exception(e.message, e)))
            Log.e(LOG_TAG, e.message.toString())
            e.printStackTrace()
        }
    }
        .flowOn(Dispatchers.IO)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = LoadingResult.Loading("Загружаем...")
        )

    val purchasesByDays: StateFlow<LoadingResult<PurchaseMap>> = combine(
        _purchasesByDays, repo.selectedApp // Слушаем триггер выбранного приложения из репозитория
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
                val filteredMap = fullMap.mapValues { (_, invoices) ->
                    invoices.filter { it.applicationCode == selectedApp.appId }
                }.filterValues { it.isNotEmpty() } // Опционально: убираем дни, где не осталось покупок

                LoadingResult.Success(filteredMap)
            }
        } else {
            // Если там Loading или Error — просто пробрасываем их наружу в UI без изменений
            loadingResult
        }
    }
        .flowOn(Dispatchers.Default) // Тяжелую фильтрацию мапы делаем на Default потоке
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = LoadingResult.Loading("Загружаем...")
        )


    val amountSumPerMonth: StateFlow<AmountSumPerMonth> = purchasesByDays.map { result ->
        if (result is LoadingResult.Success) result.data.toAmountSumPerMonth() else emptyList()
    }
        .flowOn(Dispatchers.Default) // Тяжелую фильтрацию мапы делаем на Default потоке
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = emptyList())

    val avgSumm: StateFlow<Int> = purchasesByDays.map { result ->
        if (result is LoadingResult.Success) result.data.calculateAverageDailyAmmount() else 0
    }
        .flowOn(Dispatchers.Default) // Тяжелую фильтрацию мапы делаем на Default потоке
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = 0)


    /**
     * Рекурсивная постраничкая загрузка платежей
     */
    private suspend fun query(appInfo: AppInfo, page: Int = 0): List<Invoice> = withContext(Dispatchers.IO) {
        val dateFrom = LocalDate.now().minusMonths(3) // TODO настройка количества загружаемых месяцев
            .withDayOfMonth(1).format(DateTimeFormatter.ISO_DATE)

        val dateTo = LocalDate.now().plusDays(1).format(DateTimeFormatter.ISO_DATE)

        val querySize = 250
        val result = api.getInvoices(
            appId = "${appInfo.appId}", page = page, dateFrom = dateFrom, dateTo = dateTo, size = querySize
        ).body.invoices.map { it.enrich(appInfo) }.toList()

        if (result.size < querySize) {
            Log.i(LOG_TAG, "${appInfo.appName} loaded all available InApp purchases")
        } else {
            Log.i(LOG_TAG, "${appInfo.appName} need recursive call next page ${page + 1}")
            delay(1000)
            return@withContext result.plus(query(appInfo = appInfo, page = (page + 1)))
        }
        return@withContext result
    }

    private fun Invoice.mapToUi(): PurchaseListItem {
        return PurchaseListItem(
            invoiceDateStr = invoiceDateStr,
            applicationCode = applicationCode,
            amountCurrent = amountCurrent,
            invoiceId = invoiceId,
            productName = if (visualName == "Покупка приложения") null else productName,
            applicationName = applicationName
        )
    }

    private fun List<Invoice>.toPurchaseMap(): PurchaseMap {
        return map { it.mapToUi() }.sortedByDescending { it.invoiceDate }.groupBy {
            it.invoiceDate.toMediumDateString()
        }
    }
}
