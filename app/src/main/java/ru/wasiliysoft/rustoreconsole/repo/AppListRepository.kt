package ru.wasiliysoft.rustoreconsole.repo

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.transformLatest
import ru.wasiliysoft.rustoreconsole.data.AppInfo
import ru.wasiliysoft.rustoreconsole.data.AppListResp
import ru.wasiliysoft.rustoreconsole.data.prefs.PrefHelper
import ru.wasiliysoft.rustoreconsole.network.RetrofitClient
import ru.wasiliysoft.rustoreconsole.utils.LoadingResult


object AppListRepository {
    private const val LOG_TAG = "AppListRepository"
    private val gson by lazy { Gson() }
    private val api by lazy { RetrofitClient.api }
    private val ph by lazy { PrefHelper.getInstance() }

    private val _selectedApp = MutableStateFlow<AppInfo?>(null)
    val selectedApp: StateFlow<AppInfo?> = _selectedApp.asStateFlow()
    fun selectApp(app: AppInfo?) {
        _selectedApp.value = app
    }

    // Внутренний триггер обновлений списка приложений
    // первая сработка сразу при инициализации
    private val refreshTrigger = MutableSharedFlow<Unit>(replay = 1).apply {
        tryEmit(Unit)
    }

    fun refreshData() {
        refreshTrigger.tryEmit(Unit)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val appListResultFlow: Flow<LoadingResult<List<AppInfo>>> = refreshTrigger.transformLatest {
        emit(LoadingResult.Loading("Загружаем..."))
        try {
            // Отправляем то что в кеше
            fromStorage()?.let { list ->
                emit(LoadingResult.Success(list.sortedByDescending { it.appName }))
            }

            val url = "https://backapi.rustore.ru/applicationData/retrieveUserApps"
            val rawBody = api.getRetrieveUserApps(url).string()
            // Обновляем кеш
            toStorage(rawBody)

            // Отправляем свежие данные из кеша, оибо пустой лист
            val list = fromStorage() ?: emptyList()
            emit(LoadingResult.Success(list.sortedByDescending { it.appName }))
        } catch (e: Exception) {
            e.printStackTrace()
            emit(LoadingResult.Error(e))
        }
    }
        .flowOn(Dispatchers.IO)

    fun fromStorage(): List<AppInfo>? {
        val json = ph.jsonAppListResp
        if (json.isEmpty()) return null
        try {
            return gson.fromJson(json, AppListResp::class.java)?.body?.list
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun toStorage(json: String) {
        ph.jsonAppListResp = json
    }
}