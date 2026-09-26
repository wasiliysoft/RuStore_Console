package ru.wasiliysoft.rustoreconsole.screen.apps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import ru.wasiliysoft.rustoreconsole.repo.AppListRepository
import ru.wasiliysoft.rustoreconsole.utils.LoadingResult

class ApplicationListViewModel : ViewModel() {
    private val repo = AppListRepository

    val appListResultState = repo.appListResultFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LoadingResult.Loading("Загружаем...")
    )

    fun refreshData() {
        repo.refreshData()
    }
}