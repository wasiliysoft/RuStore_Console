package ru.wasiliysoft.rustoreconsole.screen.bottomsheet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import ru.wasiliysoft.rustoreconsole.data.AppInfo
import ru.wasiliysoft.rustoreconsole.repo.AppListRepository
import ru.wasiliysoft.rustoreconsole.utils.LoadingResult

class SelectAppBottomSheetViewModel : ViewModel() {
    private val repo = AppListRepository

    // Трансформируем состояние в чистый список
    val appsList = repo.appListResultFlow
        .map { result -> if (result is LoadingResult.Success) result.data else emptyList() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun selectApp(app: AppInfo) {
        repo.selectApp(app)
    }
}