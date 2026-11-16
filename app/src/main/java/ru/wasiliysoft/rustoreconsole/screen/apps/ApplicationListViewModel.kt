package ru.wasiliysoft.rustoreconsole.screen.apps

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import ru.wasiliysoft.rustoreconsole.data.AppInfo
import ru.wasiliysoft.rustoreconsole.repo.AppListRepository
import ru.wasiliysoft.rustoreconsole.utils.LoadingResult
import kotlin.time.Duration.Companion.milliseconds

class ApplicationListViewModel : ViewModel() {
    private val repo = AppListRepository


    private val baseList = repo.appListResultFlow

    @OptIn(ExperimentalCoroutinesApi::class)
    val appListResultState: StateFlow<LoadingResult<List<AppInfo>>> = baseList.transformLatest { base ->
        when (base) {
            is LoadingResult.Loading -> emit(base)
            is LoadingResult.Error -> emit(base)
            is LoadingResult.Success -> {
                // 1. Сразу отдаём «сырой» список, чтобы UI не ждал
                emit(base)

                // 2. Обогащаем и эмитим снапшоты по мере поступления
                if (base.data.isNotEmpty()) {
                    delay(1000.milliseconds) // debounce
                    fetchLastVersion(base.data)
                }
            }
        }
    }
        .flowOn(Dispatchers.IO)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = LoadingResult.Loading("Загружаем..."),
        )


    /**
     * Обогащение списка приложений информацией о статусе последней версии
     */
    private suspend fun FlowCollector<LoadingResult<List<AppInfo>>>.fetchLastVersion(
        list: List<AppInfo>,
    ) {
        val semaphore = Semaphore(permits = 3)
        val results = MutableStateFlow(list)
        Log.d("ApplicationListViewModel", "start enrich")
        coroutineScope {
            // эмитим снапшоты из одной корутины — безопасно
            launch {
                results.collect { emit(LoadingResult.Success(it)) }
            }

            list.mapIndexed { index, appInfo ->
                async {
                    semaphore.withPermit {
                        delay(500.milliseconds)
                        runCatching { repo.fetchLastVersion(appInfo.appId) }
                            .onSuccess { lastVersion ->
                                lastVersion?.let {
                                    results.update { current ->
                                        current.toMutableList().also { it[index] = it[index].withLastVersionInfo(lastVersion) }
                                    }
                                }
                            }
                            .onFailure { it.printStackTrace() }
                    }
                }
            }.awaitAll()
        }
    }

    fun refreshData() {
        repo.refreshData()
    }
}