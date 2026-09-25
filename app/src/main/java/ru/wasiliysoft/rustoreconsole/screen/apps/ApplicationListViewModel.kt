package ru.wasiliysoft.rustoreconsole.screen.apps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import ru.wasiliysoft.rustoreconsole.data.AppInfo
import ru.wasiliysoft.rustoreconsole.repo.AppListRepository
import ru.wasiliysoft.rustoreconsole.utils.LoadingResult

class ApplicationListViewModel : ViewModel() {
    private val repo = AppListRepository

    private val refreshTrigger = MutableSharedFlow<Unit>(replay = 1).apply {
        tryEmit(Unit)
    }

    private val selectedApp = MutableSharedFlow<AppInfo?>(replay = 1).apply { tryEmit(null) }

    val selectedAppState: StateFlow<AppInfo?> = selectedApp.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    fun selectApp(appInfo: AppInfo?) {
        selectedApp.tryEmit(appInfo)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val appsState: StateFlow<LoadingResult<List<AppInfo>>> = refreshTrigger
        // transformLatest сбрасывает предыдущую загрузку, если внезапно пришел новый запрос на обновление
        .transformLatest {
            // Эмитим состояние загрузки при каждом старте обновления
            emit(LoadingResult.Loading("Загружаем..."))
            try {
                val sortedList = repo.getAppsForce()
                    .sortedByDescending { it.appName }
                emit(LoadingResult.Success(sortedList))
            } catch (e: Exception) {
                e.printStackTrace()
                emit(LoadingResult.Error(e))
            }
        }
        .flowOn(Dispatchers.IO)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = LoadingResult.Loading("Загружаем...")
        )

    val appsList: StateFlow<List<AppInfo>> = appsState
        .map { result -> if (result is LoadingResult.Success) result.data else emptyList() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList() // Начальное значение — пустой список
        )

    fun refreshData() {
        refreshTrigger.tryEmit(Unit)
    }


}