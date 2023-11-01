package ru.wasiliysoft.rustoreconsole.repo

import com.google.gson.Gson
import ru.wasiliysoft.rustoreconsole.data.AppInfo
import ru.wasiliysoft.rustoreconsole.data.AppListResp
import ru.wasiliysoft.rustoreconsole.network.RetrofitClient
import ru.wasiliysoft.rustoreconsole.utils.PrefHelper


// Интересно, но нужно чётко следить за доступом к приватным полям
// а также следить за очисткой ресурсов, так как PrefHelper использует Context
object AppListRepository {
    private const val LOG_TAG = "AppListRepository"
    private val gson by lazy { Gson() }
    private val api by lazy { RetrofitClient.api }
    private val ph by lazy { PrefHelper.getInstance() }

    // Шаг вправо, шаг влево и Gson выдаст краш, потому что нет null-полей
    // поэтому для получения данных с сервера необходимы железные контракты
    // а для хранения исключительно null-поля с default-значениями
    // либо обыгрывать получение пустоты (null) как-то иначе
    fun getApps(): List<AppInfo>? = fromJSON(ph.jsonAppListResp)?.body?.list

    suspend fun getAppsForce(): List<AppInfo> {
        val url = "https://backapi.rustore.ru/applicationData/retrieveUserApps"
        val rawBody = api.getRetrieveUserApps(url).string()
        toStorage(rawBody)
        return getApps()!!
    }

    private fun fromJSON(json: String): AppListResp? {
        if (json.isEmpty()) return null
        try {
            return gson.fromJson(json, AppListResp::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun toStorage(json: String) {
        ph.jsonAppListResp = json
    }
}