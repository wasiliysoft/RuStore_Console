package ru.wasiliysoft.rustoreconsole.repo

import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.shareIn
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
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _selectedApp = MutableStateFlow<AppInfo?>(null)
    val selectedApp: StateFlow<AppInfo?> = _selectedApp.asStateFlow()
    fun selectApp(app: AppInfo?) {
        _selectedApp.value = app
    }

    private val refreshTrigger = MutableStateFlow(0)
    fun refreshData() {
        refreshTrigger.value += 1
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    val appListResultFlow: SharedFlow<LoadingResult<List<AppInfo>>> = refreshTrigger.transformLatest {
        emit(LoadingResult.Loading("Загружаем..."))
        try {
            // Отправляем то что в кеше
            fromStorage()?.let { list ->
                emit(LoadingResult.Success(list))
            }

            val url = "https://backapi.rustore.ru/applicationData/retrieveUserApps"
            val rawBody = api.getRetrieveUserApps(url).string()
            // Обновляем кеш
            toStorage(rawBody)

            // Отправляем свежие данные из кеша, оибо пустой лист
            val list = fromStorage() ?: emptyList()
            emit(LoadingResult.Success(list))
        } catch (e: Exception) {
            e.printStackTrace()
            emit(LoadingResult.Error(e))
        }
    }
        .flowOn(Dispatchers.IO)
        .shareIn(
            scope = coroutineScope,
            started = SharingStarted.Eagerly,   // ← стартует сразу, без подписчиков
            replay = 1                          // ← новые подписчики получают последнее значение
        )

    fun fromStorage(): List<AppInfo>? {
        val json = ph.jsonAppListResp
        if (json.isEmpty()) return null
        try {
            return gson.fromJson(json, AppListResp::class.java)?.body?.list?.sortedBy { it.appName }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun toStorage(json: String) {
        ph.jsonAppListResp = json
    }
}